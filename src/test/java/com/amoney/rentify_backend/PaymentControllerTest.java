package com.amoney.rentify_backend;

import com.amoney.rentify_backend.controller.PaymentController;
import com.amoney.rentify_backend.model.PaymentRequest;
import com.amoney.rentify_backend.repository.PaymentRepository;
import com.amoney.rentify_backend.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService paymentService;

    private PaymentController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentController(paymentService, paymentRepository, new ObjectMapper());
    }

    @Test
    void makePaymentNormalizesPhoneAndStoresPendingCheckout() {
        PaymentRequest request = new PaymentRequest();
        request.setPhoneNumber("0712 345 678");
        request.setAmount(1500);
        when(paymentService.initiateStkPush("254712345678", 1500))
                .thenReturn("{\"response\":{\"CheckoutRequestID\":\"ws_CO_123\"}}");

        ResponseEntity<Map<String, Object>> response = controller.makePayment(request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("PENDING", response.getBody().get("status"));
        assertEquals("ws_CO_123", response.getBody().get("reference"));

        ArgumentCaptor<PaymentRequest> paymentCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals("254712345678", paymentCaptor.getValue().getPhoneNumber());
        assertEquals("MPESA", paymentCaptor.getValue().getPaymentMethod());
        assertEquals("PENDING", paymentCaptor.getValue().getStatus());
    }

    @Test
    void makePaymentRejectsInvalidPhoneBeforeCallingProvider() {
        PaymentRequest request = new PaymentRequest();
        request.setPhoneNumber("123");
        request.setAmount(1500);

        ResponseEntity<Map<String, Object>> response = controller.makePayment(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(paymentService, paymentRepository);
    }

    @Test
    void makePaymentRejectsNonPositiveAmountBeforeCallingProvider() {
        PaymentRequest request = new PaymentRequest();
        request.setPhoneNumber("0712345678");
        request.setAmount(0);

        ResponseEntity<Map<String, Object>> response = controller.makePayment(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(paymentService, paymentRepository);
    }

    @Test
    void makePaymentDoesNotStoreARequestWithoutProviderCheckoutReference() {
        PaymentRequest request = new PaymentRequest();
        request.setPhoneNumber("0712345678");
        request.setAmount(1500);
        when(paymentService.initiateStkPush("254712345678", 1500))
                .thenReturn("{\"message\":\"Request was not accepted\"}");

        ResponseEntity<Map<String, Object>> response = controller.makePayment(request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        verifyNoInteractions(paymentRepository);
    }
}
