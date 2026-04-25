package vn.hust.huy.backend.exception;

import lombok.Getter;

/**
 * Centralised error codes for the application.
 * Each entry pairs an HTTP status code with a human-readable message.
 */
@Getter
public enum ErrorCode {

    // Auth
    EMAIL_ALREADY_EXISTS(409, "Email đã tồn tại"),
    INVALID_CREDENTIALS(401, "Email hoặc mật khẩu không đúng"),
    INVALID_REFRESH_TOKEN(401, "Refresh token không hợp lệ hoặc đã hết hạn"),
    INVALID_ACCESS_TOKEN(401, "Access token không hợp lệ"),
    UNAUTHORIZED(401, "Bạn chưa đăng nhập"),

    // Resource
    USER_NOT_FOUND(404, "Không tìm thấy người dùng"),

    // Dictionary
    WORD_ALREADY_EXISTS(409, "Từ này đã tồn tại trong từ điển"),
    DICTIONARY_NOT_FOUND(404, "Không tìm thấy từ trong từ điển"),

    // Flashcard
    FLASHCARD_ALREADY_EXISTS(409, "Từ này đã có trong bộ flashcard của bạn"),
    FLASHCARD_NOT_FOUND(404, "Không tìm thấy flashcard"),

    // Flashcard Deck
    DECK_NOT_FOUND(404, "Không tìm thấy bộ flashcard"),
    DECK_ACCESS_DENIED(403, "Bạn không có quyền truy cập bộ flashcard này"),

    // Comment
    COMMENT_NOT_FOUND(404, "Không tìm thấy bình luận"),
    COMMENT_ACCESS_DENIED(403, "Bạn không có quyền xóa bình luận này"),

    // Generic
    INTERNAL_SERVER_ERROR(500, "Lỗi hệ thống, vui lòng thử lại sau"),
    VALIDATION_ERROR(400, "Dữ liệu đầu vào không hợp lệ");

    private final int httpStatus;
    private final String message;

    ErrorCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
