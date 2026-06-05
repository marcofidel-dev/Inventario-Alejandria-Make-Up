package com.marcofidel_dev.inventario.infrastructure.security;

import com.marcofidel_dev.inventario.domain.entity.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method as requiring a specific role.
 * Intercepted at runtime by {@link RoleEnforcementAspect}.
 *
 * <pre>
 * {@code @RequiresRole(Role.ADMIN)
 * public void deleteUser(Long id) { ... }}
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    Role value();
}
