package ps.app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.htmlparser.util.ParserException;

import com.ibm.icu.util.Calendar;

import ps.constants.AppConstants;
import ps.persistence.PersistenceController;
import ps.persistence.PersistenceController2;
import ps.struct.CitationExtractionOutput;
import ps.util.CrawlUtils;
import ps.util.TimeUtils;

public class AdvancedCitationExtractor {

	public static void main(String[] args) throws ParserException, MalformedURLException, IOException,
			ClassNotFoundException, SQLException, NamingException, InterruptedException {

		List<Integer> qList = new ArrayList<Integer>();

//		 // batch 1:
//		 qList.add(1088);
//		 qList.add(1089);
//		 qList.add(1090);
//		 qList.add(1091);
//		 qList.add(1092);
//		 qList.add(1987);
//		 qList.add(1988);
//		 qList.add(1089);
//		 qList.add(1082);
//		 qList.add(1083);

//		// batch 2:
//		qList.add(1098);
//		qList.add(1099);
//		qList.add(1102);
//		qList.add(1293);
//		qList.add(1985);
//		qList.add(1086);
//		qList.add(1104);
//		qList.add(1105);
//		qList.add(1106);
//		qList.add(1108);

		extractAndSaveAllCitationForQueryList(qList);

	}

	private static void extractAndSaveAllCitationForQueryList(List<Integer> qList) throws ParserException,
			ClassNotFoundException, SQLException, IOException, NamingException, InterruptedException {
		for (Integer queryId : qList) {
			extractAndSaveAllCitationForQueryId(queryId);
		}
	}

	private static void extractAndSaveAllCitationForQueryId(int queryId) throws ClassNotFoundException, SQLException,
			IOException, ParserException, NamingException, InterruptedException {
		// fetch all query results for the specific query id
		List<Integer> qResList = PersistenceController.getAllQueryResultsForQuery(queryId);

		// extract and save all citation information for each query result
		for (int queryResId : qResList) {
			String pubTitle = PersistenceController.fetchQueryResultTitleFromId(queryResId);
			performProcessForPubWithTitle(pubTitle, queryResId);
		}
	}

	private static void performProcessForPubWithTitle(String pubTitle, int queryResId) throws ParserException,
			MalformedURLException, IOException, ClassNotFoundException, SQLException, NamingException,
			InterruptedException {
		CitationExtractionOutput c = findNumOfCitationsAndCitedByUrl(pubTitle);
		if (c.getNumOfCitations() != null && c.getNumOfCitations() > 0) {
			Map<Integer, Integer> m = processCitationResults(c);

			Iterator<Integer> it = m.keySet().iterator();
			while (it.hasNext()) {
				Integer key = it.next();
				Integer value = m.get(key);
				System.out.println(key + " : " + value);
			}
			// persists citation info
			PersistenceController2.saveYearCitation(queryResId, m);
		} else {
			System.out.println("No citations found");
		}
	}

	/**
	 * Finds the total number of citations for the specification publication by querying Google Scholar.
	 */
	public static CitationExtractionOutput findNumOfCitationsAndCitedByUrl(String pubTitle) throws ParserException,
			MalformedURLException, IOException {
		Integer numOfCitations = null;
		String citedByUrl = null;
		pubTitle = pubTitle.replaceAll("-", " ");
		String googleScholarQuery = constructSearchQuery(pubTitle);
		String html = CrawlUtils.fetchHtmlCodeForUrl(googleScholarQuery);
		int resSectionStart = html.indexOf(AppConstants.RESULTS_SECTION);
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
			int relatedArticlesIdx = html.indexOf(AppConstants.SEARCH_RES_SECTION_CLOSE);
			int newTitle = html.indexOf("<h3>", titleEnd);
			if (newTitle > relatedArticlesIdx) {
				int citedByIdx = html.indexOf(AppConstants.CITED_BY_START);
				if (citedByIdx < relatedArticlesIdx) {
					int from = citedByIdx + AppConstants.CITED_BY_START.length();
					int to = html.indexOf(AppConstants.CITED_BY_END, from);
					try {
						numOfCitations = Integer.parseInt(html.substring(from, to));

						// extracts "Cited By" URL
						int start = html.indexOf(AppConstants.CITATION_PART, titleEnd) + AppConstants.CITATION_PART.length();
						int end = html.indexOf(AppConstants.AMPERSAND, start);
						citedByUrl = AppConstants.CITED_BY_URL + html.substring(start, end);
						// Logic.processCitationResults(citedByUrl, numOfCitations);

						System.out.println("citedByUrl = " + citedByUrl);

					} catch (Exception e) {
						numOfCitations = null;
					}

				}
			}
		}
		CitationExtractionOutput c = new CitationExtractionOutput(numOfCitations, citedByUrl);
		return c;
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

	/**
	 * Checks special case where the original title is contained in the extracted title but the extracted title contains
	 * more information.
	 * 
	 * @param title
	 *            , the extracted title
	 * @param pubTitle
	 *            , the original title
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
	 * Provides a crawler implementation that attempts to identify: i. the number of citations ii. the year of
	 * publication for all citations of a particular publication.
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws ParserException
	 * @throws InterruptedException
	 * 
	 */
	public static Map<Integer, Integer> processCitationResults(CitationExtractionOutput c) throws ParserException,
			MalformedURLException, IOException, InterruptedException {
		// --------------------------------------------------------------------------------------------------------------
		// NUMBER OF CITATIONS/YEAR OF PUBLICATION EXTRACTION
		// --------------------------------------------------------------------------------------------------------------
		// 1. For each of the publications in the citation page, extract the publication title and year of publication.
		// 2. If the total number of citations is greater than the total number of results displayed in the results page
		// then find the 'next' result page url and parse the results of the next page accordingly.
		// 3. Construct map structure that contains as key the year of publication and as value the total number of
		// encountered publications with the specific publication date.
		// --------------------------------------------------------------------------------------------------------------

		String citedByUrl = c.getCitedByUrl();
		int numOfCitations = c.getNumOfCitations();

		// the total number of Google Scholar result pages to crawl
		int pagesToCrawl = calcPagesToCrawl(numOfCitations, AppConstants.RESULTS_PER_GSCHOLAR_PAGE);
		int pageCount = 1; // since at least 1 citation page exists
		String nextPageUrl = citedByUrl; // the current citation results page

		// map containing as key the publication year and as value the total number of citations received for that
		// particular year, updated throughout all iterations (different citation result pages)
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();

		int numOfRes = 10;

		// perform logic for all Google Scholar result pages!
		while (pageCount <= pagesToCrawl) {

			TimeUtils.randomSleep();

			System.out.println("*** CURRENT PAGE : " + pageCount + "  ***");

			// a. extracts the HTML code of the next citation results page to process
			String resPageHtml = CrawlUtils.fetchHtmlCodeForUrl(nextPageUrl);

			// b. processes the extracted citation results page HTML code
			int rem = numOfCitations - ((pageCount - 1) * AppConstants.RESULTS_PER_GSCHOLAR_PAGE);
			numOfRes = rem > AppConstants.RESULTS_PER_GSCHOLAR_PAGE ? AppConstants.RESULTS_PER_GSCHOLAR_PAGE : rem;

			processCitationResultsPage(resPageHtml, m, numOfRes);

			// c. updates counter and identified the next citation result page URL to parse (probable if condition is
			// satisfied)
			pageCount++;
			// in case another citation result page exists that the crawler should crawl
			if (pageCount <= pagesToCrawl) {
				int startFrom = (pageCount - 1) * AppConstants.RESULTS_PER_GSCHOLAR_PAGE;
				int idx = citedByUrl.indexOf("http://scholar.google.com/scholar?")
						+ "http://scholar.google.com/scholar?".length();
				String citationPart = citedByUrl.substring(idx, citedByUrl.length());
				nextPageUrl = "http://scholar.google.com/scholar?" + "start=" + startFrom + "&" + citationPart;
			}
		}
		return m;
	}

	/**
	 * Performs the main crawling procedure and constructs a map that represents the number of citations received per
	 * year.
	 * 
	 * @param html
	 *            , the HTML snippet to crawl
	 * @param m
	 *            , map structure containing as key the year of publication and as value the number of citations
	 *            received in the particular year.
	 */
	private static void processCitationResultsPage(String html, Map<Integer, Integer> m, int numOfRes) {
		int defaultYear = Calendar.getInstance().get(Calendar.YEAR) - 1;
		String code = new String(html);
		Integer indexFrom = code.indexOf(AppConstants.RESULTS_FROM); // beginning of 'Results' section
		Integer indexTo = null;
		code = code.substring(indexFrom, code.length()); // filtered the interesting part
		String resultSection = "";
		int counter = 1;
		boolean hasMoreRes = true;
		while (hasMoreRes) {
			indexFrom = code.indexOf("div class=gs_rt") + "div class=gs_rt".length();
			if (counter == numOfRes) {
				indexTo = code.indexOf("Related articles", indexFrom);
				if (indexTo == -1) {
					indexTo = code.indexOf(AppConstants.ALERT_NOTIF, indexFrom);
				}
			} else {
				indexTo = code.indexOf("<div class=gs_r>", indexFrom);
				if (indexTo == -1) {
					indexTo = code.indexOf(AppConstants.ALERT_NOTIF, indexFrom);
				}
			}
			resultSection = code.substring(indexFrom, indexTo);
			int from = 0;
			int to = 0;
			from = resultSection.indexOf("href");
			// if(from > -1 && to > -1 && from > to){
			from = resultSection.indexOf(">", from) + 1;
			to = resultSection.indexOf("</a>", from);
			// }
			// else{
			// from = resultSection.indexOf("<h3><span class=gs_ctu>[CITATION]</span> ") +
			// "<h3><span class=gs_ctu>[CITATION]</span> ".length();
			// from = resultSection.indexOf("</span>") + "</span>".length();
			// to = resultSection.indexOf("</h3>", to);
			// }
			try {
				String title = resultSection.substring(from, to);
				from = resultSection.indexOf("<span class=gs_a>") + "<span class=gs_a>".length();
				if (from == -1) {
					from = resultSection.indexOf("<span class=gs_ctu>[CITATION]</span>")
							+ "<span class=gs_ctu>[CITATION]</span>".length();
				}
				to = resultSection.indexOf("</span>", from);
				String txt = resultSection.substring(from, to);
				String year = extractYearFromString(txt);

				if (year == null || "".equals(year)) {
					year = "" + defaultYear;
				}
				System.out.println(counter++ + ". TITLE: " + title + ", YEAR: " + year);
				// updates map: increase the number of citations by 1 for the specific year (the one just extracted)
				Integer d = Integer.valueOf(year);

				if (d > 2011) {
					d = defaultYear;
				}
				Integer i = m.get(d);
				if (i == null) {
					i = 0;
				}
				i++;
				m.put(d, i);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (counter < numOfRes) {
				indexFrom = indexTo + "<div class=gs_r>".length();
				code = code.substring(indexFrom, code.length());
			} else {
				hasMoreRes = false;
			}
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
		int to = 6;
		String testStr = "";
		for (int i = 0; i < n; i++) {
			testStr = s.substring(from, to);
			if (Pattern.matches("[1-2]{1}[0-9]{3}[\\s]-", testStr)) {
				return testStr.substring(0, 4);
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
	 * @param c
	 *            , the total number of citations for the particular publication
	 * @param d
	 *            , the total number of pages displayed per page in Google Scholar
	 * @return the total number of result pages to crawl
	 */
	private static int calcPagesToCrawl(double c, double d) {
		return (int) (((long) (c / d)) + ((c % 10 > 0) ? 1 : 0));
	}

}
