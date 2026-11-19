package com.amoney.rentify_backend.service;

import org.springframework.beans.factory.annotation.Value;

public class PaymentService {

    @Value("${kcb.consumer.key}") private String consumerKey;

    
}
