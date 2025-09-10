package com.ecole._2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class Intra42Client {

    @Value("${app.client_id}")
    private String UID;

    @Value("${app.client_secret}")
    private String SECRET;

    private static final String BASE_URL = "https://api.intra.42.fr";
    private static final String ACCESS_TOKEN_URL = BASE_URL + "/oauth/token";
    private static final String USERS_URL = BASE_URL + "/v2/users";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public Intra42Client() {
        client = HttpClient.newHttpClient();
        mapper = new ObjectMapper();
    }

    private void checkCredentials() {
        if (UID == null || SECRET == null) {
            throw new RuntimeException("Please set UID and SECRET in application.properties");
        }
    }

    public String getAccessToken() throws IOException, InterruptedException {
        checkCredentials();
        String body = "grant_type=client_credentials&scope=public projects profile tig elearning forum";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ACCESS_TOKEN_URL))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((UID + ":" + SECRET).getBytes()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Erreur lors de l'obtention du token: " + response.body());
        }

        JsonNode json = mapper.readTree(response.body());
        return json.get("access_token").asText();
    }

    public String getUserId(String login, String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_URL + "?filter[login]=" + login))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Erreur API lors de la récupération de l'ID utilisateur: " + response.body());
        }

        JsonNode users = mapper.readTree(response.body());
        if (!users.isArray() || users.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        return users.get(0).get("id").asText();
    }

    public List<JsonNode> getAllEvents(
            String campusId,
            String cursusId,
            String userId,
            String sort,
            String beginAt,  // format ISO 8601 : 2025-09-01T00:00:00Z
            String endAt,    // format ISO 8601 : 2025-09-30T23:59:59Z
            String token,
            int pageSize
    ) throws IOException, InterruptedException {

        List<JsonNode> allEvents = new ArrayList<>();
        int page = 1;

        while (true) {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/v2/events?page[size]=" + pageSize + "&page[number]=" + page);

            if (campusId != null) urlBuilder.append("&campus_id=").append(campusId);
            if (cursusId != null) urlBuilder.append("&cursus_id=").append(cursusId);
            if (userId != null) urlBuilder.append("&user_id=").append(userId);
            if (sort != null) urlBuilder.append("&sort=").append(sort);
            if (beginAt != null && endAt != null) {
                urlBuilder.append("&range[begin_at]=").append(beginAt).append(",").append(endAt);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlBuilder.toString()))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Erreur lors de la récupération des événements: " + response.body());
            }

            JsonNode events = mapper.readTree(response.body());
            if (!events.isArray() || events.size() == 0) break;

            events.forEach(allEvents::add);
            System.out.println("Fetched page " + page + ", got " + events.size() + " events");
            page++;
        }

        System.out.println("Total events fetched = " + allEvents.size());
        return allEvents;
    }

    public List<JsonNode> getAllLocations(
            String campusId,
            String userId,
            String sort,
            String beginAt,  // ISO 8601
            String endAt,    // ISO 8601
            String token,
            int pageSize
    ) throws IOException, InterruptedException {

        List<JsonNode> allLocations = new ArrayList<>();
        int page = 1;

        while (true) {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/v2/locations?page[size]=" + pageSize + "&page[number]=" + page);

            if (campusId != null) urlBuilder.append("&campus_id=").append(campusId);
            if (userId != null) urlBuilder.append("&user_id=").append(userId);
            if (sort != null) urlBuilder.append("&sort=").append(sort);
            if (beginAt != null && endAt != null) {
                urlBuilder.append("&range[begin_at]=").append(beginAt).append(",").append(endAt);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlBuilder.toString()))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Erreur lors de la récupération des locations: " + response.body());
            }

            JsonNode locations = mapper.readTree(response.body());
            if (!locations.isArray() || locations.size() == 0) break;

            locations.forEach(allLocations::add);
            System.out.println("Fetched page " + page + ", got " + locations.size() + " locations");
            page++;
        }

        System.out.println("Total locations fetched = " + allLocations.size());
        return allLocations;
    }




}
