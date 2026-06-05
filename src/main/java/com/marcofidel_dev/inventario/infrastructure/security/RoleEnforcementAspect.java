package com.marcofidel_dev.inventario.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that enforces {@link RequiresRole} annotations on service methods.
 * Intercepts every method annotated with {@code @RequiresRole} and delegates
 * the role check to {@link SessionContext#requireRole(com.marcofidel_dev.inventario.domain.entity.Role)}.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleEnforcementAspect {

    private final SessionContext sessionContext;

    @Around("@annotation(requiresRole)")
    public Object enforceRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {
        log.debug("Role check: {} requires {}", joinPoint.getSignature().getName(), requiresRole.value());
        sessionContext.requireRole(requiresRole.value());
        return joinPoint.proceed();
    }
}
