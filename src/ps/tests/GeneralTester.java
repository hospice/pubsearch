package ps.tests;

import ps.app.PdfExtractor;
import ps.util.PdfUtils;
import ps.util.PropertyUtils;

public class GeneralTester {

	public static void main(String[] args) {
		
		try {
			testPropertyReader();
			testPdfExtractor();
			testPdfReader();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/** tests property reading */
	private static void testPropertyReader(){
		String path = PropertyUtils.readPdfRootPath();
		System.out.println("path="+path);
	}
	
	/** tests PDF reading/parsing */
	private static void testPdfReader(){
		String pathname = PropertyUtils.readPdfRootPath() + "test.pdf";
		try {
			String text = PdfUtils.pdfToText(pathname);
			System.out.println(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** tests PDF extractor */
	private static void testPdfExtractor(){
		try {
			String pdfName = "test";
			PdfExtractor.extract(pdfName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
