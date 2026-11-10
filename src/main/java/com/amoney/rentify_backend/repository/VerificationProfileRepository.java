package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.IdentityStatus;
import com.amoney.rentify_backend.model.VerificationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationProfileRepository extends JpaRepository<VerificationProfile, Long> {
    Optional<VerificationProfile> findByUserId(Long userId);
    List<VerificationProfile> findByStatus(IdentityStatus status);
    long countByStatus(IdentityStatus status);
}
