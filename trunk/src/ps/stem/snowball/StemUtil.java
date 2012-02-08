package ps.stem.snowball;

/**
 * Stem related functionality.
 */
public class StemUtil {
	
	public static void main(String[] args) {
		String s = "web information retrieval";
		System.out.println(getEnglishStem(s));
	}
	
	/**
	 * Returns the stemmed version of the specified string. 
	 */
	public static String getEnglishStem(String s) {
		String stemmedText = "";
		SnowballStemmer stemmer = new EnglishStemmer();
		// splits the string into tokens to produce the stems
		String[] tokens = s.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			stemmer.setCurrent(tokens[i]);
			stemmer.stem();
			stemmedText += stemmer.getCurrent();
			if (i < tokens.length - 1) {
				stemmedText += " ";
			}
		}
		return stemmedText;
	}
	
}
