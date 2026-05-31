package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuickTranslateResponse {
    private final String sourceText;
    private final String translatedText;
    private final String sourceLang;
    private final String targetLang;
    private final String provider;
}
