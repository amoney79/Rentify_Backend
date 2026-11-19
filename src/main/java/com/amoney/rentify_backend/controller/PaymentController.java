package com.amoney.rentify_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;

import com.amoney.rentify_backend.model.PaymentRequest;
import com.amoney.rentify_backend.service.PaymentService;

@CrossOrigin(origins = "*") //Allows my Fontend to call this APIy
    
public class PaymentController {

    @Autowired
    private PaymentService paymentService;


    @PostMapping("/pay")
    public ResponseEntity<String> makePayment(@RequestBody PaymentRequest request) {
        try{
            String response = paymentService.iinitiateStkPush(request.getPhoneNumber(), request.getAmount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Payment failed: " + e.getMessage());
        }
    }
    
    
    
}
