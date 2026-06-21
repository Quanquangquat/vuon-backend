package com.vuon.service;

import com.vuon.dto.request.LoginRequest;
import com.vuon.dto.request.RegisterRequest;
import com.vuon.dto.response.AuthResponse;
import com.vuon.dto.response.UserResponse;
import com.vuon.exception.AppException;
import com.vuon.model.OtpCode;
import com.vuon.model.User;
import com.vuon.repository.OtpCodeRepository;
import com.vuon.repository.UserRepository;
import com.vuon.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service xử lý nghiệp vụ xác thực (Authentication)
 * Đăng ký, đăng nhập, OTP, quên mật khẩu
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository    userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder   passwordEncoder;
    private final JwtUtil           jwtUtil;
    private final EmailService      emailService;

    /** Đăng ký tài khoản mới */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.badRequest("Email đã được sử dụng");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();

        user = userRepository.save(user);

        // Gửi OTP xác thực email (bất đồng bộ - không block response)
        sendOtp(user.getEmail(), OtpCode.Type.verify);

        return UserResponse.from(user);
    }

    /** Đăng nhập - trả về JWT token */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> AppException.unauthorized("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw AppException.unauthorized("Email hoặc mật khẩu không đúng");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, UserResponse.from(user));
    }

    /** Xác thực OTP */
    @Transactional
    public void verifyOtp(String email, String code, OtpCode.Type type) {
        OtpCode otp = otpCodeRepository.findValidOtp(email, code, type)
                .orElseThrow(() -> AppException.badRequest("Mã OTP không hợp lệ hoặc đã hết hạn"));

        otp.setUsed(true);
        otpCodeRepository.save(otp);

        if (type == OtpCode.Type.verify) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));
            user.setVerified(true);
            userRepository.save(user);
        }
    }

    /** Gửi OTP về email để đặt lại mật khẩu */
    public void forgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw AppException.notFound("Email không tồn tại trong hệ thống");
        }
        sendOtp(email, OtpCode.Type.reset_password);
    }

    /** Đặt lại mật khẩu sau khi xác thực OTP */
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        verifyOtp(email, code, OtpCode.Type.reset_password);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /** Tạo và lưu OTP, gửi email */
    public void sendOtp(String email, OtpCode.Type type) {
        String code = String.format("%06d", new Random().nextInt(999999));

        OtpCode otp = OtpCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        otpCodeRepository.save(otp);

        emailService.sendOtpEmail(email, code, type);
    }
}
