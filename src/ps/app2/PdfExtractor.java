package ps.app2;

import ps.util.PdfUtils;
import ps.util.PropertiesUtils;
import ps.util.TermFrequencyUtils;

public class PdfExtractor {

	public static void extract(String pdfName) throws Exception{
		String title = pdfName + ".pdf";
		String pathname = PropertiesUtils.readPdfRootPath() + title;
		String text = PdfUtils.pdfToText(pathname);	
		//TermFrequencyUtils.extractAbstractAndBody(text);
	}
	
}
