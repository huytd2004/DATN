package vn.hust.huy.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

/**
 * Standard API response envelope as defined in instructions.md.
 * All API responses must use this wrapper.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String status;
    private final int code;
    private final String message;
    private final T data;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    // ── Factory helpers ────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data, String message, int httpCode) {
        return ApiResponse.<T>builder()
                .status("success")
                .code(httpCode)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return success(data, message, 200);
    }

    public static <T> ApiResponse<T> error(String message, int httpCode) {
        return ApiResponse.<T>builder()
                .status("error")
                .code(httpCode)
                .message(message)
                .data(null)
                .build();
    }
}
