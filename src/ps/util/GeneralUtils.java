package ps.util;

import java.util.ArrayList;
import java.util.List;

import ps.stem.snowball.StemUtil;
import ps.struct.PublicationData;

/**
 * Provides general-purpose utilities.
 */
public class GeneralUtils {

	public PublicationData fillPublicationData(String query, String pubTitle, String publicationText) throws Exception {
		if (PropertyUtils.useStemming()) {
			query = StemUtil.getEnglishStem(query);
			pubTitle = StemUtil.getEnglishStem(pubTitle);
			publicationText = StemUtil.getEnglishStem(publicationText);
		}
		return new PublicationData(query, pubTitle, publicationText);
	}

	/**
	 * Converts the specified string containing a list of authors names comma-separated into a list of string.
	 */
	public static List<String> extractAuthorNames(String authorsStr) {
		List<String> authorsList = new ArrayList<String>();
		String[] authorsArr = authorsStr.split(",");
		for (int i = 0; i < authorsArr.length; i++) {
			String auth = authorsArr[i].trim();
			authorsList.add(auth);
		}
		return authorsList;
	}

}
