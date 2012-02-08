package ps.algorithm;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;

import ps.enumerators.QueryResultEnum;
import ps.persistence.PersistenceController;

/**
 * Provides feedback score calculation functionality.
 */
public class FeedbackProvider {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, NamingException {
		int queryId = 46;
		calcFeedbackScoreForQuery(queryId);
	}

	/**
	 * Calculates the feedback score for the specified query id.
	 */
	private static void calcFeedbackScoreForQuery(int queryId) throws ClassNotFoundException, SQLException,
			IOException, NamingException {
		// feedback for query results
		int[] feedbackResults = PersistenceController.getFeedbackForQueryResults(queryId, QueryResultEnum.QUERY_RESULTS);
		double pubSearchRes = scoreResults(feedbackResults);
		// feedback for default results
		int[] feedbackResultsDefault = PersistenceController.getFeedbackForQueryResults(queryId,
				QueryResultEnum.QUERY_RESULTS_DEFAULT);
		double acmRes = scoreResults(feedbackResultsDefault);
		// persists feedback
		PersistenceController.saveFeedback(queryId, acmRes, pubSearchRes);
	}

	/**
	 * Produces a total score based on the provided feedback for all results based on a lexicographic ordering scheme.
	 */
	private static double scoreResults(int[] scores) {
		double feedbackScore = 0;
		for (int i = 9; i >= 0; i--) {
			feedbackScore += Math.pow(2, i) * scores[9 - i];
		}
		return feedbackScore;
	}

}
