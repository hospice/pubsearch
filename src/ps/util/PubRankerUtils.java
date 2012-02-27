package ps.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ps.algorithm.Ranker;
import ps.algorithm.Sorter;
import ps.constants.AppConstants;
import ps.enumerators.StatusEnum;
import ps.persistence.PersistenceController;
import ps.struct.PublicationCitation;
import ps.struct.SearchResultWeight;

public class PubRankerUtils {
	/**
	 * Performs processing of all unprocessed queries unless a specific number of executions is specified
	 */
	public static void processQueries(double bucketRange) throws Exception {
		Integer numOfQueries = AppConstants.NUM_EXECUTIONS;
		// fills map containing as key the clique id and as values all topics belonging to the clique
		Map<Integer, List<Integer>> cliqueTopicsMap = AppUtils.fillTopicsMap();

		// fills the map containing the annual depreciation
		Map<Integer, Double> annualDepreciationMap = AppUtils.annualDepreciation();

		// counter of all processed queries
		int queriesProcessed = 0;

		// map with all bucket positions and all query results id belonging to the bucket
		Map<Double, List<Integer>> bucketQueryResultsMap = new TreeMap<Double, List<Integer>>();

		Map<Integer, Double> resultDeprecScoreMap = new HashMap<Integer, Double>();

		// fetches the next unprocessed query
		Integer queryId = PersistenceController.fetchNextQueryIdToProcess();
		System.out.println("Processing Query with ID: " + queryId);

		// while more unprocessed queries exist
		while (queryId != null) {

			// fetches all query result information for the specific query
			List<PublicationCitation> queryResults = PersistenceController.getAllQueryResultsInfoForQuery(queryId);

			for (PublicationCitation publicationCitation : queryResults) {
				int resultId = publicationCitation.getResultId();

				// double queryResultWeight = WeightCalc.calcWeightForQueryResult(resultId, cliqueTopicsMap);

				// // adds to map containing the query result as key and the max. weighted clique weight as value
				// queryResultsWeightMap.put(resultId, queryResultWeight);

				// adds to map containing the bucket position as key and the list of query results belonging to the
				// bucket as value
				AppUtils.fillBucketQueryResultsMap(resultId, publicationCitation, bucketQueryResultsMap,
						annualDepreciationMap, publicationCitation.getYearOfPublication(), bucketRange,
						resultDeprecScoreMap);
			}

			// now map is sorted according to key (bucket position) from highest to lowest value
			Map<Double, List<Integer>> sortedBucketsResultsMap = Ranker.sortMap(bucketQueryResultsMap);
			Map<Double, List<Integer>> resultsMap = new LinkedHashMap<Double, List<Integer>>();

			Double prevBucketPos = null;
			int iterCount = 0;

			// In this part we are sorting the result list according to the difference in the bucket positions (the keys
			// of the sortedBucketsResultsMap), if the difference among the previous and next bucket is less than the
			// specified threshold then the results of the two buckets are merged and the max weighted clique weights
			// are the ranking criterion. If the difference is higher than the threshold then the results of the bucket
			// with the min position (max value) are promoted
			Iterator<Double> sortedBucketsResultsIter = sortedBucketsResultsMap.keySet().iterator();
			while (sortedBucketsResultsIter.hasNext()) {
				double bucketPos = sortedBucketsResultsIter.next();

				// this case will apply for the first time only
				if (iterCount == 0) {
					resultsMap.put(bucketPos, sortedBucketsResultsMap.get(bucketPos));
					// the previous bucket position is kept for next iteration
					prevBucketPos = bucketPos;
				} else {
					// if the delta is higher than the specified threshold promote results
					if (prevBucketPos - bucketPos >= AppConstants.POSITIONS) {
						resultsMap.put(bucketPos, sortedBucketsResultsMap.get(bucketPos));
						// the previous bucket position is kept for next iteration
						prevBucketPos = bucketPos;
					}

					// alternatively add results to existing list
					else {
						// the results already in the map from before
						List<Integer> previousResultList = resultsMap.get(prevBucketPos);

						// the new results to add
						List<Integer> newResultsList = sortedBucketsResultsMap.get(bucketPos);

						// adds results to existing list
						for (Integer newRes : newResultsList) {
							previousResultList.add(newRes);
						}
						// updates map and result list
						resultsMap.put(prevBucketPos, previousResultList);
					}
				}

				// updates counter
				iterCount++;
			}

			// the list that maintains the ranking order of all search results id
			List<Integer> rankingOrder = new ArrayList<Integer>();

			List<SearchResultWeight> searchResultWeightList = new ArrayList<SearchResultWeight>();
			Iterator<Double> batchResultsIter = resultsMap.keySet().iterator();

			Map<Integer, Double> resWeightMap = new HashMap<Integer, Double>();

			int count = 0;

			int bucketIdx = 0;

			Map<Integer, Integer> resultBucketMap = new HashMap<Integer, Integer>();

			while (batchResultsIter.hasNext()) {
				bucketIdx++;
				if (count == AppConstants.TOTAL_RESULTS_TO_DISPLAY) {
					break;
				}
				Double batch = batchResultsIter.next();
				List<Integer> l = resultsMap.get(batch);
				for (Integer queryResultId : l) {

					resultBucketMap.put(queryResultId, bucketIdx);

					// fetches all topics for publication
					List<Integer> resultTopics = PersistenceController.getAllTopicsForPublication(queryResultId);
					double weight = 0;
					if (resultTopics.size() > 0) {
						weight = WeightCalcUtils.calcWeightForQueryResult(resultTopics, cliqueTopicsMap);
						resWeightMap.put(queryResultId, weight);
					}
					searchResultWeightList.add(new SearchResultWeight(queryResultId, weight));
				}
				// sorts all query results belonging to the batch according to their weight value
				List<SearchResultWeight> sortedSearchResultWeightList = Sorter.reverseSort(searchResultWeightList);
				for (SearchResultWeight s : sortedSearchResultWeightList) {
					if (count == AppConstants.TOTAL_RESULTS_TO_DISPLAY) {
						break;
					}
					// places result in ranking order
					rankingOrder.add(s.getId());
					count++;
				}
				searchResultWeightList = new ArrayList<SearchResultWeight>();
			}

			// updates ranking order
			PersistenceController.updateRanking(rankingOrder);

			// update status to PROCESSED
			PersistenceController.updateStatus(StatusEnum.COMPLETE, queryId);

			// updates counter
			queriesProcessed++;

			// returns if query limit is specified and reached
			if (numOfQueries != null && queriesProcessed == numOfQueries) {
				return;
			}

			// fetches the next query to process
			queryId = PersistenceController.fetchNextQueryIdToProcess();

		}
	}
}
