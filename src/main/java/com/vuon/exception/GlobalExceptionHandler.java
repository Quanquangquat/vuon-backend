package com.vuon.exception;

import com.vuon.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * Xử lý tập trung tất cả Exception trong ứng dụng
 * Mọi lỗi đều được format về ApiResponse thống nhất
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Lỗi nghiệp vụ tuỳ chỉnh */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return ApiResponse.fail(ex.getMessage(), ex.getStatus());
    }

    /** Lỗi validation (@Valid) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ApiResponse.fail(message, HttpStatus.BAD_REQUEST);
    }

    /** Không có quyền truy cập */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ApiResponse.forbidden("Bạn không có quyền thực hiện hành động này");
    }

    /** File upload quá lớn */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileSize(MaxUploadSizeExceededException ex) {
        return ApiResponse.fail("File quá lớn, tối đa 5MB", HttpStatus.BAD_REQUEST);
    }

    /** Lỗi server không xác định */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        ex.printStackTrace();
        return ApiResponse.fail("Đã có lỗi xảy ra, vui lòng thử lại", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
