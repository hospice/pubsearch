package ps.util;

import java.util.ArrayList;
import java.util.List;

import ps.constants.NameConstants;
import ps.constants.WeightConstants;
import ps.stem.snowball.StemUtil;
import ps.struct.PublicationData;

/**
 * Provides Term Frequency (TF) related utilities.
 */
public class TermFrequencyUtils {

	/**
	 * Calculates the TF score for the entire publication.
	 */
	public static double calcTfForPublicationData(PublicationData pd) throws Exception {
		// stems the 
		if (PropertyUtils.useStemming()) {
			String query = StemUtil.getEnglishStem(pd.getTitle());
			String pubTitle = StemUtil.getEnglishStem(pd.getAbstractText());
			String publicationText = StemUtil.getEnglishStem(pd.getBody());
			List<String> queryTokens = new ArrayList<String>();
			String[] l = pd.getQueryTokens();
			for (int i = 0; i < l.length; i++) {
				queryTokens.add(StemUtil.getEnglishStem(l[i]));
			}
			pd = new PublicationData(query, pubTitle, publicationText);
			String[] queryTokensArr = new String[queryTokens.size()];
			for (int i = 0; i < queryTokens.size(); i++) {
				queryTokensArr[i] = queryTokens.get(i);
			}
			pd.setQueryTokens(queryTokensArr);
		}
		
		// 1. title
		double titleScore = tfForTitle(pd);
		System.out.println("TF for title = " + titleScore);

		// 2. abstract
		double abstractScore = tfForAbstract(pd);
		if(abstractScore > 0){
			int numOfTokensInAbstract = StringUtils.numOfTokens(pd.getAbstractText());
			abstractScore = abstractScore / (double) numOfTokensInAbstract;	
		}
		System.out.println("TF for abstract = " + abstractScore);

		// 3. body
		double bodyScore = tfForBody(pd);
		int numOfTokensInBody = StringUtils.numOfTokens(pd.getBody());
		if(numOfTokensInBody > 0){
			bodyScore = bodyScore / ((double) numOfTokensInBody / 100);
		}
		System.out.println("TF for body = " + bodyScore);

		return titleScore + abstractScore + bodyScore;
	}

	/**
	 * Calculates the TF score for the title.
	 */
	private static double tfForTitle(PublicationData pd) throws Exception {
		// NOTE: The calculation of the TF for the title is somehow straightforward since we expect that the title
		// contains max. one occurrence of each of the query terms. So we normalize the term occurrence score with
		// the number of terms in the query and multiply by the weight for the title score.
		double coverage = coverageForSentence(pd.getQueryTokens(), pd.getTitle(), pd.getAcronymArr());
		return coverage * WeightConstants.TITLE_WEIGHT;
	}

	/**
	 * Calculates the TF score for the abstract.
	 */
	private static double tfForAbstract(PublicationData pd) throws Exception {
		// NOTE: The abstract is treated as a single paragraph.
		String paragraph = new String(pd.getAbstractText().replaceAll(NameConstants.PARAGRAPH_END, ""));
		double coverage = coverageForParagraph(pd.getQueryTokens(), paragraph, pd.getAcronymArr());
		return coverage * WeightConstants.ABSTRACT_WEIGHT;
	}

	/**
	 * Calculates the TF score for the body.
	 */
	private static double tfForBody(PublicationData pd) throws Exception {
		String[] queryTokens = pd.getQueryTokens();
		String[] acronymArr = pd.getAcronymArr();
		double score = 0.0;
		String[] paragraphs = StringUtils.splitSectionIntoParagraphs(pd.getBody());
		for (String paragraph : paragraphs) {
			double coverage = coverageForParagraph(queryTokens, paragraph, acronymArr);
			score += coverage * WeightConstants.BODY_WEIGHT;
		}
		return score;
	}

	/**
	 * Extracts the publication's abstract, body and index terms from the full-publication-text. In the returned array,
	 * the abstract is placed in the first position, and the body in the second.
	 */
	public static final String[] extractAbstractBodyIndexTerms(String publicationText) throws Exception {
		if (!StringUtils.hasValue(publicationText)) {
			throw new Exception("No text found...");
		}
		String pText = new String(publicationText).toLowerCase();

		// 1a. Extracts the publication's abstract:
		String abstr = "";
		int from = pText.indexOf(NameConstants.ABSTRACT);
		if (from > -1) {
			from += NameConstants.ABSTRACT.length();
			int to = pText.indexOf(NameConstants.INTRODUCTION);
			if (to > -1) {
				abstr = pText.substring(from, to);
				from = to;
			}
		}else{
			from = 0;
		}
		// 1b. Looks for keywords in case they exist, and adjusts abstract text accordingly:
		int keywordIdx = abstr.indexOf(NameConstants.KEYWORDS);
		String keywords = "";
		if (keywordIdx > -1) {
			keywords = abstr.substring(keywordIdx + NameConstants.KEYWORDS.length(), abstr.length());
			abstr = abstr.substring(0, keywordIdx);
		}

		// 2. Extracts the publication's body:
		int to = pText.indexOf(NameConstants.REFERENCES);
		if (to == -1) {
			to = pText.length();
		}
		String body = pText.substring(from, to);
		return new String[] { abstr, body, keywords };
	}

	/**
	 * Calculates the percentage coverage of the specified query tokens in the specific sentence.
	 * 
	 * Extra logic (for acronyms):
	 * 
	 * 1) In the case that the acronym is present in the sentence, the term frequency score counted should be equal to
	 * the number of acronym tokens of the description.
	 * 
	 * 2) If the acronym and the description are present (case where the acronym is defined within the sentence) then
	 * only one of the two (acronym or description) should be taken into consideration, ignoring the other. The logic
	 * described in point (i) applies here as well.
	 */
	private static double coverageForSentence(String[] queryTokens, String sentence, String acronymArr[])
			throws Exception {
		int queryTermsFound = 0;
		String[] sentenceTokens = StringUtils.splitStringIntoTokens(sentence);
		int acronymOccur = calcNumOfOccurrences(acronymArr[0], sentence);
		for (int a = 0; a < sentenceTokens.length; a++) {
			for (int b = 0; b < queryTokens.length; b++) {
				String sentenceToken = sentenceTokens[a].trim();
				String queryToken = queryTokens[b];
				if (sentenceToken.equals(queryToken)) {
					if (acronymOccur == 0
							|| (acronymOccur > 0 && !acronymArr[0].toLowerCase().equals(sentenceToken) && !StringUtils
									.containsTerm(acronymArr[1].toLowerCase(), sentenceToken))) {
						queryTermsFound++;
					}
				}
			}
		}
		queryTermsFound = queryTermsFound + (acronymOccur * StringUtils.splitStringIntoTokens(acronymArr[1]).length);
		return (double) queryTermsFound / (double) queryTokens.length;
	}

	/**
	 * This method calculates the coverage score at paragraph level. This method works as follows: First it calculates
	 * the coverage score at sentence level. During this process it also keeps track if a perfect sentence has been
	 * encountered. If this is the case then the paragraph is considered as perfect as well. Alternatively, the method
	 * calculates the number of query tokens that have been encountered in the paragraph and respectively calculates the
	 * paragraph weight.
	 */
	private static double coverageForParagraph(String[] queryTokens, String paragraph, String acronymArr[])
			throws Exception {
		// 1. sentence-level proximity matching:
		double sentenceLevelScore = 0.0;
		boolean perfectSentenceFound = false;
		List<String> queryTokensFound = new ArrayList<String>();
		String[] sentences = StringUtils.splitParagraphIntoSentences(paragraph);
		for (String sentence : sentences) {
			double sentenceCoverage = coverageForSentence(queryTokens, sentence, acronymArr);
			perfectSentenceFound = sentenceCoverage == 1.0;
			if (!perfectSentenceFound) {
				fillQueryTokensFound(queryTokens, queryTokensFound, sentence);
				sentenceLevelScore += sentenceCoverage * WeightConstants.SENTENCE_WEIGHT;
			} else {
				// all perfect sentences gain an extra "perfect sentence" bonus
				sentenceLevelScore += WeightConstants.PERFECT_SENTENCE;
			}
		}
		// 2. paragraph-level proximity matching:
		double paragraphLevelCoverage = 0.0;
		if (perfectSentenceFound) {
			paragraphLevelCoverage = 1.0;
		} else {
			paragraphLevelCoverage = (double) queryTokensFound.size() / (double) queryTokens.length;
		}
		double paragraphLevelScore = WeightConstants.PARAGRAPH_WEIGHT * paragraphLevelCoverage;
		return sentenceLevelScore + paragraphLevelScore;
	}

	/**
	 * Fills the query tokens found list based on the encountered query tokens in the specified sentence.
	 */
	private static void fillQueryTokensFound(String[] queryTokens, List<String> queryTokensFound, String sentence) {
		String[] sentenceTokens = StringUtils.splitStringIntoTokens(sentence);
		for (String sentenceToken : sentenceTokens) {
			for (String queryToken : queryTokens) {
				if (sentenceToken.equals(queryToken) && !queryTokensFound.contains(sentenceToken)) {
					queryTokensFound.add(sentenceToken);
				}
			}
		}
	}

	/**
	 * Calculates the number of occurrences of the specified term in the specified sentence.
	 */
	private static int calcNumOfOccurrences(String term, String sentence) {
		int numOfOccur = 0;
		int ptr = 0;
		if (StringUtils.hasValue(term)) {
			do {
				ptr = sentence.indexOf(term, ptr);
				if (ptr > -1) {
					numOfOccur++;
					ptr++;
				}
			} while (ptr > -1);
		}
		return numOfOccur;
	}

	/**
	 * Splits the specified comma-separated keywords sentence into keyword tokens.
	 */
	public static String[] tokenizeKeywords(String sentence) throws Exception {
		String[] tokensArr = null;
		if (sentence != null && !"".equals(sentence)) {
			String[] splitTokens = StringUtils.splitSentence(sentence, ",", false);
			// NOTE:The crawler retrieves the section starting from the keyword "keywords" and ending at the keyword
			// "introduction. This causes the following problem: after the split occurs the last element of the array
			// may contain the "1." value that corresponds to the first 2 characters before the "introduction" section.
			// This is not always the case so we have to handle this special case with the following code.
			String lastElem = splitTokens[splitTokens.length - 1];
			boolean containsDigit = false;
			for (int i = 0; i < lastElem.length(); i++) {
				Character c = lastElem.charAt(i);
				if (Character.isDigit(c)) {
					containsDigit = true;
					break;
				}
			}
			if (containsDigit) {
				tokensArr = new String[splitTokens.length - 1];
				for (int i = 0; i < splitTokens.length - 1; i++) {
					tokensArr[i] = splitTokens[i];
				}
			} else {
				tokensArr = splitTokens;
			}
		}
		return tokensArr;
	}

}
