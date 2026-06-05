package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.application.dto.CreateUserDTO;
import com.marcofidel_dev.inventario.application.dto.UserDTO;
import com.marcofidel_dev.inventario.domain.entity.AuditAction;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.domain.entity.User;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionContext sessionContext;
    private final AuditService auditService;

    @RequiresRole(Role.ADMIN)
    @Transactional
    public UserDTO createUser(CreateUserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso: " + dto.getUsername());
        }

        User user = User.builder()
                .username(dto.getUsername())
                .fullName(dto.getFullName())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .active(true)
                .mustChangePassword(true)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        Long adminId = sessionContext.getCurrentUser().map(User::getId).orElse(null);
        auditService.log(adminId, AuditAction.CREATE, "User", saved.getId().toString(),
                "username=" + saved.getUsername() + ", role=" + saved.getRole());

        log.info("User created: {} ({})", saved.getUsername(), saved.getRole());
        return UserDTO.from(saved);
    }

    @RequiresRole(Role.ADMIN)
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        user.setActive(false);
        userRepository.save(user);

        Long adminId = sessionContext.getCurrentUser().map(User::getId).orElse(null);
        auditService.log(adminId, AuditAction.USER_DEACTIVATED, "User", id.toString(),
                "username=" + user.getUsername());

        log.info("User deactivated: {}", user.getUsername());
    }

    @RequiresRole(Role.ADMIN)
    @Transactional
    public void changeRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        Role oldRole = user.getRole();
        user.setRole(newRole);
        userRepository.save(user);

        Long adminId = sessionContext.getCurrentUser().map(User::getId).orElse(null);
        auditService.log(adminId, AuditAction.ROLE_CHANGE, "User", id.toString(),
                "username=" + user.getUsername() + ", from=" + oldRole + ", to=" + newRole);

        log.info("Role changed for {}: {} -> {}", user.getUsername(), oldRole, newRole);
    }

    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public List<UserDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::from)
                .toList();
    }
}
