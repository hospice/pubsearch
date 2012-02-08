package ps.tests;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CalcScores {

	public static void main(String[] args) throws IOException {
		String path = "c:/all_res.txt";
		Map<Double, Double[]> m = readDataFromPath(path);
		Iterator<Double> it = m.keySet().iterator();
		int count = 0;
		while (it.hasNext()) {
			Double key = it.next();
			Double[] val = m.get(key);

//			System.out.println(++count + ". For score = " + key + " contents are: ");
//			for (int i = 0; i < val.length; i++) {
//				System.out.println("  => " + val[i]);
//			}

			double ndcg = MetricUtils.ndcg(val);
			double err = MetricUtils.err(val);		
			System.out.println(++count + ". For score=\t" + key + "\tNDCG=\t" + ndcg + "\tERR=\t" + err);
		}
	}

	/**
	 * Reads all data from the specified path.
	 * @param path, the path to read from
	 * @return, the map containing as key the score and as value the ranks
	 * @throws IOException
	 */
	private static Map<Double, Double[]> readDataFromPath(String path) throws IOException {
		Map<Double, Double[]> m = new HashMap<Double, Double[]>();
		FileInputStream fis = new FileInputStream(path);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String line;
		while ((line = br.readLine()) != null) {
			String[] split = line.split(" ");
			Double score = Double.parseDouble(split[0]);
			List<Double> rankL = new ArrayList<Double>();
			String str = split[1].substring(1, split[1].length() - 1);
			String[] splitRank = str.split(",");
			for (int i = 0; i < splitRank.length; i++) {
				rankL.add(Double.valueOf(splitRank[i]));
			}
			Double[] rankArr = new Double[rankL.size()];
			for (int i = 0; i < rankL.size(); i++) {
				rankArr[i] = rankL.get(i);
			}
			m.put(score, rankArr);
		}
		dis.close();
		return m;
	}

}
