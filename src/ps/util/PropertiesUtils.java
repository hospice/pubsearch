package ps.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import ps.constants.GeneralConstants;
import ps.constants.PropertyConstants;

/**
 * Provides properties-related utilities.
 */
public class PropertiesUtils {

	public static void main(String[] args) {
		System.out.println(useStemming());
	}
	
    /**
     * Reads the PDF root path property value from the respective .properties file.
     */
    public static String readPdfRootPath() {
        return readProperty(PropertyConstants.PDF_ROOT, GeneralConstants.APP_PROPS_LOC);
    }	
    
    /**
     * Reads the "use stemming" property value from the respective .properties file.
     */
    public static boolean useStemming(){
		return "true".equals(readProperty(PropertyConstants.USE_STEMMING, GeneralConstants.CONFIG_PROPS_LOC));
    }
	
    /**
     * Generic method to read specific properties.
     */
    private static String readProperty(String propertyName, String fileLoc) {
        String propertyValue = "";
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(fileLoc));
            propertyValue = (String) properties.get(propertyName);
        } catch (IOException e) {
        }
        return propertyValue;
    }
    
}
