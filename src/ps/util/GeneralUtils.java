package ps.util;

import ps.stem.snowball.StemUtil;
import ps.struct.PublicationData;

/**
 * Provides general-purpose utilities.
 */
public class GeneralUtils {
	
	public PublicationData fillPublicationData(String query, String pubTitle, String publicationText) throws Exception {
		if(PropertiesUtils.useStemming()){
			query = StemUtil.getEnglishStem(query); 
			pubTitle = StemUtil.getEnglishStem(pubTitle);
			publicationText = StemUtil.getEnglishStem(publicationText);
		}
		return new PublicationData(query, pubTitle, publicationText);
	}
	
}
