package ps.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ps.app2.AppUtils;
import ps.persistence.PersistenceController;
import ps.struct.AcmResult;
import ps.struct.Graph;
import ps.struct.Link;
import ps.struct.PublicationData;
import ps.struct.PublicationInfo;

/**
 * Provides print-related utilities.
 */
public class PrintUtils {

	/**
	 * Prints all publication data.
	 */
	public static void printPublicationData(PublicationData pd) {

		System.out.println("PRINTING DATA FOR PUBLICATION WITH TITLE = " + pd.getTitle());

		System.out.println("ABSTRACT");
		System.out.println("***************************");
		System.out.println(pd.getAbstractText());
		System.out.println("\n\n\n");

		// System.out.println("KEYWORDS");
		// System.out.println("***************************");
		// String[] keywords = pd.getKeywordsArr();
		// for (int i = 0; i < keywords.length; i++) {
		// System.out.print(keywords[i]);
		// if (i < keywords.length - 1) {
		// System.out.print(", ");
		// }
		// }
		// System.out.println("\n\n\n");

		System.out.println("BODY");
		System.out.println("***************************");
		System.out.println(pd.getBody());
		System.out.println("\n\n\n");
	}

	/**
	 * Prints the contents of the map containing the weight buckets.
	 */
	public static void printBucketMap(Map<Integer, Integer> m) {
		Iterator<Integer> it = m.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			Integer val = m.get(key);
			int to = key * 5;
			int from = to - 4;
			System.out.println("Bucket [" + from + " - " + to + "] appears : " + val + " times");
		}
	}

	/**
	 * Prints the weight distribution of the specified graph.
	 */
	public static void printWeights(Graph g) {
		Map<Integer, Integer> m = WeightUtils.intializeBuckets();
		for (Link l : g.get_arcs()) {
			double w = l.get_weight();
			if (w > 0) {
				WeightUtils.addWeightToBuckets(w, m);
			}
		}
		printBucketMap(m);
	}

	/**
	 * Prints the specified graph.
	 */
	public static void printGraph(Graph g, Map<Integer, String> m) {
		Link[] arcsArray = g.get_arcs();
		for (int i = 0; i < arcsArray.length; i++) {
			Link l = arcsArray[i];
			if (l != null && l.getWeight() > 0) {
				System.out.println("From: " + m.get(l.getStart() + 1) + " , To: " + m.get(l.getEnd() + 1)
						+ " , Weight = " + l.getWeight());
			}
		}
	}

	/**
	 * Prints the intersection of the specified graphs.
	 */
	public static void printGraphIntersection(Graph g1, Graph g2, Map<Integer, String> m) {
		Graph clone = g1.clone();
		GraphUtils.graphIntersection(g2, clone);
		printGraph(clone, m);
	}

	/**
	 * Prints the specified max. weighted cliques.
	 */
	public static void printMaxCliques(Map<List<Integer>, Double> maxCliques) {
		Iterator<List<Integer>> it = maxCliques.keySet().iterator();
		while (it.hasNext()) {
			List<Integer> topicIdList = it.next();
			for (Integer topicId : topicIdList) {
				System.out.print(topicId + " ");
			}
			System.out.println(" val=" + maxCliques.get(topicIdList));
		}
	}

	/**
	 * Prints the specified ACM portal results.
	 */
	public static void printAcmResults(List<AcmResult> acmResults) {
		int i = 1;
		for (AcmResult acmResult : acmResults) {
			System.out.println(i++ + ". Title: " + acmResult.getTitle());
			System.out.println("URL: " + acmResult.getUrl());
			System.out.println();
		}
	}

	/**
	 * Prints all buckets and contained element values.
	 */
	public static void printBucketsMap(Map<Double, List<Integer>> m) {
		System.out.println();
		System.out.println("-----------------------------");
		System.out.println("Printing buckets:");
		System.out.println("-----------------------------");
		Iterator<Double> it = m.keySet().iterator();
		while (it.hasNext()) {
			Double bucket = it.next();
			System.out.println("Bucket = " + bucket + " has values:");
			List<Integer> values = m.get(bucket);
			for (Integer currVal : values) {
				System.out.println(" -> " + currVal);
			}
			System.out.println("- - - - - - - - - - - - -");
		}
		System.out.println("******************************");
		System.out.println();
	}

	/**
	 * Prints the specified values map.
	 */
	public static void printValuesMap(Map<Integer, Double> valuesMap) {
		System.out.println();
		System.out.println("-----------------------------");
		System.out.println("Printing values:");
		System.out.println("-----------------------------");
		Iterator<Integer> it = valuesMap.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			Double val = valuesMap.get(key);
			System.out.println(" -> key : " + key + " , value = " + val);
		}
		System.out.println("******************************");
		System.out.println();
	}

	/**
	 * Prints the ranked results.
	 */
	public static void printRankedResults(List<Integer> rankedResults) {
		int rank = 1;
		for (Integer rankedRes : rankedResults) {
			System.out.println(rank + "." + rankedRes);
			rank++;
		}
	}

	/**
	 * Prints the specified map structure.
	 */
	public static void printMap(Map<Double, List<Integer>> m) {
		Iterator<Double> iterator = m.keySet().iterator();
		while (iterator.hasNext()) {
			Double key = iterator.next();
			List<Integer> l = m.get(key);
			System.out.print("BUCKET #" + key + " HAS MEMBERS : ");
			for (Integer i : l) {
				System.out.print(i + " ");
			}
			System.out.println();
		}
	}

	/**
	 * Prints the experiment results.
	 */
	public static void printExperimentResults(List<Integer> rankingOrder, Map<Integer, Double> resWeightMap,
			Map<Integer, Double> resultDeprecScoreMap, double bucketRange, Map<Integer, Integer> resultBucketMap)
			throws SQLException, ClassNotFoundException, IOException {
		System.out.println("-----------------------------------------------");
		System.out.println("              EXPERIMENT RESULTS");
		System.out.println("-----------------------------------------------");
		int count = 0;
		for (Integer queryResultId : rankingOrder) {
			String searchResTitle = PersistenceController.fetchQueryResultTitleFromId(queryResultId);
			Double depreciatedScore = resultDeprecScoreMap.get(queryResultId);
			Double maxWeightedCliqueWeight = resWeightMap.get(queryResultId);
			Integer bucket = resultBucketMap.get(queryResultId);
			String msg = ++count + "\t" + searchResTitle + "\t" + AppUtils.getSpecPrecision(depreciatedScore, 2) + "\t"
					+ AppUtils.getSpecPrecision(maxWeightedCliqueWeight, 2) + "\t" + bucketRange + "\t" + bucket;
			System.out.println(msg);
		}
		System.out.println();
		System.out.println("-----------------------------------------------");
	}

	/**
	 * Prints the specified acronym map.
	 */
	public static void printAcronymsMap(Map<String, List<String>> map, String outputFilePath) {
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			List<String> values = map.get(key);
			String msg = key + " : \n";
			System.out.print(msg);
			IOUtils.appendToFile(msg, outputFilePath);
			for (String val : values) {
				msg = " -> " + val + " \n";
				System.out.println(msg);
				IOUtils.appendToFile(msg, outputFilePath);
			}
			System.out.println("------------------");
		}
	}

	/**
	 * Prints all publication information details for all publications
	 */
	public static void printPublicationInfo(List<PublicationInfo> publicationInfo) {
		for (PublicationInfo p : publicationInfo) {
			printPublicationInfo(p);
		}
	}

	/**
	 * Prints all publication information details
	 */
	public static void printPublicationInfo(PublicationInfo publicationInfo) {
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("TITLE: " + publicationInfo.getTitle());
		System.out.println("URL: " + publicationInfo.getUrl());
		if (publicationInfo.getAuthors().size() > 0) {
			System.out.println("AUTHORS: ");
			for (String author : publicationInfo.getAuthors()) {
				System.out.println(" -> " + author);
			}
		}
		System.out.println("NUMBER OF CITATIONS: " + publicationInfo.getNumOfCitations());
	}

}
