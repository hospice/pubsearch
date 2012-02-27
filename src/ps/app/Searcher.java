package ps.app;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ps.enumerators.StatusEnum;
import ps.persistence.PersistenceController;
import ps.struct.AcmResult;
import ps.struct.AcmTopic;
import ps.struct.PublicationInfo;
import ps.struct.Query;
import ps.util.CrawlUtils;
import ps.util.PrintUtils;
import ps.util.SearchUtils;
import ps.util.TimeUtils;

/**
 * Provides the main searching functionality: searches for the default ACM portal results for submitted queries
 */
public class Searcher {
	// the number of queries searched
	
	private final static Integer QUERIES_TO_SEARCH = 5;
	// the number of results to fetch
	private final static int NUM_OF_RESULTS = 10;
	// flag that determines if the algorithm should find the most common topics
	private final static boolean fetchMostCommon = false;
	// the number of most common topics to fetch (in case we want to find those)
	private final static int NUM_OF_TOPICS = 3;

	public static void main(String[] args) throws Exception {
		searchController();
	}

	/**
	 * this method controls the search operation
	 */
	public static void searchController() throws Exception {
		// retrieves code and description of all ACM topics
		Map<String, String> topicCodeMap = PersistenceController.fetchAllTopicCodesAndDescr();
		// number of queries searched
		int queriesSearched = 0;
		// fetches next unprocessed query
		Query query = PersistenceController.fetchNextQueryToProcess();
		while (query != null) {
			// performs the query search
			doSearch(topicCodeMap, query);
			// updates status to "TO_PROCESS"
			PersistenceController.updateStatus(StatusEnum.TO_PROCESS, query.getId());
			// updates queries searched
			queriesSearched++;
			// exits if limit is specified and reached
			if (QUERIES_TO_SEARCH != null && queriesSearched == QUERIES_TO_SEARCH) {
				return;
			}
			// fetches next unprocessed query
			query = PersistenceController.fetchNextQueryToProcess();
		}
	}

	/**
	 * Performs the main search functionality
	 * 
	 * @param topicsMap
	 *            , contains as key the topic id and as value the topic description
	 * @param query
	 *            , the query object to process
	 * 
	 * @throws Exception
	 */
	private static void doSearch(Map<String, String> topicsMap, Query query) throws Exception {
		// the query text
		String queryText = query.getText();
		// finds the most common topics in case the flag is enabled
		if (fetchMostCommon) {
			List<String> mostCommonTopicsList = findMostCommonTopics(query.getText());
			queryText = SearchUtils.constructQuery(mostCommonTopicsList, topicsMap);
		}
		// start count
		Date start = new Date();
		// fetches the results from ACM portal for the specified query
		List<AcmResult> acmResultList = CrawlUtils.fetchAcmResults(queryText, NUM_OF_RESULTS);
		if(acmResultList.size() == 0){
			PersistenceController.updateStatus(StatusEnum.COMPLETE, query.getId());
			throw new Exception("RESULTS NOT FOUND!!! EXITING...");
		}
		// prints the results
		 PrintUtils.printAcmResults(acmResultList);
		// for all ACM portal results extracts all information
		int limit = 10;
		int count = 0;
		
		// contains all visited publications
		List<PublicationInfo> visitedPublications = new ArrayList<PublicationInfo>();
		
		for (AcmResult a : acmResultList) {
			// sleeps for a while
			TimeUtils.randomSleep();
			// extracts all publication information
			PublicationInfo publicationInfo = CrawlUtils.extractAllPublicationInfo(a.getUrl());
			// sets the year of publication already fetched from result summary page
			publicationInfo.setYearOfPublication(a.getYearOfPublication());
			boolean isVisited = isPublicationEncountered(publicationInfo, visitedPublications);
			if(!isVisited){
				// prints all publication information
				printPublicationInfo(publicationInfo);
				// persists all publication information
				PersistenceController.savePublicationInfo(publicationInfo, query.getId(), count < limit, count + 1);
				visitedPublications.add(publicationInfo);
				count++;
			}
		}
		// end count
		Date end = new Date();
		// prints total execution time
		System.out.println("Total execution time : " + (end.getTime() - start.getTime()) / 1000 + " seconds");
	}
	
	private static boolean isPublicationEncountered(PublicationInfo publicationInfo, List<PublicationInfo> visited){
		for(PublicationInfo p : visited){
			if(p.getTitle().equals(publicationInfo.getTitle())){
				List<String> authList1 = p.getAuthors();
				List<String> authList2 = publicationInfo.getAuthors();
				if(authList1.size() != authList2.size()){
					return false;
				}
				for(String auth : authList1){
					if(!authList2.contains(auth)){
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds the most common topics for the specified query
	 */
	private static List<String> findMostCommonTopics(String query) throws Exception {
		return SearchUtils.findMostCommonTopicsForQuery(query, NUM_OF_RESULTS, NUM_OF_TOPICS);
	}

	/**
	 * Prints the ACM Results details
	 */
	private static void printDetails(List<AcmResult> acmResultList) throws Exception {
		System.out.println("--------------");
		System.out.println(" ACM RESULTS:");
		System.out.println("--------------");
		for (AcmResult a : acmResultList) {
			String resultUrl = a.getUrl();
			System.out.println("URL : " + resultUrl);
			PublicationInfo publicationInfo = CrawlUtils.extractAllPublicationInfo(resultUrl);
			System.out.println("TITLE: " + publicationInfo.getTitle());
			List<String> authors = publicationInfo.getAuthors();
			System.out.println("AUTHORS:");
			for (String author : authors) {
				System.out.println(" -> " + author);
			}
			System.out.println("TOPICS:");
			List<AcmTopic> l = CrawlUtils.extractTopicsFromPublication(resultUrl);
			for (AcmTopic acmTopic : l) {
				System.out.println(" -> " + acmTopic.getCode() + " - " + acmTopic.getDescription());
			}
			System.out.println("-----------");
		}
	}

	/**
	 * Prints all publication information details
	 */
	private static void printPublicationInfo(PublicationInfo publicationInfo) {
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("TITLE: " + publicationInfo.getTitle());
		System.out.println("URL: " + publicationInfo.getUrl());
		System.out.println("TITLE: " + publicationInfo.getTitle());
		System.out.println("AUTHORS: ");
		for (String author : publicationInfo.getAuthors()) {
			System.out.println(" -> " + author);
		}
		System.out.println("TOPICS: ");
		for (AcmTopic topic : publicationInfo.getTopics()) {
			System.out.println(" -> " + topic.getCode() + " - " + topic.getDescription());
		}
		System.out.println("NUMBER OF CITATIONS: " + publicationInfo.getNumOfCitations());
	}

}
