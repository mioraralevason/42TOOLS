package com.ecole._2.models;

public class UserPresenceRate {
    private String userId;
    private String login;
    private String displayname;
    private Integer joursPresent;
    private Integer joursTotaux;
    private Double tauxPresence;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public Integer getJoursPresent() {
        return joursPresent;
    }

    public void setJoursPresent(Integer joursPresent) {
        this.joursPresent = joursPresent;
    }

    public Integer getJoursTotaux() {
        return joursTotaux;
    }

    public void setJoursTotaux(Integer joursTotaux) {
        this.joursTotaux = joursTotaux;
    }

    public Double getTauxPresence() {
        return tauxPresence;
    }

    public void setTauxPresence(Double tauxPresence) {
        this.tauxPresence = tauxPresence;
    }
}