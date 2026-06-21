package com.vuon.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Tiện ích xử lý JSON Web Token (JWT)
 * - Tạo token khi đăng nhập
 * - Xác thực token trong mỗi request
 * - Trích xuất thông tin user từ token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;   // milliseconds

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Tạo JWT cho user sau khi đăng nhập thành công */
    public String generateToken(UUID userId, String email, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /** Lấy userId từ token */
    public UUID extractUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    /** Lấy role từ token */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /** Kiểm tra token còn hợp lệ không */
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
