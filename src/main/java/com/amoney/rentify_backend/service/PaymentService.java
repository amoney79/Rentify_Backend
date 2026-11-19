package com.amoney.rentify_backend.service;

import java.util.Map;

import javax.print.attribute.standard.Media;

import org.apache.tomcat.util.http.parser.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import io.netty.handler.codec.base64.Base64;

public class PaymentService {

    @Value("${kcb.consumer.key}") private String consumerKey;
    @Value("${kcb.consumer.secret}") private String consumerSecret;
    @Value("${kcb.token.url}") private String tokenUrl;
    @Value("${kcb.stk.url}") private String stk;
    @Value("${kcb.callback.url}") private String callbackUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    //Get OAuth Access Token
    public String getAccessToken() {
        String auth = consumerKey + ":" + consumerSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes()); 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FROM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return response.getBody().get("access_token").toString();
    }
    //Trigger STK Push
    public String iinitiateStkPush(String phoneNumber, double amount) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

    }

    
}
