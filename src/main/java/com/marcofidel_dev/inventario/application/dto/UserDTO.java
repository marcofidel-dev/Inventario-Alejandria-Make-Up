package com.marcofidel_dev.inventario.application.dto;

import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.domain.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Safe user view — never exposes passwordHash.
 */
@Data
public class UserDTO {

    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private boolean active;
    private boolean mustChangePassword;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public static UserDTO from(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setRole(u.getRole());
        dto.setActive(u.isActive());
        dto.setMustChangePassword(u.isMustChangePassword());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setLastLogin(u.getLastLogin());
        return dto;
    }
}
