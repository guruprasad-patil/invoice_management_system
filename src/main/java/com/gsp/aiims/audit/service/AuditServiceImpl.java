package com.gsp.aiims.audit.service;

import com.gsp.aiims.audit.entity.AuditLog;
import com.gsp.aiims.audit.repository.AuditLogRepository;
import com.gsp.aiims.common.enums.AuditAction;
import com.gsp.aiims.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String entityType, String entityId,
            String oldValue, String newValue) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .userId(SecurityUtil.getCurrentUserId())
                    .userEmail(SecurityUtil.getCurrentUserEmail())
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to write audit log for action={} entity={}/{}: {}",
                    action, entityType, entityId, e.getMessage());
        }
    }
}
