package com.amoney.rentify_backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="payments")
public class PaymentRequest {
    
    @Id
    private String id; //mongoDB automatically generates a string objectId

    @Indexed(unique = true)
    private String reference; //checkoutRequestID or VISA-XXXX

    private String phoneNumber; // Format: 2547xxxxxxxxx
    private double amount;
    private String paymentMethod; // "MPESA" OR "CARD"
    private String status;         // "PENDING", "COMPLETED", OR "FAILED"
    private String mpesaReceiptNumber;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PaymentRequest(String phoneNumber, double amount, String paymentMethod, String reference, String status) {
        this.phoneNumber = phoneNumber;
        this.amount = amount;
    }
    //Generate Getters and Setters
    public PaymentRequest() {
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber (String phoneNumber) {
        this.phoneNumber =phoneNumber;
    }

    public double getAmount(){
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
