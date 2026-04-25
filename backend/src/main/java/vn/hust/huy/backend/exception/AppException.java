package vn.hust.huy.backend.exception;

import lombok.Getter;

/**
 * Application-level exception that carries a structured {@link ErrorCode}.
 * Throw this anywhere in the service layer; the {@link GlobalExceptionHandler} will convert it
 * to the standard API response envelope automatically.
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }
}
