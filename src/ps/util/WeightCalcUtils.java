package ps.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ps.persistence.PersistenceController;

/**
 * Provides weight calculation functionality.
 */
public class WeightCalcUtils {

	private static final int MAX_DATE = 2010;
	private static final int RANGE = 5;

	public static double calcExponentialSmoothing(int cliqueDate)
			throws SQLException, ClassNotFoundException, IOException {
		return 1 - (((MAX_DATE - cliqueDate) / RANGE) * PersistenceController
				.getExponentialSmoothingWeight());
	}

	public static double calcWeightForQueryResult(List<Integer> resultTopics,
			Map<Integer, List<Integer>> cliqueTopicsMap) throws Exception {
		double totalWeight = 0.0;
		
		Iterator<Integer> it = cliqueTopicsMap.keySet().iterator();
		while (it.hasNext()) {
			Integer cliqueId = it.next();
			List<Integer> cliqueTopics = cliqueTopicsMap.get(cliqueId);
			double matchingPercent = percentMatch(cliqueTopics, resultTopics);
			if (matchingPercent > 0) {
				totalWeight += calcWeightForCliquePerGraph(cliqueId,
						matchingPercent);
			}
		}
		return totalWeight;
	}

	/**
	 * Calculates the percentage ratio of matching between the result and the
	 * clique topics
	 * 
	 * @param cliqueTopics
	 *            , the topics in the clique
	 * @param resultTopics
	 *            , the topics in the result
	 * @return the percentage
	 * @throws Exception 
	 * 
	 */
	public static double percentMatch(List<Integer> cliqueTopics,
			List<Integer> resultTopics) throws Exception {
		double percentMatch = 0.0;
		double numOfResultTopics = resultTopics.size();
		double numOfCliqueTopics = cliqueTopics.size();
		long percentage = (long) (numOfCliqueTopics * (1 - 0.75));
		double upperLim = numOfCliqueTopics + percentage;
		double lowerLim = numOfCliqueTopics - percentage;
		int totalMatches = 0;
		// calculates the total matches
		for (Integer resultTopic : resultTopics) {
			if (cliqueTopics.contains(resultTopic)) {
				totalMatches++;
			}
		}
		// I. case where the resultSet is SUPERSET of cliqueSet
		if (totalMatches == numOfCliqueTopics) {
			// 2 cases: i) perfect matching, ii) complete but partial matching (more topics in result than in clique)
			if (numOfResultTopics <= upperLim) {
				double d = numOfResultTopics - numOfCliqueTopics;
				percentMatch = 1 - (d / numOfCliqueTopics);
			}
		} else {
			// II. case where the resultSet is SUBSET of cliqueSet
			// all result topics match with clique topics but the size of result topics < clique toipcs
			if (totalMatches == numOfResultTopics) {
				if (numOfResultTopics >= lowerLim) {
					percentMatch = numOfResultTopics / numOfCliqueTopics;
				}
			}
			// iii. case where the resultSet is INTERSECTION of cliqueSet
			else if (totalMatches >= lowerLim && totalMatches <= upperLim) {
				percentMatch = totalMatches / numOfCliqueTopics;
			}
		}
		if(percentMatch < 0.75 && percentMatch > 0){
			throw new Exception("Percentage Match should not be less than 0.75!");
		}
		return percentMatch;
	}

	public static double calcWeightForCliquePerGraph(Integer cliquesPerGraphId,
			double matchingPercent) throws Exception {
		double w = PersistenceController
				.getWeightForCliquePerGraph(cliquesPerGraphId);
		List<Integer> topics = PersistenceController
				.getTopicsForClique(cliquesPerGraphId);
		double kr = calcKeywordRatio(topics, cliquesPerGraphId);
		int cliqueDate = PersistenceController.getPeriodForCliquePerGraph(
				cliquesPerGraphId).getTo();
		double es = calcExponentialSmoothing(cliqueDate);
		double ac = PersistenceController
				.getWeightForAssocCase(PersistenceController
						.getAssocCaseForCliquePerGraph(cliquesPerGraphId));
		return w * kr * es * ac * matchingPercent;
	}

	private static double calcKeywordRatio(List<Integer> topics,
			int cliquesPerGraphId) throws ClassNotFoundException, SQLException,
			IOException {
		Double d = Double.valueOf(PersistenceController
				.getNumOfKeywordsForCliquePerGraph(cliquesPerGraphId));
		return Double.valueOf(topics.size()) / d;
	}

}
