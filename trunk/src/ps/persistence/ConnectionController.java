package ps.persistence;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides database connectivity functionality.
 */
public class ConnectionController {

    private static Connection connection;

    /**
     * Returns database connection.
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        if (connection == null) {
            Class.forName(PersistenceProperties.dbDriver());
            connection = DriverManager.getConnection(PersistenceProperties.connectionString(), PersistenceProperties.userName(),
                    PersistenceProperties.password());
            return connection;
        } else {
            return connection;
        }
    }
    /**
     * Closes a database connection.
     */
    public static void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

}

