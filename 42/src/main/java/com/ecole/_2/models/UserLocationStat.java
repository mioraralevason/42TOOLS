package com.ecole._2.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserLocationStat {
    private String userId;
    private String userName;
    private List<LocationStat> stats;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public UserLocationStat() {}

    public UserLocationStat(String userId, List<LocationStat> stats) {
        this.userId = userId;
        this.stats = stats;
        this.userName = userId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) {
        this.userName = userName != null ? userName : userId;
    }

    public List<LocationStat> getStats() { return stats; }
    public void setStats(List<LocationStat> stats) { this.stats = stats; }

    private String extractDatePart(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return null;
        return dateTime.split("T")[0];
    }

    // --- Jours total ---
    @JsonProperty("nbDays")
    public long getNbDays(String dateDebut, String dateFin) {
        if (stats == null || stats.isEmpty()) return 0;

        LocalDate debut = (dateDebut != null && !dateDebut.isEmpty())
                ? LocalDate.parse(extractDatePart(dateDebut), DATE_FORMAT)
                : stats.stream().map(LocationStat::getDate).min(Comparator.naturalOrder()).orElse(LocalDate.now());

        LocalDate fin = (dateFin != null && !dateFin.isEmpty())
                ? LocalDate.parse(extractDatePart(dateFin), DATE_FORMAT)
                : LocalDate.now();

        return stats.stream()
                .map(LocationStat::getDate)
                .filter(d -> !d.isBefore(debut) && !d.isAfter(fin))
                .distinct()
                .count();
    }

    // --- Jours ouvrables ---
    @JsonProperty("nbOpenDays")
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

    // --- Total d'heures ---
    @JsonProperty("totalHours")
    public double getTotalHours(String dateDebut, String dateFin) {
        if (stats == null || stats.isEmpty()) return 0;

        LocalDate debut = (dateDebut != null && !dateDebut.isEmpty()) 
                ? LocalDate.parse(extractDatePart(dateDebut), DATE_FORMAT)
                : stats.stream().map(LocationStat::getDate).min(Comparator.naturalOrder()).orElse(LocalDate.now());

        LocalDate fin = (dateFin != null && !dateFin.isEmpty()) 
                ? LocalDate.parse(extractDatePart(dateFin), DATE_FORMAT)
                : LocalDate.now();

        Duration total = stats.stream()
                .filter(s -> !s.getDate().isBefore(debut) && !s.getDate().isAfter(fin))
                .map(LocationStat::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        return total.toMinutes() / 60.0;
    }

    // --- Filtrage de stats par date ---
    public List<LocationStat> filterStatsBetween(String dateDebut, String dateFin) {
        if (stats == null || stats.isEmpty()) return List.of();

        LocalDate debut = (dateDebut != null && !dateDebut.isEmpty())
                ? LocalDate.parse(extractDatePart(dateDebut), DATE_FORMAT)
                : stats.stream().map(LocationStat::getDate).min(Comparator.naturalOrder()).orElse(LocalDate.now());

        LocalDate fin = (dateFin != null && !dateFin.isEmpty())
                ? LocalDate.parse(extractDatePart(dateFin), DATE_FORMAT)
                : LocalDate.now();

        return stats.stream()
                .filter(s -> !s.getDate().isBefore(debut) && !s.getDate().isAfter(fin))
                .collect(Collectors.toList());
    }

    @JsonProperty("nbDays")
    public long getNbDaysDefault() {
        return getNbDays(null, null);
    }

    @JsonProperty("nbOpenDays")
    public long getNbOpenDaysDefault() {
        return getNbOpenDays(null, null);
    }

    @JsonProperty("totalHours")
    public double getTotalHoursDefault() {
        return getTotalHours(null, null);
    }

}
