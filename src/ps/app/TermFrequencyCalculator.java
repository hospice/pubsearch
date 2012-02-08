package ps.app;

import java.util.List;

import ps.app2.PdfDownloader;

public class TermFrequencyCalculator {

	public static void run(){
		String title = ""; // FIXME... THIS SHOULD BE READ...
		String author = "";
		List<String> pdfUrlList = PdfDownloader.extractDownloadUrl(title, author);
		for(String pdfUrl : pdfUrlList){
			System.out.println("Examining URL : " + pdfUrl);
			// try to download the PDF from one of the provided URLs...
			try{
			
			}catch(Exception e){
				
			}
		}		
	}
	
}
