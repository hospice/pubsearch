package ps.tmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.htmlparser.util.ParserException;

import ps.extractors.ArnetMinerExtractor;
import ps.extractors.GoogleScholarExtractor;
import ps.extractors.MicrosoftAcademicSearchExtractor;
import ps.struct.PublicationInfo;
import ps.util.CrawlUtils;
import ps.util.IOUtils;

public class Tester {

	public static final String HTML_ROOT = "C:/_tmp/new/";
	public static final int DELAY_IN_SECS = 5;

	public static void main(String[] args) throws Exception {
		// System.out.println("Now performing... processAllQueries():");
		processAllQueries();
	}

	/**
	 * PROCESS ALL 5 QUERIES HERE
	 * 
	 * @throws Exception
	 */
	private static void processAllQueries() throws Exception {
		List<String> qList = getQueryList();
		int id = 6000;
		int queryId = 1124;
		for (String query : qList) {
			List<PublicationInfo> mergedList = processForQuery(query);
			printPublicationInfoSql(id, queryId, mergedList);
			id++;
			queryId++;
		}
	}

	private static List<PublicationInfo> processForQuery(String query) throws Exception {
		// 1. ARNETMINER:
		String amHtml = fetchHtmlForQuery(query, SearchEngineEnum.ARNETMINER);
		List<PublicationInfo> amResults = ArnetMinerExtractor.extractPublicationResults2(amHtml);
		// 2. GOOGLE SCHOLAR:
		String gsHtml = fetchHtmlForQuery(query, SearchEngineEnum.GOOGLE_SCHOLAR);
		List<PublicationInfo> gsResults = GoogleScholarExtractor.extractPublicationResults2(gsHtml);
		// 3. MISCROSOFT ACADEMIC SEARCH:
		String msHtml = fetchHtmlForQuery(query, SearchEngineEnum.MISCROSOFT_ACADEMIC_SEARCH);
		List<PublicationInfo> msResults = MicrosoftAcademicSearchExtractor.extractPublicationResults2(msHtml);
		List<PublicationInfo> mergedList = mergeList(amResults, gsResults, msResults);
		return mergedList;
	}

	/**
	 * THE PROCESS FOR EACH QUERY
	 * 
	 * @throws Exception
	 */
	// private static List<PublicationInfo> processForQuery(String query) throws Exception {
	// // 1. ARNETMINER:
	// String amHtml = fetchHtmlForQuery(query, SearchEngineEnum.ARNETMINER);
	// List<PublicationInfo> amResults = ArnetMinerExtractor.extractPublicationResults2(amHtml);
	// // printPublicationInfo(amResults);
	// // System.out.println();
	// // System.out.println("*******************************");
	// // System.out.println();
	//
	// // 2. GOOGLE SCHOLAR:
	// String gsHtml = fetchHtmlForQuery(query, SearchEngineEnum.GOOGLE_SCHOLAR);
	// List<PublicationInfo> gsResults = GoogleScholarExtractor.extractPublicationResults2(gsHtml);
	// // printPublicationInfo(gsResults);
	// // System.out.println();
	// // System.out.println("*******************************");
	// // System.out.println();
	//
	// // 3. MISCROSOFT ACADEMIC SEARCH:
	// String msHtml = fetchHtmlForQuery(query, SearchEngineEnum.MISCROSOFT_ACADEMIC_SEARCH);
	// List<PublicationInfo> msResults = MicrosoftAcademicSearchExtractor.extractPublicationResults2(msHtml);
	// // printPublicationInfo(msResults);
	// // System.out.println();
	// // System.out.println("*******************************");
	// // System.out.println();
	//
	// List<PublicationInfo> mergedList = mergeList(amResults, gsResults, msResults);
	//		
	// // printPublicationInfo(mergedList);
	// // System.out.println();
	// return mergedList;
	// }

	private static void writeHtmlToFileForAllQueries() throws ParserException, InterruptedException {
		List<String> qList = getQueryList();
		for (String query : qList) {
			writeHtmlToFile(query);
		}
	}

	private static void writeHtmlToFile(String query) throws ParserException, InterruptedException {
		String fileName = query.replaceAll("\"", "");
		for (SearchEngineEnum engine : SearchEngineEnum.values()) {
			String pathname = HTML_ROOT + engine.getShortName() + "/" + fileName;
			String url = searchQueryUrlForEngine(engine, query);
			String html = CrawlUtils.fetchHtmlCodeForUrlWithProxy(url);
			IOUtils.writeToFile(html, pathname);
			Thread.sleep(DELAY_IN_SECS * 1000);
		}
	}

	private static String searchQueryUrlForEngine(SearchEngineEnum engine, String query) {
		String url = "";
		if (engine.equals(SearchEngineEnum.GOOGLE_SCHOLAR)) {
			url = GoogleScholarExtractor.constructQuery(query);
		}
		if (engine.equals(SearchEngineEnum.MISCROSOFT_ACADEMIC_SEARCH)) {
			url = MicrosoftAcademicSearchExtractor.constructQuery(query);
		}
		if (engine.equals(SearchEngineEnum.ARNETMINER)) {
			url = ArnetMinerExtractor.constructQuery(query);
		}
		return url;
	}

	/**
	 * FETCHES HTML FROM FILE SYSTEM
	 * 
	 * @throws IOException
	 */
	private static String fetchHtmlForQuery(String query, SearchEngineEnum engine) throws IOException {
		return IOUtils.readFileFromPath(HTML_ROOT + engine.getShortName() + "/" + query.replaceAll("\"", ""));
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

	// private static void printPublicationInfo(List<PublicationInfo> publicationInfo) {
	// for (PublicationInfo p : publicationInfo) {
	// printPublicationInfo(p);
	// }
	// }

	private static void printPublicationInfoSql(int id, int queryId, List<PublicationInfo> publicationInfo) {
		int rank = 1;
		for (PublicationInfo p : publicationInfo) {
			String stm = createInsertStatement(id, queryId, p, rank);
			System.out.println(stm);
			id++;
			rank++;
		}
	}

	/**
	 * Prints all publication information details
	 */
	private static void printPublicationInfo(PublicationInfo publicationInfo) {
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("TITLE: " + publicationInfo.getTitle());
		System.out.println("URL: " + publicationInfo.getUrl());
		System.out.println("TITLE: " + publicationInfo.getTitle());
		System.out.println("AUTHORS: ");
		for (String author : publicationInfo.getAuthors()) {
			System.out.println(" -> " + author);
		}
		System.out.println("NUMBER OF CITATIONS: " + publicationInfo.getNumOfCitations());
	}

	private static String createInsertStatement(int id, int queryId, PublicationInfo pi, int rank) {
		String url = pi.getUrl();
		String title = pi.getTitle();
		Integer cit = pi.getNumOfCitations();
		List<String> authList = pi.getAuthors();
		String auths = "";
		int i = 0;
		int limit = 3;
		if (authList != null && authList.size() > 0) {
			for (String a : authList) {
				if (i < limit) {
					auths += a;
					if (i < limit - 2) {
						auths += ",";
					}
					i++;
				}

			}
		}
		Integer year = pi.getYearOfPublication();
		String stmt = "insert into query_results values(" + id + ", " + queryId + ", '" + url + "', '" + title
				+ "', '', " + rank + ", NULL, " + cit + ", '" + auths + "', " + year + ", 1, '');";
		return stmt;
	}

	private static List<PublicationInfo> mergeList(List<PublicationInfo> l1, List<PublicationInfo> l2,
			List<PublicationInfo> l3) {
		List<PublicationInfo> mergedList = new ArrayList<PublicationInfo>();
		addListToMerged(l1, mergedList);
		addListToMerged(l2, mergedList);
		addListToMerged(l3, mergedList);
		return mergedList;
	}

	private static List<PublicationInfo> addListToMerged(List<PublicationInfo> l, List<PublicationInfo> mergedList) {
		for (PublicationInfo p : l) {
			boolean contains = false;
			String title = p.getTitle();
			for (PublicationInfo pm : mergedList) {
				if (title.equalsIgnoreCase(pm.getTitle())) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				mergedList.add(p);
			}
		}
		return mergedList;
	}

}
