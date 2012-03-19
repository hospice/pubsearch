package ps.tests;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.util.StringTokenizer;

/**
 * Runs test for all metrics based on the rankings specified in FILE.
 * 
 * date: 28 Nov. 2011
 */
public class RunTest {

	final static String FILE = "C:/_tmp/rankings_acmportal.txt";

	public static void main(String[] args) {
		// printAllData(readAllRankings(FILE));
		List<Double[]> l = readAllRankings(FILE);
		for (Double[] arr : l) {
			double lex = MetricUtils.lex(arr);
			double ndcg = MetricUtils.ndcg(arr);
			double err = MetricUtils.err(arr);
			System.out.println("LEX = " + lex + ", NDCG = " + ndcg + ", ERR = " + err);
		}
	}

	public static List<Double[]> readAllRankings(String file) {
		List<Double[]> l = new ArrayList<Double[]>();
		try {
			FileInputStream fis = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ",");
				List<Double> list = new ArrayList<Double>();
				while (st.hasMoreTokens()) {
					String s = st.nextToken();
					list.add(Double.parseDouble(s.trim()));
				}

				Object[] arr = list.toArray();
				Double[] arr2 = new Double[arr.length];
				for (int i = 0; i < arr.length; i++) {
					arr2[i] = (Double) arr[i];
				}
				l.add(arr2);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return l;
	}

	private static void printAllData(List<Double[]> l) {
		for (int i = 0; i < l.size(); i++) {
			Double[] arr = l.get(i);
			for (int a = 0; a < arr.length; a++) {
				System.out.print(arr[a]);
				if (a < arr.length - 1) {
					System.out.print(",");
				}
			}
			System.out.println();
		}
	}

}
