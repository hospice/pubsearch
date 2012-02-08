package ps.tests;

import java.io.IOException;

import org.junit.Test;

import ps.extractors.GoogleScholarExtractor;
import ps.util.IOUtils;

// TESTS FOR ALL FIXES RELATED WITH GOOGLE SCHOLAR
public class GoogleScholarTester {

	
	@Test
	public void identifySpecificResult(){
		String pathname = "";
		try {
			String html = IOUtils.readFileFromPath(pathname);
			String title = "";
			String author = "";
			try {
				String section = GoogleScholarExtractor.extractPublicationHtml(title, author);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
