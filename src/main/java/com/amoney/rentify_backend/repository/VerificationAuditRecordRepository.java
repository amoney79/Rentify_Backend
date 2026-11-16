package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.VerificationAuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VerificationAuditRecordRepository extends JpaRepository<VerificationAuditRecord, Long> {
    List<VerificationAuditRecord> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
    List<VerificationAuditRecord> findTop50ByOrderByTimestampDesc();
}
