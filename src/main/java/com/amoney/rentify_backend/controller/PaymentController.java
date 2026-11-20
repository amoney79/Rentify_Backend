package com.amoney.rentify_backend.controller;

import com.amoney.rentify_backend.model.PaymentRequest;
import com.amoney.rentify_backend.repository.PaymentRepository;
import com.amoney.rentify_backend.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    //Initialize ObjectMapper directly here
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentController(
            PaymentRepository paymentRepository,
            PaymentService paymentService) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> makePayment(@RequestBody PaymentRequest request) {
        if (request == null || !Double.isFinite(request.getAmount()) || request.getAmount() <= 0) {
            return error(HttpStatus.BAD_REQUEST, "Enter a valid payment amount.");
        }

        String phoneNumber = normalizeKenyanPhoneNumber(request.getPhoneNumber());
        if (phoneNumber == null) {
            return error(HttpStatus.BAD_REQUEST, "Enter a valid Kenyan M-Pesa phone number.");
        }

        try {
            String providerResponse = paymentService.initiateStkPush(phoneNumber, request.getAmount());
            if (providerResponse == null || providerResponse.isBlank()) {
                logger.warn("Payment provider returned an empty response");
                return error(HttpStatus.BAD_GATEWAY, "The payment provider returned an empty response.");
            }
            JsonNode responseJson = objectMapper.readTree(providerResponse);
            String reference = responseJson != null ? findCheckoutReference(responseJson) : null;
            if (reference == null || reference.isBlank()) {
                logger.warn("Payment provider response did not contain a checkout reference");
                return error(HttpStatus.BAD_GATEWAY, "The payment provider did not confirm the request.");
            }

            PaymentRequest payment = new PaymentRequest();
            payment.setReference(reference);
            payment.setPhoneNumber(phoneNumber);
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod("MPESA");
            payment.setStatus("PENDING");
            paymentRepository.save(payment);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "PENDING");
            response.put("reference", reference);
            response.put("message", "Payment request sent. Check your phone to complete it.");
            return ResponseEntity.accepted().body(response);
        } catch (RestClientException exception) {
            logger.error("Could not initiate M-Pesa payment with the configured provider", exception);
            return error(HttpStatus.BAD_GATEWAY, "Could not reach the payment provider. Please try again.");
        } catch (IllegalStateException exception) {
            logger.error("Payment provider returned an incomplete response", exception);
            return error(HttpStatus.BAD_GATEWAY, "The payment provider could not confirm the request.");
        } catch (JsonProcessingException exception) {
            logger.error("Payment provider returned an invalid response", exception);
            return error(HttpStatus.BAD_GATEWAY, "The payment provider returned an invalid response.");
        }
    }

    @PostMapping("/card")
    public ResponseEntity<Map<String, Object>> makeCardPayment() {
        return error(HttpStatus.NOT_IMPLEMENTED, "Card payments are not configured yet.");
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(@RequestBody String callbackData) {
        final JsonNode callback;
        try {
            callback = objectMapper.readTree(callbackData);
        } catch (JsonProcessingException exception) {
            logger.warn("Received an invalid payment callback", exception);
            return error(HttpStatus.BAD_REQUEST, "Invalid payment callback.");
        }

        if (callback == null) {
            return error(HttpStatus.BAD_REQUEST, "Invalid payment callback.");
        }
        JsonNode stkCallback = callback.path("Body").path("stkCallback");
        String reference = firstText(stkCallback, "CheckoutRequestID", "checkoutRequestID", "checkoutRequestId");
        if (reference == null) {
            return error(HttpStatus.BAD_REQUEST, "Payment callback is missing its checkout reference.");
        }

        Optional<PaymentRequest> existingPayment = paymentRepository.findByReference(reference);
        if (existingPayment.isPresent()) {
            PaymentRequest payment = existingPayment.get();
            int resultCode = stkCallback.path("ResultCode").asInt(stkCallback.path("resultCode").asInt(-1));
            payment.setStatus(resultCode == 0 ? "COMPLETED" : "FAILED");

            if (resultCode == 0) {
                JsonNode items = stkCallback.path("CallbackMetadata").path("Item");
                for (JsonNode item : items) {
                    if ("MpesaReceiptNumber".equals(firstText(item, "Name", "name"))) {
                        payment.setMpesaReceiptNumber(firstText(item, "Value", "value"));
                        break;
                    }
                }
            }
            paymentRepository.save(payment);
        } else {
            logger.warn("Received payment callback for unknown checkout reference {}", reference);
        }

        return ResponseEntity.ok(Map.of("message", "Callback processed."));
    }

    @GetMapping("/status/{reference}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String reference) {
        Map<String, Object> response = new HashMap<>();
        response.put("reference", reference);

        paymentRepository.findByReference(reference).ifPresentOrElse(
                payment -> {
                    response.put("status", payment.getStatus());
                    response.put("receiptNumber", payment.getMpesaReceiptNumber());
                },
                () -> response.put("status", "NOT_FOUND")
        );
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", "FAILED", "message", message));
    }

    private String normalizeKenyanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.startsWith("0") && digits.length() == 10) {
            digits = digits.substring(1);
        } else if (digits.startsWith("254") && digits.length() == 12) {
            digits = digits.substring(3);
        }

        return digits.matches("[17]\\d{8}") ? "254" + digits : null;
    }

    private String findCheckoutReference(JsonNode response) {
        JsonNode reference = response.findValue("CheckoutRequestID");
        if (reference == null) {
            reference = response.findValue("checkoutRequestID");
        }
        if (reference == null) {
            reference = response.findValue("checkoutRequestId");
        }
        return reference != null && reference.isValueNode() ? reference.asText() : null;
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value != null && !value.isNull() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return null;
    }
}
