package ps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import ps.constants.GeneralConstants;
import ps.constants.PropertyConstants;

/**
 * Provides generic functionality for reading/writing properties.
 */
public class PropertyUtils {
	
	public static void main(String[] args) {
		System.out.println(keepRunning());
	}
	
	public static boolean useProxy(){
		return "true".equals(readProperty(PropertyConstants.USE_PROXY, GeneralConstants.APP_PROPS_LOC));
	}
	
   /**
     * Reads the "use stemming" property value from the respective .properties file.
     */
    public static boolean keepRunning(){
		return "true".equals(readProperty(PropertyConstants.KEEP_RUNNING, GeneralConstants.PROCESSOR_PROPS_LOC));
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
