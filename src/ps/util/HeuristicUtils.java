package ps.util;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ps.persistence.PersistenceController;
import ps.persistence.PersistenceController2;
import ps.struct.HeuristicData;

/**
 * Provides heuristic utilities.
 */
public class HeuristicUtils {

	// the threshold for the ratio found/not found for processed publications
	private static final int ACCEPTANCE_THRESHOLD = 2;
	// the number of years (required to calculate the annual depreciation)
	private static final int NUM_OF_YEARS = 15;

	/**
	 * Increments the heuristic level value.
	 * 
	 * @param r
	 *            , the result structure
	 */
	public static final void updateHeuristicLevel(HeuristicData d) {
		// UPDATES CURRENT LEVEL
		int level = d.getLevel();
		level = level++;
		d.setLevel(level);
	}

	/**
	 * Applies the term frequency heuristic.
	 */
	public static final void applyTf(HeuristicData d, int queryId) throws Exception {
		// ----------------
		// I. FIRST LEVEL:
		// ----------------
		if (d.getLevel() == 1) {
			Map<Double, List<Integer>> sortedMap = bucketizeListForTF(d.getInitialList(), d.getTfBucketRange(), queryId);
			d.setFirstLevelOutMap(sortedMap);
		}
		// ------------------
		// II. SECOND LEVEL:
		// ------------------
		else if (d.getLevel() == 2) {
			Map<Double, List<Integer>> input = d.getFirstLevelOutMap();
			Iterator<Double> l1Iter = input.keySet().iterator();
			Map<Double, Map<Double, List<Integer>>> sortedMap = new LinkedHashMap<Double, Map<Double, List<Integer>>>();
			while (l1Iter.hasNext()) {
				Double l1Bucket = l1Iter.next();
				List<Integer> bucketContents = input.get(l1Bucket);
				Map<Double, List<Integer>> bucketMap = bucketizeListForTF(bucketContents, d.getTfBucketRange(), queryId);
				sortedMap.put(l1Bucket, bucketMap);
			}
			d.setSecondLevelOutMap(sortedMap);
		}
		// ------------------
		// III. THIRD LEVEL:
		// ------------------
		else if (d.getLevel() == 3) {
			Map<Double, Map<Double, List<Integer>>> input = d.getSecondLevelOutMap();
			Iterator<Double> l1Iter = input.keySet().iterator();
			List<Integer> finalRanking = new ArrayList<Integer>();
			while (l1Iter.hasNext()) {
				Double l1Bucket = l1Iter.next();
				Map<Double, List<Integer>> l2Map = input.get(l1Bucket);
				Iterator<Double> l2Iter = l2Map.keySet().iterator();
				while (l2Iter.hasNext()) {
					Double l2Bucket = l2Iter.next();
					List<Integer> bucketContents = l2Map.get(l2Bucket);
					Map<Double, List<Integer>> bucketMap = bucketizeListForTF(bucketContents, d.getTfBucketRange(),
							queryId);
					fillListFromMap(finalRanking, bucketMap);
				}
			}
			d.setFinalList(finalRanking);
		} else {
			throw new Exception("Invalid heuristic level specified!");
		}
	}

	/**
	 * Applies the depreciated citation count heuristic.
	 * 
	 * @throws Exception
	 */
	public static final void applyDcc(HeuristicData d, int queryId) throws Exception {
		Map<Integer, Double> annualDepreciationMap = RankUtils.annualDepreciation(NUM_OF_YEARS, Calendar.getInstance()
				.get(Calendar.YEAR));
		// ----------------
		// IF FIRST LEVEL:
		// ----------------
		if (d.getLevel() == 1) {
			List<Integer> input = d.getInitialList();
			Map<Double, List<Integer>> sortedMap = bucketizeListForDCC(input, annualDepreciationMap,
					d.getDccBucketRange());
			d.setFirstLevelOutMap(sortedMap);
		}
		// -----------------
		// IF SECOND LEVEL:
		// -----------------
		else if (d.getLevel() == 2) {
			Map<Double, List<Integer>> input = d.getFirstLevelOutMap();
			Iterator<Double> l1Iter = input.keySet().iterator();
			Map<Double, Map<Double, List<Integer>>> sortedMap = new LinkedHashMap<Double, Map<Double, List<Integer>>>();
			while (l1Iter.hasNext()) {
				Double l1Bucket = l1Iter.next();
				List<Integer> bucketContents = input.get(l1Bucket);
				Map<Double, List<Integer>> bucketMap = bucketizeListForDCC(bucketContents, annualDepreciationMap,
						d.getDccBucketRange());
				sortedMap.put(l1Bucket, bucketMap);
			}
			d.setSecondLevelOutMap(sortedMap);
		}
		// ----------------
		// IF THIRD LEVEL:
		// ----------------
		else if (d.getLevel() == 3) {
			Map<Double, Map<Double, List<Integer>>> input = d.getSecondLevelOutMap();
			Iterator<Double> l1Iter = input.keySet().iterator();
			List<Integer> finalRanking = new ArrayList<Integer>();
			while (l1Iter.hasNext()) {
				Double l1Bucket = l1Iter.next();
				Map<Double, List<Integer>> l2Map = input.get(l1Bucket);
				Iterator<Double> l2Iter = l2Map.keySet().iterator();
				while (l2Iter.hasNext()) {
					Double l2Bucket = l2Iter.next();
					List<Integer> bucketContents = l2Map.get(l2Bucket);
					Map<Double, List<Integer>> bucketMap = bucketizeListForDCC(bucketContents, annualDepreciationMap,
							d.getDccBucketRange());
					fillListFromMap(finalRanking, bucketMap);
				}
			}
			d.setFinalList(finalRanking);
		} else {
			throw new Exception("Invalid heuristic level specified!");
		}
	}

	/**
	 * Applies the max. weighted cliques heuristic.
	 * 
	 * @throws Exception
	 */
	public static final void applyCliques(HeuristicData d, int queryId, Map<Integer, List<Integer>> cliqueTopicsMap)
			throws Exception {
		// ----------------
		// IF FIRST LEVEL:
		// ----------------
		if (d.getLevel() == 1) {
			List<Integer> input = d.getInitialList();
			bucketizeListForCliques(input, cliqueTopicsMap, d.getCliquesRange());
		}
		// -----------------
		// IF SECOND LEVEL:
		// -----------------
		else if (d.getLevel() == 2) {
			Map<Double, List<Integer>> input = d.getFirstLevelOutMap();
			Iterator<Double> l1Iter = input.keySet().iterator();
			Map<Double, Map<Double, List<Integer>>> sortedMap = new LinkedHashMap<Double, Map<Double, List<Integer>>>();
			while (l1Iter.hasNext()) {
				Double l1Bucket = l1Iter.next();
				List<Integer> bucketContents = input.get(l1Bucket);
				Map<Double, List<Integer>> bucketMap = bucketizeListForCliques(bucketContents, cliqueTopicsMap,
						d.getCliquesRange());
				sortedMap.put(l1Bucket, bucketMap);
			}
			d.setSecondLevelOutMap(sortedMap);
		}
		// ----------------
		// IF THIRD LEVEL:
		// ----------------
		else if (d.getLevel() == 3) {
			Map<Double, Map<Double, List<Integer>>> input = d.getSecondLevelOutMap();
			Iterator<Double> l1Iter = input.keySet().iterator();
			List<Integer> finalRanking = new ArrayList<Integer>();
			while (l1Iter.hasNext()) {
				Double l1Bucket = l1Iter.next();
				Map<Double, List<Integer>> l2Map = input.get(l1Bucket);
				Iterator<Double> l2Iter = l2Map.keySet().iterator();
				while (l2Iter.hasNext()) {
					Double l2Bucket = l2Iter.next();
					List<Integer> bucketContents = l2Map.get(l2Bucket);
					Map<Double, List<Integer>> bucketMap = bucketizeListForCliques(bucketContents, cliqueTopicsMap,
							d.getCliquesRange());
					fillListFromMap(finalRanking, bucketMap);
				}
			}
			d.setFinalList(finalRanking);
		} else {
			throw new Exception("Invalid heuristic level specified!");
		}
	}

	/**
	 * Splits the specified list into buckets of specific range.
	 * 
	 * @param input
	 * @param bucketRange
	 * @param queryId
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	private static Map<Double, List<Integer>> bucketizeListForTF(List<Integer> input, double bucketRange, int queryId)
			throws ClassNotFoundException, SQLException, IOException {
		Map<Integer, Double> tfMap = new LinkedHashMap<Integer, Double>();
		List<Integer> resultsToIgnore = PersistenceController2.fetchAllResultsToIgnore(queryId, true);
		RankUtils.fillResultTFMap(queryId, tfMap, input, resultsToIgnore);
		boolean isRatioAccepted = RankUtils.foundNotFoundRatioAccepted(queryId, ACCEPTANCE_THRESHOLD);
		Map<Double, List<Integer>> sortedMap = RankUtils.bucketizeAndSortForTF2(isRatioAccepted, input, tfMap,
				bucketRange, resultsToIgnore);
		return sortedMap;
	}

	/**
	 * Fills the specified list with the contents of the map.
	 * 
	 * @param bucketContents
	 * @param m
	 */
	private static void fillListFromMap(List<Integer> bucketContents, Map<Double, List<Integer>> m) {
		Iterator<Double> iter = m.keySet().iterator();
		while (iter.hasNext()) {
			Double key = iter.next();
			bucketContents.addAll(m.get(key));
		}
	}

	/**
	 * Calculates the DCC for all specified query results
	 * 
	 * @param queryResIdList
	 *            , the list containing all query results
	 * @param annualDepreciationMap
	 *            , the map containing all annual depreciation ratios
	 * @param dccBucketRange
	 *            , the bucket range for the DCC heuristic
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	private static Map<Double, List<Integer>> bucketizeListForDCC(List<Integer> queryResIdList,
			Map<Integer, Double> annualDepreciationMap, double dccBucketRange) throws ClassNotFoundException,
			SQLException, IOException {
		Map<Double, List<Integer>> dccMap = new HashMap<Double, List<Integer>>();
		for (Integer queryResId : queryResIdList) {
			// the DCC score calculated for the current result
			double dccScore = calcDcc(queryResId, annualDepreciationMap);
			// finds the bucket that corresponds to the current DCC score
			double bucket = bucketForScore(dccScore, dccBucketRange);
			// fetches all bucket contents
			List<Integer> queryResults = dccMap.get(bucket);
			if (queryResults == null) {
				queryResults = new ArrayList<Integer>();
			}
			// adds to query result to bucket and updates map
			queryResults.add(queryResId);
			dccMap.put(bucket, queryResults);
		}
		return SortUtils.sortMapBucketContents(dccMap, annualDepreciationMap);
	}

	/**
	 * Updated implementation for calculating the depreciated citation count (dcc) that takes into consideration the
	 * annual citation count of the specified publication (query result) and depreciates the citation count based on the
	 * publication date of the citing paper.
	 * 
	 * @param queryResId
	 *            , the id of the specific publication (query result)
	 * @param annualDepreciationMap
	 *            , the annual depreciation map
	 * @return the total depreciation citation count score corresponding to the individual annual dcc scores
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	private static double calcDcc(Integer queryResId, Map<Integer, Double> annualDepreciationMap)
			throws ClassNotFoundException, SQLException, IOException {
		Double dccScoreTotal = 0.0;
		Map<Integer, Integer> citationDistMap = PersistenceController2.fetchCitationDistForQueryRes(queryResId);
		Iterator<Integer> it = citationDistMap.keySet().iterator();
		while (it.hasNext()) {
			Integer year = it.next();
			Integer annualCitationCount = citationDistMap.get(year);
			Double depreciationPercent = annualDepreciationMap.get(year);
			Double annualDccScore = depreciationPercent * annualCitationCount;
			dccScoreTotal += annualDccScore;
		}
		return dccScoreTotal;
	}

	/**
	 * Returns bucket position for specific citation count.
	 * 
	 * @param depreciatedCitationScore
	 *            , the depreciated citation score
	 * @param bucketSize
	 *            , the bucket size
	 * @return
	 */
	private static double bucketForScore(double score, double bucketSize) {
		int bucket = (int) (score / bucketSize);
		if (bucket == 0) {
			bucket = 1;
		} else if (score % bucketSize > 0) {
			bucket++;
		}
		return bucket;
	}

//	/**
//	 * Sorts map based on key (bucket position) in descending order.
//	 * 
//	 * @param m
//	 *            , the map to sort
//	 * @return the sorted map
//	 */
//	private static Map<Double, List<Integer>> sortMap(Map<Double, List<Integer>> m) {
//		List<Double> bucketPosList = new ArrayList<Double>();
//		Iterator<Double> it = m.keySet().iterator();
//		while (it.hasNext()) {
//			Double key = it.next();
//			bucketPosList.add(key);
//		}
//		Map<Double, List<Integer>> sortedMap = new LinkedHashMap<Double, List<Integer>>();
//		int pos = bucketPosList.size() - 1;
//		for (Double i : bucketPosList) {
//			Double key = bucketPosList.get(pos);
//			List<Integer> value = m.get(key);
//			sortedMap.put(key, value);
//			pos--;
//		}
//		return sortedMap;
//	}

	/**
	 * Splits the specified query result list into buckets of specified range.
	 * 
	 * @param queryResIdList
	 *            , the query result list
	 * @param cliqueTopicsMap
	 *            , the clique topics map
	 * @param cliqueBucketRange
	 *            , the clique bucket range
	 * @return
	 * @throws Exception
	 */
	public static Map<Double, List<Integer>> bucketizeListForCliques(List<Integer> queryResIdList,
			Map<Integer, List<Integer>> cliqueTopicsMap, double cliqueBucketRange) throws Exception {
		Map<Double, List<Integer>> cliquesMap = new HashMap<Double, List<Integer>>();
		Map<Integer, Double>valuesMap = new HashMap<Integer, Double>();
		for (Integer queryResId : queryResIdList) {
			List<Integer> topics = PersistenceController.getAllTopicsForPublication(queryResId);
			double weight = topics.size() > 0 ? WeightCalcUtils.calcWeightForQueryResult(topics, cliqueTopicsMap) : 0;
			valuesMap.put(queryResId, weight);
			// finds the bucket that corresponds to the current DCC score
			double bucket = bucketForScore(weight, cliqueBucketRange);
			// fetches all bucket contents
			List<Integer> queryResults = cliquesMap.get(bucket);
			if (queryResults == null) {
				queryResults = new ArrayList<Integer>();
			}
			// adds to query result to bucket and updates map
			queryResults.add(queryResId);
			cliquesMap.put(bucket, queryResults);
		}
		return SortUtils.sortMapBucketContents(cliquesMap, valuesMap);
	}
	
}
