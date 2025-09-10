package com.ecole._2.services;

import com.ecole._2.models.LocationStat;
import com.ecole._2.models.User;
import com.ecole._2.models.UserLocationStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

import com.google.common.util.concurrent.RateLimiter;

import jakarta.annotation.Nullable;

@Service
public class UserLocationStatsService {
    private static final Logger logger = LoggerFactory.getLogger(UserLocationStatsService.class);
    private static final String BASE_URL = "https://api.intra.42.fr/v2/users/";
    private static final int MAX_RETRIES = 5;
    private static final long BASE_RETRY_DELAY_MS = 10000; // 10 sec
    private static final int MAX_PARALLEL = 4;
    private String accessToken;
    /**
     * Récupère les statistiques de localisation pour un utilisateur spécifique.
     */
    public UserLocationStat getUserLocationStats(
            String userId,
            String accessToken,
            @Nullable String beginAt,
            @Nullable String endAt
    ) {
        RestTemplate restTemplate = new RestTemplate();

        // Construction de l'URL avec paramètres optionnels
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + userId + "/locations_stats");

        if (beginAt != null) {
            builder.queryParam("begin_at", beginAt);
        }
        if (endAt != null) {
            builder.queryParam("end_at", endAt);
        }

        String url = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, String> rawData = response.getBody();
            List<LocationStat> stats = new ArrayList<>();

            if (rawData != null) {
                for (Map.Entry<String, String> entry : rawData.entrySet()) {
                    LocalDate date = LocalDate.parse(entry.getKey());
                    String[] hms = entry.getValue().split(":");
                    int hours = Integer.parseInt(hms[0]);
                    int minutes = Integer.parseInt(hms[1]);
                    String[] secMicro = hms[2].split("\\.");
                    int seconds = Integer.parseInt(secMicro[0]);
                    int micros = secMicro.length > 1 ? Integer.parseInt(secMicro[1]) : 0;

                    Duration duration = Duration.ofHours(hours)
                            .plusMinutes(minutes)
                            .plusSeconds(seconds)
                            .plusNanos(micros * 1000);

                    stats.add(new LocationStat(date, duration));
                }
            }

            return new UserLocationStat(userId, stats);
        } catch (RestClientException e) {
            logger.error("Error fetching location stats for user {}: {}", userId, e.getMessage());
            return new UserLocationStat(userId, new ArrayList<>());
        }
    }


    /**
     * Récupère les statistiques de localisation pour une liste d'utilisateurs.
     * Exécute en parallèle avec une limite de 8 requêtes/s.
     */
    public List<UserLocationStat> getUserLocationStatsFromUsers(List<User> users, ApiService apiService) {
        if (users == null) {
            logger.error("User list is null");
            throw new IllegalArgumentException("User list cannot be null");
        }

        accessToken = apiService.getAccessToken();
        if (accessToken == null || accessToken.trim().isEmpty()) {
            logger.error("Access token is null or empty");
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }

        logger.info("Starting to fetch location stats for {} users", users.size());

        // RateLimiter : max 8 requêtes par seconde
        RateLimiter limiter = RateLimiter.create(8.0);

        // Pool de threads pour exécuter les appels en parallèle
        ExecutorService executor = Executors.newFixedThreadPool(MAX_PARALLEL);

        List<Future<UserLocationStat>> futures = new ArrayList<>();

        for (User user : users) {
            if (user != null && user.getId() != null) {
                String userId = user.getId();

                Future<UserLocationStat> future = executor.submit(() -> {
                    boolean success = false;
                    int retries = 0;
                    UserLocationStat result = new UserLocationStat(userId, new ArrayList<>());

                    while (!success && retries < MAX_RETRIES) {
                        try {
                            limiter.acquire(); // bloque si plus de 8 appels/s
                            result = getUserLocationStats(userId, accessToken,null,null);
                            success = true;
                        } catch (RestClientException e) {
                            String message = e.getMessage() != null ? e.getMessage() : "";
                            if (message.contains("429")) {
                                retries++;
                                long delay = BASE_RETRY_DELAY_MS * (1L << (retries - 1)); // backoff exponentiel
                                logger.warn("429 Too Many Requests for user {}. Retry {} in {}s",
                                        userId, retries, delay / 1000);
                                Thread.sleep(delay);
                                accessToken = apiService.getAccessToken(); // refresh token
                            } else {
                                logger.error("Non-429 error for user {}: {}", userId, e.getMessage());
                                break;
                            }
                        }
                    }
                    return result;
                });

                futures.add(future);
            } else {
                logger.warn("Skipping invalid user: {}", user);
            }
        }

        // Récupération des résultats
        List<UserLocationStat> results = new ArrayList<>();
        for (Future<UserLocationStat> f : futures) {
            try {
                results.add(f.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error getting future result: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        executor.shutdown();
        logger.info("Completed fetching location stats for {} users", results.size());
        return results;
    }
}
