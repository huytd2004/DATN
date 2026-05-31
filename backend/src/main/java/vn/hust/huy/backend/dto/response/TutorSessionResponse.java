package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TutorSessionResponse {
    private String sessionId;
    private String scenarioName;
    private String level;
    private Integer durationMinutes;
    private List<Object> targetWords;
    private MessageResponse initialMessage;
}
