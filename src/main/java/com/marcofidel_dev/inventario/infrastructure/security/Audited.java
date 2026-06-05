package com.marcofidel_dev.inventario.infrastructure.security;

import com.marcofidel_dev.inventario.domain.entity.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method for automatic audit logging.
 * The action and entity name are recorded by {@link AuditAspect}.
 *
 * <pre>
 * {@code @Audited(action = AuditAction.CREATE, entity = "Producto")
 * public Producto guardar(Producto producto) { ... }}
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    AuditAction action();
    String entity() default "";
}
