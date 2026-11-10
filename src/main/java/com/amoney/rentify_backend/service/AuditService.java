package com.amoney.rentify_backend.service;

import com.amoney.rentify_backend.model.AuditAction;
import com.amoney.rentify_backend.model.User;
import com.amoney.rentify_backend.model.VerificationAuditRecord;
import com.amoney.rentify_backend.repository.VerificationAuditRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final VerificationAuditRecordRepository auditRepository;

    public AuditService(VerificationAuditRecordRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional
    public VerificationAuditRecord logAudit(
            String entityType,
            Long entityId,
            User performedBy,
            AuditAction action,
            String notes,
            Double confidenceScore,
            String metadataJson) {

        VerificationAuditRecord record = VerificationAuditRecord.builder()
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performedBy)
                .action(action)
                .notes(notes)
                .confidenceScore(confidenceScore)
                .metadataJson(metadataJson)
                .timestamp(LocalDateTime.now())
                .build();

        return auditRepository.save(record);
    }

    public List<VerificationAuditRecord> getAuditTrail(String entityType, Long entityId) {
        return auditRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    public List<VerificationAuditRecord> getRecentAuditLogs() {
        return auditRepository.findTop50ByOrderByTimestampDesc();
    }
}
