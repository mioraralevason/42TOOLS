package com.ecole._2.repositories;

import com.ecole._2.models.TauxPresenceUtilisateur;
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

    public List<TauxPresenceUtilisateur> getTauxPresenceParUtilisateur(String startDate, String endDate) {
        String sql = "SELECT user_id AS userId, displayname AS displayname, jours_present AS joursPresent, " +
                    "jours_totaux AS joursTotaux, taux_presence AS tauxPresence " +
                    "FROM taux_presence_par_utilisateur(?, ?)";
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<TauxPresenceUtilisateur> results = new ArrayList<>();

        try {
            connection = dbConnection.getConnection();
            stmt = connection.prepareStatement(sql);
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            stmt.setDate(1, java.sql.Date.valueOf(start));
            stmt.setDate(2, java.sql.Date.valueOf(end));
            rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            logger.error("Error executing query for taux_presence_par_utilisateur: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve user presence rates", e);
        } finally {
            closeResources(connection, stmt, rs);
        }
    }

    public Double getTauxPresenceGlobal(String startDate, String endDate) {
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
                if (rs.wasNull()) {
                    logger.warn("Global presence rate is null for startDate: {}, endDate: {}", startDate, endDate);
                    return 0.0;
                }
                return result;
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

    private TauxPresenceUtilisateur mapRow(ResultSet rs) throws SQLException {
        TauxPresenceUtilisateur taux = new TauxPresenceUtilisateur();
        taux.setUserId(rs.getString("userId"));
        if (rs.wasNull()) {
            logger.warn("userId is null");
            taux.setUserId(null);
        }

        taux.setDisplayname(rs.getString("displayname"));
        if (rs.wasNull()) {
            logger.warn("displayname is null");
            taux.setDisplayname(null);
        }

        taux.setJoursPresent(rs.getInt("joursPresent"));
        if (rs.wasNull()) {
            logger.warn("joursPresent is null");
            taux.setJoursPresent(0);
        }

        taux.setJoursTotaux(rs.getInt("joursTotaux"));
        if (rs.wasNull()) {
            logger.warn("joursTotaux is null");
            taux.setJoursTotaux(0);
        }

        taux.setTauxPresence(rs.getDouble("tauxPresence"));
        if (rs.wasNull()) {
            logger.warn("tauxPresence is null");
            taux.setTauxPresence(0.0);
        }

        return taux;
    }

    private void closeResources(Connection connection, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Error closing ResultSet: {}", e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error closing PreparedStatement: {}", e.getMessage());
            }
        }
        dbConnection.closeConnection(connection);
    }
}