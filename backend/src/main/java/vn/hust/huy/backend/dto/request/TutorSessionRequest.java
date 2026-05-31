package vn.hust.huy.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class TutorSessionRequest {
    private String deckId;
    private String scenarioName;
    private String level;
    private Integer durationMinutes;
    private List<Object> targetWords;
}
