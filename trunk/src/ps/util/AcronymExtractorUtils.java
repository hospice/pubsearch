package ps.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import ps.persistence.PersistenceController2;
import ps.stem.snowball.StemUtil;

/**
 * Provides a set of utilities for extracting acronyms from text snippet and for construcing the acronyms maps
 */
public class AcronymExtractorUtils {

	private static final Integer MIN_OCCUR = 2;

	public static void main(String[] args) throws Exception {

	}

	/**
	 * Returns a list of all acronyms (and their descriptions) contained in the specified text. The method initially
	 * extracts all acronyms and their descriptions defined in the text and then extracts all capitalized tokens and
	 * counts their occurrences. For all capitalized tokens with more than [MINIMUM] occurrences, the method attempts to
	 * identify the acronym description.
	 */
	public static List<String[]> fetchAllAcronyms(String query, String text) throws Exception {
		List<String[]> finalList = new ArrayList<String[]>();
		// extracts all acronyms and their descriptions defined in the text
		List<String[]> acronymsList = extractAcronyms(text);
		// copies acronym list to final list
		Collections.copy(acronymsList, finalList);
		// extracts all capitalized tokens and counts their occurrences
		Map<String, Integer> allCapsTokenMap = countNumberOfOccurrences(extractAllCapTokens(text));
		// attempts to identify the acronym description from all capitalized tokens with more than minimum occurrences
		Iterator<String> it = allCapsTokenMap.keySet().iterator();
		while (it.hasNext()) {
			String acr = it.next();
			Integer occur = allCapsTokenMap.get(acr);
			if (occur.compareTo(MIN_OCCUR) == 1 && !containsAcronym(finalList, acr)) {
				String descr = fetchDescrFromAcr(acr, query, acronymsList);
				String[] acrArr = new String[2];
				acrArr[0] = acr;
				acrArr[1] = descr;
				finalList.add(acrArr);
			}
		}
		return finalList;
	}

	/**
	 * Checks if the list contains the specified acronym.
	 */
	private static boolean containsAcronym(List<String[]> list, String acr) {
		boolean contains = false;
		for (String[] arr : list) {
			if (arr[0].equals(acr)) {
				contains = true;
				break;
			}
		}
		return contains;
	}

	/**
	 * Fetches the most probable description for the specified acronym based on the provided query.
	 */
	private static String fetchDescrFromAcr(String acr, String query, List<String[]> acronymsList) throws Exception {
		List<String> descrList = fetchDescrFromAcr(acr, acronymsList);
		return findMostProbableDescrForQuery(descrList, query);
	}

	/**
	 * Returns the descriptions that is more probable to be relevant to the specified query. The method calculates for
	 * each of the provided descriptions the matching degree of the description with the specified query and returns as
	 * "most likely" description the one with highest matching score with the provided query string.
	 */
	private static String findMostProbableDescrForQuery(List<String> descrList, String query) {
		TreeMap<Double, String> m = new TreeMap<Double, String>();
		for (String descr : descrList) {
			double percMatch = calcMatchingOfAcronymWithQuery(descr, query);
			if (percMatch > 0) {
				m.put(percMatch, descr);
			}
		}
		Iterator<Double> it = m.descendingKeySet().iterator();
		return m.get(it.next());
	}

	/**
	 * Attempts to fetch all possible descriptions for the specified acronym initially in the provided
	 * acronym/description list and if no descriptions are found then it queries the database.
	 */
	private static List<String> fetchDescrFromAcr(String acr, List<String[]> acronymsList) throws Exception {
		List<String> l = new ArrayList<String>();
		String descr = "";
		for (String[] arr : acronymsList) {
			if (arr[0].equals(acr)) {
				descr = arr[1];
				l.add(descr);
				break;
			}
		}
		if (!StringUtils.hasValue(descr)) {
			l = PersistenceController2.fetchDescriptionListForAcronym(acr);
		}
		return l;
	}

	/**
	 * Extracts all acronym definitions (acronym and description) from the specified text.
	 */
	private static List<String[]> extractAcronyms(String text) {
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
		openParenIndex = text.indexOf("(");
		do {
			// checks whether the sentence ends with DOT or COMMA
			sentenceEnd = Math.max(text.lastIndexOf(". "), text.lastIndexOf(", "));
			if ((openParenIndex == -1) && (sentenceEnd == -1)) {
				// do nothing: no opening parenthesis found and sentence does not end
			} else if (openParenIndex == -1) {
				// no opening parenthesis found in current sentence, moves to next
				text = text.substring(sentenceEnd + 2);
			} else if ((closeParenIndex = text.indexOf(')', openParenIndex)) > -1) {
				sentenceEnd = Math.max(text.lastIndexOf(". ", openParenIndex), text.lastIndexOf(", ", openParenIndex));
				if (sentenceEnd == -1) {
					sentenceEnd = -2;
				}
				longForm = text.substring(sentenceEnd + 2, openParenIndex);
				shortForm = text.substring(openParenIndex + 1, closeParenIndex);
			}
			if (shortForm.length() > 0 || longForm.length() > 0) {
				if (shortForm.length() > 1 && longForm.length() > 1) {
					if ((shortForm.indexOf('(') > -1)
							&& ((newCloseParenIndex = text.indexOf(')', closeParenIndex + 1)) > -1)) {
						shortForm = text.substring(openParenIndex + 1, newCloseParenIndex);
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
						tmpIndex = text.lastIndexOf(" ", openParenIndex - 2);
						tmpStr = text.substring(tmpIndex + 1, openParenIndex - 1);
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
				text = text.substring(closeParenIndex + 1);
			} else if (openParenIndex > -1) {
				if ((text.length() - openParenIndex) > 200)
					// Matching close paren was not found
					text = text.substring(openParenIndex + 1);
				break; // Read next line
			}
			shortForm = "";
			longForm = "";
		} while ((openParenIndex = text.indexOf("(")) > -1);
		return pairsList;
	}

	/**
	 * Finds the best long form.
	 */
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

	/**
	 * Extracts an abbreviation pair.
	 */
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

	/**
	 * Checks if the string has valid short form.
	 */
	private static boolean isValidShortForm(String str) {
		return (hasLetter(str) && (Character.isLetterOrDigit(str.charAt(0)) || (str.charAt(0) == '(')));
	}

	/**
	 * Checks if the string has at least one letter.
	 */
	private static boolean hasLetter(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (Character.isLetter(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the string has at least one capital character.
	 */
	private static boolean hasCapital(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extracts all capitalized tokens.
	 */
	private static List<String> extractAllCapTokens(String str) {
		List<String> l = new ArrayList<String>();
		String delims = "[ .,?!+\\-*/^()]+";
		String[] tok = str.split(delims);
		for (int i = 0; i < tok.length; i++) {
			String token = stripStringFromNonLettersAndDigits(tok[i]);
			if (token.length() > 0 && isAllInCaps(token)) {
				l.add(token);
			}
		}
		return l;
	}

	/**
	 * Counts the number of occurrences of all list items.
	 */
	private static Map<String, Integer> countNumberOfOccurrences(List<String> l) {
		Map<String, Integer> m = new HashMap<String, Integer>();
		for (String s : l) {
			s = stripStringFromNonLettersAndDigits(s);
			Integer val = m.get(s);
			if (val == null) {
				val = 0;
			}
			m.put(s, ++val);
		}
		return m;
	}

	/**
	 * Checks if all characters of the specified string are capitalized.
	 */
	private static boolean isAllInCaps(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!Character.isDigit(c) && !Character.isUpperCase(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Strips the specified string from all non-letter and non-digit characters.
	 */
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

	/**
	 * Calculates the matching percentage of the specified description with the specified query.
	 */
	private static double calcMatchingOfAcronymWithQuery(String descr, String query) {
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
		return (double) matches / (double) query.split(" ").length;
	}

}