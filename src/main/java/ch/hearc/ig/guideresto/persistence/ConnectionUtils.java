package ch.hearc.ig.guideresto.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provide helper methods to deal with database connections.
 * Ideally, this should also manage instance pools in a bigger application.
 *
 * @author arnaud.geiser
 * @author alain.matile
 */
public final class ConnectionUtils {

    private static final Logger logger = LogManager.getLogger();
    private static Connection instance;

    //constructeur privé pour le singleton
    private ConnectionUtils() {
        throw new AssertionError("Static class - cannot be instantiated");
    }

    public static Connection getConnection() {
        try {
            if (instance == null || instance.isClosed()) { // lazy initialisation
                synchronized (ConnectionUtils.class) {
                    if (instance != null && !instance.isClosed()) { //double check
                        try {
                            // Load database credentials from resources/database.properties
                            ResourceBundle dbProps = ResourceBundle.getBundle("database");
                            String url = dbProps.getString("database.url");
                            String username = dbProps.getString("database.username");
                            String password = dbProps.getString("database.password");

                            logger.info("Trying to connect to user schema '{}' with JDBC string '{}'", username, url);

                            Connection connection = DriverManager.getConnection(url, username, password);
                            connection.setAutoCommit(false);
                            ConnectionUtils.instance = connection; //put into a static class
                        } catch (SQLException | MissingResourceException e) {
                            logger.error(e.getMessage());
                        }
                    }

                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return instance; //return the instance
    }

    public static void closeConnection() {
        synchronized (ConnectionUtils.class) {
            try {
                if (instance != null && !instance.isClosed()) {
                    instance.close();
                    instance = null;
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}

