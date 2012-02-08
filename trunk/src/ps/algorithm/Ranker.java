package ps.algorithm;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ps.enumerators.StatusEnum;
import ps.persistence.PersistenceController;
import ps.struct.PublicationCitation;
import ps.struct.SearchResultWeight;
import ps.util.WeightCalcUtils;

public class Ranker {

	private final static int CURRENT_YEAR = 2011;
	private final static int NUM_OF_YEARS = 15;
	private static final int TOTAL_RESULTS_TO_DISPLAY = 10;
	private static final int POSITIONS = 2;
	private static final int NUM_EXECUTIONS = 1;

	public static void main(String[] args) throws Exception {
		double bucketSize = 10.0;
		processQueries(bucketSize);
	}

	/**
	 * Performs processing of all unprocessed queries unless a specific number of executions is specified
	 */
	public static void processQueries(double bucketRange) throws Exception {
		Integer numOfQueries = NUM_EXECUTIONS;
		// fills map containing as key the clique id and as values all topics belonging to the clique
		Map<Integer, List<Integer>> cliqueTopicsMap = fillTopicsMap();

		// fills the map containing the annual depreciation
		Map<Integer, Double> annualDepreciationMap = annualDepreciation();

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
				fillBucketQueryResultsMap(resultId, publicationCitation, bucketQueryResultsMap, annualDepreciationMap,
						publicationCitation.getYearOfPublication(), bucketRange, resultDeprecScoreMap);
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
					if (prevBucketPos - bucketPos >= POSITIONS) {
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
				if (count == TOTAL_RESULTS_TO_DISPLAY) {
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
					if (count == TOTAL_RESULTS_TO_DISPLAY) {
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

			// prints experiment results
			printExperimentResults(rankingOrder, resWeightMap, resultDeprecScoreMap, bucketRange, resultBucketMap);

			// returns if query limit is specified and reached
			if (numOfQueries != null && queriesProcessed == numOfQueries) {
				return;
			}

			// fetches the next query to process
			queryId = PersistenceController.fetchNextQueryIdToProcess();

		}
	}

	// /**
	// * Performs the ranking of the results corresponding to the query with the specified id. The process works as
	// * follows: <br/>
	// * The system fetches the search results corresponding to the specified query id. For each of the search results
	// the
	// * number of citations is calculated. <br/>
	// * <br/>
	// * The search results are grouped based on their number of citations with the following logic: <br/>
	// * For each number of citation we calculate the bucket it should be placed in. The bucket is determined based on
	// the
	// * global variable for the bucket size. So if the bucket size = 10, the bucket positions go like 1,11,21,31 and
	// each
	// * result is placed on the bucket corresponding to the result's citation count. <br/>
	// * <br/>
	// * The results are ranked in descending order based on the bucket position they are placed. Bucplaced in buckets
	// of
	// * highest values are promoted in the ranking order (since those are most cited, thus more influential). With the
	// in
	// * descending order
	// *
	// * @param queryId
	// */
	// public static void rankResults(int queryId) {
	// i. fetch all search results corresponding to the specified query
	// ii. for each of the search results find the total number of citations
	// iii. place the search results in buckets based on the citation count
	// iv. promote the results that are in high-valued buckets and for those in the same bucket order them according
	// to max weighted cliques
	// }
	// /**
	// * Returns bucket position for specific citation count
	// */
	// public static double bucketForCitationCount(int citationCount) {
	// long bucket = (long) (citationCount / BUCKET_SIZE);
	// if (bucket == 0) {
	// bucket = 1;
	// } else if (citationCount % BUCKET_SIZE > 0) {
	// bucket++;
	// }
	// return bucket;
	// }

	/**
	 * Compares citation count 1 with 2 by the threshold difference corresponding to position in the bucket array
	 * 
	 * @param citationCount1
	 * @param citationCount2
	 * 
	 * @return 1: if cc1 > cc2 by n 2: if cc2 < cc1 by n 0: otherwise
	 */
	public static int compareByN(int citationCount1, int citationCount2, double bucketSize) {
		int res = 0;
		if (citationCount1 == citationCount2) {
			return res;
		}
		double bucket1 = bucketForCitationCount(citationCount1, bucketSize);
		double bucket2 = bucketForCitationCount(citationCount2, bucketSize);
		if (citationCount1 > citationCount2) {
			if (bucket1 - bucket2 >= POSITIONS) {
				res = 1;
			}
		} else {
			if (bucket2 - bucket1 >= POSITIONS) {
				res = 1;
			}
		}
		return res;
	}

	/**
	 * Returns map with sorted keys representing the bucket position
	 */
	public static Map<Double, List<Integer>> sortMap(Map<Double, List<Integer>> m) {
		List<Double> bucketPosList = new ArrayList<Double>();
		Iterator<Double> it = m.keySet().iterator();
		while (it.hasNext()) {
			Double key = it.next();
			bucketPosList.add(key);
		}
		Map<Double, List<Integer>> sortedMap = new LinkedHashMap<Double, List<Integer>>();
		int pos = bucketPosList.size() - 1;
		for (Double i : bucketPosList) {
			Double key = bucketPosList.get(pos);
			List<Integer> value = m.get(key);
			sortedMap.put(key, value);
			pos--;
		}
		return sortedMap;
	}

	private static void fillBucketQueryResultsMap(int resultId, PublicationCitation publicationCitation,
			Map<Double, List<Integer>> bucketQueryResultsMap, Map<Integer, Double> annualDepreciationMap,
			int yearOfPublication, double bucketSize, Map<Integer, Double> resultDeprecScoreMap) {
		Integer citationCount = publicationCitation.getCitationCount();
		if (citationCount == null) {
			citationCount = 0;
		}
		double depreciatedCitationCount = calcDepreciatedCitationScore(citationCount, yearOfPublication,
				annualDepreciationMap);

		System.out.println("RESULT : " + resultId + " has DEPRECIATED CITATIONS SCORE = " + depreciatedCitationCount);
		resultDeprecScoreMap.put(resultId, depreciatedCitationCount);
		double bucket = Ranker.bucketForCitationCount(depreciatedCitationCount, bucketSize);
		List<Integer> queryResults = bucketQueryResultsMap.get(bucket);
		if (queryResults == null) {
			queryResults = new ArrayList<Integer>();
		}
		queryResults.add(resultId);
		bucketQueryResultsMap.put(bucket, queryResults);
	}

	/**
	 * Returns bucket position for specific citation count
	 */
	public static double bucketForCitationCount(double depreciatedCitationScore, double bucketSize) {
		int bucket = (int) (depreciatedCitationScore / bucketSize);
		if (bucket == 0) {
			bucket = 1;
		} else if (depreciatedCitationScore % bucketSize > 0) {
			bucket++;
		}
		return bucket;
	}

	/**
	 * Calculates the depreciated citation score
	 */
	public static Double calcDepreciatedCitationScore(int citations, int yearOfPublication,
			Map<Integer, Double> annualDepreciationMap) {
		Double d = annualDepreciationMap.get(yearOfPublication);
		if (d == null) {
			return 0.0;
		}
		return d * citations;
	}

	/**
	 * Calculates the annual depreciation for the specified number of years
	 */
	public static Map<Integer, Double> annualDepreciation() {
		Map<Integer, Double> m = new LinkedHashMap<Integer, Double>();
		for (int i = 0; i < NUM_OF_YEARS; i++) {
			int year = CURRENT_YEAR - i;
			m.put(year, Depreciation.calculate(year));
		}
		return m;
	}

	private static Map<Integer, List<Integer>> fillTopicsMap() throws ClassNotFoundException, SQLException, IOException {
		long start = new Date().getTime();
		Map<Integer, List<Integer>> cliqueTopicsMap = PersistenceController.getAllTopicsPerClique();
		System.out.println("----------------");
		System.out.println(" = " + (new Date().getTime() - start) / 1000 + " secs");
		return cliqueTopicsMap;
	}

//	private static void printMap(Map<Double, List<Integer>> m) {
//		// prints the sorted map:
//		Iterator<Double> iterator = m.keySet().iterator();
//		while (iterator.hasNext()) {
//			Double key = iterator.next();
//			List<Integer> l = m.get(key);
//			System.out.print("bucket # " + key + " has members: ");
//			for (Integer i : l) {
//				System.out.print(i + " ");
//			}
//			System.out.println();
//
//		}
//	}

	// Search Result "Depreciated Score" Max. Weigted Cliques Score "Bucket Range" Bucket

	private static void printExperimentResults(List<Integer> rankingOrder, Map<Integer, Double> resWeightMap,
			Map<Integer, Double> resultDeprecScoreMap, double bucketRange, Map<Integer, Integer> resultBucketMap)
			throws SQLException, ClassNotFoundException, IOException {

		int count = 0;
		System.out.println("*****************************************");
		System.out.println("***         EXPERIMENT RESULTS        ***");
		System.out.println("*****************************************");
		for (Integer queryResultId : rankingOrder) {
			String searchResTitle = PersistenceController.fetchQueryResultTitleFromId(queryResultId);
			Double depreciatedScore = resultDeprecScoreMap.get(queryResultId);
			Double maxWeightedCliqueWeight = resWeightMap.get(queryResultId);
			Integer bucket = resultBucketMap.get(queryResultId);
			System.out.println(++count + "\t" + searchResTitle + "\t" + getTwoDecimalDouble(depreciatedScore) + "\t"
					+ getTwoDecimalDouble(maxWeightedCliqueWeight) + "\t" + bucketRange + "\t" + bucket);
		}
		System.out.println();
		System.out.println("*********************************************************");
	}

	private static String getTwoDecimalDouble(Double d) {
		if (d == null) {
			d = 0.0;
		}
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(d);
	}

}
