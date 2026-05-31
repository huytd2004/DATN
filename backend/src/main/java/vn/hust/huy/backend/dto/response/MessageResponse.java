package vn.hust.huy.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResponse {
    private String id;
    private String role;
    private String content;
    private String contentJa;
    private String contentVn;
    private String audioUrl;
    private List<Object> corrections;
    private List<Object> newVocabulary;
    private List<String> suggestions;
}
