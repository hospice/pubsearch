package ps.tests;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TempTest {

	private final static String ROOT = "c:/_tmp/mar13/";

	private final static String PS_AM = "ps_am/";
	private final static String PS_GS = "ps_gs/";
	private final static String PS_MS = "ps_ms/";

	public static void main(String[] args) throws Exception {
		String eng = ROOT + PS_MS;
		calcScores(eng);
	}
	
	private static void calcScores(String folder) throws Exception {
		String ps = folder + "ps.txt";
		List<Double[]> psResList = readDataFromPathAsList(ps);
		
		System.out.println("---------------------------------------------------");
		System.out.println(" RESULTS OF : " + ps);
		System.out.println("---------------------------------------------------");
		
		for (Double[] arr : psResList) {
			double lex = MetricUtils.lex(arr);
			double ndcg = MetricUtils.ndcg(arr);
			double err = MetricUtils.err(arr);
			System.out.println("LEX = " + lex + ", NDCG = " + ndcg + ", ERR = " + err);
		}
		
		String other = folder + "other.txt";
		List<Double[]> otherResList = readDataFromPathAsList(other);

		System.out.println("\n\n *************** \n\n");
		
		System.out.println("---------------------------------------------------");
		System.out.println(" RESULTS OF : " + other);
		System.out.println("---------------------------------------------------");
				
		for (Double[] arr : otherResList) {
			double lex = MetricUtils.lex(arr);
			double ndcg = MetricUtils.ndcg(arr);
			double err = MetricUtils.err(arr);
			System.out.println("LEX = " + lex + ", NDCG = " + ndcg + ", ERR = " + err);
		}
	}
	
	private static void testAll(){
		try {

			// PS - AM :
			String ps_am = ROOT + PS_AM;
			runTest(ps_am);

			// PS - GS :
			String ps_gs = ROOT + PS_GS;
			runTest(ps_gs);

			// PS - MS :
			String ps_ms = ROOT + PS_MS;
			runTest(ps_ms);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void runTest(String folder) throws Exception {
		String ps = folder + "ps.txt";
		List<Double[]> psResList = readDataFromPathAsList(ps);

		String other = folder + "other.txt";
		List<Double[]> otherResList = readDataFromPathAsList(other);

		for (int i = 0; i < psResList.size(); i++) {
			Double[] psRes = psResList.get(i);
			Double[] otherRes = otherResList.get(i);
			boolean evalRes = evaluate(psRes, otherRes);
			if (evalRes == false) {
				throw new Exception("Problem found in folder: " + folder + " - result: " + (i + 1));
			}
		}
		System.out.println("Test successful for folder: " + folder + "\n");
	}

	private static boolean evaluate(Double[] arr1, Double[] arr2) {
		List<Double> allValues = new ArrayList<Double>();
		for (int i = 0; i < arr1.length; i++) {
			allValues.add(arr1[i]);
		}
		for (int i = 0; i < arr2.length; i++) {
			double d = arr2[i];
			Iterator<Double> it = allValues.iterator();
			boolean valFound = false;
			while (it.hasNext() && valFound == false) {
				double lval = it.next();
				if (d == lval) {
					it.remove();
					valFound = true;
				}
			}
			if (valFound == false) {
				return false;
			}
		}
		return true;
	}

	private static List<Double[]> readDataFromPathAsList(String path) throws IOException {
		List<Double[]> l = new ArrayList<Double[]>();
		FileInputStream fis = new FileInputStream(path);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String line;
		while ((line = br.readLine()) != null) {
			List<Double> rankL = new ArrayList<Double>();
			String[] splitRank = line.split(",");
			for (int i = 0; i < splitRank.length; i++) {
				rankL.add(Double.valueOf(splitRank[i]));
			}
			Double[] rankArr = new Double[rankL.size()];
			for (int i = 0; i < rankL.size(); i++) {
				rankArr[i] = rankL.get(i);
			}
			l.add(rankArr);
		}
		dis.close();
		return l;
	}

}
