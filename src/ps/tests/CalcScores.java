package ps.tests;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalcScores {

	public static void main(String[] args) throws IOException {
		String path = "c:/all_res.txt";
		
		List<Double[]> l = readDataFromPathAsList(path);
		for(int i = 0 ; i < l.size() ; i++){
			Double[] val = l.get(i);
			double lex = MetricUtils.lex(val);
			double ndcg = MetricUtils.ndcg(val);
			double err = MetricUtils.err(val);
			printArray(val);
			System.out.println("LEX=\t" + lex + "\tNDCG=\t" + ndcg + "\tERR=\t" + err);
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
	
	/**
	 * Reads all data from the specified path.
	 * @param path, the path to read from
	 * @return, the map containing as key the score and as value the ranks
	 * @throws IOException
	 */
	private static List<Double[]> readDataFromPathAsList(String path) throws IOException {
		//Map<Double, Double[]> m = new HashMap<Double, Double[]>();
		List<Double[]> l = new ArrayList<Double[]>();
		FileInputStream fis = new FileInputStream(path);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String line;
		while ((line = br.readLine()) != null) {
//			String[] split = line.split(" ");
//			Double score = Double.parseDouble(split[0]);
			List<Double> rankL = new ArrayList<Double>();
//			String str = split[1].substring(1, split[1].length() - 1);
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
	
	private static void printArray(Double[] rank){
		for(int i = 0 ; i < rank.length ; i++){
			System.out.print(rank[i] + ",");
		}
	}

}
