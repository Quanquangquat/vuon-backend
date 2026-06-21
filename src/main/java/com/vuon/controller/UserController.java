package com.vuon.controller;

import com.vuon.dto.response.UserResponse;
import com.vuon.model.User;
import com.vuon.service.UserService;
import com.vuon.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UUID userId) {
        User user = userService.getById(userId);
        return ApiResponse.ok(UserResponse.from(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UUID userId,
                                           @RequestBody Map<String, String> body) {
        var updated = userService.updateProfile(userId, body.get("name"), body.get("phone"));
        return ApiResponse.ok(updated, "Cập nhật thông tin thành công");
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UUID userId,
                                            @RequestBody Map<String, String> body) {
        userService.changePassword(userId, body.get("currentPassword"), body.get("newPassword"));
        return ApiResponse.ok(null, "Đổi mật khẩu thành công");
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@AuthenticationPrincipal UUID userId,
                                          @RequestParam("avatar") MultipartFile file) throws Exception {
        String avatarUrl = userService.uploadAvatar(userId, file, uploadDir);
        return ApiResponse.ok(Map.of("avatar", avatarUrl), "Cập nhật ảnh đại diện thành công");
    }
}
