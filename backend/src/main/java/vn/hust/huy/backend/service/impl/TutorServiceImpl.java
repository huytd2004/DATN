package vn.hust.huy.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import vn.hust.huy.backend.dto.request.MessageRequest;
import vn.hust.huy.backend.dto.request.TutorSessionRequest;
import vn.hust.huy.backend.dto.response.MessageResponse;
import vn.hust.huy.backend.dto.response.TutorResultResponse;
import vn.hust.huy.backend.dto.response.TutorSessionResponse;
import vn.hust.huy.backend.model.entity.ConversationSession;
import vn.hust.huy.backend.model.entity.LearningLog;
import vn.hust.huy.backend.model.entity.TutorSessionResult;
import vn.hust.huy.backend.model.entity.User;
import vn.hust.huy.backend.repository.ConversationSessionRepository;
import vn.hust.huy.backend.repository.LearningLogRepository;
import vn.hust.huy.backend.repository.TutorSessionResultRepository;
import vn.hust.huy.backend.repository.UserRepository;
import vn.hust.huy.backend.service.ai.AiAdapter;
import vn.hust.huy.backend.service.TutorService;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private static final Logger log = LoggerFactory.getLogger(TutorServiceImpl.class);

    private final ConversationSessionRepository sessionRepository;
    private final LearningLogRepository logRepository;
    private final TutorSessionResultRepository resultRepository;
    private final UserRepository userRepository;
    private final AiAdapter aiAdapter;
    private final vn.hust.huy.backend.service.media.AudioStorageService audioStorageService;
    private final vn.hust.huy.backend.service.ai.STTAdapter sttAdapter;
    private final vn.hust.huy.backend.service.ai.TTSAdapter ttsAdapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.tutor.browser-tts:true}")
    private boolean browserTts;

    @Override
    @Transactional
    public TutorSessionResponse createSession(TutorSessionRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        ConversationSession session = ConversationSession.builder()
                .user(user)
                .scenarioName(request.getScenarioName())
                .targetWords(request.getTargetWords() == null ? "[]" : toJson(request.getTargetWords()))
                .build();
        sessionRepository.save(session);

        MessageResponse initial = aiAdapter.buildInitialMessage(session, request);

        return TutorSessionResponse.builder()
                .sessionId(session.getId().toString())
                .scenarioName(session.getScenarioName())
                .level(request.getLevel())
                .durationMinutes(request.getDurationMinutes())
                .targetWords(request.getTargetWords())
                .initialMessage(initial)
                .build();
    }

    @Override
    public TutorSessionResponse getSession(UUID sessionId, String userEmail) {
        ConversationSession session = sessionRepository.findById(sessionId).orElseThrow();
        return TutorSessionResponse.builder()
                .sessionId(session.getId().toString())
                .scenarioName(session.getScenarioName())
                .level(null)
                .durationMinutes(null)
                .targetWords(null)
                .initialMessage(null)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(UUID sessionId, MessageRequest request, org.springframework.web.multipart.MultipartFile audio, String userEmail) {
        ConversationSession session = sessionRepository.findById(sessionId).orElseThrow();
        // owner check
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        if (!session.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not owner of session");
        }
        // Guard: content must not be null (DB NOT NULL constraint)
        String userContent = (request.getContent() != null && !request.getContent().isBlank())
                ? request.getContent()
                : "";

        LearningLog userLog = LearningLog.builder()
                .session(session)
                .role("user")
                .content(userContent)
                .build();
        logRepository.save(userLog);

        // If audio present, attempt STT and update log content
        if (audio != null) {
            try {
                String transcript = sttAdapter.transcribe(audio);
                if (transcript != null && !transcript.isBlank()) {
                    request.setContent(transcript);
                    // Update stored log with transcribed text
                    userLog.setContent(transcript);
                    logRepository.save(userLog);
                }
            } catch (Exception e) {
                log.warn("STT transcription failed for session {}: {}", session.getId(), e.toString());
            }

            try {
                String saved = audioStorageService.saveAudio(session.getId().toString(), audio);
                // Append audio file reference to log content
                userLog.setContent(userLog.getContent() + "\n[audio]:" + saved);
                logRepository.save(userLog);
            } catch (Exception e) {
                log.warn("Saving audio failed for session {}: {}", session.getId(), e.toString());
            }
        }

        // Ensure request.content is never null when passed to AI
        if (request.getContent() == null || request.getContent().isBlank()) {
            request.setContent("");
        }

        List<LearningLog> allLogs = logRepository.findBySession_IdOrderByCreatedAtAsc(session.getId());
        int skip = Math.max(0, allLogs.size() - 5);
        List<String> recentLogs = allLogs.stream()
            .skip(skip)
            .map(logItem -> logItem.getRole() + ": " + logItem.getContent())
            .toList();

        MessageResponse assistant = aiAdapter.generateReply(session, request, recentLogs);

        // Browser TTS handles playback in the frontend; keep backend audio optional.
        if (!browserTts) {
            try {
                String ttsFile = ttsAdapter.synthesize(session.getId().toString(), assistant.getContentJa() != null ? assistant.getContentJa() : assistant.getContent());
                if (ttsFile != null) {
                    assistant.setAudioUrl("/api/v1/tutor/audio/" + session.getId().toString() + "/" + ttsFile);
                }
            } catch (Exception e) {
                log.warn("TTS synthesis failed for session {}: {}", session.getId(), e.toString());
                assistant.setAudioUrl(null);
            }
        }

        // Store clean Japanese content in learning log (not raw JSON)
        String logContent = assistant.getContentJa() != null && !assistant.getContentJa().isBlank()
                ? assistant.getContentJa()
                : assistant.getContent();
        LearningLog assistantLog = LearningLog.builder()
                .session(session)
                .role("assistant")
                .content(logContent)
                .build();
        logRepository.save(assistantLog);


        return assistant;
    }

    @Override
    public TutorResultResponse getResult(UUID sessionId, String userEmail) {
        ConversationSession session = sessionRepository.findById(sessionId).orElseThrow();
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        if (!session.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not owner of session");
        }

        return resultRepository.findBySession_Id(session.getId())
                .map(this::toTutorResultResponse)
                .orElseGet(() -> buildTutorResultResponse(session, logRepository.findBySession_IdOrderByCreatedAtAsc(session.getId())));
    }

    @Override
    @Transactional
    public void finishSession(UUID sessionId, String userEmail) {
        ConversationSession session = sessionRepository.findById(sessionId).orElseThrow();
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        if (!session.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not owner of session");
        }

        session.setEndedAt(java.time.Instant.now());
        sessionRepository.save(session);

        List<LearningLog> logs = logRepository.findBySession_IdOrderByCreatedAtAsc(session.getId());
        TutorResultResponse resultResponse = buildTutorResultResponse(session, logs);
        TutorSessionResult result = resultRepository.findBySession_Id(session.getId()).orElseGet(TutorSessionResult::new);
        result.setSession(session);
        result.setUser(user);
        result.setDurationMinutes(resultResponse.getDurationMinutes());
        result.setUserTurns(resultResponse.getUserTurns());
        result.setAssistantTurns(resultResponse.getAssistantTurns());
        result.setOverallScore(null);
        result.setMistakeCount(resultResponse.getCorrections() == null ? 0 : resultResponse.getCorrections().size());
        result.setCorrectionCount(resultResponse.getCorrections() == null ? 0 : resultResponse.getCorrections().size());
        result.setFluencyScore(resultResponse.getFluencyScore());
        result.setAccuracyScore(resultResponse.getAccuracyScore());
        result.setPronunciationScore(resultResponse.getPronunciationScore());
        result.setMistakes(toJson(resultResponse.getCorrections()));
        result.setNewVocabulary(toJson(resultResponse.getNewVocabulary()));
        result.setSummary("Session completed");
        result.setFinishedAt(session.getEndedAt() == null ? java.time.Instant.now() : session.getEndedAt());
        resultRepository.save(result);
        logRepository.deleteBySession_Id(session.getId());
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private TutorResultResponse buildTutorResultResponse(ConversationSession session, List<LearningLog> logs) {
        long userCount = logs.stream().filter(l -> "user".equals(l.getRole())).count();
        long assistantCount = logs.stream().filter(l -> "assistant".equals(l.getRole())).count();

        List<Object> corrections = new ArrayList<>();
        List<Object> newVocabulary = new ArrayList<>();

        for (LearningLog logItem : logs) {
            if (!"assistant".equals(logItem.getRole()) || logItem.getContent() == null) {
                continue;
            }

            JsonNode parsed = tryExtractJson(logItem.getContent());
            if (parsed == null || !parsed.isObject()) {
                continue;
            }

            JsonNode c = parsed.get("corrections");
            if (c != null && c.isArray()) {
                corrections.addAll(objectMapper.convertValue((ArrayNode) c, List.class));
            }

            JsonNode nv = parsed.get("newVocabulary");
            if (nv == null) {
                nv = parsed.get("new_vocabulary");
            }
            if (nv != null && nv.isArray()) {
                newVocabulary.addAll(objectMapper.convertValue((ArrayNode) nv, List.class));
            }
        }

        Integer durationMinutes = null;
        if (session.getStartedAt() != null && session.getEndedAt() != null) {
            durationMinutes = (int) Math.max(0, Duration.between(session.getStartedAt(), session.getEndedAt()).toMinutes());
        }

        return TutorResultResponse.builder()
                .durationMinutes(durationMinutes)
                .userTurns((int) userCount)
                .assistantTurns((int) assistantCount)
                .fluencyScore(null)
                .accuracyScore(null)
                .pronunciationScore(null)
                .corrections(corrections)
                .newVocabulary(newVocabulary)
                .build();
    }

    private TutorResultResponse toTutorResultResponse(TutorSessionResult result) {
        List<Object> corrections = readJsonList(result.getMistakes());
        List<Object> newVocabulary = readJsonList(result.getNewVocabulary());

        return TutorResultResponse.builder()
                .durationMinutes(result.getDurationMinutes())
                .userTurns(result.getUserTurns())
                .assistantTurns(result.getAssistantTurns())
                .fluencyScore(result.getFluencyScore())
                .accuracyScore(result.getAccuracyScore())
                .pronunciationScore(result.getPronunciationScore())
                .corrections(corrections)
                .newVocabulary(newVocabulary)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<Object> readJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    private JsonNode tryExtractJson(String text) {
        // Try to find JSON object/array blocks, support multiple blocks and code fences
        String cleaned = text.replaceAll("(?s)```(?:json)?\\n", "").replaceAll("```\\s*$", "");

        java.util.regex.Pattern objPat = java.util.regex.Pattern.compile("\\{[\\s\\S]*?\\}");
        java.util.regex.Matcher m = objPat.matcher(cleaned);
        while (m.find()) {
            String chunk = m.group(0);
            try {
                return objectMapper.readTree(chunk);
            } catch (Exception ex) {
                // try replacing single quotes with double quotes
                try {
                    String alt = chunk.replace('\'', '"');
                    return objectMapper.readTree(alt);
                } catch (Exception ignored) {}
            }
        }

        java.util.regex.Pattern arrPat = java.util.regex.Pattern.compile("\\[[\\s\\S]*?\\]");
        m = arrPat.matcher(cleaned);
        while (m.find()) {
            String chunk = m.group(0);
            try {
                return objectMapper.readTree(chunk);
            } catch (Exception ignored) {}
        }

        // Optionally attempt YAML parsing if snakeyaml on classpath
        try {
            org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
            Object parsed = yaml.load(cleaned);
            if (parsed != null) {
                // convert to JsonNode via ObjectMapper
                return objectMapper.valueToTree(parsed);
            }
        } catch (Throwable ignored) {
            // snakeyaml not available or parse failed
        }

        return null;
    }
}
