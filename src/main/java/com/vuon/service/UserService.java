package com.vuon.service;

import com.vuon.dto.response.UserResponse;
import com.vuon.exception.AppException;
import com.vuon.model.User;
import com.vuon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, String name, String phone) {
        User user = getById(userId);
        if (name  != null) user.setName(name);
        if (phone != null) user.setPhone(phone);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = getById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw AppException.badRequest("Mật khẩu hiện tại không đúng");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public String uploadAvatar(UUID userId, MultipartFile file, String uploadDir) throws IOException {
        User user = getById(userId);

        // Tạo thư mục nếu chưa có
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        // Lưu file
        String ext      = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), dir.resolve(filename));

        String avatarUrl = "/uploads/" + filename;
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
