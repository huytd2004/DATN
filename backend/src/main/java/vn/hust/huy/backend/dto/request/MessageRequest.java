package vn.hust.huy.backend.dto.request;

import lombok.Data;

@Data
public class MessageRequest {
    private String content;
    private String inputMode; // text | voice
}
