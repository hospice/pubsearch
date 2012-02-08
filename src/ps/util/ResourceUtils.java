package ps.util;

import java.io.FileInputStream;
import java.io.IOException;

public class ResourceUtils {

	public static void main(String[] args) throws IOException {
		readConfigFile();
	}

	/**
	 * Reads the experiments configuration file.
	 */
	public static String readConfigFile() throws IOException {
		return readResourceFile(ps.constants.GeneralConstants.CONFIG_FILE_PATH);
	}

	/**
	 * Reads the experiments queries file.
	 */
	public static String readQueriesFile() throws IOException {
		return readResourceFile(ps.constants.GeneralConstants.QUERIES_FILE_PATH);
	}

	/**
	 * Reads the specified resource file.
	 */
	private static String readResourceFile(String c) throws IOException {
		String resourceFile = "";
		FileInputStream fin = new FileInputStream(c);
		int ch = 0;
		while ((ch = fin.read()) != -1) {
			resourceFile += "" + (char) ch;
		}
		System.out.println(resourceFile);
		return resourceFile;
	}

}
