package ps.util;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ibm.icu.util.Calendar;

import ps.algorithm.Depreciation;
import ps.algorithm.Ranker;
import ps.constants.AppConstants;
import ps.persistence.PersistenceController;
import ps.struct.PublicationCitation;

/**
 * Provides a set of application utilities.
 */
public class AppUtils {

	/**
	 * Compares citation count 1 with 2 by the threshold difference corresponding to position in the bucket array and
	 * return:
	 * <ul>
	 * 		<li>	1: if (cc1 > cc2 by n)	</li>
	 * 		<li>	2: if (cc2 < cc1 by n)	</li>
	 * 		<li>	0: otherwise			</li>
	 * </ul>
	 */
	public static int compareByN(int cc1, int cc2, double bucketSize) {
		int res = 0;
		if (cc1 == cc2) {
			return res;
		}
		// fetches the bucket positions based on the size of the bucket
		double bucket1 = bucketForCitationCount(cc1, bucketSize);
		double bucket2 = bucketForCitationCount(cc2, bucketSize);
		if (cc1 > cc2) {
			if (bucket1 - bucket2 >= AppConstants.POSITIONS) {
				res = 1;
			}
		} else {
			if (bucket2 - bucket1 >= AppConstants.POSITIONS) {
				res = 1;
			}
		}
		return res;
	}

	/**
	 * Returns map with sorted keys representing the bucket position.
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

	/**
	 * Fills the bucket query results map.
	 */
	public static void fillBucketQueryResultsMap(int resultId, 
												 PublicationCitation publicationCitation,
												 Map<Double, List<Integer>> bucketQueryResultsMap, 
												 Map<Integer, Double> annualDepreciationMap,
												 int yearOfPublication, 
												 double bucketSize, 
												 Map<Integer, Double> resultDeprecScoreMap) {
		Integer citationCount = publicationCitation.getCitationCount();
		if (citationCount == null) {
			citationCount = 0;
		}
		double depreciatedCitationCount = calcDepreciatedCitationScore(citationCount, yearOfPublication, annualDepreciationMap);
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
		for (int i = 0; i < AppConstants.NUM_OF_YEARS; i++) {
			int currYear = Calendar.getInstance().get(Calendar.YEAR);
			int year = currYear - i;
			m.put(year, Depreciation.calculate(year));
		}
		return m;
	}

	public static Map<Integer, List<Integer>> fillTopicsMap() throws ClassNotFoundException, SQLException, IOException {
		long start = new Date().getTime();
		Map<Integer, List<Integer>> cliqueTopicsMap = PersistenceController.getAllTopicsPerClique();
		System.out.println("----------------");
		System.out.println(" = " + (new Date().getTime() - start) / 1000 + " secs");
		return cliqueTopicsMap;
	}

	/**
	 * Returns a string representation of a double with the specified precision.
	 */
	public static String getSpecPrecision(Double d, int p) {
		if (d == null) {
			d = 0.0;
		}
		String pattern = "#.#";
		if (p > 0) {
			for (int i = 1; i < p; i++) {
				pattern += "#";
			}
		}
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(d);
	}

}
