package ps.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ps.util.StringUtils;

/**
 * Provides term frequency functionality.
 */
public class TermFrequency {
	
	/**
	 * Finds the frequency for all query terms.
	 */
	public static Map<String, Integer> findAllTermFrequencies(String query, String pathname, boolean considerQuotes,
			int titleWeight, int abstractWeight,String pubTitle) throws IOException {
		Map<String, Integer> m = new HashMap<String, Integer>();
		for (String token : StringUtils.queryTokens(query, considerQuotes)) {
			m.put(token, findTotalTermFrequency(token, pathname, titleWeight, abstractWeight, pubTitle));
		}
		return m;
	}

	/**
	 * Finds the number of times term appears in the specified text file.
	 */
	public static int findTotalTermFrequency(String term, String pathname, int titleWeight, int abstractWeight, String pubTitle)
			throws IOException {
		String text = new String((StringUtils.readFileFromPath(pathname)).toLowerCase());
		int count = 0;

		// i. term frequency in title
		int ttf = findTermFrequency(pubTitle, term);
		count += ttf * titleWeight;
		
		// ii. term frequency in abstract
		String abstractText = "";
		try {
			abstractText = extractAbstract(text);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		int atf = findTermFrequency(abstractText, term);
		count += atf * abstractWeight;
		
		// iii. term frequency in text body
		int tf = findTermFrequency(text, term);
		count += tf;
		
		return count;
	}
	
	/**
	 * Calculates the term frequency of term in text.
	 */
	public static int findTermFrequency(String text, String term){
		int count = 0;
		int index = 0;
		while ((index = text.indexOf(term.toLowerCase(), index)) != -1) {
			++index;
			++count;
		}
		return count;
	}
	
	/**
	 * Extracts the abstract section from the specified text.
	 */
	private static String extractAbstract(String txt) throws Exception {
		String abstractSection = txt.toLowerCase();
		int from = abstractSection.indexOf("abstract") + "abstract".length();
		if (from == -1) {
			throw new Exception("abstract keyword not found!");
		}
		int to = abstractSection.indexOf("introduction", from);
		if (to == -1) {
			throw new Exception("introduction keyword not found!");
		}
		abstractSection = abstractSection.substring(from, to);
		return abstractSection;
	}

}
