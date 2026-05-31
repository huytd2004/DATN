package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DeepTranslateResponse {
    private final String sourceText;
    private final String translatedText;
    private final String sourceLang;
    private final String targetLang;
    private final String provider;
    private final List<String> detectedDomains;
    private final List<KeyVocabularyItem> keyVocabulary;
    private final List<TranslationNote> notes;
    private final List<TranslationNote> warnings;

    @Getter
    @Builder
    public static class KeyVocabularyItem {
        private final String surface;
        private final String reading;
        private final Integer jlpt;
        private final String glossVi;
        private final String domain;
        private final String register;
    }

    @Getter
    @Builder
    public static class TranslationNote {
        private final String type;
        private final String token;
        private final String content;
    }
}