package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.ViewingRequest;
import com.amoney.rentify_backend.model.ViewingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ViewingRequestRepository extends MongoRepository<ViewingRequest, Long> {
    List<ViewingRequest> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<ViewingRequest> findByLandlordIdOrderByCreatedAtDesc(Long landlordId);
    List<ViewingRequest> findByPropertyId(Long propertyId);
    List<ViewingRequest> findByLandlordIdAndStatus(Long landlordId, ViewingStatus status);
}
