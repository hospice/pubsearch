package ps.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ps.constants.NameConstants;

/**
 * Provides string manipulation utilities.
 */
public class StringUtils {

	public static void main(String[] args) {

//		// TEST 1
//		System.out.println("---------------- [ TEST 1 ] -------------------");
//		String s = "AJScJSJS";
//		boolean flag = isAllUpper(s);
//		System.out.println("FOR STRING: '" + s + "', IS ALL UPPER? " + flag);
//		System.out.println();
//
//		// TEST 2
//		System.out.println("---------------- [ TEST 2 ] -------------------");
//		String sentence = "AAAA thiss BBBB skdskds DcDDD CCCC";
//		List<String> upperTokensList = extractAllUpperToken(sentence);
//		for (String t : upperTokensList) {
//			System.out.println(t);
//		}
//		System.out.println();
		
//		// TEST 3
//		List<String> oldList = new ArrayList<String>();
//		oldList.add("AAA");
//		oldList.add("BBB");
//		oldList.add("CCC");
//		
//		List<String> newList = new ArrayList<String>();
//		newList.add("DDD");
//		newList.add("EEE");
//		newList.add("FFF");
//		
//		oldList.addAll(newList);
//		
//		for(String s : oldList){
//			System.out.println(s);
//		}
		
//		String sentence = "This is this. That is that.";
//		
//		String[] sentenceTokens = sentence.split("\\.");
//		for (int i = 0; i < sentenceTokens.length; i++) {
//			System.out.println(sentenceTokens[i]);			
//		}
		
		String text = "AAA this blah. Etc this blah AAA, AAA, AAA.";
		String tok = "AAA";
		System.out.println(countOccur(text, tok));
		
	}

	/**
	 * Checks if the specified string has value.
	 */
	public static boolean hasValue(String s) {
		return s != null && !"".equals(s);
	}

	/**
	 * Replaces all non-dot, punctuation characters with spaces in the specified string.
	 */
	public static String replaceAllNonDotPunctWithSpace(String s) {
		String stripped = s;
		if (s != null && !"".equals(s)) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) == 32 || s.charAt(i) == 46 || (s.charAt(i) >= 65 && s.charAt(i) <= 90)
						|| (s.charAt(i) >= 97 && s.charAt(i) <= 122)) {
					sb = sb.append(s.charAt(i));
				} else {
					sb = sb.append(" ");
				}
			}
			stripped = sb.toString();
		}
		return stripped;
	}

	/**
	 * Splits the specified paragraph into sentences,
	 */
	public static String[] splitParagraphIntoSentences(String paragraph) {
		List<String> sentences = new ArrayList<String>();
		String par = replaceAllNonDotPunctWithSpace(paragraph);
		String[] splitSentences = par.split("\\.");
		for (int i = 0; i < splitSentences.length; i++) {
			sentences.add(splitSentences[i].trim());
		}
		return (String[]) sentences.toArray(new String[sentences.size()]);
	}

	/**
	 * Splits the specified sentence into tokens of size greater than 1 based on the specified delimiter.
	 */
	public static String[] splitSentence(String sentence, String delimiter, boolean considerOneCharTokens)
			throws Exception {
		List<String> l = new ArrayList<String>();
		String[] splitTokens = sentence.split(delimiter);
		for (int i = 0; i < splitTokens.length; i++) {
			String token = splitTokens[i].trim();
			if (considerOneCharTokens || token.length() > 1) {
				l.add(token);
			}
		}
		return (String[]) l.toArray(new String[l.size()]);
	}

	/**
	 * Splits the specified query into lower-cased tokens.
	 */
	public static String[] splitStringIntoTokens(String sentence) {
		List<String> l = new ArrayList<String>();
		if (sentence != null) {
			String[] tokensArr = sentence.toLowerCase().split(" ");
			for (int i = 0; i < tokensArr.length; i++) {
				String token = tokensArr[i];
				if (token.length() > 0) {
					l.add(token);
				}
			}
		}
		return (String[]) l.toArray(new String[l.size()]);
	}

	/**
	 * Checks if the specified sentence contains the specified term.
	 */
	public static boolean containsTerm(String sentence, String term) throws Exception {
		String[] tokens = splitSentence(sentence, " ", false);
		return containsTerm(tokens, term);
	}

	/**
	 * Checks if the specified array of tokens contains the specified term.
	 */
	public static boolean containsTerm(String[] tokens, String term) throws Exception {
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals(term)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the specified array of tokens contains the specified term.
	 */
	public static boolean containsTerm(List<String> tokens, String term) throws Exception {
		for (String token : tokens) {
			if (token.equals(term)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Splits the specified section into paragraphs.
	 */
	public static String[] splitSectionIntoParagraphs(String section) {
		return section.split(NameConstants.PARAGRAPH_END);
	}

	/**
	 * Calculates the number of tokens in the specified text.
	 */
	public static int numOfTokens(String text) {
		return splitStringIntoTokens(replaceAllNonDotPunctWithSpace(text)).length;
	}

	/**
	 * Strips the specified string from all HTML tags.
	 */
	public static String stripTextFromHtml(String html) {
		return html.replaceAll("\\<.*?\\>", "").replaceAll("&#[\\d]*;", "").trim();
	}

	/**
	 * Strips the specified string from all non characters.
	 */
	public static String stripTextFromNonChars(String s) {
		String stripped = "";
		if (s != null && s.length() > 1) {
			String regex = "[^\\p{L}\\p{N}]";
			stripped = s.replaceAll(regex, "");
		}
		return stripped;
	}

	/**
	 * Extracts the year from the provided string.
	 */
	public static Integer extractYearFromText(String text) {
		Integer year = null;
		String regEx = "([0-9]{4}).*";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(text);
		if (m.find()) {
			year = Integer.parseInt(m.group(1));
		}
		return year;
	}

	/**
	 * Reads the file located in the specified path.
	 */
	public static String readFileFromPath(String pathname) throws IOException {
		FileInputStream fis = new FileInputStream(pathname);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String line;
		String text = "";
		while ((line = br.readLine()) != null)
			text += line + "\n";
		dis.close();
		return text;
	}

	/**
	 * Splits the query into token based on whether and to what place quotes have been entered For example, query =
	 * "web mining" information retrieval, will split in 3 tokens: 1. web mining, 2. information and 3. retrieval.
	 */
	public static List<String> queryTokens(String query, boolean considerQuotes) {
		List<String> l = new ArrayList<String>();
		String[] arr = query.split(" ");
		if (!considerQuotes) {
			for (int i = 0; i < arr.length; i++) {
				l.add(arr[i].replace("\"", ""));
			}
			return l;
		}
		String compositeToken = "";
		boolean isCompositeTrig = false;
		String tmpToken = "";
		for (int i = 0; i < arr.length; i++) {
			tmpToken = arr[i];
			if (tmpToken.contains("\"")) {
				// enter for first time (opening of quotes)
				if (isCompositeTrig == false) {
					compositeToken = tmpToken;
					isCompositeTrig = true;
				}
				// enter last time (closing of quotes)
				else {
					compositeToken += " " + tmpToken;
					l.add(compositeToken.replaceAll("\"", ""));
					isCompositeTrig = false;
				}
			} else {
				if (isCompositeTrig) {
					compositeToken += " " + tmpToken;
				} else {
					l.add(tmpToken);
				}
			}
		}
		return l;
	}

	/**
	 * Calculates the total number of words.
	 */
	public static long wordCount(String fName, BufferedReader in) throws IOException {
		long numChar = 0;
		long numLine = 0;
		long numWords = 0;
		String line;
		do {
			line = in.readLine();
			if (line != null) {
				numChar += line.length();
				numWords += StringUtils.wordcount(line);
				numLine++;
			}
		} while (line != null);
		return numWords;
	}

	/**
	 * Calculates the total number of tokens.
	 */
	public static long totalTokens(String fileName) {
		BufferedReader in = null;
		try {
			FileReader fileReader = new FileReader(fileName);
			in = new BufferedReader(fileReader);
			return wordCount(fileName, in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Calculates the total number of words.
	 */
	public static long wordcount(String line) {
		long numWords = 0;
		int index = 0;
		boolean prevWhiteSpace = true;
		while (index < line.length()) {
			char c = line.charAt(index++);
			boolean currWhiteSpace = Character.isWhitespace(c);
			if (prevWhiteSpace && !currWhiteSpace) {
				numWords++;
			}
			prevWhiteSpace = currWhiteSpace;
		}
		return numWords;
	}

	/**
	 * Removes the content surrounded by specified open/close characters from the specified string.
	 */
	public static String removeContentBetweenDelimiters(String s, String openChar, String closeChar) {
		int beg = s.indexOf(openChar);
		while (beg > -1) {
			int parenEnd = s.indexOf(closeChar) + 1;
			String clean1 = s.substring(0, beg);
			String clean2 = s.substring(parenEnd, s.length());
			s = clean1 + clean2;
			beg = s.indexOf(openChar);
		}
		return s.trim();
	}
	
	/**
	 * Returns a list of all tokens of the provided sentence that contain only upper-cased characters.
	 */
	static List<String> extractAllUpperToken(String sentence) {
		List<String> upperTokens = new ArrayList<String>();
		String[] tokens = sentence.split(" ");
		for (String t : tokens) {
			if (isAllUpper(t)) {
				upperTokens.add(t);
			}
		}
		return upperTokens;
	}

	/**
	 * Checks if all characters of a specific string are upper-cased.
	 */
	private static boolean isAllUpper(String s) {
		for (char c : s.toCharArray()) {
			if (Character.isLetter(c) && Character.isLowerCase(c)) {
				return false;
			}
		}
		return true;
	}
	

	/**
	 * Escapes all apostrophe(') characters.
	 */
	public static String escapeChars(String text) {
		String escaped = new String(text);
		if (text.indexOf("'") != -1) {
			StringBuffer sb = new StringBuffer();
			char c;
			for (int i = 0; i < text.length(); i++) {
				if ((c = text.charAt(i)) == '\'') {
					sb.append("''");
				} else {
					sb.append(c);
				}
			}
			escaped = sb.toString();
		}
		return escaped;
	}
	
	/**
	 * Counts the number of occurrences of the specified token in the specified text snippet.
	 * 
	 * @param snippet,	the text snippet to examine
	 * @param token,	the token whose occurrences we need to calculate
	 * @return			the total number of occurrences
	 */
	public static int countOccur(String snippet, String token) {
		String s = new String(snippet);
		int n = 0;
		int i = s.indexOf(token);
		while (i > -1) {
			n++;
			s = s.substring(i + 1);
			i = s.indexOf(token);
		}
		return n;
	}
	
}
