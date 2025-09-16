package com.ecole._2.services;

import com.ecole._2.models.UserPresenceRate;
import com.ecole._2.models.UserPresenceRate;
import com.ecole._2.repositories.StatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class StatsService {

    private final StatsRepository statsRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    /**
     * Retourne le taux de présence par utilisateur avec option de filtrage par userId.
     *
     * @param startDate date de début (yyyy-MM-dd)
     * @param endDate   date de fin (yyyy-MM-dd)
     * @param userId    facultatif, filtre sur un utilisateur précis si non null/empty
     * @return liste des taux de présence
     */
    public List<UserPresenceRate> getUserPresenceRates(String startDate, String endDate, String userId) {
        validateDates(startDate, endDate);

        if (StringUtils.hasText(userId)) {
            return statsRepository.getUserPresenceRateByUserId(startDate, endDate, userId);
        } else {
            return statsRepository.getUserPresenceRate(startDate, endDate);
        }
    }

    public Double getTauxGlobal(String startDate, String endDate) {
        validateDates(startDate, endDate);
        Double result = statsRepository.getGlobalPresenceRate(startDate, endDate);
        if (result == null) {
            throw new IllegalStateException("Global presence rate could not be retrieved for the given date range.");
        }
        return result;
    }

    private void validateDates(String startDate, String endDate) {
        if (!StringUtils.hasText(startDate) || !StringUtils.hasText(endDate)) {
            throw new IllegalArgumentException("Start date and end date must not be empty.");
        }

        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start date must not be after end date.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd", e);
        }
    }
}
