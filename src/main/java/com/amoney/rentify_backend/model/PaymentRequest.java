package com.amoney.rentify_backend.model;

public class PaymentRequest {
    
    private String phoneNumber; // Format: 2547xxxxxxxxx
    private String amount;

    public PaymentRequest(java.lang.String phoneNumber, java.lang.String amount) {
        this.phoneNumber = phoneNumber;
        this.amount = amount;
    }
    //Generate Getters and Setters
    public PaymentRequest() {
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
