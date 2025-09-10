package com.ecole._2.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class LoginController {

    @Value("${app.client_id}")
    private String CLIENT_ID;

    @Value("${app.client_secret}")
    private String CLIENT_SECRET;

    @Value("${app.redirect_uri}")
    private String REDIRECT_URI;

    private final String RESPONSE_TYPE = "code";
    private final String SCOPE = "public";
    private final String STATE = "123456";

    @GetMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String redirectUriEncoded = URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8.toString());
        String authUrl = "https://api.intra.42.fr/oauth/authorize" +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + redirectUriEncoded +
                "&response_type=" + RESPONSE_TYPE +
                "&scope=" + SCOPE +
                "&state=" + STATE;

        response.sendRedirect(authUrl);
    }

    @GetMapping("/")
    public void index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        login(request, response);
    }
}
