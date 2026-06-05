package com.marcofidel_dev.inventario.infrastructure.security;

import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Singleton session holder for the currently authenticated user.
 * Only one user can be logged in per process (desktop app).
 */
@Component
@Slf4j
public class SessionContext {

    private volatile User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        log.debug("Session set for user: {} ({})", user.getUsername(), user.getRole());
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public void clear() {
        log.debug("Session cleared for user: {}", currentUser != null ? currentUser.getUsername() : "none");
        this.currentUser = null;
    }

    public boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Enforces that the current user has the required role.
     *
     * @throws AccessDeniedException if the role requirement is not met
     */
    public void requireRole(Role role) {
        if (!hasRole(role)) {
            String currentRole = currentUser != null ? currentUser.getRole().name() : "unauthenticated";
            throw new AccessDeniedException(
                    "Acceso denegado. Se requiere rol " + role + ", pero el usuario tiene: " + currentRole);
        }
    }
}
