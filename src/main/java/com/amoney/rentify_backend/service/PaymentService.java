package com.amoney.rentify_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

public class PaymentService {

    @Value("${kcb.consumer.key}") private String consumerKey;
    @Value("${kcb.consumer.secret}") private String consumerSecret;
    @Value("${kcb.token.url}") private String tokenUrl;
    @Value("${kcb.stk.url}") private String stk;
    @Value("${kcb.callback.url}") private String callbackUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    
}
