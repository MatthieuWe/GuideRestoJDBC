package ch.hearc.ig.guideresto.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class ConnectionUtils {

    private static final Logger logger = LogManager.getLogger();
    private static Connection instance;

    private ConnectionUtils() {
        throw new AssertionError("Static class - cannot be instantiated");
    }

    public static Connection getConnection() {
        try {
            if (instance == null || instance.isClosed()) {
                synchronized (ConnectionUtils.class) {
                    // Double-check after synchronization
                    if (instance == null || instance.isClosed()) {
                        createConnection();
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking connection status: {}", e.getMessage(), e);
            throw new RuntimeException("Database connection check failed", e);
        }
        return instance;
    }

    private static void createConnection() {
        try {
            // Load database credentials from resources/database.properties
            ResourceBundle dbProps = ResourceBundle.getBundle("database");
            String url = dbProps.getString("database.url");
            String username = dbProps.getString("database.username");
            String password = dbProps.getString("database.password");

            logger.info("Trying to connect to user schema '{}' with JDBC string '{}'", username, url);

            Connection connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);
            instance = connection;

            logger.info("Database connection established successfully!");

        } catch (SQLException | MissingResourceException e) {
            logger.error("FAILED to create database connection: {}", e.getMessage(), e);
            // CRITICAL: Throw the exception so we know it failed!
            throw new RuntimeException("Cannot establish database connection", e);
        }
    }

    public static void closeConnection() {
        synchronized (ConnectionUtils.class) {
            try {
                if (instance != null && !instance.isClosed()) {
                    instance.close();
                    instance = null;
                    logger.info("Database connection closed");
                }
            } catch (SQLException e) {
                logger.error("Error closing database connection: {}", e.getMessage(), e);
            }
        }
    }
}