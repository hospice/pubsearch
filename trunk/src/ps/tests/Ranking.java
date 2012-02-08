package ps.tests;

import java.util.Arrays;

public class Ranking {

	public static void main(String[] args) {
		//double[] ranking = new double[] { 1, 1, 1, 1, 2, 4, 4, 5, 5, 5 };
		double[] ranking = new double[] {3,2,3,0,1,2};
		
		//MAP, NDCG, or ERR
		
		
		runForDCG(ranking);
		
//		runForDefScore(ranking);
		
//		runForDCG2(ranking);
	}

	private final int[] produceRankingForResult(){
		int[] rankOrder = new int[10];
		
		return rankOrder;
	}
	
	private static void runForDefScore(double[] ranking ){
		double score = calcDefScore(ranking);
		System.out.println("--------------------------------------------------------------");
		System.out.println("TOTAL DEFAULT SCORE = " + score);
	}
	
	private static void runForDCG2(double[] ranking ){
		double score = calcDcg2ForAll(ranking);
		System.out.println("--------------------------------------------------------------");
		System.out.println("TOTAL DCG SCORE = " + score);
	}
	
	private static double calcDcg2ForAll(double[] ranking){
		double totalScore = 0;
		for(int i = 0 ; i < ranking.length ; i++){
			double eval = ranking[i];
			int rank = i+1;
			double currScore = calcDcg2(eval, rank);
			System.out.println("The score for rank = " + rank + " is = " + currScore);
			totalScore += currScore;
		}
		return totalScore;
	}
	
	private static double calcDcg2(double eval, int rank){
		return (Math.pow(2, eval)-1) / Math.log(rank + 1);
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
	
	private static void runForDCG(double[] ranking ){
		
		// score 1
		double score1 = calcDcgScore(ranking);
		System.out.println("--------------------------------------------------------------");
		System.out.println("score1 = " + score1);

		// score 2
		double[] rankingClone = ranking.clone();
		Arrays.sort(rankingClone);
		rankingClone = reverseSort(rankingClone);
		
		
		double score2 = calcDcgScore(rankingClone);
		System.out.println("score2 = " + score2);
		
		double normalizedScore = score1/score2;
		System.out.println("normalized score = " + normalizedScore);
	}
	
	private static double[] reverseSort(double[] arr){
		double[] reversed = new double[arr.length];
		for(int i = 0 ; i < arr.length ; i++){
			reversed[i] = arr[i];
		}
		return reversed;
	}

	private static double calcDcgScore(double[] evalArr) {
		double score = 0;
		System.out.println("For position: 1 and eval = " + evalArr[0] + " the dcg = " + evalArr[0]);
		for (int i = 1; i < evalArr.length; i++) {
			double currScore = calcDcgForPos(evalArr[i], i + 1);
			System.out.println("For position: " + (i + 1) + " and eval = " + evalArr[i] + " the dcg = " + currScore);
			score += currScore;
		}
		return evalArr[0] + score;
	}

	private static double calcDcgForPos(double score, int pos) {
		return score / log2(pos);
	}

	public static double log2(double num) {
		return (Math.log(num) / Math.log(2));
	}

}
