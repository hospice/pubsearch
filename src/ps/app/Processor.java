package ps.app;

import java.util.List;
import java.util.Map;

import ps.extractors.AcmPortalExtractor;
import ps.extractors.GoogleScholarExtractor;
import ps.extractors.SearchResultsAccumulator;
import ps.persistence.PersistenceController;
import ps.struct.PublicationInfo;
import ps.struct.Query;
import ps.util.PropertyUtils;
import ps.util.RankUtils;
import ps.util.TimeUtils;

public class Processor {

	private final static int MAX_RES = 10;
	private final static double TF_BUCKET_SIZE = 100;

	public static void main(String[] args) {
		int totalInactiveLoops = 0;
		int totalExecutions = 0;
		do {
			try {
				if (PersistenceController.pendingQueryExists()) {
					totalInactiveLoops = 0;
					runProcess();
					totalExecutions++;
				} else {
					// CASE WHERE NO PENDING QUERIES EXIST
					totalInactiveLoops++;
					System.out.print("INACTIVE ITERATION #" + totalInactiveLoops + " : ");
					TimeUtils.sleepForSecs(30);
				}
			} catch (Exception e) {
				// FIXME: handle exception here...
				e.printStackTrace();
			}
		} while (PropertyUtils.keepRunning());
	}

	/**
	 * Runs the entire process.
	 */
	public static void runProcess() throws Exception {

		// FETCHES THE NEXT, UNPROCESSED QUERY:
		// -------------------------------------
		Query query = PersistenceController.fetchNextQueryToProcess();

		
		// EXTRACTS THE PUBLICATION RESULTS RETURNED FROM THE SPECIFIED ACADEMIC SEARCH ENGINES:
		// --------------------------------------------------------------------------------------
		List<PublicationInfo> acmList = AcmPortalExtractor.extractPublicationResults(query.getText());
		List<PublicationInfo> gsList = GoogleScholarExtractor.extractPublicationResults(query.getText());
		List<PublicationInfo> pList = SearchResultsAccumulator.mergeDistinctResults(MAX_RES, acmList, gsList);

		
		// FIXME: ***CHECKPOINT: Persist the results in TMP table!!!***

		
		// RETRIEVES THE CITATION DISTRIBUTION MAP FOR ALL PUBLICATION RESULTS:
		// ---------------------------------------------------------------------
		
		Map<PublicationInfo, Map<Integer, Integer>> citationDistributionMap = AdvancedCitationExtractor.extractCitationDistribution(pList);
		// FIXME: ****Calculate the depreciation of the citation distribution!!!***
		Map<PublicationInfo, Double> citationDepreciationMap = null; // contains the depreciation score for the citation distribution
		
		
		// CALCULATES THE TERM FREQUENCY SCORE FOR ALL PUBLICATION RESULTS:
		// -----------------------------------------------------------------

		// FIXME: ****Calculate the term frequency score for all results!!!***
		Map<PublicationInfo, Double> termFrequencyMap = null; // contains the term frequency score
		
//		for (PublicationInfo p : pList) {
//			String publicationText = PdfDownloader.downloadPdfAndConvertToText(p);
//			PublicationData pd = new PublicationData(query.getText(), p.getTitle(), publicationText);
//			TermFrequencyScore tfs = TFCalculator.calcTfForPublication(pd);
//			// FIXME: Save TERM FREQUENCY SCORE!!!
//		}

	
		// RANKS THE RESULTS BASED ON THE CITATION AND DEPRECIATION CITATION DISTRIBUTION SCORES:
		// ---------------------------------------------------------------------------------------
		List<PublicationInfo> resultsRanking = RankUtils.rankResults(citationDepreciationMap, termFrequencyMap, TF_BUCKET_SIZE);
		
		// FIXME: SAVE THE RANK AND UPDATE STATUS TO COMPLETE!!!

	}

}
