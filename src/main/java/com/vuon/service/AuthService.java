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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vuon.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Đăng ký tài khoản mới. Trả về user + devOtp (chỉ khi tắt gửi mail thật) */
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
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

        // Gửi OTP xác thực email (chế độ dev sẽ trả mã về cho frontend hiển thị)
        String devOtp = sendOtp(user.getEmail(), OtpCode.Type.verify);

        Map<String, Object> result = new HashMap<>();
        result.put("user", UserResponse.from(user));
        if (devOtp != null) result.put("devOtp", devOtp);
        return result;
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

    /**
     * Đăng nhập bằng Google.
     * credential = ID token (JWT) lấy từ Google Identity Services ở frontend.
     * Xác minh token với Google, tìm/tạo user theo email rồi cấp JWT của hệ thống.
     */
    @Transactional
    public AuthResponse loginWithGoogle(String credential) {
        if (credential == null || credential.isBlank()) {
            throw AppException.badRequest("Thiếu Google credential");
        }

        JsonNode payload = verifyGoogleToken(credential);

        String email = payload.path("email").asText(null);
        boolean emailVerified = "true".equals(payload.path("email_verified").asText());
        if (email == null || !emailVerified) {
            throw AppException.unauthorized("Tài khoản Google chưa xác thực email");
        }

        // Kiểm tra token được phát cho đúng ứng dụng của mình (nếu đã cấu hình Client ID)
        if (googleClientId != null && !googleClientId.isBlank()) {
            String aud = payload.path("aud").asText("");
            if (!googleClientId.equals(aud)) {
                throw AppException.unauthorized("Google credential không hợp lệ cho ứng dụng này");
            }
        }

        String name = payload.path("name").asText(email.split("@")[0]);
        String picture = payload.path("picture").asText(null);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.builder()
                    .name(name)
                    .email(email)
                    // Tài khoản Google không dùng mật khẩu — đặt hash ngẫu nhiên
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .avatar(picture)
                    .build();
            u.setVerified(true);
            return userRepository.save(u);
        });

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, UserResponse.from(user));
    }

    /** Gọi Google tokeninfo để xác minh ID token, trả về payload đã giải mã */
    private JsonNode verifyGoogleToken(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> res = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw AppException.unauthorized("Google credential không hợp lệ hoặc đã hết hạn");
            }
            return objectMapper.readTree(res.body());
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw AppException.unauthorized("Không xác minh được Google credential");
        }
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

    /** Gửi OTP về email để đặt lại mật khẩu. Trả về devOtp khi tắt gửi mail thật */
    public String forgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw AppException.notFound("Email không tồn tại trong hệ thống");
        }
        return sendOtp(email, OtpCode.Type.reset_password);
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

    /** Tạo và lưu OTP, gửi email. Trả về mã (devOtp) khi tắt gửi mail thật, ngược lại null */
    public String sendOtp(String email, OtpCode.Type type) {
        String code = String.format("%06d", new Random().nextInt(1000000));

        OtpCode otp = OtpCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        otpCodeRepository.save(otp);

        emailService.sendOtpEmail(email, code, type);

        return emailEnabled ? null : code;
    }
}
