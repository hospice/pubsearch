package ps.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import ps.constants.NameConstants;
import ps.util.PdfUtils;

/**
 * Provides download functionality.
 */
public class Downloader {

	private final static String OUT_FOLDER = "c:/tmp";

	public static void main(String[] args) throws MalformedURLException {
		String path = "C:/PDF/The Anatomy of a Large-Scale Hypertextual Web Search Engine.pdf";
		URL url = new File(path).toURI().toURL();
		try {
			System.out.println(downloadPdfAndConvertToText(url));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
	 * Downloads the PDF located in the specified URL as a <code>ByteArrayOutputStream</code> in memory and converts it
	 * to text.
	 */
	public static String downloadPdfAndConvertToText(URL url) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedInputStream in = new java.io.BufferedInputStream(url.openStream());
		byte[] data = new byte[512];
		int len;
		while (true) {
			len = in.read(data);
			if (len == -1) {
				break;
			}
			out.write(data);
		}
		out.close();
		in.close();
		return PdfUtils.pdfToText(new ByteArrayInputStream(out.toByteArray()));
	}

	/**
	 * Method overload.
	 */
	public static String downloadPdfAndConvertToText(String pdfUrl) throws Exception {
		return downloadPdfAndConvertToText(new URL(pdfUrl));
	}

	/**
	 * Returns the default output PDF file path
	 */
	public static String getDefaultPdfOutPath(String fileName) {
		return OUT_FOLDER + "\\" + fileName + ".pdf";
	}

}
