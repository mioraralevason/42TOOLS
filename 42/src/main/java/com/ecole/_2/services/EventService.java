package com.ecole._2.services;

import com.ecole._2.models.Event;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private static final String BASE_URL = "https://api.intra.42.fr/v2/events";
    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 100;

    private final RateLimiter rateLimiter = RateLimiter.create(8.0); // 8 req/sec
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private final ObjectMapper mapper;

    public EventService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    /**
     * Récupère une page d'événements
     */
    public List<Event> getEventsPage(String campusId, String cursusId, String userId,
                                     String sort, String beginAt, String endAt,
                                     String token, int pageNumber, int pageSize) {

        int page = pageNumber > 0 ? pageNumber : 1;
        int size = pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        StringBuilder urlBuilder = new StringBuilder(BASE_URL + "?page[size]=" + size + "&page[number]=" + page);

        if (campusId != null) urlBuilder.append("&campus_id=").append(campusId);
        if (cursusId != null) urlBuilder.append("&cursus_id=").append(cursusId);
        if (userId != null) urlBuilder.append("&user_id=").append(userId);
        if (sort != null) urlBuilder.append("&sort=").append(sort);
        if (beginAt != null && endAt != null) {
            urlBuilder.append("&range[begin_at]=").append(beginAt).append(",").append(endAt);
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            rateLimiter.acquire(); // limite la fréquence

            ResponseEntity<String> response = restTemplate.exchange(
                    urlBuilder.toString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error fetching events: " + response.getBody());
            }

            List<Event> events = mapper.readValue(response.getBody(), new TypeReference<List<Event>>() {});
            return events != null ? events : Collections.emptyList();

        } catch (RestClientException | java.io.IOException e) {
            throw new RuntimeException("Error fetching events page " + page + ": " + e.getMessage(), e);
        }
    }

    /**
     * Récupère tous les événements (multi-thread + rate-limited)
     */
    public List<Event> getAllEvents(String campusId, String cursusId, String userId,
                                    String sort, String beginAt, String endAt,
                                    String token, int pageSize) {

        List<Event> allEvents = new ArrayList<>();
        int currentPage = 1;
        boolean hasMorePages = true;
        List<Future<List<Event>>> futures = new ArrayList<>();

        while (hasMorePages) {
            final int page = currentPage;
            futures.add(executor.submit(() -> getEventsPage(campusId, cursusId, userId, sort, beginAt, endAt, token, page, pageSize)));
            currentPage++;

            if (futures.size() >= 8) { // batch de 8 threads max
                hasMorePages = processFutures(futures, allEvents, pageSize);
                futures.clear();
            }
        }

        // traiter la dernière batch
        processFutures(futures, allEvents, pageSize);

        logger.info("Finished fetching all events. Total: {}", allEvents.size());
        return allEvents;
    }

    /**
     * Traite les futures et détermine si d'autres pages existent
     */
    private boolean processFutures(List<Future<List<Event>>> futures, List<Event> accumulator, int pageSize) {
        boolean hasMore = true;
        for (Future<List<Event>> f : futures) {
            try {
                List<Event> pageEvents = f.get();
                if (pageEvents.isEmpty() || pageEvents.size() < pageSize) {
                    hasMore = false;
                }
                accumulator.addAll(pageEvents);
            } catch (Exception e) {
                logger.error("Error fetching event page: {}", e.getMessage(), e);
            }
        }
        return hasMore;
    }
}
