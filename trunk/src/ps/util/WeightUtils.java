package ps.util;

import java.util.Map;
import java.util.TreeMap;

/**
 * Provides association weight functionality.
 */
public class WeightUtils {

	/**
	 * Initializes map with buckets.
	 */
	public static Map<Integer, Integer> intializeBuckets() {
		Map<Integer, Integer> m = new TreeMap<Integer, Integer>();
		for (int i = 1; i <= 300; i++) {
			m.put(i, 0);
		}
		return m;
	}

	/**
	 * Adds the weight to the respective bucket of the map and increments counter value.
	 */
	static void addWeightToBuckets(double w, Map<Integer, Integer> m) {
		int bucket = calcBucket(w);

		int count = m.get(bucket) + 1;
		m.put(bucket, count);

	}

	/**
	 * Calculates to which bucket should the weight value be added (buckets are as follows: [1-5], [6-10], [11-15],
	 * [16-20], etc).
	 */
	private static int calcBucket(double weight) {
		Double bucket = weight / 5;
		String[] nums = bucket.toString().split("\\.");
		if (Integer.parseInt(nums[1]) > 0) {
			bucket = bucket + 1;
		}
		return bucket.intValue();
	}

}