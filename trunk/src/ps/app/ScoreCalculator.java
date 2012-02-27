package ps.app;

/**
 * Provides functionality for calculating the feedback score from the search result evaluations.
 */
public class ScoreCalculator {

	/**
	 * Calculates the total feedbaack score received.
	 * @param scores, the array containing the score for each rank position (determined by the array index).
	 * @return the total feedback score
	 */
	public static double calcFeedbackScore(int[] scores) {
		double feedbackScore = 0;
		int count = 0;
		for (int i = 9; i >= 0; i--) {
			if (count < 10) {
				feedbackScore += Math.pow(2, i) * scores[9 - i];
				count++;
			} else {
				return feedbackScore;
			}
		}
		return feedbackScore;
	}	
	
}
