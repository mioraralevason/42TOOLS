package com.ecole._2.repositories;

import com.ecole._2.models.UserPresenceRate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StatsRepository {

    private static final Logger logger = LoggerFactory.getLogger(StatsRepository.class);
    private final DatabaseConnection dbConnection;

    public StatsRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // --- Methode pour tous les utilisateurs ---
    public List<UserPresenceRate> getUserPresenceRate(String startDate, String endDate) {
        String sql = "SELECT user_id AS userId, displayname AS displayname, jours_present AS joursPresent, " +
                     "jours_totaux AS joursTotaux, taux_presence AS tauxPresence " +
                     "FROM taux_presence_par_utilisateur(?, ?)";
        return executeQueryForUsers(sql, startDate, endDate, null);
    }

    // --- Methode pour un utilisateur specifique ---
    public List<UserPresenceRate> getUserPresenceRateByUserId(String startDate, String endDate, String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must not be empty");
        }

        String sql = "SELECT user_id AS userId, displayname AS displayname, jours_present AS joursPresent, " +
                     "jours_totaux AS joursTotaux, taux_presence AS tauxPresence " +
                     "FROM taux_presence_par_utilisateur(?, ?) " +
                     "WHERE user_id = ?";

        return executeQueryForUsers(sql, startDate, endDate, userId);
    }

    // --- Methode interne pour factoriser le code ---
    private List<UserPresenceRate> executeQueryForUsers(String sql, String startDate, String endDate, String userId) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<UserPresenceRate> results = new ArrayList<>();

        try {
            connection = dbConnection.getConnection();
            stmt = connection.prepareStatement(sql);
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            stmt.setDate(1, java.sql.Date.valueOf(start));
            stmt.setDate(2, java.sql.Date.valueOf(end));
            if (userId != null) {
                stmt.setString(3, userId);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            logger.error("Error executing query for user presence: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve user presence rates", e);
        } finally {
            closeResources(connection, stmt, rs);
        }
    }

    public Double getGlobalPresenceRate(String startDate, String endDate) {
        String sql = "SELECT taux_presence_global(?, ?)";
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getConnection();
            stmt = connection.prepareStatement(sql);
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            stmt.setDate(1, java.sql.Date.valueOf(start));
            stmt.setDate(2, java.sql.Date.valueOf(end));
            rs = stmt.executeQuery();

            if (rs.next()) {
                double result = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : result;
            } else {
                logger.warn("No result returned for taux_presence_global with startDate: {}, endDate: {}", startDate, endDate);
                return 0.0;
            }
        } catch (SQLException e) {
            logger.error("Error executing query for taux_presence_global: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve global presence rate", e);
        } finally {
            closeResources(connection, stmt, rs);
        }
    }

    private UserPresenceRate mapRow(ResultSet rs) throws SQLException {
        UserPresenceRate taux = new UserPresenceRate();
        taux.setUserId(rs.getString("userId"));
        taux.setDisplayname(rs.getString("displayname"));
        taux.setJoursPresent(rs.getInt("joursPresent"));
        taux.setJoursTotaux(rs.getInt("joursTotaux"));
        taux.setTauxPresence(rs.getDouble("tauxPresence"));
        return taux;
    }

    private void closeResources(Connection connection, PreparedStatement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { logger.error("Error closing ResultSet: {}", e.getMessage()); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { logger.error("Error closing PreparedStatement: {}", e.getMessage()); }
        dbConnection.closeConnection(connection);
    }
}
