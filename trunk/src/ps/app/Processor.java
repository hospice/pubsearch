package ps.app;

import java.util.List;

import ps.extractors.GoogleScholarExtractor;
import ps.persistence.PersistenceController;
import ps.struct.PublicationData;
import ps.struct.PublicationInfo;
import ps.struct.Query;
import ps.struct.TermFrequencyScore;
import ps.util.PropertyUtils;
import ps.util.TimeUtils;

public class Processor {

	public static void main(String[] args) {
		int totalInactiveLoops = 0;
		int totalExecutions = 0;
		do {
			try {
				if(PersistenceController.pendingQueryExists()){
					totalInactiveLoops = 0;
					runProcess();
					totalExecutions++;
				}else{
					// CASE WHERE NO PENDING QUERIES EXIST
					totalInactiveLoops++;
					System.out.print ("INACTIVE ITERATION #" + totalInactiveLoops + " : ");
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
		
		// FETCH THE NEXT UNPROCESSED QUERY
		Query query = PersistenceController.fetchNextQueryToProcess();
		
		// 1. FETCH ALL RESULTS FOR THE SPECIFIC QUERY
		
		List<PublicationInfo> googleScholarResList = GoogleScholarExtractor.extractPublicationResults(query.getText());
		// FIXME: the results should be saved in persistence!!!
		
		// 2. FETCH THE PDF FOR ALL RESULTS AND CALCULATE AND SAVE THE TERM FREQUENCY
		for(PublicationInfo p : googleScholarResList){
			String publicationText = PdfDownloader.downloadPdfAndConvertToText(p);
			PublicationData pd = new PublicationData(query.getText(), p.getTitle(), publicationText);
			TermFrequencyScore tfs = TFCalculator.calcTfForPublication(pd);
			//FIXME: Save TERM FREQUENCY SCORE!!!
		}
		
		// 3. CALCULATE DCC
		for(PublicationInfo p : googleScholarResList){
			
			
		}
		
		// 4. APPLY THE RANKER AND SAVE ALT RANK
		
		// UPDATE STATUS TO COMPLETE
		
	}

}
