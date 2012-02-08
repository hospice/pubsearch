package ps.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides ACM topic-related functionality.
 */
public class TopicUtils {

	/**
	 * Constructs the topics map.
	 */
	public static Map<Integer, String> constructTopicsMap(String filePath) {
		Map<Integer, String> m = new HashMap<Integer, String>();
		try {
			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] strLineSplit = strLine.split(",");
				m.put(Integer.parseInt(strLineSplit[0]), strLineSplit[1] + "(" + strLineSplit[2] + ")");
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return m;
	}
	
}
