package com.amoney.rentify_backend.repository;

import com.amoney.rentify_backend.model.PropertyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyDocumentRepository extends JpaRepository<PropertyDocument, Long> {
    List<PropertyDocument> findByPropertyId(Long propertyId);
}
