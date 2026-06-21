package com.vuon.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception tuỳ chỉnh cho ứng dụng VƯƠN
 * Ném ra khi gặp lỗi nghiệp vụ (business logic error)
 */
@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    // Shorthand constructors
    public static AppException badRequest(String message) {
        return new AppException(message, HttpStatus.BAD_REQUEST);
    }

    public static AppException notFound(String message) {
        return new AppException(message, HttpStatus.NOT_FOUND);
    }

    public static AppException unauthorized(String message) {
        return new AppException(message, HttpStatus.UNAUTHORIZED);
    }

    public static AppException forbidden(String message) {
        return new AppException(message, HttpStatus.FORBIDDEN);
    }
}
