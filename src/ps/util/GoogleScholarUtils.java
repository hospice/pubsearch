package ps.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.htmlparser.util.ParserException;

/**
 * Provides Google Scholar related functionality.
 */
public class GoogleScholarUtils {

	private static final int RESULTS_PER_PAGE = 10;
	
	private static final String NEXT_PAGE_START = "http://scholar.google.com/scholar?start=";
	private static final String NEXT_PAGE_END = "&q=A+view+of+cloud+computing&hl=en&as_sdt=0,5";
	private static final String RESULTS_FROM = "<font size=-1>Results";
	public final static String CITED_BY_START = "Cited by ";
	public final static String CITED_BY_END = "</a>";
	public final static String RESULTS_SECTION = "Results <b>";
	public final static String SEARCH_RES_SECTION_CLOSE = "Related articles";
	public final static String CITATION_PART = "<a href=\"/scholar?cites=";
	public final static String AMPERSAND = "&amp;";
	
	/**
	 * Finds the total number of citations for the specification publication by querying Google Scholar.
	 */
	public static Integer findNumOfCitations(String pubTitle) throws ParserException, MalformedURLException,
			IOException {
		Integer numOfCitations = null;
		pubTitle = pubTitle.replaceAll("-", " ");
		String googleScholarQuery = constructSearchQuery(pubTitle);

		String html = CrawlUtils.fetchHtmlCodeForUrl(googleScholarQuery);

		int resSectionStart = html.indexOf(RESULTS_SECTION);
		int titleStart = html.indexOf(" sec)&nbsp;", resSectionStart) + " sec)&nbsp;".length();
		int titleEnd = html.indexOf("</a>", titleStart);
		String title = html.substring(titleStart, titleEnd).replaceAll("\\<[^>]*>", "");
		title = title.replaceAll("-", " ");

		if (title.contains("Did you mean")) {
			titleStart = html.indexOf("<h3>", titleEnd) + "<h3>".length();
			titleStart = html.indexOf(">", titleStart) + 1;
			titleEnd = html.indexOf("</a>", titleStart);
			title = html.substring(titleStart, titleEnd).replaceAll("\\<[^>]*>", "");
			title = title.replaceAll("-", " ");
		}

		if (evaluateTitles(title.toLowerCase(), pubTitle.toLowerCase())) {
			int relatedArticlesIdx = html.indexOf(SEARCH_RES_SECTION_CLOSE);
			int newTitle = html.indexOf("<h3>", titleEnd);
			if (newTitle > relatedArticlesIdx) {
				int citedByIdx = html.indexOf(CITED_BY_START);
				if (citedByIdx < relatedArticlesIdx) {
					int from = citedByIdx + CITED_BY_START.length();
					int to = html.indexOf(CITED_BY_END, from);
					try {
						numOfCitations = Integer.parseInt(html.substring(from, to));
						
						// extracts "Cited By" URL
						int  start = html.indexOf(CITATION_PART, titleEnd) + CITATION_PART.length();
						int  end = html.indexOf(AMPERSAND, start);
						String citedByUrl = CITATION_PART + html.substring(start, end);
						processCitationResults(citedByUrl, numOfCitations);
						
					} catch (Exception e) {
						numOfCitations = null;
					}

				}
			}
		}
		return numOfCitations;
	}
	
	/**
	 * Provides a crawler implementation that attempts to identify: 
	 *   i.  the number of citations
	 *   ii. the year of publication for all citations of a particular publication.
	 * 
	 */
	public static void processCitationResults(String citedByUrl, int numOfCitations){
		// --------------------------------------------------------------------------------------------------------------
		// NUMBER OF CITATIONS/YEAR OF PUBLICATION EXTRACTION
		// --------------------------------------------------------------------------------------------------------------
		// 1. For each of the publications in the citation page, extract the publication title and year of publication.
		// 2. If the total number of citations is greater than the total number of results displayed in the results page
		//    then find the 'next' result page url and parse the results of the next page accordingly.
		// 3. Construct map structure that contains as key the year of publication and as value the total number of 
		//    encountered publications with the specific publication date.
		// --------------------------------------------------------------------------------------------------------------
		
		// the total number of Google Scholar result pages to crawl
		int pagesToCrawl = calcPagesToCrawl(numOfCitations, RESULTS_PER_PAGE);
		int pageCount = 1; // since at least 1 citation page exists
		String nextPageUrl = citedByUrl; // the current citation results page
		
		// map containing as key the publication year and as value the total number of citations received for that
		// particular year, updated throughout all iterations (different citation result pages)
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		
		// perform logic for all Google Scholar result pages!
		while (pageCount <= pagesToCrawl) {
			
			// a. extracts the HTML code of the next citation results page to process 
			String resPageHtml = ""; // FIXME: extract HTML code based on 'currCitPage' param!!!!
			
			// b. processes the extracted citation results page HTML code
			processCitationResultsPage(resPageHtml, m);
			
			// c. updates counter and identified the next citation result page URL to parse (probable if condition is satisfied)
			pageCount++;
			// in case another citation result page exists that the crawler should crawl
			if (pageCount <= pagesToCrawl){
				int startFrom = (pageCount-1) * RESULTS_PER_PAGE; 
				nextPageUrl = NEXT_PAGE_START + startFrom + NEXT_PAGE_END; // this is the next citation page to crawl
			}
		}
		
	}

	/**
	 * Performs the main crawling procedure and constructs a map that represents the number of citations received per
	 * year.
	 * 
	 * @param html, the HTML snippet to crawl
	 * @param m, map structure containing as key the year of publication and as value the number of citations received
	 *            in the particular year.
	 */
	private static void processCitationResultsPage(String html, Map<Integer, Integer> m) {
		String code = new String(html);
		Integer indexFrom = code.indexOf(RESULTS_FROM); // beginning of 'Results' section
		Integer indexTo = null;
		code = code.substring(indexFrom, code.length()); // filtered the interesting part
		String resultSection = "";
		int counter = 1;
		boolean hasMoreRes = true;
		while (hasMoreRes) {
			indexFrom = code.indexOf("div class=gs_rt") + "div class=gs_rt".length();
			if (counter == 10) {
				indexTo = code.indexOf("Related articles", indexFrom);
			} else {
				indexTo = code.indexOf("<div class=gs_r>", indexFrom);
			}
			resultSection = code.substring(indexFrom, indexTo);
			int from = 0;
			int to = 0;
			from = resultSection.indexOf("href");
			from = resultSection.indexOf(">", from) + 1;
			to = resultSection.indexOf("</a>", from);
			String title = resultSection.substring(from, to);
			from = resultSection.indexOf("<span class=gs_a>") + "<span class=gs_a>".length();
			to = resultSection.indexOf("</span>", from);
			String txt = resultSection.substring(from, to);
			String year = extractYearFromString(txt);
			if (counter < 10) {
				indexFrom = indexTo + "<div class=gs_r>".length();
				code = code.substring(indexFrom, code.length());
			} else {
				hasMoreRes = false;
			}
			System.out.println(counter++ + ". TITLE: " + title + ", YEAR: " + year);
			// updates map: increase the number of citations by 1 for the specific year (the one just extracted)
			Integer d = Integer.valueOf(year);
			m.put(d,  m.get(d) + 1);
		}
	}
	
	/**
	 * Extracts the year from the specified string.
	 */
	private static String extractYearFromString(String s) {
		if (s.length() < 4) {
			return "";
		}
		int n = s.length() - 3;
		int from = 0;
		int to = 4;
		String testStr = "";
		for (int i = 0; i < n; i++) {
			testStr = s.substring(from, to);
			if (Pattern.matches("^[1-2]{1}[0-9]{3}$", testStr)) {
				return testStr;
			}
			from++;
			to++;
			if (from > s.length() || to >= s.length()) {
				return "";
			}
		}
		return "";
	}
	
	/**
	 * Calculates the total number of result pages to crawl.
	 * 
	 * @param c, the total number of citations for the particular publication
	 * @param d, the total number of pages displayed per page in Google Scholar
	 * @return the total number of result pages to crawl
	 */
	private static int calcPagesToCrawl(double c, double d) {
		return (int) (((long) (c / d)) + ((c % 10 > 0) ? 1 : 0));
	}

	/**
	 * Checks special case where the original title is contained in the extracted title but the extracted title 
	 * contains more information.
	 * 
	 * @param title, the extracted title
	 * @param pubTitle, the original title
	 */
	public static boolean evaluateTitles(String title, String pubTitle) {
		if (title.contains(pubTitle)) {
			if (title.trim().length() > pubTitle.length()) {
				// if length is different checks if at least first words match (ex. POLYPHONET case)
				title = title.trim().split(" ")[0].replaceAll("[^A-Za-z]", "");
				pubTitle = pubTitle.split(" ")[0].replaceAll("[^A-Za-z]", "");
				return title.equals(pubTitle);
			}
			return true;
		}
		return false;
	}

	/**
	 * Constructs a Google Scholar search query based on the publication title.
	 */
	public static String constructSearchQuery(String pubTitle) {
		String query = pubTitle;
		String pref = "http://scholar.google.gr/scholar?hl=en&q=";
		String title = query.replaceAll(" ", "+");
		String suff = "&btnG=%C1%ED%E1%E6%DE%F4%E7%F3%E7&as_ylo=&as_vis=0";
		return pref + title + suff;
	}

}