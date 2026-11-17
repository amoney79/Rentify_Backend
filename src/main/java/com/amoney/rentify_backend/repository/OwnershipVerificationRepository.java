package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.OwnershipVerification;
import com.amoney.rentify_backend.model.PropertyVerificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface OwnershipVerificationRepository extends MongoRepository<OwnershipVerification, Long> {
    Optional<OwnershipVerification> findByPropertyId(Long propertyId);
    List<OwnershipVerification> findByApplicantUserId(Long applicantUserId);
    List<OwnershipVerification> findByStatus(PropertyVerificationStatus status);
    long countByStatus(PropertyVerificationStatus status);
    long countByStatusIn(List<PropertyVerificationStatus> statuses);
}
