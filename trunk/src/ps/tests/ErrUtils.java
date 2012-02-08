package ps.tests;

import java.util.ArrayList;
import java.util.List;

public class ErrUtils {

	private static double G_MAX = 5;
	
	public static void main(String[] args) {
		
		List<Double[]> l = new ArrayList<Double[]>();
		
		Double[] ranking1 = new Double[] { 1d, 1d, 1d, 1d, 1d, 1d, 1d, 1d, 1d, 1d };
		Double[] ranking2 = new Double[] { 2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d };
		Double[] ranking3 = new Double[] { 3d, 3d, 2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d };
		Double[] ranking4 = new Double[] { 4d, 4d, 4d, 4d, 4d, 4d, 4d, 4d, 4d, 4d };
		Double[] ranking5 = new Double[] { 5d, 5d, 5d, 5d, 5d, 5d, 5d, 5d, 5d, 5d };
		l.add(ranking1);
		l.add(ranking2);
		l.add(ranking3);
		l.add(ranking4);
		l.add(ranking5);
		
		for (int i = 0; i < l.size(); i++) {
			Double[] arr = l.get(i);
			System.out.println(i + 1 + ". " + err(arr));
		}
	}

//	Require: Relevance grades g_i, i <= i <= n, and mapping
//	function R such as the one defined in (1)
//		p<-1, ERR<-0.
	
//		for r=1 to n do
//			R<-R(g_r)
//			ERR <- ERR + p * R/r
//			p<-p*(1-R)
//		end for
//		return ERR
	
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
