package vn.hust.huy.backend.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import vn.hust.huy.backend.dto.request.MessageRequest;
import vn.hust.huy.backend.dto.request.TutorSessionRequest;
import vn.hust.huy.backend.dto.response.MessageResponse;
import vn.hust.huy.backend.model.entity.ConversationSession;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class RestAiAdapter implements AiAdapter {

    private static final Logger log = LoggerFactory.getLogger(RestAiAdapter.class);

    private final ObjectMapper objectMapper;

    @Value("${app.tutor.ai-base-url:http://localhost:8001}")
    private String aiBaseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public MessageResponse buildInitialMessage(ConversationSession session, TutorSessionRequest request) {
        try {
            String targetWordsJson = "[]";
            if (request.getTargetWords() != null && !request.getTargetWords().isEmpty()) {
                try { targetWordsJson = objectMapper.writeValueAsString(request.getTargetWords()); } catch (Exception ignored) {}
            }
            String body = formData(
                    "scenario", request.getScenarioName(),
                    "level", request.getLevel(),
                    "session_id", session.getId().toString(),
                    "target_words", targetWordsJson
            );
            URI uri = URI.create(aiBaseUrl + "/v1/tutor/" + session.getId() + "/initial");
            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return parseMessageResponse(response.body(), "initial message");
        } catch (Exception e) {
            log.warn("AI initial message REST call failed for session {}: {}", session.getId(), e.toString());
            return fallbackInitial(session, request);
        }
    }

    @Override
    public MessageResponse generateReply(ConversationSession session, MessageRequest messageRequest, List<String> recentLogs) {
        try {
            String targetWords = session.getTargetWords() == null ? "[]" : session.getTargetWords();
            String historyJson = objectMapper.writeValueAsString(recentLogs == null ? List.of() : recentLogs);
            String body = formData(
                    "user_utterance", messageRequest.getContent(),
                    "target_words", targetWords,
                    "history", historyJson,
                    "session_id", session.getId().toString()
            );

            URI uri = URI.create(aiBaseUrl + "/v1/tutor/" + session.getId() + "/reply");
            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String respBody = response.body();
            if (status != 200) {
                log.warn("AI reply REST call returned non-200 ({}). session {}: {}", status, session.getId(), respBody);
                throw new IllegalStateException("AI service returned status " + status);
            }
            try {
                return parseMessageResponse(respBody, "assistant reply");
            } catch (Exception e) {
                log.warn("Failed to parse AI reply for session {}: {}; response body: {}", session.getId(), e.toString(), respBody);
                throw e;
            }
        } catch (Exception e) {
            log.warn("AI reply REST call failed for session {}: {}", session.getId(), e.toString());
            return fallbackReply(messageRequest);
        }
    }

    private MessageResponse parseMessageResponse(String responseBody, String context) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode messageNode = root.path("message");
        if (messageNode.isMissingNode() || messageNode.isNull()) {
            throw new IllegalStateException("Missing message node in " + context);
        }
        return objectMapper.treeToValue(messageNode, MessageResponse.class);
    }

    private MessageResponse fallbackInitial(ConversationSession session, TutorSessionRequest request) {
        return MessageResponse.builder()
                .id(java.util.UUID.randomUUID().toString())
                .role("assistant")
                .content("こんにちは。今日は" + (session.getScenarioName() == null ? "会話" : session.getScenarioName()) + "の練習をします。始めましょう！")
                .suggestions(List.of("はい、準備できました", "まだ準備できていません"))
                .build();
    }

    private MessageResponse fallbackReply(MessageRequest messageRequest) {
        String user = messageRequest.getContent() == null ? "" : messageRequest.getContent();
        String reply = "(Fallback AI) " + (user.isBlank() ? "どうしましたか？" : user + " — 良いですね。次に〜");
        return MessageResponse.builder()
                .id(java.util.UUID.randomUUID().toString())
                .role("assistant")
                .content(reply)
                .suggestions(List.of("Gợi ý 1", "Gợi ý 2"))
                .build();
    }

    private String formData(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i += 2) {
            if (parts[i + 1] == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8));
            builder.append('=');
            builder.append(URLEncoder.encode(parts[i + 1], StandardCharsets.UTF_8));
        }
        return builder.toString();
    }
}
