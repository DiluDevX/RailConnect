package com.railway.routemanagement.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ============================================================================
 * MODULE 2: ROUTE MANAGEMENT (UC-05) - DATABASE CONNECTION MANAGER
 * ============================================================================
 * 
 * Architectural Pattern: Singleton Design Pattern (Creational Pattern)
 * 
 * Responsibility:
 * Centralizes JDBC connection lifecycle, credentials, and connection recycling.
 * Prevents resource exhaustion by avoiding uncontrolled DriverManager calls.
 * 
 * @author Railway Management System Architecture Team (SE2030)
 */
public class DatabaseConnection {

    // Default configuration (can be overridden via environment variables)
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/railway_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "password";

    private static DatabaseConnection instance;
    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        // Read configuration with fallback to local defaults
        this.url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : DEFAULT_URL;
        this.user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DEFAULT_USER;
        this.password = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : DEFAULT_PASSWORD;

        try {
            // Load MySQL Connector/J driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] MySQL Driver (com.mysql.cj.jdbc.Driver) not detected on classpath.");
        }
    }

    /**
     * Thread-safe Singleton access method.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Obtains an active connection to the MySQL database.
     * Caller is responsible for closing the connection (or using try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests whether the MySQL database is currently reachable.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }
}
