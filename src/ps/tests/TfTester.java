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
import ps.util.PdfUtils;

public class TfTester {

	public static String PDF_HOME = "C:/PDF/";

	public static void main(String[] args) throws Exception {
//		String pdfName = "Information Retrieval on the Web.pdf";		
//		testForSpecificPdf(pdfName);
//		String query = "web information retrieval";
//		PublicationData pd = readPublicationData(query, pdfName);
//		System.out.println("\n\n\n\n\n");
//		System.out.println("*** ABSTRACT ***");
//		System.out.println("----------------");
//		System.out.println(pd.getAbstractText());
//		System.out.println("------------------------------------------------------------------");
//		System.out.println("\n\n\n\n\n");
//		System.out.println("*** FULL-TEXT ***");
//		System.out.println("-----------------");
//		System.out.println(pd.getBody());
//		System.out.println("------------------------------------------------------------------");
//		System.out.println("\n\n\n\n\n");
//		testSectionExtractionForAllPdfsInPath();
		
		TreeMap<Double, String> tfMap = (TreeMap<Double, String>) testForAllPdfsInPath();
		printTreeMapDescOrder(tfMap);
		
	}
	
	private static void testForSpecificPdf(String pdfName) throws Exception{
		String query = "web information retrieval";
		PublicationData pd = readPublicationData(query, pdfName);
		calcTfAndPrintResults(pd);
	}

	private static Map<Double, String> testForAllPdfsInPath() throws Exception{
		Map<Double, String> tfMap = new TreeMap<Double, String>();
		List<String> files = readPdfFilesInDir(PDF_HOME);
		String query = "web information retrieval";
		int count = 0;
		for (String f : files) {
			System.out.println(++count + ". " + f);
			PublicationData pd = readPublicationData(query, f);
			double totalTf = calcTfAndPrintResults(pd);
			System.out.println(" => Total TF score = " + totalTf);
			tfMap.put(totalTf, pd.getTitle());
			System.out.println("\n --------------------------- \n");
		}
		return tfMap;
	}
	
	private static void testSectionExtractionForAllPdfsInPath() throws Exception{
		List<String> files = readPdfFilesInDir(PDF_HOME);
		String query = "web information retrieval";
		int count = 0;
		for (String f : files) {
			System.out.println(++count + ". " + f);
			PublicationData pd = readPublicationData(query, f);
			
			System.out.println("\n\n\n\n\n");
			
			System.out.println("*** ABSTRACT ***");
			System.out.println("----------------");
			System.out.println(pd.getAbstractText());
			System.out.println("------------------------------------------------------------------");
			
			System.out.println("\n\n\n\n\n");
			
			System.out.println("*** FULL-TEXT ***");
			System.out.println("-----------------");
			System.out.println(pd.getBody());
			System.out.println("------------------------------------------------------------------");
			
			System.out.println("\n\n\n\n\n");
			
			System.out.println("\n --------------------------- \n");
		}
	}
	
	/**
	 * Calculates the TF and prints the results.
	 */
	private static double calcTfAndPrintResults(PublicationData pd) throws Exception {
		TermFrequencyScore tfs = TFCalculator.calcTfForPublication(pd);
		double titleScore = tfs.getTitle();
		double abstrScore = tfs.getAbstractText();
		double bodyScore = tfs.getBody();
		double totalTf = titleScore + abstrScore + bodyScore;
		return totalTf;
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
	
	private static void printTreeMapDescOrder(TreeMap<Double, String> treeMap){
		Iterator<Double> it = treeMap.descendingKeySet().iterator();
		while(it.hasNext()){
			Double key = it.next();
			String val = treeMap.get(key);
			System.out.println(key + " has score = " + val);
		}
	}

}