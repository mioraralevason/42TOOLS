package com.ecole._2.controller;

import com.ecole._2.models.UserPresenceRate;
import com.ecole._2.services.StatsService;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/stats")
public class StatsController {
    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/users")
    public List<UserPresenceRate> getUserPresenceRates(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "userId", required = false) String userId,
            HttpSession session) {
        
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        var kind = (String) session.getAttribute("kind");

        if (!"admin".equalsIgnoreCase(kind)) {
            userId = userResponse.getId();
        }

        return statsService.getUserPresenceRates(startDate, endDate, userId);
    }

    @GetMapping("/global")
    public Double getTauxGlobal(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            HttpSession session) {
        
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        return statsService.getTauxGlobal(startDate, endDate);
    }
}