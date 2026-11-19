package com.amoney.rentify_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.amoney.rentify_backend.model.PaymentRequest;
import com.amoney.rentify_backend.service.PaymentService;

@CrossOrigin(origins = "*") //Allows my Fontend to call this APIy
    
public class PaymentController {

    @Autowired
    private  PaymentService paymentService;


    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @PostMapping("/pay")
    public ResponseEntity<String> makePayment(@RequestBody PaymentRequest request) {
        try{
            String response = paymentService.iinitiateStkPush(request.getPhoneNumber(), request.getAmount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Payment failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody String callbackData) {
        // KCB sends the payment confirmation JSON here
        System.out.println("KCB callback Received: " + callbackData);
        //parse JSON and update your database order status to PAID
        return ResponseEntity.ok("Received");
    }
}
