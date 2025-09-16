package com.ecole._2.controller;

import com.ecole._2.models.UserPresenceRate;
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
            HttpSession session) {  // need session to get logged-in user info
        try {
            // Get the logged-in user info from session
            var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
            var kind = (String) session.getAttribute("kind");

            if (!"admin".equalsIgnoreCase(kind)) {
                // If not admin, ignore the request parameter and use session user ID
                userId = userResponse != null ? userResponse.getId() : null;
            }

            return statsService.getUserPresenceRates(startDate, endDate, userId);
        } catch (Exception e) {
            logger.error("Error processing /stats/users for startDate: {}, endDate: {}, userId: {}", startDate, endDate, userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve user presence rates", e);
        }
    }

    @GetMapping("/global")
    public Double getTauxGlobal(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            return statsService.getTauxGlobal(startDate, endDate);
        } catch (Exception e) {
            logger.error("Error processing /stats/global for startDate: {}, endDate: {}", startDate, endDate, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve global presence rate", e);
        }
    }
}