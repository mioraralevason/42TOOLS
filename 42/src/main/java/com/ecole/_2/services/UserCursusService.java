package com.ecole._2.services;

import com.ecole._2.models.CursusUser;
import com.ecole._2.models.CursusUserList;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class UserCursusService {

    private static final String BASE_URL = "https://api.intra.42.fr/v2/users/";
    private static final String BASE_URL_ALL_CURSUS_USERS = "https://api.intra.42.fr/v2/cursus_users";

    public CursusUserList getUserCursus(String userId, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        String url = BASE_URL + userId + "/cursus_users";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Add 500ms delay before the API call
        try {
            Thread.sleep(125);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }

        ResponseEntity<CursusUser[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CursusUser[].class
        );

        return new CursusUserList(userId, Arrays.asList(response.getBody()));
    }

    public CursusUserList getAllCursusUsers(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        String url = BASE_URL_ALL_CURSUS_USERS;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Add 500ms delay before the API call
        try {
            Thread.sleep(125);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }

        ResponseEntity<CursusUser[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CursusUser[].class
        );

        return new CursusUserList("000000", Arrays.asList(response.getBody()));
    }
}