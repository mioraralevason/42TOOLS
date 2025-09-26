package com.ecole._2.models;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String id;
    private String email;
    private String login;
    private String first_name;
    private String last_name;
    private String usual_full_name;
    private String kind;
    private Image image;
    private String pool_month;
    private String pool_year;
    private List<CursusUser> cursus_users;

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getUsual_full_name() {
        return usual_full_name;
    }

    public void setUsual_full_name(String usual_full_name) {
        this.usual_full_name = usual_full_name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getPool_month() {
        return pool_month;
    }

    public void setPool_month(String pool_month) {
        this.pool_month = pool_month;
    }

    public String getPool_year() {
        return pool_year;
    }

    public void setPool_year(String pool_year) {
        this.pool_year = pool_year;
    }

    public List<CursusUser> getCursus_users() {
        return cursus_users;
    }

    public void setCursus_users(List<CursusUser> cursus_users) {
        this.cursus_users = cursus_users;
    }

    public static List<User> filterUsersByPool(List<User> users, String poolMonth, String poolYear) {
        return users.stream()
                .filter(user -> poolMonth.equalsIgnoreCase(user.getPool_month())
                        && poolYear.equalsIgnoreCase(user.getPool_year()))
                .collect(Collectors.toList());
    }
}