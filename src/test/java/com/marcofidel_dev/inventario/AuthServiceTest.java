package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.application.service.AuditService;
import com.marcofidel_dev.inventario.application.service.AuthService;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.domain.entity.User;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private SessionContext sessionContext;
    @Mock private AuditService auditService;

    @InjectMocks private AuthService authService;

    private User activeAdminUser;

    @BeforeEach
    void setUp() {
        activeAdminUser = User.builder()
                .id(1L)
                .username("admin")
                .passwordHash("$2a$10$hashed")
                .fullName("Administrador")
                .role(Role.ADMIN)
                .active(true)
                .build();
    }

    @Test
    void login_withValidCredentials_returnsUserAndSetsSession() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(activeAdminUser));
        when(passwordEncoder.matches("Admin123*", activeAdminUser.getPasswordHash())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(activeAdminUser);

        Optional<User> result = authService.login("admin", "Admin123*");

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        verify(sessionContext).setCurrentUser(activeAdminUser);
        verify(auditService).log(eq(1L), any(), anyString(), anyString(), any());
    }

    @Test
    void login_withInvalidPassword_returnsEmpty() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(activeAdminUser));
        when(passwordEncoder.matches("wrongPassword", activeAdminUser.getPasswordHash())).thenReturn(false);

        Optional<User> result = authService.login("admin", "wrongPassword");

        assertTrue(result.isEmpty());
        verify(sessionContext, never()).setCurrentUser(any());
    }

    @Test
    void login_withNonExistentUser_returnsEmpty() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> result = authService.login("unknown", "anyPassword");

        assertTrue(result.isEmpty());
        verify(sessionContext, never()).setCurrentUser(any());
    }

    @Test
    void login_withInactiveUser_returnsEmpty() {
        User inactiveUser = User.builder()
                .id(2L)
                .username("colaborador")
                .passwordHash("$2a$10$hashed")
                .role(Role.COLABORADOR)
                .active(false)
                .build();

        when(userRepository.findByUsername("colaborador")).thenReturn(Optional.of(inactiveUser));

        Optional<User> result = authService.login("colaborador", "somePassword");

        assertTrue(result.isEmpty());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(sessionContext, never()).setCurrentUser(any());
    }

    @Test
    void logout_clearsSessionAndLogsAudit() {
        when(sessionContext.getCurrentUser()).thenReturn(Optional.of(activeAdminUser));

        authService.logout();

        verify(auditService).log(eq(1L), any(), anyString(), anyString(), any());
        verify(sessionContext).clear();
    }

    @Test
    void changePassword_withInvalidOldPassword_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeAdminUser));
        when(passwordEncoder.matches("wrongOld", activeAdminUser.getPasswordHash())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword(1L, "wrongOld", "NewPass123*"));
    }
}
