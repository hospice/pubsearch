package ps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Provides generic functionality for reading/writing properties.
 */
public class PropertyUtils {

    /**
     * Generic method to read specific properties.
     */
    public static String readProperty(String propertyName, String fileLoc) {
        String propertyValue = "";
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(fileLoc));
            propertyValue = (String) properties.get(propertyName);
        } catch (IOException e) {
        }
        return propertyValue;
    }

    /**
     * Generic method to write specific properties.
     */
    public static void writeProperty(String key, String value, String fileLoc) throws IOException {
        File f = new File(fileLoc);
        FileInputStream in = new FileInputStream(f);
        Properties pro = new Properties();
        pro.load(in);
        pro.setProperty(key, value);
        pro.store(new FileOutputStream(fileLoc), null);
    }
    
}
