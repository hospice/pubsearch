package ps.extractors;

import java.util.ArrayList;
import java.util.List;

import ps.struct.PublicationInfo;
import ps.util.CrawlUtils;
import ps.util.IOUtils;
import ps.util.PrintUtils;
import ps.util.StringUtils;

/**
 * Performs the extraction of all publication results from Microsoft Academic Search. </p>
 * 
 * NOTE: For the number of citations and the year of publication, the information is extracted directly from Google
 * Scholar.
 */
public class MicrosoftAcademicSearchExtractor {

	public static void main(String[] args) throws Exception {
		String pathname = "";
		List<String> qList = getQueryList();
		for (String q : qList) {
			pathname = "C:\\_tmp\\new\\ms\\" + q.replaceAll("\"", "");
			String html = IOUtils.readFileFromPath(pathname);
			List<PublicationInfo> pList = extractPublicationResults2(html);
			PrintUtils.printPublicationInfo(pList);
			System.out.println("***********************");
		}
	}
	
	private static List<String> getQueryList() {
		List<String> qList = new ArrayList<String>();
//		qList.add("\"page rank\" clustering");
//		qList.add("\"social network\" \"information retrieval\"");
//		qList.add("\"unsupervised learning\"");
//		qList.add("clustering \"information retrieval\"");
		qList.add("\"web mining\"");
		return qList;
	}
	/**
	 * Extracts all publication results' related information from the HTML of the results page.
	 */
	public static List<PublicationInfo> extractPublicationResults(String query) throws Exception {
		List<PublicationInfo> results = new ArrayList<PublicationInfo>();
		String html = CrawlUtils.fetchHtmlCodeForUrl(constructQuery(query));
		int from = html.indexOf(ExtrConstants.MS_RES_BEG);
		boolean hasNext = from > -1;
		if (hasNext) {
			int to = from;
			while (hasNext) {
				to = html.indexOf(ExtrConstants.MS_RES_END, from);
				// fetches the current result section
				String section = html.substring(from, to);
				String title = extractTitle(section);
				String url = extractURL(section);
				PublicationInfo p = new PublicationInfo(title, url, extractAuthors(html));
				GoogleScholarExtractor.updatePublicationCitationsAndDate(p);
				results.add(p);
				html = html.substring(to, html.length());
				from = html.indexOf(ExtrConstants.MS_RES_BEG);
				hasNext = from > -1;
			}
		}
		return results;
	}
	
	/**
	 * Extracts all publication results' related information from the HTML of the results page.
	 */
	public static List<PublicationInfo> extractPublicationResults2(String html) throws Exception {
		List<PublicationInfo> results = new ArrayList<PublicationInfo>();
		int from = html.indexOf(ExtrConstants.MS_RES_BEG);
		boolean hasNext = from > -1;
		if (hasNext) {
			int to = from;
			while (hasNext) {
				to = html.indexOf(ExtrConstants.MS_RES_END, from);
				// fetches the current result section
				String section = html.substring(from, to);
				String title = extractTitle(section);
				String url = extractURL(section);
				PublicationInfo p = new PublicationInfo(title, url, extractAuthors(html));
				//GoogleScholarExtractor.updatePublicationCitationsAndDate(p);
				results.add(p);
				html = html.substring(to, html.length());
				from = html.indexOf(ExtrConstants.MS_RES_BEG);
				hasNext = from > -1;
			}
		}
		return results;
	}

	/**
	 * Extracts the publication title from the provided HTML snippet.
	 */
	private static String extractTitle(String html) {
		int from = html.indexOf(ExtrConstants.H3_TAG);
		from = html.indexOf(ExtrConstants.OPEN_A_TAG, from);
		from = html.indexOf(ExtrConstants.RIGHT_TAG_CHAR, from) + 1;
		int to = html.indexOf(ExtrConstants.A_HREF_END, from);
		String title = html.substring(from, to);
		return StringUtils.stripTextFromHtml(title);
	}

	/**
	 * Extracts the publication URL from the provided HTML snippet.
	 */
	private static String extractURL(String html) {
		int from = html.indexOf(ExtrConstants.PUBLICATION_URL_PART);
		int to = html.indexOf("\"", from);
		return ExtrConstants.MS_URL_PREF + "/" + html.substring(from, to);
	}

	/**
	 * Extracts the publication authors from the provided HTML snippet.
	 */
	private static List<String> extractAuthors(String html) {
		String copy = new String(html);
		List<String> l = new ArrayList<String>();
		boolean hasMoreAuthors = true;
		while (hasMoreAuthors) {
			int from = copy.indexOf(ExtrConstants.MS_AUTH_PREF);
			if (from > -1) {
				from = copy.indexOf(ExtrConstants.RIGHT_TAG_CHAR, from) + 1;
				int to = copy.indexOf(ExtrConstants.A_HREF_END, from);
				String author = StringUtils.stripTextFromHtml(copy.substring(from, to));
				l.add(author);
				copy = copy.substring(to, copy.length());
			} else {
				hasMoreAuthors = false;
			}
		}
		return l;
	}

	/**
	 * Constructs the query to be submitted to Microsoft Academic Search.
	 */
	public static String constructQuery(String orgQuery) {
		return ExtrConstants.MS_QUERY_PREF + new String(orgQuery).replaceAll(" ", ExtrConstants.URL_ENC_SPACE);
	}

}
