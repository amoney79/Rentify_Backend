package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.Property;
import com.amoney.rentify_backend.model.PropertyVerificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PropertyRepository extends MongoRepository<Property, Long> {
    List<Property> findByLandlordId(Long landlordId);
    List<Property> findByIsPubliclyVisibleTrue();
    List<Property> findByVerificationStatus(PropertyVerificationStatus status);
    long countByVerificationStatus(PropertyVerificationStatus status);
}
