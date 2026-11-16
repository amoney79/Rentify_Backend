package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.OwnershipVerification;
import com.amoney.rentify_backend.model.PropertyVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OwnershipVerificationRepository extends JpaRepository<OwnershipVerification, Long> {
    Optional<OwnershipVerification> findByPropertyId(Long propertyId);
    List<OwnershipVerification> findByApplicantUserId(Long applicantUserId);
    List<OwnershipVerification> findByStatus(PropertyVerificationStatus status);
    long countByStatus(PropertyVerificationStatus status);
    long countByStatusIn(List<PropertyVerificationStatus> statuses);
}
