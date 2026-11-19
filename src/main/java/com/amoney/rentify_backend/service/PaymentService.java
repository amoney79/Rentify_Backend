package com.amoney.rentify_backend.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {

    @Value("${kcb.consumer.key}") private String consumerKey;
    @Value("${kcb.consumer.secret}") private String consumerSecret;
    @Value("${kcb.token.url}") private String tokenUrl;
    @Value("${kcb.stk.url}") private String stkUrl;
    @Value("${kcb.callback.url}") private String callbackUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    //Get OAuth Access Token
    @SuppressWarnings("rawtypes")
    public String getAccessToken() {
        String auth = consumerKey + ":" + consumerSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        Map responseBody = response.getBody();
        Object accessToken = responseBody != null ? responseBody.get("access_token") : null;
        if (accessToken == null || accessToken.toString().isBlank()) {
            throw new IllegalStateException("Payment provider did not return an access token");
        }
        return accessToken.toString();
    }
    //Trigger STK Push
    public String initiateStkPush(String phoneNumber, double amount) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> payload = new HashMap<>();
        payload.put("phoneNumber", phoneNumber);
        payload.put("amount", amount);
        payload.put("invoiceNumber", "INV-" + System.currentTimeMillis()); //Unique reference
        payload.put("sharedShortCode", true);
        payload.put("orgShortCode","");
        payload.put("orgPassKey","");
        payload.put("callbackUrl",callbackUrl);
        payload.put("transactionDescription","Order Payment");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(stkUrl, request, String.class);

        if (response.getBody() == null || response.getBody().isBlank()) {
            throw new IllegalStateException("Payment provider returned an empty STK response");
        }
        return response.getBody();

    }
}
