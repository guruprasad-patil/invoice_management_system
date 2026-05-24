package com.gsp.aiims.audit.service;

import com.gsp.aiims.common.enums.AuditAction;

public interface AuditService {

    void log(AuditAction action, String entityType, String entityId, String oldValue, String newValue);
}
