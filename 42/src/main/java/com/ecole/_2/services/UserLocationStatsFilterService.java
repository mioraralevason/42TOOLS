package com.ecole._2.services;

import com.ecole._2.models.LocationStat;
import com.ecole._2.models.UserLocationStat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserLocationStatsFilterService {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Extrait la partie date d'une chaîne datetime (format ISO avec T)
     */
    private String extractDatePart(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return null;
        return dateTime.split("T")[0];
    }
    
    public List<UserLocationStat> filterUserLocationStatsByDateRange(
            List<UserLocationStat> userLocationStats, 
            String dateDebut, 
            String dateFin) throws IllegalArgumentException {
        
        if (userLocationStats == null) {
            throw new IllegalArgumentException("UserLocationStats list cannot be null");
        }
        
        if (dateDebut == null || dateDebut.trim().isEmpty()) {
            throw new IllegalArgumentException("Date debut cannot be null or empty");
        }
        
        if (dateFin == null || dateFin.trim().isEmpty()) {
            throw new IllegalArgumentException("Date fin cannot be null or empty");
        }
        
        // Parser les dates
        LocalDate debut;
        LocalDate fin;
        
        try {
            debut = LocalDate.parse(extractDatePart(dateDebut), DATE_FORMAT);
            fin = LocalDate.parse(extractDatePart(dateFin), DATE_FORMAT);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss");
        }
        
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("Date debut cannot be after date fin");
        }
        
        // Filtrer la liste
        return userLocationStats.stream()
                .filter(userLocationStat -> userLocationStat != null && 
                        userLocationStat.getStats() != null &&
                        !userLocationStat.getStats().isEmpty())
                .filter(userLocationStat -> hasDateInRange(userLocationStat.getStats(), debut, fin))
                .collect(Collectors.toList());
    }
    
    public List<UserLocationStat> filterUserLocationStatsAndStatsbyDateRange(
            List<UserLocationStat> userLocationStats, 
            String dateDebut, 
            String dateFin) throws IllegalArgumentException {
        
        if (userLocationStats == null) {
            throw new IllegalArgumentException("UserLocationStats list cannot be null");
        }
        
        if (dateDebut == null || dateDebut.trim().isEmpty()) {
            throw new IllegalArgumentException("Date debut cannot be null or empty");
        }
        
        if (dateFin == null || dateFin.trim().isEmpty()) {
            throw new IllegalArgumentException("Date fin cannot be null or empty");
        }
        
        // Parser les dates
        LocalDate debut;
        LocalDate fin;
        
        try {
            debut = LocalDate.parse(extractDatePart(dateDebut), DATE_FORMAT);
            fin = LocalDate.parse(extractDatePart(dateFin), DATE_FORMAT);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss");
        }
        
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("Date debut cannot be after date fin");
        }
        
        // Filtrer et créer de nouveaux objets UserLocationStat avec les stats filtrées
        return userLocationStats.stream()
                .filter(userLocationStat -> userLocationStat != null && 
                        userLocationStat.getStats() != null &&
                        !userLocationStat.getStats().isEmpty())
                .map(userLocationStat -> {
                    List<LocationStat> filteredStats = userLocationStat.getStats().stream()
                            .filter(locationStat -> locationStat != null && 
                                    locationStat.getDate() != null)
                            .filter(locationStat -> {
                                LocalDate date = locationStat.getDate();
                                return date.compareTo(debut) >= 0 && date.compareTo(fin) <= 0;
                            })
                            .collect(Collectors.toList());
                    
                    // Ne créer un nouveau UserLocationStat que si il y a des stats filtrées
                    if (!filteredStats.isEmpty()) {
                        return new UserLocationStat(userLocationStat.getUserId(), filteredStats);
                    }
                    return null;
                })
                .filter(userLocationStat -> userLocationStat != null)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie si une liste de LocationStat contient au moins une date dans la plage spécifiée.
     */
    private boolean hasDateInRange(List<LocationStat> stats, LocalDate debut, LocalDate fin) {
        return stats.stream()
                .filter(stat -> stat != null && stat.getDate() != null)
                .anyMatch(stat -> {
                    LocalDate date = stat.getDate();
                    return date.compareTo(debut) >= 0 && date.compareTo(fin) <= 0;
                });
    }
    
    public List<UserLocationStat> filterUserLocationStatsByDateRange(
            List<UserLocationStat> userLocationStats, 
            LocalDate dateDebut, 
            LocalDate dateFin) throws IllegalArgumentException {
        
        if (userLocationStats == null) {
            throw new IllegalArgumentException("UserLocationStats list cannot be null");
        }
        
        if (dateDebut == null) {
            throw new IllegalArgumentException("Date debut cannot be null");
        }
        
        if (dateFin == null) {
            throw new IllegalArgumentException("Date fin cannot be null");
        }
        
        if (dateDebut.isAfter(dateFin)) {
            throw new IllegalArgumentException("Date debut cannot be after date fin");
        }
        
        return userLocationStats.stream()
                .filter(userLocationStat -> userLocationStat != null && 
                        userLocationStat.getStats() != null &&
                        !userLocationStat.getStats().isEmpty())
                .filter(userLocationStat -> hasDateInRange(userLocationStat.getStats(), dateDebut, dateFin))
                .collect(Collectors.toList());
    }
}