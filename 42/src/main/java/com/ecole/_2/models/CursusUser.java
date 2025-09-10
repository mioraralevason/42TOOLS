package com.ecole._2.models;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CursusUser {
    private int id;
    private String begin_at;
    private String end_at;
    private String grade;
    private double level;
    private int cursus_id;
    private boolean has_coalition;
    private User user;
    private Cursus cursus;

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBegin_at() {
        return begin_at;
    }

    public void setBegin_at(String begin_at) {
        this.begin_at = begin_at;
    }

    public String getEnd_at() {
        return end_at;
    }

    public void setEnd_at(String end_at) {
        this.end_at = end_at;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public int getCursus_id() {
        return cursus_id;
    }

    public void setCursus_id(int cursus_id) {
        this.cursus_id = cursus_id;
    }

    public boolean isHas_coalition() {
        return has_coalition;
    }

    public void setHas_coalition(boolean has_coalition) {
        this.has_coalition = has_coalition;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Cursus getCursus() {
        return cursus;
    }

    public void setCursus(Cursus cursus) {
        this.cursus = cursus;
    }

    public int getMilestone(){
        System.out.println((int)getLevel());
        return (int)(getLevel());
    }

    public String getFormattedBeginAt() {
        ZonedDateTime zdt = ZonedDateTime.parse(this.begin_at); 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH);
        return zdt.format(formatter);
    }
    
}
