package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TutorResultResponse {
    private Integer durationMinutes;
    private Integer userTurns;
    private Integer assistantTurns;
    private Integer fluencyScore;
    private Integer accuracyScore;
    private Integer pronunciationScore;
    private List<Object> corrections;
    private List<Object> newVocabulary;
}
