package ps.stem.snowball;

import ps.struct.PublicationData;

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
	
	/**
	 * Stems all publication data.
	 */
	public static PublicationData stemAllPublicationData(PublicationData orgPd) throws Exception {
		String[] queryTokens = new String[orgPd.getQueryTokens().length];
		for (int i = 0; i < orgPd.getQueryTokens().length; i++) {
			queryTokens[i] = getEnglishStem(orgPd.getQueryTokens()[i]);
		}
		String pubTitle = getEnglishStem(orgPd.getTitle());
		String abstractText = getEnglishStem(orgPd.getAbstractText());
		String body = getEnglishStem(orgPd.getBody());
		return new PublicationData(queryTokens, pubTitle, abstractText, body);
	}
	
}
