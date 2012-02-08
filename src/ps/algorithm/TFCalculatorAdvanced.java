package ps.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TFCalculatorAdvanced {

	private final static boolean CONSIDER_QUOTES = true;

	private final static double WEIGHT_FOR_TITLE = 20;
	private final static double WEIGHT_FOR_ABSTRACT = 10;
	private final static double WEIGHT_FOR_BODY = 5;

	private final static String QUOTES = "\"";
	private final static String EMPTY = "";

	public static void main(String[] args) {
		String orgText = "\"quoted text\" example";
		List<String> tokens = splitQueryTokens(orgText, CONSIDER_QUOTES);
		for (String token : tokens) {
			System.out.println(token);
		}
	}

	public static double calculateTermFrequencyScore(String orgText, String title, List<String> tokens) {
		double scoreForTitle = scoreForTitle(title);
		String abstractTxt = ""; // FIXME: isolate Abstract section
		double scoreForAbstract = scoreForAbstract(abstractTxt);
		String bodyTxt = ""; // FIXME: isolate body section
		double scoreForBody = scoreForBody(bodyTxt);
		return scoreForTitle + scoreForAbstract + scoreForBody;
	}

	/**
	 * Calculates the total TF score for the title text.
	 * The following cases should be considered:
	 *   1. All tokens appear next to each other in the title,
	 *   2. All tokens appear together in the title, but not next to each other
	 *   3. Some tokens appear together in the title
	 * 
	 * @param title, the title text
	 * @return
	 */
	private static double scoreForTitle(String title) {
		int tf = 0;
		double scoreForTitle = tf * WEIGHT_FOR_TITLE;
		return scoreForTitle;
	}

	/**
	 * Calculates the total TF score for the abstract text.
	 * @param abstractText, the abstract text
	 * @return
	 */
	private static double scoreForAbstract(String abstractText) {
		int tf = 0;
		double scoreForTitle = tf * WEIGHT_FOR_ABSTRACT;
		return scoreForTitle;
	}

	/**
	 * Calculates the total TF score for the body text.
	 * @param bodyText, the body text
	 * @return
	 */
	private static double scoreForBody(String bodyText) {
		int tf = 0;
		double scoreForTitle = tf * WEIGHT_FOR_BODY;
		return scoreForTitle;
	}

	/**
	 * Splits the original query text into tokens to isolate specific token pairs. The method is parameterized in such
	 * as a way so as to consider (or not) the use of quotes for specifying query terms.
	 * 
	 * @param orgQueryText
	 *            , the originally submitted query text
	 * @param considerQuotes
	 *            , flag that determines if the quotes should be considered or not in order to split the original query
	 *            text into token pairs (when specified)
	 * @return the list of query tokens
	 */
	public static List<String> splitQueryTokens(String orgQueryText, boolean considerQuotes) {
		String tmpQueryText = new String(orgQueryText);
		List<String> tokensList = new ArrayList<String>();
		// case 1: quotes should be considered
		if (considerQuotes) {
			while (tmpQueryText.contains(QUOTES)) {
				int from = tmpQueryText.indexOf(QUOTES);
				int to = tmpQueryText.indexOf(QUOTES, from + 1);
				String token = tmpQueryText.substring(from + 1, to);
				tokensList.add(token.trim());
				tmpQueryText = tmpQueryText.substring(to + 1, tmpQueryText.length());
			}
			if (tmpQueryText.length() > 0) {
				tokensList.add(tmpQueryText.trim());
			}
		}
		// case 2: quotes should NOT be considered
		else {
			tmpQueryText.replaceAll(QUOTES, EMPTY);
			String[] tokensArr = tmpQueryText.split(" ");
			tokensList = Arrays.asList(tokensArr);
		}
		return tokensList;
	}

}
