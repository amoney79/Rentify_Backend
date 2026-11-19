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

    public PaymentRequest(String reference, String phoneNumber, double amount, String paymentMethod, String status) {
        this.reference = reference;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id ;
    }
    public String getReference() {
        return reference;
    }
    public void setReference(String Reference) {
        this.reference = reference;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus(){
        return status;
    }
    public void setStatus (String status) {
        this.status = status ;
    }
    public String getMpesaReceiptNumber() {
        return mpesaReceiptNumber;
    }
    public void setMpesaReceiptNumber(String mpesaReceiptNumber) {
        this.mpesaReceiptNumber = mpesaReceiptNumber;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    } 
}
