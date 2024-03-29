package ps.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import ps.constants.NameConstants;

/**
 * Provides PDF-related utilities.
 */
public class PdfUtils {
	
	/**
	 * Converts the specified PDF file to text.
	 */
	public static String pdfToText(String pathname) throws Exception {
		PDFParser parser = null;
		String parsedText = null;
		PDFTextStripper stripper = null;
		PDDocument pdDoc = null;
		COSDocument doc = null;
		System.out.print("Converting " + pathname + " to text...");
		File file = new File(pathname);
		if (!file.isFile()) {
			throw new Exception("File " + pathname + " does not exist!");
		}
		try {
			parser = new PDFParser(new FileInputStream(file));
		} catch (Exception e) {
			throw new Exception("Unable to open PDF Parser!");
		}
		try {
			parser.parse();
			stripper = new PDFTextStripper();
			doc = parser.getDocument();
			pdDoc = new PDDocument(doc);
			stripper.setParagraphEnd(NameConstants.PARAGRAPH_END);
			parsedText = stripper.getText(pdDoc);
		} catch (Exception e1) {
			try {
				if (doc != null) {
					doc.close();
				}
				if (pdDoc != null) {
					pdDoc.close();
				}
			} catch (Exception e2) {
				e1.printStackTrace();
			}
			throw new Exception("An exception occured in parsing the PDF Document!");
		}
		System.out.println("done");
		if (doc != null) {
			doc.close();
		}
		if (pdDoc != null) {
			pdDoc.close();
		}
		return parsedText;
	}
	
	/**
	 * Converts the specified PDF file to text.
	 */
	public static String pdfToText(ByteArrayInputStream istream) throws Exception {
		PDFParser parser = null;
		PDFTextStripper stripper = null;
		PDDocument pdDoc = null;
		COSDocument doc = null;
		String parsedText = "";
		try {
			parser = new PDFParser(istream);
		} catch (Exception e) {
			throw new Exception("Unable to open PDF Parser!");
		}
		try {
			parser.parse();
			stripper = new PDFTextStripper();
			doc = parser.getDocument();
			pdDoc = new PDDocument(doc);
			stripper.setParagraphEnd(NameConstants.PARAGRAPH_END);
			parsedText = stripper.getText(pdDoc);
			System.out.println(parsedText);
		} catch (Exception e1) {
			try {
				if (doc != null) {
					doc.close();
				}
				if (pdDoc != null) {
					pdDoc.close();
				}
			} catch (Exception e2) {
				e1.printStackTrace();
			}
			throw new Exception("An exception occured in parsing the PDF Document!");
		}	
		return parsedText;
	}
	
}
