package ps.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import ps.exception.GraphException;
import ps.struct.Graph;
import ps.struct.Node;

/**
 * Provide I/O functionality.
 */
public class IOUtils {
	
	/**
	 * Appends the content to the file at the specified location. 
	 */
	public static void appendToFile(String fileContent, String pathname) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(pathname, true));
			out.append(fileContent);
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Writes the content to the file at the specified location (overwrites all file contents, in case they exist). 
	 */
	public static void writeToFile(String fileContent, String pathname) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(pathname, false));
			out.append(fileContent);
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Reads the file located in the specified path.
	 */
	public static String readFileFromPath(String pathname) throws IOException {
		FileInputStream fis = new FileInputStream(pathname);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String line;
		String text = "";
		while ((line = br.readLine()) != null)
			text += line + "\n";
		dis.close();
		return text;
	}
	
	/**
	 * Reads graph from the specified file path with the following format:
     * First row: numarcs numnodes 1
     * All other: weighta starta enda
     * (The starta, enda are in [1...num_nodes], weighta is int (non-negative))
	 */
	public static Graph readGraphFromFile(String filePath) throws IOException, GraphException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		try {
			Graph g = null;
			if (br.ready()) {
				// reads first line
				String line = br.readLine();
				StringTokenizer st = new StringTokenizer(line, " ");
				int numarcs = Integer.parseInt(st.nextToken());
				int numnodes = Integer.parseInt(st.nextToken());
				g = new Graph(numnodes, numarcs);
				int starta, enda;
				double weighta;
				while (true) {
					line = br.readLine();
					if (line == null || line.length() == 0) {
						break;
					}
					st = new StringTokenizer(line, " ");
					weighta = Integer.parseInt(st.nextToken());
					starta = Integer.parseInt(st.nextToken());
					enda = Integer.parseInt(st.nextToken());
					g.addLink(starta - 1, enda - 1, weighta);
				}
			}
			// sets cardinality values
			for (int i = 0; i < g.getNumNodes(); i++) {
				Node ni = g.getNode(i);
				ni.setWeight("cardinality", new Double(1.0));
			}
			return g;
		} finally {
			if (br != null)
				br.close();
		}
	}

}
