package com.ecole._2.controller;

import com.ecole._2.models.TauxPresenceUtilisateur;
import com.ecole._2.services.StatsService;
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
    public List<TauxPresenceUtilisateur> getTauxParUtilisateur(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            return statsService.getTauxParUtilisateur(startDate, endDate);
        } catch (Exception e) {
            logger.error("Error processing /stats/users for startDate: {}, endDate: {}", startDate, endDate, e);
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