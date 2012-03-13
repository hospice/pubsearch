package ps.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.htmlparser.Parser;
import org.htmlparser.http.ConnectionManager;
import org.htmlparser.util.ParserException;

import ps.constants.GeneralConstants;
import ps.struct.AcmResult;
import ps.struct.AcmTopic;
import ps.struct.PublicationInfo;

/**
 * Provides crawling functionality.
 */
public class CrawlUtils {
	
	private final static String PROXY_HOST = "wsa.central.nbg.gr";
	private final static String PROXY_USER = "bank\\e74269";
	private final static String PROXY_PASS = "o2lyqmd8!";
	private final static int PROXY_PORT = 8080;

	public static void main(String[] args) throws ParserException {
		String url = "http://scholar.google.com";
		String html = fetchHtmlCodeForUrlWithProxy(url);
		System.out.println(html);
	}
	
	public static String fetchHtmlCodeForUrlWithProxy(String url) throws ParserException{
		String html = "";
		ConnectionManager cm = Parser.getConnectionManager();
		cm.setProxyHost(PROXY_HOST);
		cm.setProxyUser(PROXY_USER);
		cm.setProxyPassword(PROXY_PASS);
		cm.setProxyPort(PROXY_PORT);
		try {
			new URL(url);
			Parser parser = new Parser(url);
			return parser.parse(null).toHtml();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return html;
	}
	
	/**
	 * Fetches the HTML code for specific URL.
	 */
	public static String fetchHtmlCodeForUrl(String url) throws ParserException, IOException, MalformedURLException {
		try {
			new URL(url);
			Parser parser = new Parser(url);
			return parser.parse(null).toHtml();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Constructs search URL according to new version of ACM Portal.
	 */
	public static String constructSearchUrl(String query) {
		String queryUrl = "";
		String quotes = "%22";
		String space = "%20";
		StringTokenizer s = new StringTokenizer(query, " ");
		while (s.hasMoreTokens()) {
			String token = s.nextToken();
			queryUrl += token.replace("\"", quotes);
			if (s.hasMoreTokens()) {
				queryUrl += space;
			}
		}
		String start = "http://portal.acm.org/results.cfm?h=1&cfid=7413750&cftoken=53145256&query=";
		String end = "&dl=GUIDE";
		return start + queryUrl + end;
	}

	/**
	 * Extracts all publication information.
	 */
	public static PublicationInfo extractAllPublicationInfo(String publicationUrl) throws Exception {
		PublicationInfo publicationInfo = new PublicationInfo();
		publicationInfo.setUrl(publicationUrl);
		// Topics:
		publicationInfo.setTopics(extractTopicsFromPublication(publicationUrl));
		String html = fetchHtmlCodeForUrl(publicationUrl);

		// If publication contains the specified terms it is not eligible for processing (still the information is
		// extracted)
		boolean isEligible = !html.contains("General Chairs") && !html.contains("Program Chairs");
		publicationInfo.setIsEligible(isEligible);

		// Title:
		int titleFrom = html.indexOf(GeneralConstants.TITLE_START) + GeneralConstants.TITLE_START.length();
		int titleTo = html.indexOf(GeneralConstants.TITLE_END);
		String title = html.substring(titleFrom, titleTo);
		title = title.replace("'", "\\'");
		publicationInfo.setTitle(title);
		publicationInfo.setAbstractTxt("");
		// Authors:
		List<String> authorsList = new ArrayList<String>();
		// case of 1 author
		if (html.contains(GeneralConstants.AUTHOR)) {
			int authPtr = html.indexOf(GeneralConstants.AUTHOR) + GeneralConstants.AUTHOR.length();
			int authFrom = html.indexOf(GeneralConstants.AUTHOR_START, authPtr)
					+ GeneralConstants.AUTHOR_START.length();
			int authTo = html.indexOf(GeneralConstants.AUTHOR_END, authFrom);
			String author = html.substring(authFrom, authTo);
			authorsList.add(author);
		}
		// case of > 1 author
		if (html.contains(GeneralConstants.AUTHORS)) {
			int authFrom = html.indexOf(GeneralConstants.AUTHORS) + GeneralConstants.AUTHORS.length();
			int authTo = html.indexOf(GeneralConstants.AUTHORS_SECTION_END, authFrom);
			String authorsSection = html.substring(authFrom, authTo);
			while (authorsSection.contains(GeneralConstants.AUTHOR_START)) {
				authFrom = authorsSection.indexOf(GeneralConstants.AUTHOR_START)
						+ GeneralConstants.AUTHOR_START.length();
				authTo = authorsSection.indexOf(GeneralConstants.AUTHOR_END, authFrom);
				String author = authorsSection.substring(authFrom, authTo);
				authorsList.add(author);
				authorsSection = authorsSection.substring(authTo + GeneralConstants.AUTHOR_END.length(),
						authorsSection.length());
			}
		}
		publicationInfo.setAuthors(authorsList);

		// finds the total number of citations as calculated by Google Scholar
		publicationInfo.setNumOfCitations(GoogleScholarUtils.findNumOfCitations(title));
		return publicationInfo;
	}

	/**
	 * Fetches the ACM Portal results.
	 */
	public static List<AcmResult> fetchAcmResults(String query, int n) throws ParserException, MalformedURLException,
			IOException {
		List<AcmResult> acmResults = new ArrayList<AcmResult>();
		String queryUrl = constructSearchUrl(query);
		String orgHtml = CrawlUtils.fetchHtmlCodeForUrl(queryUrl);
		if (orgHtml.contains("was not found.")) {
			return acmResults;
		}
		String html = new String(orgHtml);
		int cursorFrom = 0;
		int count = 0;
		while (count < n) {
			// i. extracts the publication's URL:
			cursorFrom = html.indexOf(GeneralConstants.ACM_RESULT_START, cursorFrom)
					+ GeneralConstants.ACM_RESULT_START.length();
			int cursorTo = html.indexOf("\"", cursorFrom);
			String urlPart = html.substring(cursorFrom, cursorTo);
			String pubUrl = GeneralConstants.ACM_PUB_URL_PREFIX + urlPart;
			// ii. extracts the publication's title:
			cursorFrom = html.indexOf(GeneralConstants.SELF, cursorTo) + GeneralConstants.SELF.length();
			cursorTo = html.indexOf("</A>", cursorFrom);
			String pubTitle = "";
			try {
				pubTitle = html.substring(cursorFrom, cursorTo);
				cursorFrom = cursorTo;
			} catch (Exception e) {
			}
			// iv. extracts the year of publish:
			Integer yearOfPublication = null;
			cursorFrom = html.indexOf(GeneralConstants.ACM_PUB_YEAR_FROM, cursorTo)
					+ GeneralConstants.ACM_PUB_YEAR_FROM.length();
			cursorTo = html.indexOf(GeneralConstants.ACM_PUB_YEAR_TO, cursorFrom);
			String yearAsStr = html.substring(cursorFrom, cursorTo);
			if (yearAsStr != null) {
				String[] arr = yearAsStr.trim().split(" ");
				if (arr.length == 2) {
					yearOfPublication = Integer.parseInt(arr[1]);
				}
			}
			acmResults.add(new AcmResult(pubTitle, pubUrl, yearOfPublication));
			// checks is other results exist, else exits
			if (html.indexOf(GeneralConstants.ACM_RESULT_START, cursorFrom) == -1) {
				break;
			}
			count++;
		}
		if (count < n) {
			String resultPage = "Result page:";
			int resPagePos = orgHtml.indexOf(resultPage);
			if (resPagePos > -1) {
				int from = resPagePos + resultPage.length();
				from = orgHtml.indexOf("results.cfm", from);
				int to = orgHtml.indexOf("\">", from);
				String nextPageUrl = "http://portal.acm.org/" + orgHtml.substring(from, to);
				String nextPageHtml = CrawlUtils.fetchHtmlCodeForUrl(nextPageUrl);
				cursorFrom = 0;
				while (count < n) {
					cursorFrom = nextPageHtml.indexOf(GeneralConstants.ACM_RESULT_START, cursorFrom)
							+ GeneralConstants.ACM_RESULT_START.length();
					int cursorTo = nextPageHtml.indexOf("\"", cursorFrom);
					String urlPart = nextPageHtml.substring(cursorFrom, cursorTo);
					String pubUrl = GeneralConstants.ACM_PUB_URL_PREFIX + urlPart;
					cursorFrom = nextPageHtml.indexOf(GeneralConstants.SELF, cursorTo) + GeneralConstants.SELF.length();
					cursorTo = nextPageHtml.indexOf("</A>", cursorFrom);
					String pubTitle = "";
					try {
						pubTitle = nextPageHtml.substring(cursorFrom, cursorTo);
						cursorFrom = cursorTo;
					} catch (Exception e) {
					}
					// iv. extracts the year of publish:
					Integer yearOfPublication = null;
					cursorFrom = nextPageHtml.indexOf(GeneralConstants.ACM_PUB_YEAR_FROM, cursorTo)
							+ GeneralConstants.ACM_PUB_YEAR_FROM.length();
					cursorTo = nextPageHtml.indexOf(GeneralConstants.ACM_PUB_YEAR_TO, cursorFrom);
					String yearAsStr = nextPageHtml.substring(cursorFrom, cursorTo);
					if (yearAsStr != null) {
						String[] arr = yearAsStr.trim().split(" ");
						if (arr.length == 2) {
							yearOfPublication = Integer.parseInt(arr[1]);
						}
					}
					acmResults.add(new AcmResult(pubTitle, pubUrl, yearOfPublication));
					// checks is other results exist, else exits
					if (nextPageHtml.indexOf(GeneralConstants.ACM_RESULT_START, cursorFrom) == -1) {
						break;
					}
					count++;
				}
			}
		}
		return acmResults;
	}

	/**
	 * Extracts all ACM topics for the specific publication.
	 */
	public static List<AcmTopic> extractTopicsFromPublication(String publicationUrl) throws Exception {
		List<AcmTopic> topicList = new ArrayList<AcmTopic>();
		String html = CrawlUtils.fetchHtmlCodeForUrl(publicationUrl + GeneralConstants.FLAT_VIEW_PARAM);
		int from = html.indexOf(GeneralConstants.TOPICS_FROM) + GeneralConstants.TOPICS_FROM.length();
		int to = html.indexOf(GeneralConstants.TOPICS_TO);
		if (from > to) {
			throw new Exception("Required HTML elements not found! Check url: " + publicationUrl);
		}
		String filteredHtml = html.substring(from, to);
		int pointerFrom = filteredHtml.indexOf(GeneralConstants.STRONG_OPEN);
		while (pointerFrom > -1) {
			pointerFrom = pointerFrom + GeneralConstants.STRONG_OPEN.length();
			int pointerTo = filteredHtml.indexOf(GeneralConstants.STRONG_CLOSE, pointerFrom);
			String topicCode = filteredHtml.substring(pointerFrom, pointerTo);
			if (!GeneralConstants.SUBJECTS.equals(topicCode)) {
				int topicDescrFrom = filteredHtml.indexOf(GeneralConstants.DESCR_START, pointerFrom)
						+ GeneralConstants.DESCR_START.length();
				int topicDescrTo = filteredHtml.indexOf(GeneralConstants.DESCR_END, topicDescrFrom);
				String topicDescr = filteredHtml.substring(topicDescrFrom, topicDescrTo);
				AcmTopic acmTopic = new AcmTopic(topicCode, topicDescr);
				topicList.add(acmTopic);
			}
			pointerFrom = filteredHtml.indexOf(GeneralConstants.STRONG_OPEN, pointerTo);
		}
		// necessary step to remove duplicate topics
		List<AcmTopic> topicsFiltered = new ArrayList<AcmTopic>();
		for (AcmTopic a : topicList) {
			if (!checkIfMatch(a, topicsFiltered)) {
				topicsFiltered.add(a);
			}
		}
		return topicsFiltered;
	}

	/**
	 * Checks if matching exists.
	 */
	private static boolean checkIfMatch(AcmTopic a, List<AcmTopic> l) {
		boolean hasMatch = false;
		for (AcmTopic b : l) {
			if (b.getCode().equals(a.getCode())) {
				return true;
			}
		}
		return hasMatch;
	}

}
