package ps.util;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import ps.algorithm.Depreciation;
import ps.algorithm.Sorter;
import ps.comparator.DescKeyComparator;
import ps.persistence.PersistenceController;
import ps.persistence.PersistenceController2;
import ps.struct.PublicationCitation;
import ps.struct.PublicationInfo;
import ps.struct.SearchResultWeight;

/**
 * Provides ranking functionality.
 */
public class RankUtils {

	/**
	 * Ranks the publication info lists based on the term frequency and depreciated citation distribution scores.s
	 */
	public static List<PublicationInfo> rankResults(Map<PublicationInfo, Double> citationDepreciationMap,
			Map<PublicationInfo, Double> termFrequencyMap, double tfBucketSize) {

		// 1. sorts the results based on their TF-value in descending order and groups them based on the TF bucket size
		TreeMap<PublicationInfo, Double> tfTreeMap = getSortedMapDesc(termFrequencyMap);
		Map<Integer, List<PublicationInfo>> bucketizedForTfMap = bucketize(tfTreeMap, tfBucketSize);

		// 2. sorts the grouped results based on their DCC-value in descending order
		Map<Integer, List<PublicationInfo>> bucketizedForTfAndDccMap = furtherBucketize(bucketizedForTfMap,
				citationDepreciationMap);

		// 3. returns the sorted map as list
		return convertMapToList(bucketizedForTfAndDccMap);
	}
	
	/**
	 * Converts the sorted map to a list and returns it
	 */
	private static List<PublicationInfo> convertMapToList(Map<Integer, List<PublicationInfo>> m){
		List<PublicationInfo> rankedList = new ArrayList<PublicationInfo>();
		Iterator<Integer> iter = m.keySet().iterator();
		while(iter.hasNext()){
			Integer bucket = iter.next();
			List<PublicationInfo> pubList = m.get(bucket);
			for(PublicationInfo p : pubList){
				rankedList.add(p);
			}
		}
		return rankedList;
	}
	
	/**
	 * Sorts the specified map based on the entry values in descending order (from highest to lowest).
	 */
	private static TreeMap<PublicationInfo, Double> getSortedMapDesc(Map<PublicationInfo, Double> m) {
		List<PublicationInfo> sortedList = new ArrayList<PublicationInfo>();
		ValueComparator bvc = new ValueComparator(m);
		TreeMap<PublicationInfo, Double> sortedMap = new TreeMap(bvc);
		sortedMap.putAll(m);
		return sortedMap;
	}
	
	/**
	 * Sorts the specified map based on the entry values in descending order (from highest to lowest).
	 */
	private static List<PublicationInfo> getSortedListDesc(Map<PublicationInfo, Double> m) {
		TreeMap<PublicationInfo, Double> sortedMap = getSortedMapDesc(m);
		List<PublicationInfo> sortedList = new ArrayList<PublicationInfo>();
		Iterator<PublicationInfo> it = sortedMap.keySet().iterator();
		while(it.hasNext()){
			PublicationInfo p = it.next();
			sortedList.add(p);
		}
		return sortedList;
	}
	
	/**
	 * Groups the specified map into buckets of the specified size.
	 */
	private static Map<Integer, List<PublicationInfo>> bucketize(TreeMap<PublicationInfo, Double> m, double bucketSize){
		Map<Integer, List<PublicationInfo>> bucketsMap = new HashMap<Integer, List<PublicationInfo>>();
		Iterator<PublicationInfo> it = m.keySet().iterator();
		while (it.hasNext()) {
			PublicationInfo pubInfo = it.next();
			Double value = m.get(pubInfo);
			int bucket = bucketForValue(value, bucketSize);
			List<PublicationInfo> l = bucketsMap.get(Integer.valueOf(bucket));
			if (l == null) {
				l = new ArrayList<PublicationInfo>();
			}
			l.add(pubInfo);
			bucketsMap.put(bucket, l);
		}
		return bucketsMap;
	}
	
	/**
	 * Groups the specified map by sorting the contents of each of the buckets based on their citation distribution score.
	 */
	private static Map<Integer, List<PublicationInfo>> furtherBucketize(Map<Integer, List<PublicationInfo>> m,
			Map<PublicationInfo, Double> citationDepreciationMap) {
		Map<Integer, List<PublicationInfo>> map = new HashMap<Integer, List<PublicationInfo>>();
		Iterator<Integer> it = m.keySet().iterator();
		while (it.hasNext()) {
			Integer bucket = it.next();
			List<PublicationInfo> pubList = m.get(bucket);
			List<PublicationInfo> sortedPubList = sortListBasedOnMapValues(pubList, citationDepreciationMap);
			map.put(bucket, sortedPubList);
		}
		return map;
	}
	
	/**
	 * Sorts the list based on the values containined in the specified map.
	 */
	private static List<PublicationInfo> sortListBasedOnMapValues(List<PublicationInfo> list, Map<PublicationInfo, Double> map){
		List<PublicationInfo> sortedList = new ArrayList<PublicationInfo>();
		Map<PublicationInfo, Double> newMap = new HashMap<PublicationInfo, Double>();
		for(PublicationInfo p : list){
			newMap.put(p, map.get(p));
		}
		TreeMap<PublicationInfo, Double> sortedMap = getSortedMapDesc(newMap);
		Iterator<PublicationInfo> it = sortedMap.keySet().iterator();
		while(it.hasNext()){
			PublicationInfo pubInfo = it.next();
			sortedList.add(pubInfo);
		}
		return sortedList;
	}
	
	/**
	 * Returns the bucket "position" that corresponding to the specified value based on the specified bucket size. 
	 */
	public static int bucketForValue(double value, double bucketSize) {
		int bucket = (int) (value / bucketSize);
		if (bucket == 0) {
			bucket = 1;
		} else if (value % bucketSize > 0) {
			bucket++;
		}
		return bucket;
	}
	
	/**
	 * Fills the provided results list and values map.
	 */
	public static void fillResultListAndValuesMap(int queryId, Map<Integer, Double> valuesMap,
			List<Integer> resultList, List<PublicationCitation> pubCitationsList, List<Integer> resultsWithIgnoreFlag)
			throws SQLException, ClassNotFoundException, IOException {
		for (PublicationCitation p : pubCitationsList) {
			int queryResultId = p.getResultId();
			resultList.add(queryResultId);
			if (!resultsWithIgnoreFlag.contains(queryResultId)) {
				double val = PersistenceController2.findTf(queryId, queryResultId);
				valuesMap.put(queryResultId, val);
			}
		}
	}
	
	public static void fillResultTFMap(int queryId, 
									   Map<Integer, Double> tfMap, 
									   List<Integer> resultList, 
									   List<Integer> resultsToIgnore)
					     			   throws SQLException, ClassNotFoundException, IOException{
		for(int resId : resultList){
			resultList.add(resId);
			if (!resultsToIgnore.contains(resId)) {
				double val = PersistenceController2.findTf(queryId, resId);
				tfMap.put(resId, val);
			}
		}
	}

	/**
	 * Performs "bucketization" based on the result's term frequency and sorts the map based on its key (term frequency)
	 * in descending order.
	 */
	public static Map<Double, List<Integer>> bucketizeAndSortForTF(boolean isRatioAccepted, List<Integer> resultList,
			double tfBucketRange, Map<Integer, Double> valuesMap, double bucketRange,
			List<Integer> resultsWithIgnoreFlag) {
		Map<Double, List<Integer>> bucketsMap = new HashMap<Double, List<Integer>>();
		if (isRatioAccepted) {
			bucketsMap = bucketizeForTF(resultList, bucketRange, valuesMap, resultsWithIgnoreFlag);
		} else {
			// if the ratio is not accepted, then this means we ignore the TF "bucketization" and we put all results
			// into 1 bucket
			bucketsMap.put(1.0, resultList);
		}
		return SortUtils.sortMapByKey(bucketsMap, new DescKeyComparator(bucketsMap));
	}
	
	/**
	 * Performs "bucketization" based on the result's term frequency and sorts the map based on its key (term frequency)
	 * in descending order.
	 */
	public static Map<Double, List<Integer>> bucketizeAndSortForTF2(boolean isRatioAccepted, List<Integer> resultList,
			 Map<Integer, Double> valuesMap, double bucketRange,
			List<Integer> resultsWithIgnoreFlag) {
		Map<Double, List<Integer>> bucketsMap = new HashMap<Double, List<Integer>>();
		if (isRatioAccepted) {
			bucketsMap = bucketizeForTF2(resultList, bucketRange, valuesMap, resultsWithIgnoreFlag);
		} else {
			// if the ratio is not accepted, then this means we ignore the TF "bucketization" and we put all results
			// into 1 bucket
			bucketsMap.put(1.0, resultList);
		}
		return SortUtils.sortMapByKey(bucketsMap, new DescKeyComparator(bucketsMap));
	}

	
	// FIXME: THIS IS A CLONE OF: bucketizeAndSortForTF METHOD 
	public static Map<Double, List<Integer>> bucketizeAndSortForTF2(int queryId,
																	int acceptanceThreshold,
																	List<Integer> resultList,
																	double tfBucketRange,
																	Map<Integer,
																	Double> valuesMap,
																	double bucketRange,
																	List<Integer> resultsWithIgnoreFlag)
																	throws ClassNotFoundException, SQLException, IOException {
		
		Map<Double, List<Integer>> bucketsMap = new HashMap<Double, List<Integer>>();
		boolean isRatioAccepted = RankUtils.foundNotFoundRatioAccepted(queryId, acceptanceThreshold);
		if (isRatioAccepted) {
			bucketsMap = bucketizeForTF(resultList, bucketRange, valuesMap, resultsWithIgnoreFlag);
		} else {
			// if the ratio is not accepted then we put all results into 1 bucket
			bucketsMap.put(1.0, resultList);
		}
		return SortUtils.sortMapByKey(bucketsMap, new DescKeyComparator(bucketsMap));
	}
	
	/**
	 * For each bucket in the provided map, performs a second level "bucketing" based on the publication's number of
	 * citations. For all results in the new buckets, sorts the results based on the calculated max. weighted clique
	 * score of each publication.
	 * 
	 * @param queryId
	 *            , the query id
	 * @param map
	 *            , the sorted, "bucketized" map by term frequency
	 * @param citBucketRange
	 *            , the citation bucket range
	 * @param annualDepreciationMap
	 *            , the annual depreciation map
	 * @param cliqueTopicsMap
	 *            , map containing all topics per clique
	 * @param pubCitList
	 *            , list of all publication-citations pairs
	 * 
	 * @throws Exception
	 */
	public static List<Integer> bucketizeForCitations(int queryId, Map<Double, List<Integer>> map,
			double citBucketRange, Map<Integer, Double> annualDepreciationMap,
			Map<Integer, List<Integer>> cliqueTopicsMap, List<PublicationCitation> pubCitList,
			double citationsBucketRange, boolean includeCliques) throws Exception {

		// list containing result-weight pairs
		List<SearchResultWeight> resultWeightList = new ArrayList<SearchResultWeight>();

		// list containing the ranked results
		List<Integer> rankedResultIds = new ArrayList<Integer>();

		// map containing as key the bucket and as value the list of publication ids belonging to the bucket
		Map<Double, Map<Double, List<Integer>>> bucketQueryResultsMap = new TreeMap<Double, Map<Double, List<Integer>>>();

		// fills map containing as key the bucket position and as value the results belonging to the bucket
		Map<Integer, Double> resultDeprecScoreMap = new HashMap<Integer, Double>();

		Map<Integer, Integer> citationCountMap = fillCitationCountMap(pubCitList);
		Map<Integer, Integer> yearOfPublicationMap = fillYearOfPublicationMap(pubCitList);

		fillBucketQueryResultsMap(citationCountMap, yearOfPublicationMap, map, bucketQueryResultsMap,
				annualDepreciationMap, resultDeprecScoreMap, citationsBucketRange);

		// sorts map according to key (bucket position) from highest to lowest value
		Map<Double, Map<Double, List<Integer>>> sortedBucketsQueryResultsMap = sortSuperMap(bucketQueryResultsMap);

		// traverses all results
		Iterator<Double> bucketsIter = sortedBucketsQueryResultsMap.keySet().iterator();
		
		// i. if we want to apply the max weighted clique bucketizing step:
		if (includeCliques) {
			while (bucketsIter.hasNext()) {
				Double bucket = bucketsIter.next();
				// ll publications in the same bucket
				Map<Double, List<Integer>> citMap = sortedBucketsQueryResultsMap.get(bucket);
				Iterator<Double> citIter = citMap.keySet().iterator();
				while (citIter.hasNext()) {
					Double d = citIter.next();
					List<Integer> pubs = citMap.get(d);
					for (Integer pubId : pubs) {
						List<Integer> topics = PersistenceController.getAllTopicsForPublication(pubId);
						double weight = topics.size() > 0 ? WeightCalcUtils
								.calcWeightForQueryResult(topics, cliqueTopicsMap) : 0;
						resultWeightList.add(new SearchResultWeight(pubId, weight));
					}
					List<SearchResultWeight> sortedSearchResultWeightList = Sorter.reverseSort(resultWeightList);
					for (SearchResultWeight s : sortedSearchResultWeightList) {
						rankedResultIds.add(s.getId());
					}
					resultWeightList = new ArrayList<SearchResultWeight>();
				}
			}
		}
		
		// ii. alternatively:
		else{ 
			while (bucketsIter.hasNext()) {
				Double bucket = bucketsIter.next();
				// all publications in the same bucket
				Map<Double, List<Integer>> citMap = sortedBucketsQueryResultsMap.get(bucket);
				Iterator<Double> citIter = citMap.keySet().iterator();
				while (citIter.hasNext()) {
					Double d = citIter.next();
					List<Integer> pubs = citMap.get(d);
					for (Integer pubId : pubs) {
						rankedResultIds.add(pubId);
					}
				}
			}
		}
		
		return rankedResultIds;
	}
	
	/**
	 * For each bucket in the provided map, performs a second level "bucketing" based on the publication's number of
	 * citations. For all results in the new buckets, sorts the results based on the calculated max. weighted clique
	 * score of each publication.
	 * 
	 * @param queryId
	 *            , the query id
	 * @param map
	 *            , the sorted, "bucketized" map by term frequency
	 * @param citBucketRange
	 *            , the citation bucket range
	 * @param annualDepreciationMap
	 *            , the annual depreciation map
	 * @param cliqueTopicsMap
	 *            , map containing all topics per clique
	 * @param pubCitList
	 *            , list of all publication-citations pairs
	 * 
	 * @throws Exception
	 */
	public static List<Integer> bucketizeForCitationsFinal(int queryId, Map<Double, List<Integer>> map,
			double citBucketRange, Map<Integer, Double> annualDepreciationMap,
			Map<Integer, List<Integer>> cliqueTopicsMap, List<PublicationCitation> pubCitList,
			double citationsBucketRange, boolean includeCliques) throws Exception {

		// list containing result-weight pairs
		List<SearchResultWeight> resultWeightList = new ArrayList<SearchResultWeight>();

		// list containing the ranked results
		List<Integer> rankedResultIds = new ArrayList<Integer>();

		// map containing as key the bucket and as value the list of publication ids belonging to the bucket
		Map<Double, Map<Double, List<Integer>>> bucketQueryResultsMap = new TreeMap<Double, Map<Double, List<Integer>>>();

		// fills map containing as key the bucket position and as value the results belonging to the bucket
		Map<Integer, Double> resultDeprecScoreMap = new HashMap<Integer, Double>();

		Map<Integer, Integer> citationCountMap = fillCitationCountMap(pubCitList);
		Map<Integer, Integer> yearOfPublicationMap = fillYearOfPublicationMap(pubCitList);

		fillBucketQueryResultsMap(citationCountMap, yearOfPublicationMap, map, bucketQueryResultsMap,
				annualDepreciationMap, resultDeprecScoreMap, citationsBucketRange);

		// sorts map according to key (bucket position) from highest to lowest value
		Map<Double, Map<Double, List<Integer>>> sortedBucketsQueryResultsMap = sortSuperMap(bucketQueryResultsMap);

		// traverses all results
		Iterator<Double> bucketsIter = sortedBucketsQueryResultsMap.keySet().iterator();
		
		// i. if we want to apply the max weighted clique bucketizing step:
		if (includeCliques) {
			while (bucketsIter.hasNext()) {
				Double bucket = bucketsIter.next();
				// all publications in the same bucket
				Map<Double, List<Integer>> citMap = sortedBucketsQueryResultsMap.get(bucket);
				Iterator<Double> citIter = citMap.keySet().iterator();
				while (citIter.hasNext()) {
					Double d = citIter.next();
					List<Integer> pubs = citMap.get(d);
					for (Integer pubId : pubs) {
						List<Integer> topics = PersistenceController.getAllTopicsForPublication(pubId);
						double weight = topics.size() > 0 ? WeightCalcUtils
								.calcWeightForQueryResult(topics, cliqueTopicsMap) : 0;
						resultWeightList.add(new SearchResultWeight(pubId, weight));
					}
					List<SearchResultWeight> sortedSearchResultWeightList = Sorter.reverseSort(resultWeightList);
					for (SearchResultWeight s : sortedSearchResultWeightList) {
						rankedResultIds.add(s.getId());
					}
					resultWeightList = new ArrayList<SearchResultWeight>();
				}
			}
		}
		
		// ii. alternatively:
		else{ 
			while (bucketsIter.hasNext()) {
				Double bucket = bucketsIter.next();
				// all publications in the same bucket
				Map<Double, List<Integer>> citMap = sortedBucketsQueryResultsMap.get(bucket);
				Iterator<Double> citIter = citMap.keySet().iterator();
				while (citIter.hasNext()) {
					Double d = citIter.next();
					List<Integer> pubs = citMap.get(d);
					for (Integer pubId : pubs) {
						rankedResultIds.add(pubId);
					}
				}
			}
		}
		
		return rankedResultIds;
	}

	/**
	 * Calculates and prints feedback.
	 */
	public static double[] calcAndPrintFeedback(int queryId, List<Integer> rankedResultIds, int totalResToDisp)
			throws SQLException, ClassNotFoundException, IOException {

		// PUB SEARCH RESULTS:
		int[] pubSearchFeedbacks = new int[totalResToDisp];
		int i = 0;
		for (Integer r : rankedResultIds) {
			pubSearchFeedbacks[i] = PersistenceController.fetchFeedbackForResultDef(r);
			i++;
		}
		double pubSearchRes = calcFeedbackScore(pubSearchFeedbacks, totalResToDisp);

		// ACM RESULTS:
		List<Integer> queryResultsDefault = PersistenceController.fetchQueryResultsDefault(queryId, totalResToDisp);
		int[] acmFeedbacks = new int[totalResToDisp];
		i = 0;
		for (Integer qrd : queryResultsDefault) {
			acmFeedbacks[i] = PersistenceController.fetchFeedbackForQueryResultDefault(qrd);
			i++;
		}
		double acmRes = calcFeedbackScore(acmFeedbacks, totalResToDisp);

		// Print the results:
		System.out.println("pubSearchRes = " + pubSearchRes);
		System.out.println("acmRes       = " + acmRes);

		// PersistenceController.saveFeedback3(queryId, acmRes, pubSearchRes, BUCKET_RANGE);
		double[] arr = new double[2];
		arr[0] = pubSearchRes;
		arr[1] = acmRes;
		return arr;
	}

	/**
	 * Groups results into buckets based on their term frequency.
	 * 
	 * @param resultList	, the list containing the results
	 * @param tfBucketRange	, the bucket range for the term frequency buckets
	 * @param valuesMap		, the map containing the values based on which "bucketing" will take places
	 */
	private static Map<Double, List<Integer>> bucketizeForTF(List<Integer> resultList, double tfBucketRange,
			Map<Integer, Double> valuesMap, List<Integer> resultsWithIgnoreFlag) {
		Map<Double, List<Integer>> bucketsMap = new TreeMap<Double, List<Integer>>();
		for (Integer resultId : resultList) {
			if (!resultsWithIgnoreFlag.contains(resultId)) {
				double tfVal = valuesMap.get(resultId);
				double bucket = RankUtils.bucketForVal(tfVal, tfBucketRange);
				List<Integer> queryResults = bucketsMap.get(bucket);
				if (queryResults == null) {
					queryResults = new ArrayList<Integer>();
				}
				queryResults.add(resultId);
				bucketsMap.put(bucket, queryResults);
			}
		}
		// for all results with ignore flag = "true" place them in random buckets
		for (Integer resultId : resultsWithIgnoreFlag) {
			int elem = RankUtils.calcRandomIndex(bucketsMap.size());
			double bucket = getIthBucket(elem, bucketsMap);
			List<Integer> queryResults = bucketsMap.get(bucket);
			queryResults.add(resultId);
			bucketsMap.put(bucket, queryResults);
		}
		return bucketsMap;
	}
	
	private static Map<Double, List<Integer>> bucketizeForTF2(List<Integer> resultList, double tfBucketRange,
			Map<Integer, Double> valuesMap, List<Integer> resultsWithIgnoreFlag) {
		Map<Double, List<Integer>> bucketsMap = new TreeMap<Double, List<Integer>>();
		for (Integer resultId : resultList) {
			if (!resultsWithIgnoreFlag.contains(resultId)) {
				double tfVal = valuesMap.get(resultId);
				double bucket = RankUtils.bucketForVal(tfVal, tfBucketRange);
				List<Integer> queryResults = bucketsMap.get(bucket);
				if (queryResults == null) {
					queryResults = new ArrayList<Integer>();
				}
				queryResults.add(resultId);
				bucketsMap.put(bucket, queryResults);
			}
		}
		// for all results with ignore flag = "true" place them in random buckets
		for (Integer resultId : resultsWithIgnoreFlag) {
			int elem = RankUtils.calcRandomIndex(bucketsMap.size());
			double bucket = getIthBucket(elem, bucketsMap);
			List<Integer> queryResults = bucketsMap.get(bucket);
			queryResults.add(resultId);
			bucketsMap.put(bucket, queryResults);
		}
		return SortUtils.sortMapBucketContents(bucketsMap, valuesMap);
	}

	/**
	 * Calculates the depreciated citation score.
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
	 * Returns bucket position for specific citation count.
	 */
	private static double bucketForCitationCount(double depreciatedCitationScore, double bucketSize) {
		int bucket = (int) (depreciatedCitationScore / bucketSize);
		if (bucket == 0) {
			bucket = 1;
		} else if (depreciatedCitationScore % bucketSize > 0) {
			bucket++;
		}
		return bucket;
	}

	/**
	 * Fills map containing as key the result id and as value the total number of citations.
	 */
	private static Map<Integer, Integer> fillCitationCountMap(List<PublicationCitation> pubCitList) {
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		for (PublicationCitation pc : pubCitList) {
			m.put(pc.getResultId(), pc.getCitationCount());
		}
		return m;
	}

	/**
	 * Fills map containing as key the result id and as value the year of publication.
	 */
	private static Map<Integer, Integer> fillYearOfPublicationMap(List<PublicationCitation> pubCitList) {
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		for (PublicationCitation pc : pubCitList) {
			m.put(pc.getResultId(), pc.getYearOfPublication());
		}
		return m;
	}

	/**
	 * Fills map containing as key the bucket position and as value the results belonging to the bucket.
	 */
	private static void fillBucketQueryResultsMap(Map<Integer, Integer> citationCountMap,
													Map<Integer, Integer> yearOfPublicationMap,
													Map<Double, List<Integer>> sortedMap,
			Map<Double, Map<Double, List<Integer>>> bucketQueryResultsMap, Map<Integer, Double> annualDepreciationMap,
			Map<Integer, Double> resultDeprecScoreMap, double bucketRange) {
		Iterator<Double> iter = sortedMap.keySet().iterator();
		while (iter.hasNext()) {
			Map<Double, List<Integer>> bucketMap = new TreeMap<Double, List<Integer>>();
			Double key = iter.next();
			List<Integer> results = sortedMap.get(key);
			for (Integer res : results) {
				Integer citationCount = citationCountMap.get(res);
				if (citationCount == null) {
					citationCount = 0;
				}
				double depreciatedCitationCount = calcDepreciatedCitationScore(citationCount, yearOfPublicationMap
						.get(res), annualDepreciationMap);
				resultDeprecScoreMap.put(res, depreciatedCitationCount);
				double bucket = bucketForCitationCount(depreciatedCitationCount, bucketRange);
				List<Integer> queryResults = bucketMap.get(bucket);
				if (queryResults == null) {
					queryResults = new ArrayList<Integer>();
				}
				queryResults.add(res);
				bucketMap.put(bucket, queryResults);
			}
			bucketQueryResultsMap.put(key, bucketMap);
		}
	}

	/**
	 * Sorts map according to key (bucket position) from highest to lowest value.
	 */
	private static Map<Double, Map<Double, List<Integer>>> sortSuperMap(Map<Double, Map<Double, List<Integer>>> m) {
		Map<Double, Map<Double, List<Integer>>> sortedMap = new LinkedHashMap<Double, Map<Double, List<Integer>>>();
		Iterator<Double> it = m.keySet().iterator();
		while (it.hasNext()) {
			Double key = it.next();
			sortedMap.put(key, sortMap(m.get(key)));
		}
		return sortedMap;
	}

	/**
	 * Sorts map based on key (bucket position) in descending order.
	 */
	private static Map<Double, List<Integer>> sortMap(Map<Double, List<Integer>> m) {
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

	/**
	 * Calculates the feedback score according to a purely lexicographic ordering, e.g. the score is = 2^(10-RANK), so
	 * for the first result the feedback score is multiplied by 2^9, second 2^8 etc...
	 */
	private static double calcFeedbackScore(int[] scores, int totalResToDisp) {
		double feedbackScore = 0;
		int count = 0;
		for (int i = 9; i >= 0; i--) {
			if (count < totalResToDisp) {
				feedbackScore += Math.pow(2, i) * scores[9 - i];
				count++;
			} else {
				return feedbackScore;
			}
		}
		return feedbackScore;
	}

	/**
	 * Calculates the annual depreciation for the specified number of years.
	 */
	public static Map<Integer, Double> annualDepreciation(int numOfYears, int currentYear) {
		Map<Integer, Double> m = new LinkedHashMap<Integer, Double>();
		for (int i = 0; i < numOfYears; i++) {
			int year = currentYear - i;
			m.put(year, Depreciation.calculate(year));
		}
		return m;
	}

	/**
	 * Returns bucket position for specific citation count.
	 */
	public static double bucketForVal(double val, double bucketSize) {
		int bucket = (int) (val / bucketSize);
		if (bucket == 0) {
			bucket = 1;
		} else if (val % bucketSize > 0) {
			bucket++;
		}
		return bucket;
	}

	/**
	 * Calculates a random index value from 0 to specified max value -1 (adjusted since this is going to be an index
	 * value).
	 */
	public static int calcRandomIndex(int max) {
		int random = 0;
		try {
			random = (new Random()).nextInt(max);
		} catch (Exception e) {
			System.err.println("Exception thrown for parameter max = " + max);
		}
		return random;
	}

	/**
	 * Calculates if the ratio of publications for each a PDF version of the publication is found is above or equal the
	 * specified threshold.
	 */
	public static boolean foundNotFoundRatioAccepted(int queryId, double threshold) throws ClassNotFoundException,
			SQLException, IOException {
		Integer resFound = PersistenceController2.calcIgnoreFlagsForQueryId(queryId, false);
		Integer resNotFound = PersistenceController2.calcIgnoreFlagsForQueryId(queryId, true);
		return resNotFound > 0 ? resFound / resNotFound >= threshold : true;
	}

	/**
	 * Returns the i'th bucket.
	 */
	private static double getIthBucket(int idx, Map<Double, List<Integer>> bucketsMap) {
		Iterator<Double> it = bucketsMap.keySet().iterator();
		int count = 0;
		Double bucket = null;
		while (count <= idx) {
			bucket = it.next();
			count++;
		}
		return bucket;
	}
	
	/**
	 * Fills topics map.
	 */
	public static Map<Integer, List<Integer>> fillTopicsMap() throws ClassNotFoundException, SQLException, IOException {
		long start = new Date().getTime();
		Map<Integer, List<Integer>> cliqueTopicsMap = PersistenceController.getAllTopicsPerClique();
		System.out.println("----------------");
		System.out.println(" = " + (new Date().getTime() - start) / 1000 + " secs");
		return cliqueTopicsMap;
	}

	/**
	 * Prints the experiment results.
	 */
	public static void printExperimentResults(List<Integer> rankingOrder, Map<Integer, Double> resWeightMap,
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

	/**
	 * Returns a string, two-decimal precision representation of the specified double. 
	 */
	private static String getTwoDecimalDouble(Double d) {
		if (d == null) {
			d = 0.0;
		}
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(d);
	}
	
}

class ValueComparator implements Comparator {

	Map base;
	
	public ValueComparator(Map base) {
		this.base = base;
	}

	public int compare(Object a, Object b) {
		if ((Double) base.get(a) < (Double) base.get(b)) {
			return 1;
		} else if ((Double) base.get(a) == (Double) base.get(b)) {
			return 0;
		} else {
			return -1;
		}
	}
}
