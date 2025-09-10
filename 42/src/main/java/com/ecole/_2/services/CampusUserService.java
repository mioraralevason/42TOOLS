package com.ecole._2.services;

import com.ecole._2.models.CampusUser;
import com.ecole._2.models.CampusUserList;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class CampusUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(CampusUserService.class);
    private static final String BASE_URL = "https://api.intra.42.fr/v2/campus_users";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public CampusUserList getCampusUsers(String campusId, String accessToken) {
        try {
            logger.info("=== START FETCHING CAMPUS USERS ===");
            logger.info("Campus ID: {}", campusId);
            logger.info("Token present: {}", accessToken != null ? "YES" : "NO");
            
            RestTemplate restTemplate = new RestTemplate();
            String url = BASE_URL + "?campus_id=" + campusId;
            logger.info("Request URL: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.info("Sending request to 42 API...");
            ResponseEntity<CampusUser[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CampusUser[].class
            );
            
            logger.info("Response status: {}", response.getStatusCode());
            logger.info("Response headers: {}", response.getHeaders());
            
            CampusUser[] campusUsers = response.getBody();
            if (campusUsers == null) {
                logger.warn("WARNING: Response body is null");
                return new CampusUserList(campusId, Arrays.asList());
            }
            
            logger.info("Number of users fetched: {}", campusUsers.length);
            
            int maxToLog = Math.min(5, campusUsers.length);
            for (int i = 0; i < maxToLog; i++) {
                CampusUser user = campusUsers[i];
                logger.info("User {}: {}", i + 1, logCampusUser(user));
            }
            
            if (campusUsers.length > 5) {
                logger.info("... and {} more users", campusUsers.length - 5);
            }
            
            List<CampusUser> userList = Arrays.asList(campusUsers);
            CampusUserList result = new CampusUserList(campusId, userList);
            
            logger.info("=== END FETCHING CAMPUS USERS ===");
            logger.info("Final result - Campus ID: {}, Number of users: {}", 
                       result.getCampusId(), result.getCampusUsers().size());
            
            return result;
            
        } catch (HttpClientErrorException e) {
            logger.error("HTTP client error ({}): {}", e.getStatusCode(), e.getMessage());
            logger.error("Error response body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("42 API authentication error: " + e.getStatusCode());
            
        } catch (HttpServerErrorException e) {
            logger.error("HTTP server error ({}): {}", e.getStatusCode(), e.getMessage());
            logger.error("Error response body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("42 API server error: " + e.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Unexpected error while fetching campus users", e);
            throw new RuntimeException("Error fetching campus users: " + e.getMessage(), e);
        }
    }
    
    /**
     * Utility method to log details of a CampusUser in a readable way
     */
    private String logCampusUser(CampusUser user) {
        if (user == null) {
            return "null";
        }
        
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("CampusUser{");
            
            if (user.getId() != null) sb.append("id=").append(user.getId()).append(", ");
            if (user.getUser_id() != null) sb.append("userId=").append(user.getUser_id()).append(", ");
            if (user.getCampus_id() != null) sb.append("campusId=").append(user.getCampus_id()).append(", ");
            sb.append("isPrimary=").append(user.is_primary()).append(", ");
            if (user.getCreated_at() != null) sb.append("createdAt=").append(user.getCreated_at()).append(", ");
            if (user.getUpdated_at() != null) sb.append("updatedAt=").append(user.getUpdated_at());

            sb.append("}");
            return sb.toString();
            
        } catch (Exception e) {
            logger.warn("Error while logging CampusUser: {}", e.getMessage());
            return "CampusUser{serialization error}";
        }
    }
    
    /**
     * Debug method to display the raw API response
     */
    public String getCampusUsersRawResponse(String campusId, String accessToken) {
        try {
            logger.info("=== FETCHING RAW RESPONSE ===");
            
            RestTemplate restTemplate = new RestTemplate();
            String url = BASE_URL + "?campus_id=" + campusId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            String rawResponse = response.getBody();
            logger.info("Raw API response:");
            logger.info("Status: {}", response.getStatusCode());
            logger.info("Content-Type: {}", response.getHeaders().getContentType());
            logger.info("Response size: {} characters", rawResponse != null ? rawResponse.length() : 0);
            
            if (rawResponse != null) {
                String preview = rawResponse.length() > 500 ? 
                    rawResponse.substring(0, 500) + "..." : rawResponse;
                logger.info("Response preview: {}", preview);
                
                try {
                    objectMapper.readTree(rawResponse);
                    logger.info("Valid JSON ✓");
                } catch (Exception e) {
                    logger.error("Invalid JSON ✗: {}", e.getMessage());
                }
            }
            
            return rawResponse;
            
        } catch (Exception e) {
            logger.error("Error while fetching raw response", e);
            return null;
        }
    }
}