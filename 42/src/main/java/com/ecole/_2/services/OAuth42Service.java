package com.ecole._2.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.ecole._2.models.TokenResponse;

@Service
public class OAuth42Service {

    @Value("${app.client_id}")
    private String clientId;

    @Value("${app.client_secret}")
    private String clientSecret;

    @Value("${app.redirect_uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://api.intra.42.fr/oauth/token";

    public TokenResponse getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // Add 500ms delay before the API call
        try {
            Thread.sleep(125);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<TokenResponse> response =
                restTemplate.postForEntity(TOKEN_URL, request, TokenResponse.class);

        return response.getBody();
    }
}