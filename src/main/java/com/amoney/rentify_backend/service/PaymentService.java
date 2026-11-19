package com.amoney.rentify_backend.service;

import org.springframework.beans.factory.annotation.Value;

public class PaymentService {

    @Value("${kcb.consumer.key}") private String consumerKey;
    @Value("${kcb.consumer.secret}") private String consumerSecret;
    @Value("${kcb.token.url}") private String tokenUrl;
    

    
}
