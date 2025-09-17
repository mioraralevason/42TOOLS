package com.ecole._2.models;

public class UserPresenceRate {
    private String userId;
    private String login;
    private String displayname;
    private String firstName;
    private String lastName;
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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
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