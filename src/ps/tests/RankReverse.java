package ps.tests;

public class RankReverse {


	
	
	private final static double[] RANKING = new double[] {5, 5, 5, 5, 4, 3, 4, 2, 3, 2};
	
	public static void main(String[] args) {
		double score = calcDefScore(RANKING);
		System.out.println("--------------------------------------------------------------");
		System.out.println("TOTAL DEFAULT SCORE = " + score);
	}

	private static double calcDefScore(double[] ranking) {
		double feedbackScore = 0;
		for (int i = 9; i >= 0; i--) {
			int idx = 9 - i;
			double currScore = Math.pow(2, i) * ranking[idx];
			System.out
					.println("For position: " + (i + 1) + " and eval = " + ranking[idx] + " the score = " + currScore);
			feedbackScore += currScore;
			if (idx == ranking.length - 1) {
				return feedbackScore;
			}
		}
		return feedbackScore;
	}
	
}
