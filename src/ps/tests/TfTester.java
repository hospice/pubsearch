package ps.tests;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ps.app.TFCalculator;
import ps.struct.PublicationData;
import ps.struct.TermFrequencyScore;
import ps.util.FormatterUtils;
import ps.util.PdfUtils;

public class TfTester {

	public static String PDF_HOME = "C:/PDF/";

	public static void main(String[] args) throws Exception {
		String query = "web information retrieval";
		TreeMap<Double, String> tfMap = (TreeMap<Double, String>) testForAllPdfsInPath(query);
		printResultsInDescOrder(tfMap);
		
	}

	/**
	 * Performs the test for all PDFs in the root (PDF_HOME) folder. 
	 */
	private static Map<Double, String> testForAllPdfsInPath(String query) throws Exception {
		System.out.println("******************************************************************************");
		System.out.println("                    PROCESSING ALL INDIVIDUAL RESULTS");
		System.out.println("******************************************************************************");
		System.out.println();
		Map<Double, String> tfMap = new TreeMap<Double, String>();
		List<String> files = readPdfFilesInDir(PDF_HOME);
		int count = 0;
		for (int i = 0; i < files.size(); i++) {
			String f = files.get(i);
			System.out.println("[" + ++count + "] FILE NAME: " + f);
			PublicationData pd = readPublicationData(query, f);
			double totalTf = calcTfAndPrintResults(pd);
			tfMap.put(totalTf, pd.getTitle());
			String fmtScore = FormatterUtils.getTwoDecimalDouble(totalTf);
			System.out.println(" SCORE = " + fmtScore);
			System.out.println();
		}
		return tfMap;
	}
	
	/**
	 * Calculates the TF and prints the results.
	 */
	private static double calcTfAndPrintResults(PublicationData pd) throws Exception {
		TermFrequencyScore tfs = TFCalculator.calcTfForPublication(pd);
		double titleScore = tfs.getTitle();
		double abstrScore = tfs.getAbstractText();
		double bodyScore = tfs.getBody();
		return  titleScore + abstrScore + bodyScore;
	}

	/**
	 * Reads the specified file and converts it to publication data.
	 */
	private static PublicationData readPublicationData(String query, String filename) throws Exception {
		String pubTitle = filename.substring(0, filename.indexOf("."));
		String pathname = PDF_HOME + filename;
		String publicationText = PdfUtils.pdfToText(pathname);
		return new PublicationData(query, pubTitle, publicationText);
	}

	/**
	 * Reads all PDF files in the specified path.
	 */
	private static List<String> readPdfFilesInDir(String path) throws Exception {
		List<String> filenames = new ArrayList<String>();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".pdf");
			}
		};
		File dir = new File(path);
		String[] children = dir.list(filter);
		if (children == null) {
			throw new Exception("Either dir does not exist or is not a directory!");
		} else {
			for (int i = 0; i < children.length; i++) {
				filenames.add(children[i]);
			}
		}
		return filenames;
	}
	
	/**
	 * Prints the results in descending order. 
	 */
	private static void printResultsInDescOrder(TreeMap<Double, String> treeMap){
		System.out.println("\n\n");
		System.out.println("******************************************************************************");
		System.out.println("                               RESULTS");
		System.out.println("******************************************************************************");
		System.out.println();
		Iterator<Double> it = treeMap.descendingKeySet().iterator();
		while(it.hasNext()){
			Double score = it.next();
			String title = treeMap.get(score);
			String fmtScore = FormatterUtils.getTwoDecimalDouble(score);
			System.out.println("[" + fmtScore + "] TITLE : " + title);
			System.out.println();
		}
	}

}