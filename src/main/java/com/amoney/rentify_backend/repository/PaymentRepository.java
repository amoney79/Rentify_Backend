package com.amoney.rentify_backend.repository;

public class PaymentRepository {

    @Repository
    public interface PaymentRepository extends MongoRepository<Payment, String> {

        //
    }
    
}
