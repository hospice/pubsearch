package ps.tests;

import java.util.Arrays;

public class SortTest {

	public static void main(String[] args) {
		double[] rank = new double[]{2,3,4,2,4,2,6,8};
		Arrays.sort(rank);
		for(int i = 0 ; i < rank.length ; i++){
			System.out.println(rank[i]);
		}
	}
	
}
