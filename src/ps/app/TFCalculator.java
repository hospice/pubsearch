package ps.app;

import java.util.ArrayList;
import java.util.List;

import ps.struct.PublicationData;
import ps.struct.TermFrequencyScore;
import ps.util.StringUtils;

/**
 * Provides all Term Frequency calculation functionality.
 */
public class TFCalculator {

	private final static double TITLE_WEIGHT = 100d;
	private final static double ABSTRACT_WEIGHT = 10d;
	private final static double BODY_WEIGHT = 1d;
	private final static double PARAGRAPH_LEVEL_BONUS = 5d;
		
	/**
	 * Calculates all different TF scores for the specific publication.
	 */
	public static TermFrequencyScore calcTfForPublication(PublicationData pd) {
		String[] queryTokens = pd.getQueryTokens();
		double titleTf = calcScoreForSentence(pd.getTitle(), queryTokens, TITLE_WEIGHT);
		double abstractTf = calcScoreForSection(pd.getAbstractText(), queryTokens, ABSTRACT_WEIGHT, PARAGRAPH_LEVEL_BONUS);
		double bodyTf = calcScoreForSection(pd.getBody(), queryTokens, BODY_WEIGHT, PARAGRAPH_LEVEL_BONUS);
		return new TermFrequencyScore(titleTf, abstractTf, bodyTf);
	}

	/**
	 * Calculates the TF score for the specified section.
	 */
	private static double calcScoreForSection(String text, String[] queryTokens, double sectionWeight,
			double paragraphLevelBonusWeight) {
		double score = 0d;
		String[] paragraphs = StringUtils.splitSectionIntoParagraphs(text);
		for (String paragraph : paragraphs) {
			List<String> tokensFoundInParagraph = new ArrayList<String>();
			boolean perfectSentenceFound = false;
			String[] sentences = StringUtils.splitParagraphIntoSentences(paragraph);
			for (String sentence : sentences) {
				List<String> tokensFoundInCurrSentence = findQueryTokensInSentence(sentence, queryTokens);
				updateTokensFound(tokensFoundInParagraph, tokensFoundInCurrSentence);
				double currSentenceCoverage = (double) tokensFoundInCurrSentence.size() / (double) queryTokens.length;
				if (perfectSentenceFound == false && currSentenceCoverage == 1.0) {
					perfectSentenceFound = true;
				}
				score += currSentenceCoverage * sectionWeight;
			}
			double paragraphLevelBonus = paragraphLevelBonusWeight * tokensFoundInParagraph.size()
					/ (double) queryTokens.length;
			score += paragraphLevelBonus;
		}
		return score;
	}

	/**
	 * Calculates the TF score for the specified sentence.
	 */
	private static double calcScoreForSentence(String sentence, String[] queryTokens, double weight) {
		List<String> tokensFound = findQueryTokensInSentence(sentence, queryTokens);
		double coverage = (double) tokensFound.size() / (double) queryTokens.length;
		return coverage * weight;
	}

	/**
	 * Updates the specified list of tokens found in paragraph with the tokens found in sentence.
	 */
	private static void updateTokensFound(List<String> tokensFoundInParagraph, List<String> tokensFoundInSentence) {
		for (String token : tokensFoundInSentence) {
			if (!tokensFoundInParagraph.contains(token)) {
				tokensFoundInParagraph.add(token);
			}
		}
	}

	/**
	 * Returns a list of the query tokens found in the sentence
	 */
	private static List<String> findQueryTokensInSentence(String sentence, String[] queryTokens) {
		List<String> l = new ArrayList<String>();
		String[] sentenceTokens = StringUtils.splitStringIntoTokens(sentence);
		for (int a = 0; a < sentenceTokens.length; a++) {
			for (int b = 0; b < queryTokens.length; b++) {
				String sentenceToken = sentenceTokens[a].trim();
				String queryToken = queryTokens[b];
				if (sentenceToken.equals(queryToken)) {
					l.add(queryToken);
				}
			}
		}
		return l;
	}
	
	
	
	

//   *****************************************************************************************************************
//	/**
//	 * Performs the TF Calculation process for manually downloaded publications.
//	 */
//	public static void tfCalcManual(int queryId) throws Exception {
//
//		String query = PersistenceController2.fetchQueryForId(queryId);
//		Map<String, Integer> m = readResultsMap(queryId);
//
//		String dirName = query.replaceAll("\"", "");
//		dirName = dirName.replaceAll("&#955;", "ë");
//		String dir = ROOT + dirName + "/";
//		List<String> filenames = readFilesInDir(dir);
//
//		boolean considerQuotes = false;
//		List<String> queryTokens = StringUtils.queryTokens(query, considerQuotes);
//
//		// check for special chars here:
//		for (String q : queryTokens) {
//			q = q.replaceAll("&#955;", "ë");
//		}
//
//		// i. processed all results for which publication has been downloaded
//		String fullpath = "";
//		List<Integer> pubsDownloaded = new ArrayList<Integer>(); // IDs of publication found locally
//		for (String filename : filenames) {
//			fullpath = dir + filename;
//			String title = filename.substring(0, filename.indexOf(".pdf"));
//			title = title.replaceAll("_", "/"); // put here all cases of special characters
//			title = title.replaceAll("002F;", "&#x002F;");
//			Integer resId = m.get(title);
//			if (resId == null) {
//				throw new Exception("NO ID found for result with title: " + filename);
//			}
//			try {
//				String txtPathname = PdfUtil.convertAndSavePdfToTxt(fullpath);
//				Map<String, Integer> tfMap = TermFrequency.findAllTermFrequencies(query, txtPathname, considerQuotes,
//						TITLE_WEIGHT, ABSTRACT_WEIGHT, title);
//				Iterator<String> it = tfMap.keySet().iterator();
//				while (it.hasNext()) {
//					String token = it.next();
//					Integer freq = tfMap.get(token);
//					System.out.println(token + " appears " + freq + " times.");
//				}
//				int totalTokens = (int) StringUtils.totalTokens(txtPathname);
//				PersistenceController.saveResultTf(queryId, tfMap, resId, totalTokens);
//				pubsDownloaded.add(resId);
//			} catch (Exception e) {
//				System.out.println();
//				System.err.println("Unable to process result :  " + title);
//			}
//		}
//		// ii. persists all results for which publication has not been downloaded
//		List<Integer> pubsNotDownloaded = findPubsNotDownloaded(m, pubsDownloaded);
//		PersistenceController2.saveEmptyTFResult(queryId, pubsNotDownloaded, queryTokens);
//	}
//
//	public static void tfCalcManualOneRes(int queryId, int resId, String filename, boolean saveResult, boolean parsePdf)
//			throws Exception {
//		String query = PersistenceController2.fetchQueryForId(queryId);
//
//		String dirName = query.replaceAll("\"", "");
//		String dir = ROOT + dirName + "/";
//
//		boolean considerQuotes = false;
//
//		// i. processed all results for which publication has been downloaded
//		String fullpath = "";
//
//		fullpath = "";
//		String title = "";
//		String txtPathname = "";
//		if (parsePdf) {
//			title = filename.substring(0, filename.indexOf(".pdf"));
//			fullpath = dir + filename;
//			txtPathname = PdfUtil.convertAndSavePdfToTxt(fullpath);
//		} else {
//			title = filename.substring(0, filename.indexOf(".txt"));
//			txtPathname = dir + filename;
//		}
//
//		Map<String, Integer> tfMap = TermFrequency.findAllTermFrequencies(query, txtPathname, considerQuotes,
//				TITLE_WEIGHT, ABSTRACT_WEIGHT, title);
//		Iterator<String> it = tfMap.keySet().iterator();
//		while (it.hasNext()) {
//			String token = it.next();
//			Integer freq = tfMap.get(token);
//			System.out.println(token + " appears " + freq + " times.");
//		}
//		int totalTokens = (int) StringUtils.totalTokens(txtPathname);
//		System.out.println("Total tokens = " + totalTokens);
//		if (saveResult) {
//			PersistenceController.saveResultTf(queryId, tfMap, resId, totalTokens);
//		}
//	}
//	/**
//	 * Performs the TF Calculation process for automatically downloaded publication:
//	 */
//	public static void tfCalcAutoFindByTitle(String pubTitle, String query, int queryId, int queryResultId,
//			boolean considerQuotes) throws Exception {
//		// Searches for the given publication in Google Scholar & identifies a download-able copy of it
//		String url = locateDownloadableCopy(pubTitle);
//		String pdfFileLoc = Downloader.getDefaultPdfOutPath(pubTitle);
//		// Downloads publication
//		Downloader.download(url, pdfFileLoc);
//		// Converts publication to TXT format
//		String pathname = PdfUtil.convertAndSavePdfToTxt(pdfFileLoc);
//		// Applies TF(IDF?) calculation for the given query tokens
//		Map<String, Integer> tfMap = TermFrequency.findAllTermFrequencies(query, pathname, considerQuotes,
//				TITLE_WEIGHT, ABSTRACT_WEIGHT, pubTitle);
//		Iterator<String> it = tfMap.keySet().iterator();
//		while (it.hasNext()) {
//			String token = it.next();
//			Integer freq = tfMap.get(token);
//			System.out.println(token + " appears " + freq + " times.");
//		}
//		// Persists results
//		int totalTokens = (int) StringUtils.totalTokens(pathname);
//		PersistenceController.saveResultTf(queryId, tfMap, queryResultId, totalTokens);
//	}
//	/**
//	 * Performs the TF Calculation process for automatically downloaded publication
//	 */
//	public static void tfCalcAutoFindByUrl(String url, String pubTitle, String query, int queryId, int queryResultId,
//			boolean considerQuotes) throws Exception {
//		String pdfFileLoc = Downloader.getDefaultPdfOutPath(pubTitle);
//		// Downloads publication
//		Downloader.download(url, pdfFileLoc);
//		// Converts publication to TXT format
//		String pathname = PdfUtil.convertAndSavePdfToTxt(pdfFileLoc);
//		// Applies TF(IDF?) calculation for the given query tokens
//		Map<String, Integer> tfMap = TermFrequency.findAllTermFrequencies(query, pathname, considerQuotes,
//				TITLE_WEIGHT, ABSTRACT_WEIGHT, pubTitle);
//		Iterator<String> it = tfMap.keySet().iterator();
//		while (it.hasNext()) {
//			String token = it.next();
//			Integer freq = tfMap.get(token);
//			System.out.println(token + " appears " + freq + " times.");
//		}
//		// Persists results
//		int totalTokens = (int) StringUtils.totalTokens(pathname);
//		PersistenceController.saveResultTf(queryId, tfMap, queryResultId, totalTokens);
//	}
//	/**
//	 * Reads and returns a map containing the title and the result id pairs for all default results for the specified
//	 * query id
//	 */
//	private static Map<String, Integer> readResultsMap(int queryId) throws Exception {
//		List<Integer> idList = PersistenceController.fetchQueryResultsDefault(queryId, 10);
//		Map<String, Integer> m = new HashMap<String, Integer>();
//		for (Integer id : idList) {
//			String title = PersistenceController2.getDefaultResultTitleForId(id);
//			if (title == null || "".equals(title)) {
//				throw new Exception("Empty title found!");
//			}
//			m.put(title, id);
//		}
//		return m;
//	}
//	 /**
//	 * Prints the results map
//	 */
//	 private static void printResultsMap(Map<String, Integer> m) {
//	 Iterator<String> it = m.keySet().iterator();
//	 while (it.hasNext()) {
//	 String title = it.next();
//	 Integer id = m.get(title);
//	 System.out.println("Title: " + title + " , ID: " + id);
//	 }
//	 }
//
//	/**
//	 * Reads all files in the specified directory
//	 */
//	private static List<String> readFilesInDir(String path) throws Exception {
//		List<String> filenames = new ArrayList<String>();
//		// filter list to contain only PDF files
//		FilenameFilter filter = new FilenameFilter() {
//			public boolean accept(File dir, String name) {
//				return name.endsWith(".pdf");
//			}
//		};
//		File dir = new File(path);
//		String[] children = dir.list(filter);
//
//		if (children == null) {
//			throw new Exception("Either dir does not exist or is not a directory!");
//		} else {
//			for (int i = 0; i < children.length; i++) {
//				filenames.add(children[i]);
//			}
//		}
//		return filenames;
//	}
//
//	/**
//	 * Finds which publications in the map have not been downloaded
//	 */
//	private static List<Integer> findPubsNotDownloaded(Map<String, Integer> m, List<Integer> pubsDownloadedIdList) {
//		List<Integer> pubsNotFound = new ArrayList<Integer>();
//		Iterator<String> it = m.keySet().iterator();
//		while (it.hasNext()) {
//			Integer pubId = m.get(it.next());
//			if (!pubsDownloadedIdList.contains(pubId)) {
//				pubsNotFound.add(pubId);
//			}
//		}
//		return pubsNotFound;
//	}
//
//	/**
//	 * Identifies a downloadable copy of a publication (temporary implementation returns first PDF link...)
//	 */
//	private static String locateDownloadableCopy(String pubTitle) throws ParserException, MalformedURLException,
//			IOException {
//		String googleScholarQuery = GoogleScholarUtils.constructSearchQuery(pubTitle.replaceAll("-", " "));
//		String tmpHtml = new String(CrawlUtils.fetchHtmlCodeForUrl(googleScholarQuery));
//		int from, to = 0;
//		String ahref = "<a href=\"";
//		String tmpUrl = "";
//		while (tmpHtml.contains(ahref)) {
//			from = tmpHtml.indexOf(ahref) + ahref.length();
//			to = tmpHtml.indexOf("\"", from);
//			tmpUrl = tmpHtml.substring(from, to).toLowerCase();
//			if (tmpUrl.endsWith("pdf") && !tmpUrl.contains("springer") && !tmpUrl.contains("elsevier")
//					&& !tmpUrl.contains("acm")) {
//				tmpUrl = tmpUrl.replaceAll("&amp;", "&"); // replaces special character
//				return tmpUrl;
//			}
//			tmpHtml = tmpHtml.substring(to + 1);
//		}
//		return null;
//	}

}