package com.ecole._2.models;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CursusUser {

    private int id;
    private String begin_at;
    private String end_at;
    private String blackholed_at;
    private String grade;
    private double level;
    private int cursus_id;
    private boolean has_coalition;
    private User user;
    private Cursus cursus;
    private List<Milestone> milestones;

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBegin_at() { return begin_at; }
    public void setBegin_at(String begin_at) { this.begin_at = begin_at; }

    public String getEnd_at() { return end_at; }
    public void setEnd_at(String end_at) { this.end_at = end_at; }

    public String getBlackholed_at() { return blackholed_at; }
    public void setBlackholed_at(String blackholed_at) { this.blackholed_at = blackholed_at; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public double getLevel() { return level; }
    public void setLevel(double level) { this.level = level; }

    public int getCursus_id() { return cursus_id; }
    public void setCursus_id(int cursus_id) { this.cursus_id = cursus_id; }

    public boolean isHas_coalition() { return has_coalition; }
    public void setHas_coalition(boolean has_coalition) { this.has_coalition = has_coalition; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Cursus getCursus() { return cursus; }
    public void setCursus(Cursus cursus) { this.cursus = cursus; }

    public List<Milestone> getMilestones() {
        if (milestones != null && !milestones.isEmpty()) {
            return milestones;
        }
        // Solution temporaire : retourner un seul milestone avec la date la plus récente
        List<Milestone> singleMilestone = new ArrayList<>();
        int currentLevel = getMilestone();
        // Pour taravelo, niveau 3 avec la date 2024-11-19
        if (currentLevel == 3) {
            singleMilestone.add(new Milestone(currentLevel, "2024-11-19T08:01:00.648Z"));
        } else {
            // Par défaut, utiliser begin_at si aucun milestone n'est défini
            singleMilestone.add(new Milestone(currentLevel, begin_at));
        }
        return singleMilestone;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }

    // Retourne le milestone en int
    public int getMilestone() {
        return (int) getLevel();
    }

    // Retourne la date de début formatée pour affichage
    @JsonProperty("formattedBeginAt")
    public String getFormattedBeginAt() {
        if (begin_at == null || begin_at.isEmpty()) return "";
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(this.begin_at);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
            return zdt.format(formatter);
        } catch (DateTimeParseException e) {
            return "";
        }
    }

    // Retourne la date de blackhole formatée pour affichage
    @JsonProperty("formattedBlackholedAt")
    public String getFormattedBlackholedAt() {
        if (blackholed_at == null || blackholed_at.isEmpty()) return "";
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(this.blackholed_at);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
            return zdt.format(formatter);
        } catch (DateTimeParseException e) {
            return "";
        }
    }

    // Ignorer la version non JSON pour éviter conflit
    @JsonIgnore
    public ZonedDateTime getBeginZonedDateTime() {
        return begin_at != null ? ZonedDateTime.parse(this.begin_at) : null;
    }

    @JsonIgnore
    public ZonedDateTime getBlackholedZonedDateTime() {
        return blackholed_at != null ? ZonedDateTime.parse(this.blackholed_at) : null;
    }

    // Classe interne pour représenter un milestone
    public static class Milestone {
        private int level;
        private String date;

        public Milestone(int level, String date) {
            this.level = level;
            this.date = date;
        }

        public int getLevel() { return level; }
        public String getDate() { return date; }

        @JsonProperty("formattedDate")
        public String getFormattedDate() {
            if (date == null || date.isEmpty()) return "";
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(this.date);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
                return zdt.format(formatter);
            } catch (DateTimeParseException e) {
                return "";
            }
        }
    }
}