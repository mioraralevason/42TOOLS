package com.ecole._2.models;

import java.time.ZonedDateTime;

public class CampusUser {
    private String id;
    private String user_id;
    private String campus_id;
    private boolean is_primary;
    private ZonedDateTime created_at;
    private ZonedDateTime updated_at;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCampus_id() {
        return String.valueOf(campus_id);
    }

    public void setCampus_id(String campus_id) {
        this.campus_id = campus_id;
    }

    public boolean is_primary() {
        return is_primary;
    }

    public void set_primary(boolean primary) {
        is_primary = primary;
    }

    public ZonedDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(ZonedDateTime created_at) {
        this.created_at = created_at;
    }

    public ZonedDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(ZonedDateTime updated_at) {
        this.updated_at = updated_at;
    }

    
}