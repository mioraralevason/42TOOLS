package com.ecole._2;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class Intra42ClientTest {

    @Autowired
    private Intra42Client intra42Client;

    @Test
    public void testFetchEvents() throws Exception {
        String token = intra42Client.getAccessToken();
        System.out.println("Access token: " + token);

        String login = "juramaha";
        String userId = intra42Client.getUserId(login, token);
        System.out.println("User ID for login " + login + ": " + userId);

        List<JsonNode> events = intra42Client.getAllEvents(
                "65",                     // campusId
                null,                     // cursusId
                null,                     // userId
                "begin_at",               // sort
                "2025-09-01T00:00:00Z",  // beginAt
                "2025-09-30T23:59:59Z",  // endAt
                token,
                100                       // pageSize
        );

        if (!events.isEmpty()) {
            System.out.println("First event: " + events.get(0).get("name").asText());
            for (JsonNode event : events) {
                String name = event.has("name") && !event.get("name").isNull() ? event.get("name").asText() : "N/A";
                String begin = event.has("begin_at") && !event.get("begin_at").isNull() ? event.get("begin_at").asText() : "N/A";
                String end = event.has("end_at") && !event.get("end_at").isNull() ? event.get("end_at").asText() : "N/A";
                String location = "N/A";
                if (event.has("location") && !event.get("location").isNull() && event.get("location").has("name")) {
                    location = event.get("location").get("name").asText();
                }

                System.out.println("Event: " + name
                        + " | Begin: " + begin
                        + " | End: " + end
                        + " | Location: " + location
                );
            }

        } else {
            System.out.println("No events found");
        }
        List<JsonNode> locations = intra42Client.getAllLocations(
                "65",                     // campusId
                null,                     // userId
                "begin_at",               // sort
                "2025-09-09T00:00:00Z",  // beginAt
                "2025-09-9T23:59:59Z",  // endAt
                token,
                100                       // pageSize
        );

        if (!locations.isEmpty()) {
            for (JsonNode loc : locations) {
                String user = loc.has("user_id") ? loc.get("user_id").asText() : "N/A";
                String begin = loc.has("begin_at") && !loc.get("begin_at").isNull() ? loc.get("begin_at").asText() : "N/A";
                String end = loc.has("end_at") && !loc.get("end_at").isNull() ? loc.get("end_at").asText() : "N/A";
                System.out.println("User: " + user + " | Begin: " + begin + " | End: " + end);
            }
        } else {
            System.out.println("No locations found");
        }
    }
}
