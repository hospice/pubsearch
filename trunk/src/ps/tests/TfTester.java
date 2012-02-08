package ps.tests;

import ps.stem.snowball.StemUtil;
import ps.struct.PublicationData;
import ps.util.PdfUtils;
import ps.util.PrintUtils;
import ps.util.PropertiesUtils;
import ps.util.TermFrequencyUtils;

public class TfTester {

//	public static void main(String[] args) throws Exception {
//		
//		String pubTitle = "A Survey of Eigenvector Methods for Web Information Retrieval";
//		String query = "web information retrieval";
//		String pathname = PropertiesUtils.readPdfRootPath() + pubTitle + ".pdf";
//		String publicationText = PdfUtils.pdfToText(pathname);
//		
//		String stemmed = StemUtil.getEnglishStem(publicationText);
//		System.out.println(stemmed);
//		
//	}
	
	
	public static void main(String[] args) throws Exception {
		PublicationData pd = createMockPublicationData();
		PrintUtils.printPublicationData(pd);
		double totalTf = TermFrequencyUtils.calcTfForPublicationData(pd);
		System.out.println("total TF score = " + totalTf);
	}

//	/** pagerank PDF **/
	private static PublicationData createMockPublicationData() throws Exception {
		//String pubTitle = "The Anatomy of a Large-Scale Hypertextual Web Search Engine";
		String pubTitle = "A Survey of Eigenvector Methods for Web Information Retrieval";
		String query = "web information retrieval";
		String pathname = PropertiesUtils.readPdfRootPath() + pubTitle + ".pdf";
		String publicationText = PdfUtils.pdfToText(pathname);
		//System.out.println(publicationText);
		return new PublicationData(query, pubTitle, publicationText);
	}
	
////	/** peer-to-peer networks PDF **/
//	private static PublicationData createMockPublicationData2() throws Exception {
//		String pubTitle = "PEER-TO-PEER NETWORKS";
//		String query = "peer-to-peer networks";
//		String pathname = PropertiesUtils.readPdfRootPath() + pubTitle + ".pdf";
//		String publicationText = PdfUtils.pdfToText(pathname);
//		//System.out.println(publicationText);
//		return new PublicationData(query, pubTitle, publicationText);
//	}
	
//	/** dummy text file **/
//	private static PublicationData createMockPublicationData() throws Exception {
//		String pubTitle = "information retrieval for web";
//		String query = "web information retrieval";
//		String pathname = PropertiesUtils.readPdfRootPath() + pubTitle + ".txt";
//		String publicationText = readFileFromPath(pathname);
//		return new PublicationData(query, pubTitle, publicationText);
//	}
	
//	/** dummy text file **/
//	private static PublicationData createMockPublicationData() throws Exception {
//		String pubTitle = "The Anatomy of a Large-Scale Hypertextual Web Search Engine";
//		String query = "web information retrieval";
//		String pathname = PropertiesUtils.readPdfRootPath() + pubTitle + ".txt";
//		String publicationText = readFileFromPath(pathname);
//		return new PublicationData(query, pubTitle, publicationText);
//	}
	
}
