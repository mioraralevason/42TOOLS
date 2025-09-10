package com.ecole._2.models;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ecole._2.services.ApiService;

public class CampusUserList {
    private String campusId;
    private List<CampusUser> campusUsers;
    private ApiService apiService;

    public CampusUserList(String campusId, List<CampusUser> campusUsers) {
        this.campusId = campusId;
        this.campusUsers = campusUsers;
        this.apiService = new ApiService();
    }

    // Getters and setters
    public String getCampusId() {
        return campusId;
    }

    public void setCampusId(String campusId) {
        this.campusId = campusId;
    }

    public List<CampusUser> getCampusUsers() {
        return campusUsers;
    }

    public void setCampusUsers(List<CampusUser> campusUsers) {
        this.campusUsers = campusUsers;
    }
    public List<String> getUserLogins(String accessToken) {
        return campusUsers.stream()
                .map(campusUser -> {
                    try {
                        Map<String, Object> userData = apiService.getUser(campusUser.getUser_id(), accessToken);
                        return (String) userData.get("login");
                    } catch (Exception e) {
                        System.err.println("Error fetching login for user ID " + campusUser.getUser_id() + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(login -> login != null)
                .collect(Collectors.toList());
    }
}