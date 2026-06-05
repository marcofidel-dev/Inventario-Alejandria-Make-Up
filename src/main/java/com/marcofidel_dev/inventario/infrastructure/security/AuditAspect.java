package com.marcofidel_dev.inventario.infrastructure.security;

import com.marcofidel_dev.inventario.application.service.AuditService;
import com.marcofidel_dev.inventario.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP aspect that automatically records audit entries for methods
 * annotated with {@link Audited}.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final SessionContext sessionContext;
    private final AuditService auditService;

    @AfterReturning(pointcut = "@annotation(audited)", returning = "result")
    public void audit(JoinPoint joinPoint, Audited audited, Object result) {
        try {
            User user = sessionContext.getCurrentUser().orElse(null);
            Long userId = user != null ? user.getId() : null;

            String entityName = audited.entity().isBlank()
                    ? joinPoint.getSignature().getDeclaringType().getSimpleName()
                    : audited.entity();

            String entityId = extractEntityId(result);

            // Intentionally NOT serializing args: JPA entities with @Data generate
            // circular toString() (e.g. Compra → CompraItem → Compra) which causes
            // StackOverflowError. Log only the method signature instead.
            String details = "method=" + joinPoint.getSignature().toShortString();

            auditService.log(userId, audited.action(), entityName, entityId, details);
        } catch (Throwable t) {
            // Catch Throwable (not just Exception) to also handle StackOverflowError
            // and other Errors that can arise from entity serialization or lazy-init.
            log.warn("Audit logging failed for {}: {}", joinPoint.getSignature().getName(), t.getMessage());
        }
    }

    private String extractEntityId(Object result) {
        if (result == null) return null;
        try {
            // Attempt to call getId() via reflection on entity return values
            var method = result.getClass().getMethod("getId");
            Object id = method.invoke(result);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
