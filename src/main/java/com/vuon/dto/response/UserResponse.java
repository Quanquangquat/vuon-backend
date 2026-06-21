package com.vuon.dto.response;

import com.vuon.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO trả về thông tin user (không bao gồm password)
 */
@Data
public class UserResponse {
    private UUID          id;
    private String        name;
    private String        email;
    private String        phone;
    private String        avatar;
    private String        role;
    private boolean       isVerified;
    private int           totalOrders;
    private long          totalSpent;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setName(user.getName());
        r.setEmail(user.getEmail());
        r.setPhone(user.getPhone());
        r.setAvatar(user.getAvatar());
        r.setRole(user.getRole().name());
        r.setVerified(user.isVerified());
        r.setTotalOrders(user.getTotalOrders());
        r.setTotalSpent(user.getTotalSpent());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}
