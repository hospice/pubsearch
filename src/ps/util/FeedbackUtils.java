package ps.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ps.persistence.PersistenceController;

/**
 * Provides utilities for calculating and printing the ranking feedback score.
 */
public class FeedbackUtils {

	private static final int TOTAL_RESULTS = 10;

	/**
	 * Computes and prints the total feedback score
	 */
	public static void computeAndPrintFeedback(int queryId, List<Integer> rankedResultIds) throws SQLException,
			ClassNotFoundException, IOException {

		// *** PUBSEARCH ***
		
		// i. Fetches the evaluation score for all query results produced by PubSearch 
		int[] pubSearchScores = new int[TOTAL_RESULTS];
		for(int i = 0 ; i < rankedResultIds.size() ; i++){
			pubSearchScores[i] = PersistenceController.fetchFeedbackForResultDef(rankedResultIds.get(i));
		}
		// calculates the total feedback score for PubSearch
		double pubSearchTotalScore = calcFeedbackScore(pubSearchScores);

		// *** ACM PORTAL ***
		
		// ii. Fetches the evaluation score for all query results produced by ACM Portal
		List<Integer> acmResults = PersistenceController.fetchQueryResultsDefault(queryId, TOTAL_RESULTS);
		int[] acmScores = new int[TOTAL_RESULTS];
		for(int i = 0 ; i < acmResults.size() ; i++){
			acmScores[i] = PersistenceController.fetchFeedbackForQueryResultDefault(acmResults.get(i));
		}
		// calculates the total feedback score for ACM Portal
		double acmTotalScore = calcFeedbackScore(acmScores);
		
		// *** PRINTING ***
		
		// Finally, print the results:
		System.out.println("pubSearchRes = " + pubSearchTotalScore);
		System.out.println("acmRes       = " + acmTotalScore);
	}

	/**
	 * Calculates the feedback score according to a purely lexicographic ordering, e.g. the score is = 2^(10-RANK), so
	 * for the first result the feedback score is multiplied by 2^9, second 2^8 etc...
	 */
	private static double calcFeedbackScore(int[] scores) {
		double feedbackScore = 0;
		int count = 0;
		for (int i = 9; i >= 0; i--) {
			if (count < TOTAL_RESULTS) {
				feedbackScore += Math.pow(2, i) * scores[9 - i];
				count++;
			} else {
				return feedbackScore;
			}
		}
		return feedbackScore;
	}
	
}
