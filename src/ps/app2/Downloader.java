package ps.app2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides download functionality.
 */
public class Downloader {

	private final static String OUT_FOLDER = "c:/tmp";

	/**
	 * Downloads from the specified URL to the specified output file
	 */
	public static void download(String url, String outputFilePath) throws MalformedURLException, IOException {
		System.out.print("Downloading from : " + url + " ... ");
		BufferedInputStream in = new java.io.BufferedInputStream(new URL(url).openStream());
		File out = new File(outputFilePath);
		FileOutputStream fos = new FileOutputStream(out);
		BufferedOutputStream buff = new BufferedOutputStream(fos, 16384);
		byte[] data = new byte[512];
		int len;
		while (true) {
			len = in.read(data);
			if (len == -1) {
				break;
			}
			buff.write(data, 0, len);
		}
		buff.close();
		in.close();
		System.out.print("DONE.");
		System.out.println();
	}

	/**
	 * Returns the default output PDF file path
	 */
	public static String getDefaultPdfOutPath(String fileName) {
		return OUT_FOLDER + "\\" + fileName + ".pdf";
	}

}
