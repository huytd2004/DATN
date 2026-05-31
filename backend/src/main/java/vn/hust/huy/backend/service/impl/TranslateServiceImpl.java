package vn.hust.huy.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.hust.huy.backend.dto.request.QuickTranslateRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.DeepTranslateResponse;
import vn.hust.huy.backend.dto.response.DeepTranslateResponse.KeyVocabularyItem;
import vn.hust.huy.backend.dto.response.DeepTranslateResponse.TranslationNote;
import vn.hust.huy.backend.dto.response.QuickTranslateResponse;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.service.TranslateService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslateServiceImpl implements TranslateService {

    private static final String DEFAULT_SOURCE_LANG = "ja";
    private static final String DEFAULT_TARGET_LANG = "vi";

    private final ObjectMapper objectMapper;

    @Value("${app.translate.quick-url:https://translate.googleapis.com/translate_a/single}")
    private String quickTranslateUrl;

    @Value("${app.translate.python-command:python3}")
    private String pythonCommand;

    @Value("${app.translate.ai-root:../ai}")
    private String aiRoot;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public ApiResponse<QuickTranslateResponse> quickTranslate(QuickTranslateRequest request) {
        String sourceText = request.getText().trim();
        String sourceLang = normalizeLang(request.getSourceLang(), DEFAULT_SOURCE_LANG);
        String targetLang = normalizeLang(request.getTargetLang(), DEFAULT_TARGET_LANG);

        String translatedText = translateWithGoogleEndpoint(sourceText, sourceLang, targetLang);

        QuickTranslateResponse response = QuickTranslateResponse.builder()
                .sourceText(sourceText)
                .translatedText(translatedText)
                .sourceLang(sourceLang)
                .targetLang(targetLang)
                .provider("google-translate-public")
                .build();

        return ApiResponse.success(response, "Dịch nhanh thành công");
    }

    @Override
    public ApiResponse<DeepTranslateResponse> deepTranslate(QuickTranslateRequest request) {
        String sourceText = request.getText().trim();
        String sourceLang = normalizeLang(request.getSourceLang(), DEFAULT_SOURCE_LANG);
        String targetLang = normalizeLang(request.getTargetLang(), DEFAULT_TARGET_LANG);

        JsonNode payload = runLangGraphPipeline(sourceText);
        DeepTranslateResponse response = mapDeepTranslateResponse(payload, sourceText, sourceLang, targetLang);

        return ApiResponse.success(response, "Phân tích chuyên sâu thành công");
    }

    private JsonNode runLangGraphPipeline(String sourceText) {
        try {
            String pythonExecutable = resolvePythonExecutable();
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add("-m");
            command.add("langraph_pipeline.runner");
            command.add("--text");
            command.add(sourceText);
            command.add("--json");

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(resolveAiRoot().toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("LangGraph pipeline failed with exit code {} and output: {}", exitCode, output);
                throw new AppException(ErrorCode.DEEP_TRANSLATION_FAILED);
            }

            if (output == null || output.isBlank()) {
                throw new AppException(ErrorCode.DEEP_TRANSLATION_FAILED);
            }

            return parsePipelineJson(output);
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Deep translate pipeline error: {}", ex.getMessage(), ex);
            throw new AppException(ErrorCode.DEEP_TRANSLATION_FAILED);
        }
    }

    private JsonNode parsePipelineJson(String output) {
        try {
            return objectMapper.readTree(output);
        } catch (Exception ex) {
            String jsonCandidate = extractJsonObject(output);
            if (jsonCandidate == null) {
                log.warn("Pipeline output is not JSON. First 200 chars: {}", output.substring(0, Math.min(200, output.length())));
                throw new AppException(ErrorCode.DEEP_TRANSLATION_FAILED);
            }
            try {
                return objectMapper.readTree(jsonCandidate);
            } catch (Exception nestedEx) {
                log.warn("Failed to parse extracted JSON. First 200 chars: {}", jsonCandidate.substring(0, Math.min(200, jsonCandidate.length())));
                throw new AppException(ErrorCode.DEEP_TRANSLATION_FAILED);
            }
        }
    }

    private String extractJsonObject(String output) {
        int marker = output.indexOf("\"translation\"");
        if (marker >= 0) {
            int start = output.lastIndexOf('{', marker);
            int end = output.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return output.substring(start, end + 1);
            }
        }

        int start = output.lastIndexOf('{');
        int end = output.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return output.substring(start, end + 1);
        }
        return null;
    }

    private DeepTranslateResponse mapDeepTranslateResponse(
            JsonNode payload,
            String sourceText,
            String sourceLang,
            String targetLang) {

        String translatedText = payload.path("translation").asText("");
        if (translatedText.isBlank()) {
            throw new AppException(ErrorCode.DEEP_TRANSLATION_FAILED);
        }

        // detectedDomains
        List<String> detectedDomains = readTextList(payload.path("detectedDomains"));

        // keyVocabulary
        List<KeyVocabularyItem> keyVocabulary = new ArrayList<>();
        JsonNode kvNode = payload.path("keyVocabulary");
        if (kvNode.isArray()) {
            for (JsonNode kv : kvNode) {
                keyVocabulary.add(KeyVocabularyItem.builder()
                        .surface(kv.path("surface").asText(null))
                        .reading(kv.path("reading").asText(null))
                        .jlpt(kv.path("jlpt").isNull() ? null : kv.path("jlpt").asInt())
                        .glossVi(kv.path("glossVi").asText(null))
                        .domain(kv.path("domain").asText(null))
                        .register(kv.path("register").asText(null))
                        .build());
            }
        }

        // notes
        List<TranslationNote> notes = readNoteList(payload.path("notes"));
        List<TranslationNote> warnings = readNoteList(payload.path("warnings"));

        return DeepTranslateResponse.builder()
                .sourceText(sourceText)
                .translatedText(translatedText)
                .sourceLang(sourceLang)
                .targetLang(targetLang)
                .provider("langraph_pipeline")
                .detectedDomains(detectedDomains)
                .keyVocabulary(keyVocabulary)
                .notes(notes)
                .warnings(warnings)
                .build();
    }

    private List<TranslationNote> readNoteList(JsonNode node) {
        List<TranslationNote> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                result.add(TranslationNote.builder()
                        .type(item.path("type").asText(null))
                        .token(item.path("token").asText(null))
                        .content(item.path("content").asText(null))
                        .build());
            }
        }
        return result;
    }

    private List<String> readTextList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String value = item.asText(null);
                if (value != null && !value.isBlank()) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    private Path resolveAiRoot() {
        return Paths.get(aiRoot).toAbsolutePath().normalize();
    }

    private String resolvePythonExecutable() {
        List<Path> candidates = new ArrayList<>();

        if (pythonCommand != null && !pythonCommand.isBlank()) {
            Path configured = Paths.get(pythonCommand);
            if (configured.isAbsolute()) {
                candidates.add(configured);
            } else if (configured.getNameCount() > 1) {
                candidates.add(resolveAiRoot().resolve(configured).normalize());
            } else {
                candidates.add(configured);
            }
        }

        Path aiRootPath = resolveAiRoot();
        candidates.addAll(Arrays.asList(
                aiRootPath.resolve(".venv/bin/python"),
                aiRootPath.resolve("venv/bin/python"),
                aiRootPath.resolve("env/bin/python")
        ));

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                return candidate.toString();
            }
        }

        return (pythonCommand == null || pythonCommand.isBlank()) ? "python3" : pythonCommand;
    }


    private String translateWithGoogleEndpoint(String text, String sourceLang, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String endpoint = String.format(
                    "%s?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                    quickTranslateUrl,
                    sourceLang,
                    targetLang,
                    encodedText
            );

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                log.warn("Quick translate provider returned HTTP {}", response.statusCode());
                throw new AppException(ErrorCode.TRANSLATION_FAILED);
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode segments = root.path(0);
            if (!segments.isArray() || segments.isEmpty()) {
                throw new AppException(ErrorCode.TRANSLATION_FAILED);
            }

            StringBuilder translated = new StringBuilder();
            for (JsonNode segment : segments) {
                String piece = segment.path(0).asText();
                if (!piece.isBlank()) {
                    translated.append(piece);
                }
            }

            String finalText = translated.toString().trim();
            if (finalText.isBlank()) {
                throw new AppException(ErrorCode.TRANSLATION_FAILED);
            }

            return finalText;
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Quick translate error: {}", ex.getMessage(), ex);
            throw new AppException(ErrorCode.TRANSLATION_FAILED);
        }
    }

    private String normalizeLang(String lang, String fallback) {
        if (lang == null || lang.isBlank()) {
            return fallback;
        }
        return lang.trim().toLowerCase();
    }
}
