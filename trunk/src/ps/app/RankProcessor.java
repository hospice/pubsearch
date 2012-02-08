package ps.app;

import java.util.List;

import ps.extractors.SearchResultsAccumulator;
import ps.struct.PublicationInfo;

public class RankProcessor {
	public static void main(String[] args) {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void run() throws Exception {
		String query = ""; // call fetchNextQueryToProcess();
		List<PublicationInfo> searchResults = SearchResultsAccumulator.accumulateResults(query);
		// Rank r = rankResults(searchResults);
		// saveRank(r);
	}

}
