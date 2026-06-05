package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.domain.entity.AuditAction;
import com.marcofidel_dev.inventario.domain.entity.User;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionContext sessionContext;
    private final AuditService auditService;

    /**
     * Attempts to authenticate the user.
     *
     * @return the authenticated User if credentials are valid and the account is active;
     *         empty otherwise (reason is deliberately not disclosed to the caller).
     */
    @Transactional
    public Optional<User> login(String username, String password) {
        Optional<User> found = userRepository.findByUsername(username);

        if (found.isEmpty()) {
            log.warn("Login failed: user '{}' not found", username);
            return Optional.empty();
        }

        User user = found.get();

        if (!user.isActive()) {
            log.warn("Login failed: user '{}' is inactive", username);
            return Optional.empty();
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Login failed: invalid password for user '{}'", username);
            return Optional.empty();
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        sessionContext.setCurrentUser(user);

        auditService.log(user.getId(), AuditAction.LOGIN, "User", user.getId().toString(), null);
        log.info("Login successful for user '{}' with role {}", username, user.getRole());

        return Optional.of(user);
    }

    /**
     * Logs out the current user and clears the session.
     */
    @Transactional
    public void logout() {
        sessionContext.getCurrentUser().ifPresent(user -> {
            auditService.log(user.getId(), AuditAction.LOGOUT, "User", user.getId().toString(), null);
            log.info("User '{}' logged out", user.getUsername());
        });
        sessionContext.clear();
    }

    /**
     * Changes the password for the given user after validating the old password.
     *
     * @throws IllegalArgumentException if old password is incorrect
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        auditService.log(userId, AuditAction.PASSWORD_CHANGE, "User", userId.toString(), null);
        log.info("Password changed for user '{}'", user.getUsername());
    }
}
