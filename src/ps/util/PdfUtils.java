package ps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import ps.constants.NameConstants;

/**
 * Provides PDF-related utilities.
 */
public class PdfUtils {

	public static void main(String[] args) {
		String pubTitle = "pagerank";
		String pathname = PropertyUtils.readPdfRootPath() + pubTitle + ".pdf";
		try {
			String text = PdfUtils.pdfToText(pathname);
			System.out.println(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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
	 * Write the parsed text from PDF to a file.
	 */
	private static void writeToFile(String pdfText, String fileName) {
		System.out.print("Writing PDF text to output text file " + fileName + "...");
		try {
			PrintWriter pw = new PrintWriter(fileName);
					pw.print(pdfText);
			pw.close();
		} catch (Exception e) {
			System.out.println("An exception occured in writing the pdf text to file!");
			e.printStackTrace();
		}
		System.out.println("done");
		System.out.println();
	}

	/**
	 * Converts PDF file to TXT and saves it in the same directory.
	 */
	public static String convertAndSavePdfToTxt(String pdfFile) throws Exception {
		String extractedText = pdfToText(pdfFile);
		String txtFile = "";
		if (extractedText != null) {
			txtFile = pdfFile.substring(0, pdfFile.indexOf(".pdf")) + ".txt";
			writeToFile(extractedText, txtFile);
		} else {
			throw new Exception("Unable to convert the file: " + pdfFile + " to text format!");
		}
		return txtFile;
	}
	
}
