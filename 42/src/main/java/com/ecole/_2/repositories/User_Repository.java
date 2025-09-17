package com.ecole._2.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ecole._2.models.User_;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class User_Repository {

    private static final Logger logger = LoggerFactory.getLogger(User_Repository.class);
    private final DatabaseConnection dbConnection;

    public User_Repository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // --- Create a new user ---
    public void createUser_(User_ user) {
        String sql = "INSERT INTO User_ (user_id, email, login, first_name, last_name, usual_full_name, " +
                     "usual_first_name, url, phone, displayname, kind, staff, correction_point, " +
                     "anonymize_date, data_erasure_date, created_at, updated_at, alumnized_at, " +
                     "alumni, active, pool_month, wallet, pool_year, location, image_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = dbConnection.getConnection();
            stmt = connection.prepareStatement(sql);
            setUser_Parameters(stmt, user);
            stmt.executeUpdate();
            logger.info("User_ created successfully with user_id: {}", user.getUserId());
        } catch (SQLException e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new RuntimeException("Failed to create user", e);
        } finally {
            closeResources(connection, stmt, null);
        }
    }

    // --- Retrieve all users ---
    public List<User_> getAllUser_s() {
        String sql = "SELECT u.*, i.link, i.large_, i.medium, i.small, i.micro, i.created_at AS image_created_at " +
                     "FROM User_ u LEFT JOIN Image i ON u.image_id = i.image_id";
        return executeQueryForUser_s(sql, null);
    }

    // --- Retrieve a user by user_id ---
    public User_ getUser_ById(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must not be empty");
        }

        String sql = "SELECT u.*, i.link, i.large_, i.medium, i.small, i.micro, i.created_at AS image_created_at " +
                     "FROM User_ u LEFT JOIN Image i ON u.image_id = i.image_id WHERE u.user_id = ?";
        List<User_> users = executeQueryForUser_s(sql, userId);
        return users.isEmpty() ? null : users.get(0);
    }

    // --- Update a user ---
    public void updateUser_(User_ user) {
        String sql = "UPDATE User_ SET email = ?, login = ?, first_name = ?, last_name = ?, usual_full_name = ?, " +
                     "usual_first_name = ?, url = ?, phone = ?, displayname = ?, kind = ?, staff = ?, " +
                     "correction_point = ?, anonymize_date = ?, data_erasure_date = ?, created_at = ?, " +
                     "updated_at = ?, alumnized_at = ?, alumni = ?, active = ?, pool_month = ?, wallet = ?, " +
                     "pool_year = ?, location = ?, image_id = ? WHERE user_id = ?";
        
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = dbConnection.getConnection();
            stmt = connection.prepareStatement(sql);
            setUser_Parameters(stmt, user);
            stmt.setString(25, user.getUserId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warn("No user found with user_id: {}", user.getUserId());
                throw new RuntimeException("User_ not found");
            }
            logger.info("User_ updated successfully with user_id: {}", user.getUserId());
        } catch (SQLException e) {
            logger.error("Error updating user: {}", e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        } finally {
            closeResources(connection, stmt, null);
        }
    }

    // --- Delete a user by user_id ---
    public void deleteUser_(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must not be empty");
        }

        String sql = "DELETE FROM User_ WHERE user_id = ?";
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = dbConnection.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warn("No user found with user_id: {}", userId);
                throw new RuntimeException("User_ not found");
            }
            logger.info("User_ deleted successfully with user_id: {}", userId);
        } catch (SQLException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        } finally {
            closeResources(connection, stmt, null);
        }
    }

    // --- Internal method to execute queries ---
    private List<User_> executeQueryForUser_s(String sql, String userId) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<User_> results = new ArrayList<>();

        try {
            connection = dbConnection.getConnection();
            stmt = connection.prepareStatement(sql);
            if (userId != null) {
                stmt.setString(1, userId);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            logger.error("Error executing query for users: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve users", e);
        } finally {
            closeResources(connection, stmt, rs);
        }
    }

    // --- Map ResultSet to User_ object ---
    private User_ mapRow(ResultSet rs) throws SQLException {
        User_ user = new User_();
        user.setUserId(rs.getString("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setUsualFullName(rs.getString("usual_full_name"));
        user.setUsualFirstName(rs.getString("usual_first_name"));
        user.setUrl(rs.getString("url"));
        user.setPhone(rs.getString("phone"));
        user.setDisplayname(rs.getString("displayname"));
        user.setKind(rs.getString("kind"));
        user.setStaff(rs.getBoolean("staff"));
        user.setCorrectionPoint(rs.getDouble("correction_point"));
        user.setAnonymizeDate(rs.getTimestamp("anonymize_date") != null ? rs.getTimestamp("anonymize_date").toInstant() : null);
        user.setDataErasureDate(rs.getTimestamp("data_erasure_date") != null ? rs.getTimestamp("data_erasure_date").toInstant() : null);
        user.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null);
        user.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null);
        user.setAlumnizedAt(rs.getTimestamp("alumnized_at") != null ? rs.getTimestamp("alumnized_at").toInstant() : null);
        user.setAlumni(rs.getBoolean("alumni"));
        user.setActive(rs.getBoolean("active"));
        user.setPoolMonth(rs.getString("pool_month"));
        user.setWallet(rs.getDouble("wallet"));
        user.setPoolYear(rs.getString("pool_year"));
        user.setLocation(rs.getString("location"));
        user.setImageId(rs.getInt("image_id"));

        // Map Image fields
        if (rs.getString("link") != null) {
            user.setImageLink(rs.getString("link"));
            user.setImageLarge(rs.getString("large_"));
            user.setImageMedium(rs.getString("medium"));
            user.setImageSmall(rs.getString("small"));
            user.setImageMicro(rs.getString("micro"));
            user.setImageCreatedAt(rs.getTimestamp("image_created_at") != null ? rs.getTimestamp("image_created_at").toInstant() : null);
        }

        return user;
    }

    // --- Set parameters for PreparedStatement ---
    private void setUser_Parameters(PreparedStatement stmt, User_ user) throws SQLException {
        stmt.setString(1, user.getUserId());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getLogin());
        stmt.setString(4, user.getFirstName());
        stmt.setString(5, user.getLastName());
        stmt.setString(6, user.getUsualFullName());
        stmt.setString(7, user.getUsualFirstName());
        stmt.setString(8, user.getUrl());
        stmt.setString(9, user.getPhone());
        stmt.setString(10, user.getDisplayname());
        stmt.setString(11, user.getKind());
        stmt.setBoolean(12, user.isStaff());
        stmt.setDouble(13, user.getCorrectionPoint());
        stmt.setTimestamp(14, user.getAnonymizeDate() != null ? Timestamp.from(user.getAnonymizeDate()) : null);
        stmt.setTimestamp(15, user.getDataErasureDate() != null ? Timestamp.from(user.getDataErasureDate()) : null);
        stmt.setTimestamp(16, user.getCreatedAt() != null ? Timestamp.from(user.getCreatedAt()) : null);
        stmt.setTimestamp(17, user.getUpdatedAt() != null ? Timestamp.from(user.getUpdatedAt()) : null);
        stmt.setTimestamp(18, user.getAlumnizedAt() != null ? Timestamp.from(user.getAlumnizedAt()) : null);
        stmt.setBoolean(19, user.isAlumni());
        stmt.setBoolean(20, user.isActive());
        stmt.setString(21, user.getPoolMonth());
        stmt.setDouble(22, user.getWallet());
        stmt.setString(23, user.getPoolYear());
        stmt.setString(24, user.getLocation());
        stmt.setInt(25, user.getImageId());
    }

    // --- Close database resources ---
    private void closeResources(Connection connection, PreparedStatement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { logger.error("Error closing ResultSet: {}", e.getMessage()); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { logger.error("Error closing PreparedStatement: {}", e.getMessage()); }
        dbConnection.closeConnection(connection);
    }
}