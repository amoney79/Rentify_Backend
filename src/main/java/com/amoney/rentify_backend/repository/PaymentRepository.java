package com.amoney.rentify_backend.repository;

public class PaymentRepository {

    @Repository
    public interface PaymentRepository extends MongoRepository<Payment, String> {

        //Finds a paymentt document by its reference or checkoutRequestID
        Optional<Payment> findByReference(String refference) ;
    }
    
}
