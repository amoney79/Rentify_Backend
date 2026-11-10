package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.ListingApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListingApprovalRepository extends JpaRepository<ListingApproval, Long> {
    Optional<ListingApproval> findByPropertyId(Long propertyId);
}
