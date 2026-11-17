package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.VerificationAuditRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface VerificationAuditRecordRepository extends MongoRepository<VerificationAuditRecord, Long> {
    List<VerificationAuditRecord> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
    List<VerificationAuditRecord> findTop50ByOrderByTimestampDesc();
}
