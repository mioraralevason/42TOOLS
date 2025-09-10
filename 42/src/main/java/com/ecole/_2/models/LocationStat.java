package com.ecole._2.models;

import java.time.Duration;
import java.time.LocalDate;

public class LocationStat {
    private LocalDate date;
    private Duration duration;
    private String durationStr;

    public LocationStat() {}

    public LocationStat(LocalDate date, Duration duration) {
        this.date = date;
        this.duration = duration;
        this.durationStr = getDurationStr();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
        this.durationStr = getDurationStr();
    }

    // getter et setter pour Thymeleaf
    public String getDurationStr() {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    public void setDurationStr(String durationStr) {
        this.durationStr = durationStr;
    }

}
