package ps.extractors;

import java.util.ArrayList;
import java.util.List;

import ps.struct.PublicationInfo;
import ps.util.IOUtils;
import ps.util.PrintUtils;

public class ArnetMinerExtractor {

	public static void main(String[] args) throws Exception {
		String pathname = "";
		List<String> qList = getQueryList();
		for (String q : qList) {
			pathname = "C:\\_tmp\\new\\am\\" + q.replaceAll("\"", "");
			String html = IOUtils.readFileFromPath(pathname);
			List<PublicationInfo> pList = extractPublicationResults2(html);
			PrintUtils.printPublicationInfo(pList);
		}
	}
	
	private static List<String> getQueryList() {
		List<String> qList = new ArrayList<String>();
		qList.add("\"page rank\" clustering");
		qList.add("\"social network\" \"information retrieval\"");
		qList.add("\"unsupervised learning\"");
		qList.add("clustering \"information retrieval\"");
		qList.add("\"web mining\"");
		return qList;
	}
	
	public static List<PublicationInfo> extractPublicationResults2(String html) throws Exception {
		List<PublicationInfo> results = new ArrayList<PublicationInfo>();
		boolean hasMoreRes = true;
		while (hasMoreRes) {
			int from = html.indexOf(ExtrConstants.ARN_RES_ITEM);
			if (from > -1) {
				int to = html.indexOf(ExtrConstants.CLOSE_DIV, from);
				if (to > -1) {
					String resSection = html.substring(from, to);
					html = html.substring(to);
					results.add(fillPublicationInfo(resSection));
				} else {
					hasMoreRes = false;
					break;
				}
			} else {
				hasMoreRes = false;
				break;
			}
		}
		return results;
	}

	public static List<PublicationInfo> extractPublicationResults(String query) throws Exception {
		List<PublicationInfo> results = new ArrayList<PublicationInfo>();

		// ******* FIXME ***********
		// String html = CrawlUtils.fetchHtmlCodeForUrl(constructQuery(query));
		String pathname = "C:/arnet.html";
		String html = IOUtils.readFileFromPath(pathname);
		// **************************
		boolean hasMoreRes = true;
		while (hasMoreRes) {
			int from = html.indexOf(ExtrConstants.ARN_RES_ITEM);
			if (from > -1) {
				int to = html.indexOf(ExtrConstants.CLOSE_DIV, from);
				if (to > -1) {
					String resSection = html.substring(from, to);
					html = html.substring(to);
					results.add(fillPublicationInfo(resSection));
				} else {
					hasMoreRes = false;
					break;
				}
			} else {
				hasMoreRes = false;
				break;
			}
		}
		return results;
	}

	private static PublicationInfo fillPublicationInfo(String resSection) {
		String html = new String(resSection);
		PublicationInfo p = null;
		String urlPref = ExtrConstants.ARN_AUTH_URL_PREF;
		String urlStart = ExtrConstants.A_HREF_BEG + urlPref;
		int from = html.indexOf(urlStart);
		if (from > -1) {
			// URL
			String url = "";
			from += urlStart.length();
			int to = html.indexOf("\"", from);
			if(to > -1){
				url = urlPref + html.substring(from, to);
			}
			// TITLE
			String title = "";
			from = html.indexOf(">", to);
			if (from > -1) {
				from += 1;
				to = html.indexOf(ExtrConstants.A_HREF_END, from);
				if (to > -1) {
					title = html.substring(from, to);
				}
			}
			// AUTHORS
			String authorsSectionBeg = ExtrConstants.ARN_AUTH_SECT_START;
			List<String> authors = new ArrayList<String>();
			from = html.indexOf(authorsSectionBeg);
			if(from > -1){
				from += authorsSectionBeg.length();
				to = html.indexOf(ExtrConstants.ARN_PUBLISH_YEAR);
				if(to > -1){
					to += ExtrConstants.ARN_PUBLISH_YEAR.length();
					String authorsHtml = html.substring(from, to);
					boolean hasMoreAuths = true;
					while (hasMoreAuths) {
						int authFrom = authorsHtml.indexOf(ExtrConstants.ARN_AUTH_START);
						if (authFrom > -1) {
							authFrom = authFrom + ExtrConstants.A_HREF_BEG.length();
							authFrom = authorsHtml.indexOf("\">") + 2;
							int authTo = authorsHtml.indexOf(ExtrConstants.A_HREF_END, authFrom);
							String authorName = authorsHtml.substring(authFrom, authTo);
							authors.add(authorName);
							authorsHtml = authorsHtml.substring(authTo);
						} else {
							hasMoreAuths = false;
						}
					}
				}
			}
			// YEAR
			from = to;
			to = html.indexOf(ExtrConstants.MS_RES_END, from);
			String s = html.substring(from, to);
			Integer year = Integer.parseInt(s.trim());
			p = new PublicationInfo(title, url, authors, year);
		}
		return p;
	}

	/**
	 * Constructs the query to be submitted to GoogleScholar.
	 */
	public static String constructQuery(String orgQuery) {
		return ExtrConstants.ARN_QUERY_PREF + orgQuery.replaceAll(" ", "+");
	}
}
