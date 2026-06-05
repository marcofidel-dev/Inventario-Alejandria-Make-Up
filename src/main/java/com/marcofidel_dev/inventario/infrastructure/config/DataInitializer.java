package com.marcofidel_dev.inventario.infrastructure.config;

import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.domain.entity.User;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Seeds the default admin user on first run.
 * Credentials: admin / Admin123*  (user will be forced to change password).
 * This runs after Flyway creates the schema, before the UI starts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initialize() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("Admin123*"))
                    .fullName("Administrador")
                    .role(Role.ADMIN)
                    .active(true)
                    .mustChangePassword(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created. Username: admin | Password: Admin123* (change on first login)");
        }
    }
}
