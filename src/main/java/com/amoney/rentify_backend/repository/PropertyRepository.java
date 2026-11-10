package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.Property;
import com.amoney.rentify_backend.model.PropertyVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByLandlordId(Long landlordId);
    List<Property> findByIsPubliclyVisibleTrue();
    List<Property> findByVerificationStatus(PropertyVerificationStatus status);
    long countByVerificationStatus(PropertyVerificationStatus status);
}
