package com.ecole._2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecole._2.models.User;

import jakarta.servlet.http.HttpSession;

@RestController
public class AuthRestController {   

    @GetMapping("/api/user")
    public User getUser(HttpSession session) {
        User userResponse = (User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        userResponse.setKind((String) session.getAttribute("kind"));
        return userResponse;
    }
}