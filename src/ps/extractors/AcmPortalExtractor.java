package ps.extractors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.htmlparser.util.ParserException;

import ps.constants.GeneralConstants;
import ps.struct.PublicationInfo;
import ps.util.CrawlUtils;
import ps.util.PrintUtils;
import ps.util.PropertyUtils;
import ps.util.StringUtils;

public class AcmPortalExtractor {
	
	public static void main(String[] args) throws ParserException, MalformedURLException, IOException {
		String query = "web information retrieval";
		List<PublicationInfo> l = extractPublicationResults(query);
		PrintUtils.printPublicationInfo(l);
	}

	public static List<PublicationInfo> extractPublicationResults(String query) throws ParserException, MalformedURLException,
			IOException {
		
		List<PublicationInfo> l = new ArrayList<PublicationInfo>();
		
		String queryUrl = constructSearchUrl(query);
		
		String orgHtml = "";
		
		if(PropertyUtils.useProxy()){
			orgHtml = CrawlUtils.fetchHtmlCodeForUrlWithProxy(constructSearchUrl(query));	
		}else{
			orgHtml = CrawlUtils.fetchHtmlCodeForUrl(queryUrl);
		}
		
		if (orgHtml.contains("was not found.")) {
			return l;
		}
		
		String html = new String(orgHtml);
		int cursorFrom = 0;
		int count = 0;
		
		while (count < 10) {
			
			// 1. extracts the publication's URL:
			cursorFrom = html.indexOf(GeneralConstants.ACM_RESULT_START, cursorFrom) + GeneralConstants.ACM_RESULT_START.length();
			int cursorTo = html.indexOf("\"", cursorFrom);
			String urlPart = html.substring(cursorFrom, cursorTo);
			String pubUrl = GeneralConstants.ACM_PUB_URL_PREFIX + urlPart;
			
			// 2. extracts the publication's title:
			cursorFrom = html.indexOf(GeneralConstants.SELF, cursorTo) + GeneralConstants.SELF.length();
			cursorTo = html.indexOf("</A>", cursorFrom);
			String pubTitle = html.substring(cursorFrom, cursorTo);
			cursorFrom = cursorTo;
			
			// 3. extracts the authors:
			String authSectionStart = "<div class=\"authors\">";
			cursorFrom = html.indexOf(authSectionStart);
			cursorTo = html.indexOf("</div>", cursorFrom);
			String authSection = html.substring(cursorFrom, cursorTo);
			List<String> auths = new ArrayList<String>();
			int ptr = authSection.indexOf("author_page.cfm?");
			while(ptr > -1){
				ptr = authSection.indexOf(">", ptr);
				int ptr2 = authSection.indexOf("</a>", ptr);
				String auth = authSection.substring(ptr, ptr2);
				if(StringUtils.hasValue(auth)){
					auths.add(auth.trim());
				}
				ptr = authSection.indexOf("author_page.cfm?", ptr2);
			}
			cursorFrom = cursorTo;
			
			// 4. extracts the year of publish:
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
		
			PublicationInfo p = new PublicationInfo();
			p.setAuthors(auths);
			p.setTitle(pubTitle);
			p.setUrl(pubUrl);
			p.setYearOfPublication(yearOfPublication);
			l.add(p);
			
			count++;
			
			html = html.substring(cursorTo);
			
			// checks is other results exist, else exits
			if (html.indexOf(GeneralConstants.ACM_RESULT_START, cursorFrom) == -1) {
				break;
			}
		}
		return l;
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

}
