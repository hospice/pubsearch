package ps.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CitationUtils {

	private static final String RESULTS_FROM = "<font size=-1>Results";

	/**
	 * Checks if the provided string is a valid year.
	 * 
	 * @param s
	 *            , the string to be validated
	 * @return, true if string is a valid year, false otherwise
	 */
	private static boolean isYearMatch(String s) {
		String yearExp = "^[1-2]{1}[0-9]{3}$";
		Pattern p = Pattern.compile(yearExp);
		Matcher m = p.matcher(s);
		return m.find();
	}

	/**
	 * Extracts the year from the specified string.
	 * 
	 * @param s, the string to be parsed
	 * @return the year in 4-digit format
	 */
	private static String extractYearFromString(String s) {
		if (s.length() < 4) {
			return "";
		}
		int n = s.length() - 3;
		int from = 0;
		int to = 4;
		String testStr = "";
		for (int i = 0; i < n; i++) {
			testStr = s.substring(from, to);
			if (isYearMatch(testStr)) {
				return testStr;
			}
			from++;
			to++;
			if (from > s.length() || to >= s.length()) {
				return "";
			}
		}
		return "";
	}

	/**
	 * Extracts all information from the citations page.
	 */
	public static void extractAllCitationInfo(String html) {
		String text = new String(html);
		Integer indexFrom = text.indexOf(RESULTS_FROM); // beginning of results section
		Integer indexTo = null;
		text = text.substring(indexFrom, text.length()); // filtered out the interesting part
		String resultSection = "";
		int counter = 1;
		boolean hasMoreRes = true;
		while (hasMoreRes) {
			indexFrom = text.indexOf("div class=gs_rt") + "div class=gs_rt".length();
			if (counter == 10) {
				indexTo = text.indexOf("Related articles", indexFrom);
			} else {
				indexTo = text.indexOf("<div class=gs_r>", indexFrom);
			}
			resultSection = text.substring(indexFrom, indexTo);
			int from = 0;
			int to = 0;
			from = resultSection.indexOf("href");
			from = resultSection.indexOf(">", from) + 1;
			to = resultSection.indexOf("</a>", from);
			String title = resultSection.substring(from, to);
			from = resultSection.indexOf("<span class=gs_a>") + "<span class=gs_a>".length();
			to = resultSection.indexOf("</span>", from);
			String txt = resultSection.substring(from, to);
			String year = extractYearFromString(txt);
			if (counter < 10) {
				indexFrom = indexTo + "<div class=gs_r>".length();
				text = text.substring(indexFrom, text.length());
			} else {
				hasMoreRes = false;
			}
			System.out.println(counter++ + ". TITLE: " + title + ", YEAR: " + year);
		}
	}

}
