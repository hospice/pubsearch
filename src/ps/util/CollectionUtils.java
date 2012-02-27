package ps.util;

import java.util.List;

/**
 * Provides collection related utility.
 */
public class CollectionUtils {

	/**
	 * Converts the specified list of integer to an array of the same type.
	 */
	public static Integer[] convertIntegerListToArray(List<Integer> l) {
		Integer[] arr = new Integer[l.size()];
		int idx = 0;
		for (Integer i : l) {
			arr[idx] = i;
			idx++;
		}
		return arr;
	}

	/**
	 * Converts the specified list of string to an array of the same type.
	 */
	public static String[] convertStringListToArray(List<String> l) {
		String[] arr = new String[l.size()];
		int idx = 0;
		for (String s : l) {
			arr[idx] = s;
			idx++;
		}
		return arr;
	}

	/**
	 * Converts the specified list of double to an array of the same type.
	 */
	public static Double[] convertDoubleListToArray(List<Double> l) {
		Double[] arr = new Double[l.size()];
		int idx = 0;
		for (Double d : l) {
			arr[idx] = d;
			idx++;
		}
		return arr;
	}
	
}
