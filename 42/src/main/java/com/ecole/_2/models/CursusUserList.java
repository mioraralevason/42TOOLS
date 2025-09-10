package com.ecole._2.models;
import java.util.List;
import java.util.stream.Collectors;

public class CursusUserList {
    private String userId;
    private List<CursusUser> cursus_users;

    public CursusUserList() {}

    public CursusUserList(String userId, List<CursusUser> cursus_users) {
        this.setUserId(userId);
        this.setCursusUsers(cursus_users);
    }

    public String getUserId() throws IllegalStateException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalStateException("User ID is not set");
        }
        return userId;
    }

    public void setUserId(String userId) throws IllegalArgumentException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        this.userId = userId;
    }

    public List<CursusUser> getCursusUsers() throws IllegalStateException {
        if (cursus_users == null || cursus_users.isEmpty()) {
            throw new IllegalStateException("Cursus users list is empty");
        }
        return cursus_users;
    }

    public void setCursusUsers(List<CursusUser> cursus_users) {
        this.cursus_users = cursus_users;
    }

    /**
     * Filtre les CursusUser par grade.
     * @param grade le grade à filtrer (ex: "Cadet", "Pisciner")
     * @return CursusUser correspondant au grade
     */
    public CursusUser filterByGrade(String grade) throws IllegalStateException {
        List<CursusUser> cursusUserList = getCursusUsers();
        CursusUser result = cursusUserList.stream()
                .filter(c -> grade.equalsIgnoreCase(c.getGrade()))
                .findFirst()
                .orElse(null);
        if (result == null) {
            throw new IllegalStateException("No CursusUser found with grade: " + grade);
        }
        return result;
    }

    /**
     * Filtre les CursusUser par mois de piscine.
     * @param poolMonth le mois de piscine à filtrer (ex: "september", "october")
     * @return Liste des CursusUser correspondant au mois de piscine
     * @throws IllegalArgumentException si le mois est null ou vide
     * @throws IllegalStateException si aucun CursusUser n'est trouvé
     */
    public List<CursusUser> filterByPoolMonth(String poolMonth) throws IllegalArgumentException, IllegalStateException {
        if (poolMonth == null || poolMonth.trim().isEmpty()) {
            throw new IllegalArgumentException("Pool month cannot be null or empty");
        }
        
        List<CursusUser> cursusUserList = getCursusUsers();
        List<CursusUser> filteredList = cursusUserList.stream()
                .filter(c -> c.getUser() != null && 
                        poolMonth.toLowerCase().equals(c.getUser().getPool_month()))
                .collect(Collectors.toList());
        
        if (filteredList.isEmpty()) {
            throw new IllegalStateException("No CursusUser found with pool month: " + poolMonth);
        }
        
        return filteredList;
    }

    /**
     * Filtre les CursusUser par année de piscine.
     * @param poolYear l'année de piscine à filtrer (ex: "2025", "2024")
     * @return Liste des CursusUser correspondant à l'année de piscine
     * @throws IllegalArgumentException si l'année est null ou vide
     * @throws IllegalStateException si aucun CursusUser n'est trouvé
     */
    public List<CursusUser> filterByPoolYear(String poolYear) throws IllegalArgumentException, IllegalStateException {
        if (poolYear == null || poolYear.trim().isEmpty()) {
            throw new IllegalArgumentException("Pool year cannot be null or empty");
        }
        
        List<CursusUser> cursusUserList = getCursusUsers();
        List<CursusUser> filteredList = cursusUserList.stream()
                .filter(c -> c.getUser() != null && 
                        poolYear.equals(c.getUser().getPool_year()))
                .collect(Collectors.toList());
        
        if (filteredList.isEmpty()) {
            throw new IllegalStateException("No CursusUser found with pool year: " + poolYear);
        }
        
        return filteredList;
    }

    /**
     * Filtre les CursusUser par mois ET année de piscine.
     * @param poolMonth le mois de piscine à filtrer (ex: "september")
     * @param poolYear l'année de piscine à filtrer (ex: "2025")
     * @return Liste des CursusUser correspondant au mois et à l'année de piscine
     * @throws IllegalArgumentException si le mois ou l'année sont null ou vides
     * @throws IllegalStateException si aucun CursusUser n'est trouvé
     */
    public List<CursusUser> filterByPoolMonthAndYear(String poolMonth, String poolYear) 
            throws IllegalArgumentException, IllegalStateException {
        if (poolMonth == null || poolMonth.trim().isEmpty()) {
            throw new IllegalArgumentException("Pool month cannot be null or empty");
        }
        if (poolYear == null || poolYear.trim().isEmpty()) {
            throw new IllegalArgumentException("Pool year cannot be null or empty");
        }
        
        List<CursusUser> cursusUserList = getCursusUsers();
        List<CursusUser> filteredList = cursusUserList.stream()
                .filter(c -> c.getUser() != null && 
                        poolMonth.toLowerCase().equals(c.getUser().getPool_month()) &&
                        poolYear.equals(c.getUser().getPool_year()))
                .collect(Collectors.toList());
        
        if (filteredList.isEmpty()) {
            throw new IllegalStateException("No CursusUser found with pool month: " + 
                    poolMonth + " and pool year: " + poolYear);
        }
        
        return filteredList;
    }
}