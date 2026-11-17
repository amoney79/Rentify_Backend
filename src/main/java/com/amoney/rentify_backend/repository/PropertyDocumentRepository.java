package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.PropertyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PropertyDocumentRepository extends MongoRepository<PropertyDocument, Long> {
    List<PropertyDocument> findByPropertyId(Long propertyId);
}
