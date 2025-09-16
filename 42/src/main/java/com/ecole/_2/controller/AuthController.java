package com.ecole._2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecole._2.models.TokenResponse;
import com.ecole._2.models.User;
import com.ecole._2.services.OAuth42Service;
import com.ecole._2.services.User42Service;
import com.ecole._2.utils.CheckingUtils;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private OAuth42Service oauth42Service;

    @Autowired
    private User42Service user42Service;

    // URL du front React
    private static final String FRONT_URL = "http://localhost:5173"; 

    @GetMapping("/auth")
    public String auth(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpSession session
    ) {
        logger.info("Starting authentication process with code: {}", code);

        TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
        User userResponse = (User) session.getAttribute("userResponse");

        if (tokenResponse == null || userResponse == null) {
            tokenResponse = oauth42Service.getAccessToken(code);
            if (tokenResponse == null) {
                logger.error("Failed to retrieve access token");
                return "redirect:" + FRONT_URL + "/?error=token_failed";
            }

            userResponse = user42Service.getUserInfo(tokenResponse.getAccessToken());
            if (userResponse == null) {
                logger.error("Failed to retrieve user info");
                return "redirect:" + FRONT_URL + "/?error=user_failed";
            }

            session.setAttribute("tokenResponse", tokenResponse);
            session.setAttribute("userResponse", userResponse);
            session.setAttribute("code", code);
            session.setAttribute("state", state);

            logger.info("Authenticated user: {} (ID: {})", userResponse.getLogin(), userResponse.getId());
        }

        // String kind = CheckingUtils.determineUserKind(userResponse);
        String kind = "admin";
        session.setAttribute("kind", kind);

        // Redirection vers React avec un flag login_success
        return "redirect:" + FRONT_URL + "/?login_success=true";
    }
}
