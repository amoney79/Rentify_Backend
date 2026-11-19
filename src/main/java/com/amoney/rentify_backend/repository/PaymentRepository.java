package com.amoney.rentify_backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<PaymentRequest, String> {

    // Finds a payment document by its reference.
    Optional<PaymentRequest> findByReference(String reference);
}
