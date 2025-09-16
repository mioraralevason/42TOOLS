package com.ecole._2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecole._2.models.User;

import jakarta.servlet.http.HttpSession;

@RestController
public class AuthRestController {   

    @GetMapping("/api/user")
    public User getUser(HttpSession session) {
        // Endpoint que React appelle pour récupérer l'utilisateur
        User userResponse = (User) session.getAttribute("userResponse");
        if (userResponse == null) {
            // non connecté
            return null;
        }
        userResponse.setKind((String) session.getAttribute("kind"));
        return userResponse;
    }
}
