package com.ecole._2.services;

import com.ecole._2.models.User;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

@Service
public class CampusUsersService {
    private static final Logger logger = LoggerFactory.getLogger(CampusUsersService.class);
    private static final String BASE_URL_CAMPUS = "https://api.intra.42.fr/v2/campus";
    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 100;

    // ✅ Limiteur global → 8 requêtes / seconde
    private final RateLimiter rateLimiter = RateLimiter.create(8.0);

    // ✅ Pool de 8 threads (aligné avec la limite API)
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    /**
     * Récupère une page de users
     */
    public List<User> getCampusUsers(String campusId, String accessToken, Integer pageNumber, Integer pageSize) {
        if (campusId == null || campusId.trim().isEmpty()) {
            throw new IllegalArgumentException("Campus ID cannot be null or empty");
        }
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }

        int page = (pageNumber != null && pageNumber > 0) ? pageNumber : 1;
        int size = (pageSize != null && pageSize > 0) ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL_CAMPUS + "/" + campusId + "/users?page[number]=" + page + "&page[size]=" + size;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            rateLimiter.acquire(); // ✅ applique la limite avant chaque requête

            ResponseEntity<User[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                User[].class
            );

            User[] users = response.getBody();
            return users != null ? Arrays.asList(users) : new ArrayList<>();

        } catch (RestClientException e) {
            throw new RuntimeException("Error fetching users for campus " + campusId + " page " + page + ": " + e.getMessage(), e);
        }
    }

    /**
     * Récupère TOUS les users d’un campus (multi-threadé avec 8 workers)
     */
    public List<User> getAllCampusUsers(String campusId, String accessToken) {
        List<User> allUsers = new ArrayList<>();
        int currentPage = 1;
        boolean hasMorePages = true;

        List<Future<List<User>>> futures = new ArrayList<>();

        while (hasMorePages) {
            final int page = currentPage;
            futures.add(executor.submit(() -> getCampusUsers(campusId, accessToken, page, MAX_PAGE_SIZE)));

            currentPage++;

            // ✅ limiter la taille des batchs (évite un burst trop gros)
            if (futures.size() >= 8) { 
                hasMorePages = processFutures(futures, allUsers);
                futures.clear();
            }
        }

        // ✅ traiter la dernière batch
        processFutures(futures, allUsers);

        logger.info("Finished fetching all users for campus {}. Total: {}", campusId, allUsers.size());
        return allUsers;
    }

    /**
     * Récupère tous les users actifs (pareil que ci-dessus mais avec filtre)
     */
    public List<User> getAllActiveCampusUsers(String campusId, String accessToken) {
        List<User> allActiveUsers = new ArrayList<>();
        int currentPage = 1;
        boolean hasMorePages = true;

        List<Future<List<User>>> futures = new ArrayList<>();

        while (hasMorePages) {
            final int page = currentPage;
            futures.add(executor.submit(() ->
                getCampusUsersWithFilter(campusId, accessToken, page, MAX_PAGE_SIZE, "active", "true")
            ));

            currentPage++;

            if (futures.size() >= 8) { // ✅ batch = 8 max
                hasMorePages = processFutures(futures, allActiveUsers);
                futures.clear();
            }
        }

        processFutures(futures, allActiveUsers);

        logger.info("Finished fetching all active users for campus {}. Total: {}", campusId, allActiveUsers.size());
        return allActiveUsers;
    }

    /**
     * Récupération users avec filtres
     */
    public List<User> getCampusUsersWithFilter(String campusId, String accessToken, Integer pageNumber,
                                               Integer pageSize, String filterKey, String filterValue) {
        if (campusId == null || campusId.trim().isEmpty()) {
            throw new IllegalArgumentException("Campus ID cannot be null or empty");
        }
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }

        int page = (pageNumber != null && pageNumber > 0) ? pageNumber : 1;
        int size = (pageSize != null && pageSize > 0) ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL_CAMPUS + "/" + campusId + "/users?page[number]=" + page + "&page[size]=" + size;

        if (filterKey != null && !filterKey.trim().isEmpty() &&
            filterValue != null && !filterValue.trim().isEmpty()) {
            url += "&filter[" + filterKey + "]=" + filterValue;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            rateLimiter.acquire();

            ResponseEntity<User[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                User[].class
            );

            User[] users = response.getBody();
            return users != null ? Arrays.asList(users) : new ArrayList<>();

        } catch (RestClientException e) {
            throw new RuntimeException("Error fetching filtered users for campus " + campusId + " page " + page + ": " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les résultats des futures et détermine s’il reste des pages
     */
    private boolean processFutures(List<Future<List<User>>> futures, List<User> accumulator) {
        boolean hasMore = true;
        for (Future<List<User>> f : futures) {
            try {
                List<User> pageUsers = f.get();
                if (pageUsers.isEmpty() || pageUsers.size() < MAX_PAGE_SIZE) {
                    hasMore = false;
                }
                accumulator.addAll(pageUsers);
            } catch (Exception e) {
                logger.error("Error fetching page: {}", e.getMessage(), e);
            }
        }
        return hasMore;
    }
}
