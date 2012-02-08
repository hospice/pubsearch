package ps.util;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ps.persistence.PersistenceController;

public class ExperimentResultsUtils {

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

	public static String getTwoDecimalDouble(Double d) {
		if (d == null) {
			d = 0.0;
		}
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(d);
	}

	public static void printMap(Map<Double, List<Integer>> m) {
		// prints the sorted map:
		Iterator<Double> iterator = m.keySet().iterator();
		while (iterator.hasNext()) {
			Double key = iterator.next();
			List<Integer> l = m.get(key);
			System.out.print("bucket # " + key + " has members: ");
			for (Integer i : l) {
				System.out.print(i + " ");
			}
			System.out.println();

		}
	}

}
