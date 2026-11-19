package com.amoney.rentify_backend.model;

public class PaymentRequest {
    
    private String phoneNumber; // Format: 2547xxxxxxxxx
    private double amount;

    public PaymentRequest(String phoneNumber, double amount) {
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
