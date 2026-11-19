package com.amoney.rentify_backend.controller;


import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amoney.rentify_backend.model.PaymentRequest;
import com.amoney.rentify_backend.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*") //Allows my Fontend to call this APIy
    
public class PaymentController {

    @Autowired
    private  PaymentService paymentService;

    //Temporary in-memory stotre for payment statuses(replace)


    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    //Initiate M-Pesa STK Push and Save PENDING Record to DB
    @PostMapping("/pay")
    public ResponseEntity<String> makePayment(@RequestBody PaymentRequest request) {
        try{
            String response = paymentService.initiateStkPush(request.getPhoneNumber(), request.getAmount());
            return ResponseEntity.ok(response);

            // Extract CheckoutRequestID from KCB's JSON response to match the callback later
            String reference = "MPESA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            try {
                JsonNode root = objectMapper.readTree(response);
                if (root.has("response") && root.get("response").has("CheckoutRequestID")) {
                    reference = root.get("response").get("CheckoutRequestID").asText();
                } else if (root.has("CheckoutRequestID")) {
                    reference = root.get("CheckoutRequestID").asText();
                }
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
