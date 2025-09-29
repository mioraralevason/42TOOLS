package com.ecole._2.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final String URL = "jdbc:postgresql://localhost:5432/e42";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Discovery@123456";

    public Connection getConnection() throws SQLException {
        try {
            // Ensure PostgreSQL driver is loaded
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(true);
            logger.info("Database connection established");
            return connection;
        } catch (ClassNotFoundException e) {
            logger.error("PostgreSQL driver not found: {}", e.getMessage());
            throw new SQLException("Failed to load PostgreSQL driver", e);
        } catch (SQLException e) {
            logger.error("Failed to connect to database: {}", e.getMessage());
            throw e;
        }
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection: {}", e.getMessage());
            }
        }
    }
}