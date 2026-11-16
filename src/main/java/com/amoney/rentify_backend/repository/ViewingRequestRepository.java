package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.ViewingRequest;
import com.amoney.rentify_backend.model.ViewingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ViewingRequestRepository extends JpaRepository<ViewingRequest, Long> {
    List<ViewingRequest> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<ViewingRequest> findByLandlordIdOrderByCreatedAtDesc(Long landlordId);
    List<ViewingRequest> findByPropertyId(Long propertyId);
    List<ViewingRequest> findByLandlordIdAndStatus(Long landlordId, ViewingStatus status);
}
