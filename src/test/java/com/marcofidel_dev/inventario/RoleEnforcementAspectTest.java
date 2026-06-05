package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.domain.entity.User;
import com.marcofidel_dev.inventario.infrastructure.security.AccessDeniedException;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import com.marcofidel_dev.inventario.infrastructure.security.RoleEnforcementAspect;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies RoleEnforcementAspect intercepts @RequiresRole.
 * Uses a minimal Spring context — no database or JavaFX involved.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RoleEnforcementAspectTest.SecurityTestConfig.class)
class RoleEnforcementAspectTest {

    @Configuration
    @EnableAspectJAutoProxy
    static class SecurityTestConfig {

        @Bean
        public SessionContext sessionContext() {
            return new SessionContext();
        }

        @Bean
        public RoleEnforcementAspect roleEnforcementAspect(SessionContext sessionContext) {
            return new RoleEnforcementAspect(sessionContext);
        }

        /** The bean under test — a plain class with an @RequiresRole method. */
        @Bean
        public AdminOnlyService adminOnlyService() {
            return new AdminOnlyService();
        }
    }

    /** Simple service used only for testing the aspect. */
    static class AdminOnlyService {
        @RequiresRole(Role.ADMIN)
        public String adminOnlyMethod() {
            return "admin-result";
        }
    }

    @Autowired private SessionContext sessionContext;
    @Autowired private AdminOnlyService adminOnlyService;

    @BeforeEach
    void resetSession() {
        sessionContext.clear();
    }

    // -------------------------------------------------------
    // Tests
    // -------------------------------------------------------

    @Test
    void givenColaborador_whenCallsAdminMethod_thenThrowsAccessDeniedException() {
        User colaborador = User.builder()
                .id(2L).username("colab").role(Role.COLABORADOR).active(true).build();
        sessionContext.setCurrentUser(colaborador);

        assertThrows(AccessDeniedException.class, adminOnlyService::adminOnlyMethod,
                "COLABORADOR debe recibir AccessDeniedException al llamar un método de ADMIN");
    }

    @Test
    void givenAdmin_whenCallsAdminMethod_thenExecutesSuccessfully() {
        User admin = User.builder()
                .id(1L).username("admin").role(Role.ADMIN).active(true).build();
        sessionContext.setCurrentUser(admin);

        assertDoesNotThrow(() -> adminOnlyService.adminOnlyMethod(),
                "ADMIN debe poder llamar métodos de ADMIN sin excepción");
    }

    @Test
    void givenNoSession_whenCallsAdminMethod_thenThrowsAccessDeniedException() {
        // sessionContext is cleared in @BeforeEach — no user in session
        assertThrows(AccessDeniedException.class, adminOnlyService::adminOnlyMethod,
                "Una llamada sin sesión activa debe ser denegada");
    }
}
