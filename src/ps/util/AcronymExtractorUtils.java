package ps.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ps.persistence.PersistenceController2;
import ps.stem.snowball.StemUtil;

/**
 * Provides a set of utilities for extracting acronyms from text snippet and for construcing the acronyms maps
 */
public class AcronymExtractorUtils {

	public static void main(String[] args) {

	}
	
	public static void acronymExtraction2(String query, String text) throws ClassNotFoundException, SQLException, IOException {
		
		// extracts all acronyms and their descriptions as defined in the text
		List<String[]> acronymsList = extractAcronyms(text);
		
		// extracts all capitalized tokens and counts their occurrences
		Map<String, Integer> allCapTokenMap = countNumberOfOccurrences(extractAllCapTokens(text));
		Map<String, Double> acronymMatchingMap = new HashMap<String, Double>();

		Iterator<String> it = allCapTokenMap.keySet().iterator();
		while(it.hasNext()){
			String acronym = it.next();
			
			boolean acronymDescrFound = false;
			
			// CASE 1: attempts to identify the description from the acronym list extracted from text
			for(String[] pair : acronymsList){
				String acr = pair[0];
				String descr = pair[1];
				if(acronym.equals(acr)){
					acronymDescrFound = true;
					double matchPerc = calcRelevanceOfAcronymWithQuery(descr, query);
					acronymMatchingMap.put(acr, matchPerc);
					break;
				}
			}
			
			// CASE 2: queries DB to fetch the list of descriptions for the specified query
			if(!acronymDescrFound){
				List<String> descrList = PersistenceController2.fetchDescriptionListForAcronym(acronym);
				
			}

			// Integer occurrences = allCapTokenMap.get(acronym);
			// calcRelevanceOfAcronymWithQuery(pair[1], query);
			
			
			
		}
	}
	

//	public static void acronymExtraction() throws ClassNotFoundException, SQLException, IOException {
//
//		String query = "web information retrieval";
//
//		String text = "This is a test of schwartz's excellent abbreviation tool (SEAT) on a simple example.  ABC is not defined here. \n \r Web Information Retrieval (IR).";
//
//		System.out.println("PRINTING RESULTS FOR QUERY = " + query);
//
//		// CASE 1 : Acronym defined in text
//		List<String[]> acronymsList = extractAcronyms(text);
//		for (String[] pair : acronymsList) {
//			String acr = pair[0];
//			String descr = pair[1];
//			compareAcrDescrWithQuery(descr, query);
//		}
//
//		// CASE 2 : Acronyms used in text
//		List<String> allCapTokens = extractAllCapTokens(text);
//		Map<String, Integer> allCapTokenMap = countNumberOfOccurrences(allCapTokens);
//		
//
//		// CASE 3: Acronyms used in query
//		List<String> acronymsExistingInQuery = findAcronymsFromQuery(query, allCapTokenMap);
//
//		// CASE 4: Acronyms identified by examining all combinations of query terms
//		List<String> allAcronymCandidates = findAcronymsForAllCombinations(query);
//		List<String> acronymsFromQueryTermCombinations = fetchListOfAppearingAcronyms(allAcronymCandidates, allCapTokenMap);
//	}

//	/**
//	 *  Returns a list of all acronyms that appear at least one time.
//	 */
//	private static List<String> fetchListOfAppearingAcronyms(List<String> allAcronymCandidates, Map<String, Integer> map){
//		List<String> l = new ArrayList<String>();
//		Iterator<String> it = map.keySet().iterator();
//		while (it.hasNext()) {
//			String key = it.next();
//			Integer val = map.get(key);
//			if (val > 0) {
//				for(String acr : allAcronymCandidates){
//					if(acr.equals(key)){
//						l.add(key);
//						break;
//					}
//				}
//			}
//		}
//		return l;
//	}
//	
//	private static List<String> findAcronymsForAllCombinations(String query) throws ClassNotFoundException,
//			SQLException, IOException {
//		List<String> acronyms = new ArrayList<String>();
//		List<String> combinations = findAllCombinations(query);
//		for (String c : combinations) {
//			List<String> l = PersistenceController2.fetchAcronymsForDescription(c);
//			for (String acr : l) {
//				if (!acronyms.contains(acr)) {
//					acronyms.add(c);
//				}
//			}
//		}
//		return acronyms;
//	}
//
//	private static List<String> findAcronymsFromQuery(String query, Map<String, Integer> allCapTokenMap){
//		List<String> acronymList = new ArrayList<String>();
//		List<String> allCapTokensInQuery = extractAllCapTokens(query);
//		Iterator<String> it = allCapTokenMap.keySet().iterator();
//		while(it.hasNext()){
//			String key = it.next();
//			Integer val = allCapTokenMap.get(key);
//			if(val>0){
//				for(String s : allCapTokensInQuery){
//					if(s.equals(key)){
//						acronymList.add(s);
//						break;
//					}
//				}
//			}
//		}
//		
//		return acronymList;
//	}
	
	private static List<String[]> extractAcronyms(String str) {
		List<String[]> pairsList = new ArrayList<String[]>();
		String tmpStr = "";
		String longForm = "";
		String shortForm = "";
		int openParenIndex = -1;
		int closeParenIndex = -1;
		int sentenceEnd = -1;
		int newCloseParenIndex = -1;
		int tmpIndex = -1;
		StringTokenizer shortTokenizer = null;
		openParenIndex = str.indexOf("(");
		do {
			// checks whether the sentence ends with DOT or COMMA
			sentenceEnd = Math.max(str.lastIndexOf(". "), str.lastIndexOf(", "));
			if ((openParenIndex == -1) && (sentenceEnd == -1)) {
				// do nothing: no opening parenthesis found and sentence does not end
			} else if (openParenIndex == -1) {
				// no opening parenthesis found in current sentence, moves to next
				str = str.substring(sentenceEnd + 2);
			} else if ((closeParenIndex = str.indexOf(')', openParenIndex)) > -1) {
				sentenceEnd = Math.max(str.lastIndexOf(". ", openParenIndex), str.lastIndexOf(", ", openParenIndex));
				if (sentenceEnd == -1) {
					sentenceEnd = -2;
				}
				longForm = str.substring(sentenceEnd + 2, openParenIndex);
				shortForm = str.substring(openParenIndex + 1, closeParenIndex);
			}
			if (shortForm.length() > 0 || longForm.length() > 0) {
				if (shortForm.length() > 1 && longForm.length() > 1) {
					if ((shortForm.indexOf('(') > -1)
							&& ((newCloseParenIndex = str.indexOf(')', closeParenIndex + 1)) > -1)) {
						shortForm = str.substring(openParenIndex + 1, newCloseParenIndex);
						closeParenIndex = newCloseParenIndex;
					}
					if ((tmpIndex = shortForm.indexOf(", ")) > -1) {
						shortForm = shortForm.substring(0, tmpIndex);
					}
					if ((tmpIndex = shortForm.indexOf("; ")) > -1) {
						shortForm = shortForm.substring(0, tmpIndex);
					}
					shortTokenizer = new StringTokenizer(shortForm);
					if (shortTokenizer.countTokens() > 2 || shortForm.length() > longForm.length()) {
						// Long form in ( )
						tmpIndex = str.lastIndexOf(" ", openParenIndex - 2);
						tmpStr = str.substring(tmpIndex + 1, openParenIndex - 1);
						longForm = shortForm;
						shortForm = tmpStr;
						if (!hasCapital(shortForm)) {
							shortForm = "";
						}
					}
					if (isValidShortForm(shortForm)) {
						String[] pair = extractAbbrPair(shortForm.trim(), longForm.trim());
						if (pair != null) {
							pairsList.add(pair);
						}
					}
				}
				str = str.substring(closeParenIndex + 1);
			} else if (openParenIndex > -1) {
				if ((str.length() - openParenIndex) > 200)
					// Matching close paren was not found
					str = str.substring(openParenIndex + 1);
				break; // Read next line
			}
			shortForm = "";
			longForm = "";
		} while ((openParenIndex = str.indexOf("(")) > -1);
		return pairsList;
	}

	private static String findBestLongForm(String shortForm, String longForm) {
		int sIndex;
		int lIndex;
		char currChar;

		sIndex = shortForm.length() - 1;
		lIndex = longForm.length() - 1;
		for (; sIndex >= 0; sIndex--) {
			currChar = Character.toLowerCase(shortForm.charAt(sIndex));
			if (!Character.isLetterOrDigit(currChar))
				continue;
			while (((lIndex >= 0) && (Character.toLowerCase(longForm.charAt(lIndex)) != currChar))
					|| ((sIndex == 0) && (lIndex > 0) && (Character.isLetterOrDigit(longForm.charAt(lIndex - 1)))))
				lIndex--;
			if (lIndex < 0)
				return null;
			lIndex--;
		}
		lIndex = longForm.lastIndexOf(" ", lIndex) + 1;
		return longForm.substring(lIndex);
	}

	private static String[] extractAbbrPair(String shortForm, String longForm) {
		String bestLongForm;
		StringTokenizer tokenizer;
		int longFormSize, shortFormSize;
		if (shortForm.length() == 1)
			return null;
		bestLongForm = findBestLongForm(shortForm, longForm);
		if (bestLongForm == null)
			return null;
		tokenizer = new StringTokenizer(bestLongForm, " \t\n\r\f-");
		longFormSize = tokenizer.countTokens();
		shortFormSize = shortForm.length();
		for (int i = shortFormSize - 1; i >= 0; i--)
			if (!Character.isLetterOrDigit(shortForm.charAt(i)))
				shortFormSize--;
		if (bestLongForm.length() < shortForm.length() || bestLongForm.indexOf(shortForm + " ") > -1
				|| bestLongForm.endsWith(shortForm) || longFormSize > shortFormSize * 2
				|| longFormSize > shortFormSize + 5 || shortFormSize > 10) {
			return null;
		}
		String[] pair = new String[2];
		pair[0] = shortForm;
		pair[1] = bestLongForm;
		return pair;
	}

	private static boolean isValidShortForm(String str) {
		return (hasLetter(str) && (Character.isLetterOrDigit(str.charAt(0)) || (str.charAt(0) == '(')));
	}

	private static boolean hasLetter(String str) {
		for (int i = 0; i < str.length(); i++)
			if (Character.isLetter(str.charAt(i)))
				return true;
		return false;
	}

	private static boolean hasCapital(String str) {
		for (int i = 0; i < str.length(); i++)
			if (Character.isUpperCase(str.charAt(i)))
				return true;
		return false;
	}

	private static List<String> extractAllCapTokens(String str) {
		List<String> l = new ArrayList<String>();
		String[] tok = str.split(" ");
		for (int i = 0; i < tok.length; i++) {
			String token = stripStringFromNonLettersAndDigits(tok[i]);
			if (token.length() > 0 && isTokenAllInCaps(token)) {
				l.add(token);
			}
		}
		return l;
	}
	
	private static Map<String, Integer> countNumberOfOccurrences(List<String> l) {
		Map<String, Integer> m = new HashMap<String, Integer>();
		for (String s : l) {
			Integer val = m.get(s);
			if (val == null) {
				val = 0;
			}
			m.put(s, ++val);
		}
		return m;
	}

	private static boolean isTokenAllInCaps(String token) {
		for (int b = 0; b < token.length(); b++)
			if (!Character.isUpperCase(token.charAt(b))) {
				return false;
			}
		return true;
	}

	private static String stripStringFromNonLettersAndDigits(String s) {
		String stripped = "";
		if (s != null) {
			s = s.trim();
			for (int b = 0; b < s.length(); b++) {
				Character ch = s.charAt(b);
				if (Character.isLetterOrDigit(ch)) {
					stripped += ch;
				}
			}
		}
		return stripped;
	}

	private static double calcRelevanceOfAcronymWithQuery(String descr, String query) {
		int matches = 0;
		String[] descrTokens = descr.split(" ");
		String[] queryTokens = query.split(" ");
		for (int a = 0; a < descrTokens.length; a++) {
			for (int b = 0; b < queryTokens.length; b++) {
				if (StemUtil.getEnglishStem(descrTokens[a].toLowerCase()).equals(
						StemUtil.getEnglishStem(queryTokens[b].toLowerCase()))) {
					matches++;
				}
			}
		}
		double score = (double) matches / (double) query.split(" ").length;
		return score;
	}

	/**
	 * Returns a list with all matching combinations of the query terms.
	 */
	private static List<String> findAllCombinations(String query) {
		List<String> tokenCombinations = new ArrayList<String>();
		String[] allTokens = query.split(" ");
		for (int currLen = 1; currLen <= allTokens.length; currLen++) {
			updateCombinations("", currLen, allTokens, tokenCombinations);
		}
		return tokenCombinations;
	}

	/**
	 * Updates the token combinations by recursion.
	 */
	private static void updateCombinations(String s, int len, String[] tokens, List<String> tokenCombinations) {
		String[] sArr = s.split(" ");
		if (sArr.length == len && !"".equals(sArr[0])) {
			if (isCombinationUnique(sArr)) {
				tokenCombinations.add(s);
			}
			return;
		} else {
			for (int i = 0; i < tokens.length; i++) {
				String a = s.trim() + (!"".equals(s.trim()) ? " " : "") + tokens[i];
				updateCombinations(a, len, tokens, tokenCombinations);
			}
		}
	}

	/**
	 * Checks if the elements included in the array are unique (no duplicates exist).
	 */
	private static boolean isCombinationUnique(String[] sArr) {
		List<String> encounteredTokens = new ArrayList<String>();
		for (int i = 0; i < sArr.length; i++) {
			String term = sArr[i];
			if (encounteredTokens.contains(term)) {
				return false;
			} else {
				encounteredTokens.add(term);
			}
		}
		return true;
	}

}