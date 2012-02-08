package ps.extractors;

import java.util.ArrayList;
import java.util.List;

import ps.struct.PublicationInfo;

public class SearchResultsAccumulator {

	
	/**
	 * Accumulates all distinct results.
	 */
	public final static List<PublicationInfo> accumulateResults(String query) throws Exception {
		// 1. fetches results from ACM Portal
		List<PublicationInfo> acmPortalRes = new ArrayList<PublicationInfo>(); //FIXME...
		// 2. fetches results from Microsoft Academic Search
		List<PublicationInfo> microsoftResults = MicrosoftAcademicSearchExtractor.extractPublicationResults(query);
		// 3. fetches results from Google scholar
		List<PublicationInfo> googleScholarResults = GoogleScholarExtractor.extractPublicationResults(query);		
		// 4. fetches results from ArnetMiner
		List<PublicationInfo> arnetMinerScholarResults = ArnetMinerExtractor.extractPublicationResults(query);
		// accumulates all distinct results
		return mergeDistinctResults(acmPortalRes, microsoftResults, googleScholarResults, arnetMinerScholarResults);
	}

	/**
	 * Merges all distinct results.
	 */
	public static List<PublicationInfo> mergeDistinctResults(List<PublicationInfo>... lists) {
		List<PublicationInfo> merged = new ArrayList<PublicationInfo>();
		for (List<PublicationInfo> list : lists) {
			for (PublicationInfo p : list) {
				if (!containsPublicationInfo(merged, p)) {
					merged.add(p);
				}
			}
		}
		return merged;
	}

	/**
	 * Checks if the publication is contained in the list.
	 */
	private static boolean containsPublicationInfo(List<PublicationInfo> l, PublicationInfo p) {
		boolean contains = false;
		for (PublicationInfo el : l) {
			if (el.getTitle().equals(p.getTitle())) {
				contains = true;
				break;
			}
		}
		return contains;
	}

}
