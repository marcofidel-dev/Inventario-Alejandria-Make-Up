package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.domain.entity.AuditAction;
import com.marcofidel_dev.inventario.domain.entity.AuditLog;
import com.marcofidel_dev.inventario.infrastructure.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(Long userId, AuditAction action, String entityName, String entityId, String details) {
        try {
            AuditAction persistedAction = normalizeActionForCurrentSchema(action);
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .action(persistedAction)
                    .entityName(entityName)
                    .entityId(entityId)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();
            auditLogRepository.save(entry);
            log.debug("Audit: {} {} {} by userId={}", persistedAction, entityName, entityId, userId);
        } catch (Exception e) {
            log.error("Failed to write audit log entry: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Backward-compatible mapping for databases created before newer audit actions
     * were introduced in the enum and constrained by a legacy CHECK on audit_log.action.
     */
    private AuditAction normalizeActionForCurrentSchema(AuditAction action) {
        if (action == null) {
            return AuditAction.UPDATE;
        }

        return switch (action) {
            case OPEN_CASH_SESSION, SALE -> AuditAction.CREATE;
            case CLOSE_CASH_SESSION, VOID_SALE -> AuditAction.UPDATE;
            default -> action;
        };
    }
}
