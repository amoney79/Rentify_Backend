package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.ListingApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ListingApprovalRepository extends JpaRepository<ListingApproval, Long> {
    Optional<ListingApproval> findByPropertyId(Long propertyId);
}
