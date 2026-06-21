package com.vuon.controller;

import com.vuon.dto.request.LoginRequest;
import com.vuon.dto.request.RegisterRequest;
import com.vuon.dto.response.UserResponse;
import com.vuon.model.OtpCode;
import com.vuon.model.User;
import com.vuon.repository.UserRepository;
import com.vuon.service.AuthService;
import com.vuon.util.ApiResponse;
import com.vuon.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService    authService;
    private final UserRepository userRepository;
    private final JwtUtil        jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        var user = authService.register(req);
        return ApiResponse.created(user, "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        var result = authService.login(req);
        return ApiResponse.ok(result, "Đăng nhập thành công");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        authService.verifyOtp(body.get("email"), body.get("otp"), OtpCode.Type.verify);
        return ApiResponse.ok(null, "Xác thực tài khoản thành công");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        authService.forgotPassword(body.get("email"));
        return ApiResponse.ok(null, "Đã gửi mã OTP về email của bạn");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        authService.resetPassword(body.get("email"), body.get("otp"), body.get("newPassword"));
        return ApiResponse.ok(null, "Đặt lại mật khẩu thành công");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> body) {
        String type = body.getOrDefault("type", "verify");
        authService.sendOtp(body.get("email"), OtpCode.Type.valueOf(type));
        return ApiResponse.ok(null, "Đã gửi lại mã OTP");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return ApiResponse.ok(UserResponse.from(user));
    }
}
