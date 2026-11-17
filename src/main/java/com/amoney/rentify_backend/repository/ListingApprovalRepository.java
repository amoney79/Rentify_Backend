package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.ListingApproval;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ListingApprovalRepository extends MongoRepository<ListingApproval, Long> {
    Optional<ListingApproval> findByPropertyId(Long propertyId);
}
