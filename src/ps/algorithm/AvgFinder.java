package ps.algorithm;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ps.persistence.PersistenceController;

public class AvgFinder {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

		int numOfBuckets = 30;
		int[] qArr = new int[10];
		qArr[0] = 55;
		qArr[1] = 56;
		qArr[2] = 57;
		qArr[3] = 58;
		qArr[4] = 59;
		qArr[5] = 60;
		qArr[6] = 61;
		qArr[7] = 63;
		qArr[8] = 64;
		qArr[9] = 65;
		
		
		System.out.println();
		System.out.println("*** PubSearch *** ");
		System.out.println();
		
		List<Double> l = PersistenceController.fetchAverageFeedbackForBucketsPubSearch(numOfBuckets);
		
		int i = 0;
		for(Double d : l){
			System.out.println("Bucket Range = \t" + ++i + "\t Average Feedback = " + d);
		}
		
		System.out.println();
		System.out.println("------------------------------");
		System.out.println();
		System.out.println("*** ACM *** ");
		System.out.println();
		
		
		List<Double> l2 = PersistenceController.fetchAverageFeedbackForBucketsACM(numOfBuckets);

		 int i2 = 0;
		for(Double d2 : l2){
			System.out.println("Bucket Range = \t" + ++i2 + "\t Average Feedback = " + d2);
		}

	}

}
