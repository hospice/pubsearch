package ps.tests;

import java.util.Arrays;

/**
 * Provides utility functionality for the different ranking evaluation metrics.
 */
public class MetricUtils {
	
	// the max grade
	private static double G_MAX = 5;

	public static void main(String[] args) {
		// Rank:
		Double[] ranking = new Double[] { 3d, 2d, 3d, 0d, 1d, 2d };

		// LEXICOGRAPHIC ORDERING:
		double lexScore = lex(ranking);
		System.out.println("LEX = " + lexScore);
		
		// DCG TEST:
		double dcgScore = dcg(ranking);
		System.out.println("DCG = " + dcgScore);

		// NDCG TEST:
		double ndcgScore = ndcg(ranking);
		System.out.println("NDCG = " + ndcgScore);
	}

	public static double lex(Double[] ranking) {
		double feedbackScore = 0;
		for (int i = 9; i >= 0; i--) {
			int idx = 9 - i;
			double currScore = Math.pow(2, i) * ranking[idx];
			feedbackScore += currScore;
			if (idx == ranking.length - 1) {
				return feedbackScore;
			}
		}
		return feedbackScore;
	}
	
	/**
	 * Calculates the DCG (Discounted cumulative gain) score for the specified ranking evaluation.
	 * 
	 * @param ranking
	 *            the ranking evaluation
	 * @return the DCG score for the ranking
	 */
	public static double dcg(Double[] ranking) {
		double score = 0;
		for (int i = 1; i < ranking.length; i++) {
			score += calcDcgForPos(ranking[i], i + 1);
		}
		return ranking[0] + score;
	}

	/**
	 * Calculates the DCG (Discounted cumulative gain) score for the specified evaluation score at the specified rank
	 * position.
	 * 
	 * @param score
	 *            the evaluation score
	 * @param rank
	 *            the rank position
	 * @return the DCG score
	 */
	private static double calcDcgForPos(double score, int rank) {
		double log2Pos = Math.log(rank) / Math.log(2);
		return score / log2Pos;
	}

	/**
	 * Calculates the NDCG (Normalized discounted cumulative gain) score for the specified ranking evaluation.
	 * 
	 * @param ranking
	 *            the ranking evaluation
	 * @return the NDCG score for the ranking
	 */
	public static double ndcg(Double[] ranking) {
		return dcg(ranking) / dcg(reverseSortDesc(ranking));
	}

	/**
	 * Reverse sorts the specified array in descending order based on the evaluation value.
	 * 
	 * @param ranking
	 *            the ranking evaluation
	 * @return the sorted array
	 */
	public static Double[] reverseSortDesc(Double[] ranking) {
		Double[] reverseSorted = new Double[ranking.length];
		Arrays.sort(ranking);
		int arrLen = ranking.length;
		for (int i = arrLen - 1; i >= 0; i--) {
			int pos = arrLen - 1 - i;
			reverseSorted[pos] = ranking[i];
		}
		return reverseSorted;
	}
	
	/**
	 * Calculates the ERR (Expected Reciprocal Rank).
	 * 
	 * @param ranking
	 *            the ranking evaluation
	 * @return the ERR score
	 */
	public static double err(Double[] ranking) {
		double p = 1;
		double errScore = 0;
		int n = ranking.length;
		for (int r = 1; r <= n; r++) {
			double g = ranking[r - 1];
			double rg = (Math.pow(2, g) - 1) / Math.pow(2, G_MAX);
			errScore += p * (rg / r);
			p = p * (1 - rg);
		}
		return errScore;
	}

}