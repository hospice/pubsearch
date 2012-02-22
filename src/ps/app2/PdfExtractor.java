package ps.app2;

import ps.util.PdfUtils;
import ps.util.PropertyUtils;

public class PdfExtractor {

	public static void extract(String pdfName) throws Exception{
		String title = pdfName + ".pdf";
		String pathname = PropertyUtils.readPdfRootPath() + title;
		String text = PdfUtils.pdfToText(pathnme);	
		//TermFrequencyUtils.extractAbstractAndBody(text);
	}
	
}
