package ps.persistence;

import ps.constants.GeneralConstants;
import ps.util.PropertyUtils;

/**
 * Provides functionality for reading/writing persistence-related properties
 */
public class PersistenceProperties {

    /**
     * Returns the DB username
     */
    public static String userName() {
        return PropertyUtils.readProperty(GeneralConstants.USERNAME, GeneralConstants.PERSISTENCE_PROPERTIES_FILE_LOCATION);
    }

    /**
     * Returns the DB password
     */
    public static String password() {
        return PropertyUtils.readProperty(GeneralConstants.PASSWORD, GeneralConstants.PERSISTENCE_PROPERTIES_FILE_LOCATION);
    }

    /**
     * Returns the DB connectionString
     */
    public static String connectionString() {
        return PropertyUtils.readProperty(GeneralConstants.CONNECTION, GeneralConstants.PERSISTENCE_PROPERTIES_FILE_LOCATION);
    }

    /**
     * Returns the DB driver
     */
    public static String dbDriver() {
        return PropertyUtils.readProperty(GeneralConstants.DRIVER, GeneralConstants.PERSISTENCE_PROPERTIES_FILE_LOCATION);
    }

}
