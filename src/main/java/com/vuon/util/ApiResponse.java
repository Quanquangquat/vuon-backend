package com.vuon.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Chuẩn hoá response trả về cho frontend
 * Mọi API đều trả về cùng 1 format:
 * { "success": true/false, "message": "...", "data": {...} }
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String  message;
    private final T       data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data    = data;
    }

    // ---- Factory methods ----

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok(data, "Thành công");
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(new ApiResponse<>(true, message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> fail(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                             .body(new ApiResponse<>(false, message, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return fail(message, HttpStatus.BAD_REQUEST);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return fail(message, HttpStatus.NOT_FOUND);
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return fail(message, HttpStatus.UNAUTHORIZED);
    }

    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return fail(message, HttpStatus.FORBIDDEN);
    }
}
