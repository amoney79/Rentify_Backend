package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.PropertyDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyDocumentRepository extends JpaRepository<PropertyDocument, Long> {
    List<PropertyDocument> findByPropertyId(Long propertyId);
}
