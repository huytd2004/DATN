package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DeepTranslateEvidenceResponse {
    private final String token;
    private final String lexemeId;
    private final String senseId;
    private final double score;
    private final String glossVi;
    private final String glossEn;
    private final String domainId;
    private final String registerId;
    private final List<String> cues;
    private final List<String> examples;
}