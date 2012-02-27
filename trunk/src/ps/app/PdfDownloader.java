package ps.app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.util.StringUtil;
import org.htmlparser.util.ParserException;

import ps.extractors.GoogleScholarExtractor;
import ps.struct.PublicationInfo;
import ps.util.CrawlUtils;
import ps.util.StringUtils;

/**
 * Provides functionality for publication PDF download from Google Scholar.
 */
public class PdfDownloader {

	private final static String ALL = "\">All ";
	private final static String A_HREF = "<a href=\"";
	private final static String PDF_EXT = ".pdf";
	private final static String SCHOLAR_PREF = "http://scholar.google.com/";
	private final static int GS_RES_PER_PAGE = 10;

	/**
	 * Downloads and saves the PDF of the specified publication.
	 */
	public static String downloadPdfAndConvertToText(PublicationInfo p) {
		String author = "";
		if (p.getAuthors() != null && p.getAuthors().size() > 0) {
			author = p.getAuthors().get(0);
		}
		List<String> downloadUrls = extractDownloadUrl(p.getTitle(), author);
		for (String downloadUrl : downloadUrls) {
			try {
				String publicationText = Downloader.downloadPdfAndConvertToText(downloadUrl);
				if (StringUtils.hasValue(publicationText)) {
					return publicationText;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static List<String> extractDownloadUrl(String title, String author) {
		List<String> pdfDownloadList = new ArrayList<String>();
		try {
			String html = GoogleScholarExtractor.extractPublicationHtml(title, author);

			// extracts the URL of the publication PDF from the main results page
			String pdfUrl = extractPdfUrl(html);
			pdfDownloadList.add(pdfUrl);

			// extracts the "All results" page URL and collect all download URLs
			String allResultsUrl = extractAllResultsUrl(html);
			return extractsAllPdfUrls(allResultsUrl, pdfDownloadList);
		} catch (Exception e) {
			e.printStackTrace();
			return pdfDownloadList;
		}
	}

	/**
	 * Extracts the URL of the publication PDF from the publication info HTML section retrieved from the main results
	 * page of Google Scholar.
	 */
	private static String extractPdfUrl(String html) {
		String url = "";
		int to = html.indexOf(PDF_EXT);
		if (to > -1) {
			int from = html.lastIndexOf(A_HREF, to);
			if (from > -1) {
				from += A_HREF.length();
				url = html.substring(from, to) + PDF_EXT;
			}
		}
		return url;
	}

	/**
	 * Extracts the "All Results" URL of the publication PDF from the main results page of Google Scholar.
	 */
	private static String extractAllResultsUrl(String html) {
		String url = "";
		int to = html.indexOf(ALL);
		if (to > -1) {
			int from = html.lastIndexOf(A_HREF);
			if (from > -1) {
				from = from + A_HREF.length();
				url = SCHOLAR_PREF + html.substring(from, to);
			}
		}
		return url;
	}

	private static List<String> extractsAllPdfUrls(String url, List<String> pdfDownloadList) throws IOException,
			ParserException {
		String html = CrawlUtils.fetchHtmlCodeForUrl(url);
		boolean hasNextResultPage = true;
		int currPage = 1;
		while (hasNextResultPage) {
			boolean hasNextResult = true;
			while (hasNextResult) {
				int to = html.indexOf(PDF_EXT);
				if (to > -1) {
					int from = html.lastIndexOf(A_HREF, to);
					if (from > -1) {
						from += A_HREF.length();
						String pdfUrl = html.substring(from, to) + PDF_EXT;
						if (!pdfDownloadList.contains(pdfUrl)) {
							pdfDownloadList.add(pdfUrl);
						}
						html = html.substring(to + PDF_EXT.length());
					}
				} else {
					hasNextResult = false;
				}
			}
			String nextPageUrl = extractNextPageUrl(html, currPage);
			if (nextPageUrl != "") {
				html = CrawlUtils.fetchHtmlCodeForUrl(url);
				currPage++;
			} else {
				hasNextResultPage = false;
			}
		}
		return pdfDownloadList;
	}

	/**
	 * Extracts the URL corresponding to the "next page" in the search results list.
	 */
	private static String extractNextPageUrl(String html, int currPage) {
		String url = "";
		int startNum = currPage * GS_RES_PER_PAGE;
		String pattern = "/scholar?start=" + startNum;
		int from = html.indexOf(pattern);
		if (from > -1) {
			from = from + pattern.length();
			int to = html.indexOf("\"", from);
			if (to > -1) {
				url = SCHOLAR_PREF + html.substring(from, to);
			}
		}
		return url;
	}

}