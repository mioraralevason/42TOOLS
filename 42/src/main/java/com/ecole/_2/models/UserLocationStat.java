package com.ecole._2.models;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserLocationStat {
    private String userId;
    private String userName; // Nouveau champ pour stocker le nom de l'utilisateur
    private List<LocationStat> stats;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public UserLocationStat() {}

    public UserLocationStat(String userId, List<LocationStat> stats) {
        this.userId = userId;
        this.stats = stats;
        this.userName = userId; // Fallback initial sur userId
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName != null ? userName : userId; // Fallback sur userId si userName est null
    }

    public List<LocationStat> getStats() {
        return stats;
    }

    public void setStats(List<LocationStat> stats) {
        this.stats = stats;
    }

    private String extractDatePart(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return null;
        return dateTime.split("T")[0];
    }

    public long getNbDays(String dateDebut, String dateFin) {
        if (stats == null || stats.isEmpty()) {
            return 0;
        }

        LocalDate debut = (dateDebut != null && !dateDebut.isEmpty())
                ? LocalDate.parse(extractDatePart(dateDebut), DATE_FORMAT)
                : stats.stream()
                    .map(LocationStat::getDate)
                    .min(Comparator.naturalOrder())
                    .orElse(LocalDate.now());

        LocalDate fin = (dateFin != null && !dateFin.isEmpty())
                ? LocalDate.parse(extractDatePart(dateFin), DATE_FORMAT)
                : LocalDate.now();

        return stats.stream()
                .map(LocationStat::getDate)
                .filter(d -> !d.isBefore(debut) && !d.isAfter(fin))
                .distinct()
                .count();
    }

    public double getTotalHours(String dateDebut, String dateFin) {
        LocalDate debut = (dateDebut != null && !dateDebut.isEmpty()) 
                ? LocalDate.parse(extractDatePart(dateDebut), DATE_FORMAT)
                : stats.stream()
                    .map(LocationStat::getDate)
                    .min(Comparator.naturalOrder())
                    .orElse(LocalDate.now());

        LocalDate fin = (dateFin != null && !dateFin.isEmpty()) 
                ? LocalDate.parse(extractDatePart(dateFin), DATE_FORMAT)
                : LocalDate.now();

        Duration total = stats.stream()
                .filter(s -> !s.getDate().isBefore(debut) && !s.getDate().isAfter(fin))
                .map(LocationStat::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        return total.toMinutes() / 60.0;
    }

    public long getNbOpenDays(String dateDebut, String dateFin) {
        LocalDate debut = (dateDebut != null && !dateDebut.isEmpty())
                ? LocalDate.parse(extractDatePart(dateDebut), DATE_FORMAT)
                : LocalDate.now().minusMonths(1);

        LocalDate fin = (dateFin != null && !dateFin.isEmpty()) 
                ? LocalDate.parse(extractDatePart(dateFin), DATE_FORMAT)
                : LocalDate.now();

        long joursOuvrables = 0;
        for (LocalDate date = debut; !date.isAfter(fin); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                joursOuvrables++;
            }
        }
        return joursOuvrables;
    }

    public List<LocationStat> filterStatsBetween(String dateDebut, String dateFin) {
        if (stats == null || stats.isEmpty()) {
            return List.of();
        }

        LocalDate debut = (dateDebut != null && !dateDebut.isEmpty())
                ? LocalDate.parse(extractDatePart(dateDebut), DATE_FORMAT)
                : stats.stream()
                    .map(LocationStat::getDate)
                    .min(Comparator.naturalOrder())
                    .orElse(LocalDate.now());

        LocalDate fin = (dateFin != null && !dateFin.isEmpty())
                ? LocalDate.parse(extractDatePart(dateFin), DATE_FORMAT)
                : LocalDate.now();

        return stats.stream()
                .filter(s -> !s.getDate().isBefore(debut) && !s.getDate().isAfter(fin))
                .collect(Collectors.toList());
    }
}