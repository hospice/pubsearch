package ps.app;

import java.util.List;

import ps.extractors.SearchResultsAccumulator;
import ps.struct.PublicationInfo;

/**
 * Performs all query processing functionality.
 */
public class QueryProcessor {
	
	private final static String QUERY = "";

	public static void main(String[] args) {
		try {
			run(QUERY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public static void run(String query) throws Exception {
		List<PublicationInfo> searchResults = SearchResultsAccumulator.accumulateResults(query);
		// saveResults(searchResults);
	}
	
	/**
	 * 
	 */
	public static void run() throws Exception {
		String query = ""; // fetchNextQueryToProcess();
		List<PublicationInfo> searchResults = SearchResultsAccumulator.accumulateResults(query);
		// saveResults(searchResults);
	}

}
