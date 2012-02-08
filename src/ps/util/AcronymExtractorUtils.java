package ps.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ps.persistence.PersistenceController2;

/**
 * Provides a set of utilities for extracting acronyms from text snippet and for construcing the acronyms maps
 */
public class AcronymExtractorUtils {

	private final static String CONNECTOR = "%";
	private final static int MIN_OCCURRENCES = 2;

	public static void main(String[] args) {
		String query = "web information retrieval"; // "peer-to-peer networks";
		List<String> allTermCombinations = findAllCombinations(query);

	}

	/**
	 * Constructs and returns the acronym map by reading two differently formatted acronym files. Optionally the map is
	 * persisted and saved to the specified file.
	 * 
	 * @param persistMap
	 *            , flag that determines if the map should be persisted in database
	 * @param saveMapToFile
	 *            , flag that determines if the map should be saved in file
	 * @param outputFile
	 *            , the path of the output file in case that we want to save the map in file
	 * @return the constructed map
	 * @throws Exception
	 */
	public static Map<String, List<String>> constructAcronymsMap(boolean persistMap, boolean saveMapToFile,
			String outputFile) throws Exception {
		Map<String, List<String>> acronymsMap = new TreeMap<String, List<String>>();
		readAcronymsFile1(acronymsMap);
		readAcronymsFile2(acronymsMap);
		if (saveMapToFile) {
			PrintUtils.printAcronymsMap(acronymsMap, outputFile);
		}
		if (persistMap) {
			PersistenceController2.saveAllAcronyms(acronymsMap);
		}
		return acronymsMap;
	}

	/**
	 * Reads the acronym list according to the format of acronym list 1.
	 */
	private static void readAcronymsFile1(Map<String, List<String>> map) throws Exception {
		String pathName = "C:/_tmp/ACRONYMS_LIST.txt";
		FileInputStream fis = new FileInputStream(pathName);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String line;
		String acronymPart = "";
		int lineNum = 1;
		while ((line = br.readLine()) != null) {
			line = StringUtils.removeContentBetweenDelimiters(line, "(", ")");
			line = StringUtils.removeContentBetweenDelimiters(line, "[", "]");
			String[] lineTokens = line.split("\t");
			if (lineTokens.length < 2) {
				throw new Exception("No 2 tokens for line[" + lineNum + "] : " + line);
			}
			acronymPart = lineTokens[0];
			if (acronymPart.length() == 0) {
				throw new Exception("NULL acronym found at line: " + lineNum);
			}
			String descr = StringUtils.escapeChars(lineTokens[1]);
			List<String> descrList = map.get(acronymPart);
			if (descrList == null) {
				descrList = new ArrayList<String>();
			}
			addToListIfNew(descrList, descr);
			map.put(acronymPart, descrList);
			lineNum++;
		}
	}

	/**
	 * Reads the acronym list according to the format of acronym list 2.
	 */
	private static void readAcronymsFile2(Map<String, List<String>> map) throws Exception {
		FileInputStream fis = new FileInputStream("C:/_tmp/ACRONYMS_LIST_2.txt");
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String line;
		String acronymPart = "";
		int lineNum = 1;
		while ((line = br.readLine()) != null) {
			line = StringUtils.removeContentBetweenDelimiters(line, "(", ")");
			line = StringUtils.removeContentBetweenDelimiters(line, "[", "]");
			String[] lineTokens = line.split(" ");
			acronymPart = lineTokens[0];
			if (acronymPart.length() == 0) {
				throw new Exception("NULL acronym found at line: " + lineNum);
			}
			String descrPart = line.substring(acronymPart.length());
			String[] descrArr = descrPart.split(CONNECTOR);
			for (int i = 0; i < descrArr.length; i++) {
				String descr = StringUtils.escapeChars(descrArr[i].trim());
				List<String> descrList = map.get(acronymPart);
				if (descrList == null) {
					descrList = new ArrayList<String>();
				}
				addToListIfNew(descrList, descr);
				map.put(acronymPart, descrList);
			}
			lineNum++;
		}
	}

	/**
	 * Adds the element to the specified list if it not already exists.
	 */
	private static void addToListIfNew(List<String> l, String elem) {
		for (String listElem : l) {
			if (listElem.equalsIgnoreCase(elem)) {
				return;
			}
		}
		l.add(elem);
	}

	public static List<String> findAllAcronymsString(String query, String title, String abstractText, String body)
			throws ClassNotFoundException, SQLException, IOException {
		List<String> acronymList = new ArrayList<String>();
		// i. all possible acronyms based on db data
		for (String descr : findAllCombinations(query)) {
			System.out.println("Possible acronyms for description: " + descr);
			List<String> allAcronyms = PersistenceController2.findAcronymsForDescr(descr);
			for (String acr : allAcronyms) {
				acronymList.add(acr);
			}
		}
		// ii. all possible acronyms based on combinations:
		for (String acr : findAcronym(query, title, abstractText, body)) {
			acronymList.add(acr);
		}
		return acronymList;
	}

	/**
	 * Attempts to identify an acronym in the specified title, abstract or body.
	 */
	public static String[] findAcronym(String query, String title, String abstractText, String body) {
		String[] acronymArr = AcronymExtractorUtils.findDefinitionAndAcronym(title, query);
		if (acronymArr[0] == null) {
			acronymArr = AcronymExtractorUtils.findDefinitionAndAcronym(abstractText, query);
		} else if (acronymArr[0] == null) {
			acronymArr = AcronymExtractorUtils.findDefinitionAndAcronym(body, query);
		}
		return acronymArr;
	}

	/**
	 * Replaces the acronym with its description in the specified sentence.
	 */
	public static String replaceAcronymWithDescr(String sentence, String[] acronymArr) {
		return sentence.toLowerCase().replaceAll(acronymArr[0], acronymArr[1]);
	}

	/**
	 * Identifies the acronym in snippet based on the query terms.
	 */
	private static String[] findDefinitionAndAcronym(String snippet, String q) {
		String s = new String(snippet.toLowerCase());
		String[] acronymArr = new String[2];
		if (s.length() > 0) {
			List<String> allTermCombinations = findAllCombinations(q.toLowerCase());
			for (String c : allTermCombinations) {
				String acronym = extractContentInParenth(s, c);
				if (acronym != null) {
					acronymArr[0] = acronym;
					acronymArr[1] = c;
					return acronymArr;
				}
			}
		}
		return acronymArr;
	}

	/**
	 * Queries the database to fetch all acronyms matching the description that is produced by combining the query
	 * terms.
	 */
	private static List<String> updateAcronymListFromDB(List<String> allTermCombinations) throws Exception {
		List<String> l = new ArrayList<String>();
		for (String comb : allTermCombinations) {
			List<String> acrList = PersistenceController2.findAcronymsForDescr(comb);
			for (String s : acrList) {
				if (!StringUtils.containsTerm(l, s)) {
					l.add(s);
				}
			}
		}
		return l;
	}

	/**
	 * Finds all possible acronyms in the provided snippet.
	 */
	private static List<String> extractAllCandidateAcronyms(String snippet, List<String> allTermCombinations,
			List<String> acronymList) throws Exception {
		List<String> l = new ArrayList<String>();
		String[] sentences = snippet.split("\\.");
		for (int i = 0; i < sentences.length; i++) {
			l.addAll(findAcronyms(sentences[i].trim(), allTermCombinations, acronymList));
		}
		return l;
	}

	/**
	 * Finds all possible acronyms by examining the list of all query term combinations in the provided sentence.
	 */
	private static List<String> findAcronyms(String sentence, List<String> allTermCombinations, List<String> acronymList)
			throws Exception {
		List<String> l = new ArrayList<String>();
		if (sentence.length() > 0) {
			String copy = new String(sentence);
			List<String> allUpperTokens = StringUtils.extractAllUpperToken(copy);
			if (allUpperTokens.size() > 0) {
				for (String currAllUpperToken : allUpperTokens) {
					if (!StringUtils.containsTerm(acronymList, currAllUpperToken)) {
						if (acronymMatch(currAllUpperToken, allTermCombinations)) {
							l.add(currAllUpperToken);
						}
					}
				}
			}
		}
		return l;
	}

	/**
	 * Checks if there exists a match of the acronym with all the query term combinations.
	 */
	private static boolean acronymMatch(String acronym, List<String> allTermCombinations) {
		for (String currCombination : allTermCombinations) {
			String acronymCandidate = "";
			String tokens[] = currCombination.split(" ");
			for (String s : tokens) {
				acronymCandidate += s.charAt(0);
			}
			if (acronym.equalsIgnoreCase(acronymCandidate)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extracts from the snippet the text in the parenthesis after the occurrence of q.
	 */
	private static String extractContentInParenth(String s, String c) {
		String content = null;
		int from = s.indexOf(c);
		if (from > -1) {
			from = s.indexOf(c) + c.length();
			if (from < s.length()) {
				Character ch = s.charAt(from);
				while (ch == ' ') {
					from++;
					ch = s.charAt(from);
				}
				if (s.charAt(from) == '(') {
					int to = s.indexOf(')', from);
					String tmp = s.substring(from + 1, to);
					content = tmp.toLowerCase().charAt(0) == c.toLowerCase().charAt(0) ? tmp : null;
				}
			}
		}
		return content;
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

	/**
	 * Returns the list of acronyms used in all publications.
	 */
	public static List<String> fetchAcronymListUsed(String query, List<String> pubList) throws Exception {
		List<String> allTermCombinations = findAllCombinations(query);
		
		// 1. list is updated by fetching all acronyms whose description matches with the query combinations
		List<String> acronymList = updateAcronymListFromDB(allTermCombinations);

		// 2. list is updated by fetching all acronyms whose description matches with the query combinations
		List<String> finalAcronymList = new ArrayList<String>();
		for (String currPub : pubList) {
			finalAcronymList.addAll(findAcronymsInText(currPub, allTermCombinations, acronymList));
		}
		return finalAcronymList;
	}

	/**
	 * Attempts to identify possible acronyms in the specified snippet and calculate their occurrences.
	 */
	public static List<String> findAcronymsInText(String text, List<String> allTermCombinations, List<String> acronymList) 
			throws Exception {
		// extracts all candidate acronyms (all terms consisted only of upper-cased characters)
		List<String> allAcronymList = extractAllCandidateAcronyms(text, allTermCombinations, acronymList);

		// contains all acronyms that are encountered more than MIN_OCCURRENCES time in the publications
		List<String> usedAcronymList = new ArrayList<String>();
		for (Iterator<String> iter = allAcronymList.iterator(); iter.hasNext();) {
			String acronym = (String) iter.next();
			if (StringUtils.countOccur(text, acronym) >= MIN_OCCURRENCES) {
				usedAcronymList.add(acronym);
			}
		}
		return usedAcronymList;
	}

}
