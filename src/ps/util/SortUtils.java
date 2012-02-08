package ps.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ps.comparator.AscValComparator;
import ps.comparator.DescValComparator;
import ps.struct.SearchResultWeight;

/**
 * Provides sorting functionality.
 */
public class SortUtils {

	public static Map<Double, List<Integer>> sortMapBucketContents(Map<Double, List<Integer>> m,
			Map<Integer, Double> valuesMap) {
		Map<Double, List<Integer>> resultMap = new HashMap<Double, List<Integer>>();
		Iterator<Double> it = m.keySet().iterator();
		while (it.hasNext()) {
			Double bucket = it.next();
			List<Integer> contents = m.get(bucket);
			List<SearchResultWeight> l = new ArrayList<SearchResultWeight>();
			for (Integer elem : contents) {
				l.add(new SearchResultWeight(elem, valuesMap.get(elem)));
			}
			resultMap.put(bucket, sortListByWeight(l));
		}
		return resultMap;
	}

	public static List<Integer> sortListByWeight(List<SearchResultWeight> l) {
		List<Integer> res = new ArrayList<Integer>();
		List<SearchResultWeight> sortedList = reverseSort(l);
		for (SearchResultWeight s : sortedList) {
			res.add(s.getId());
		}
		return res;
	}

	private static List<SearchResultWeight> reverseSort(List<SearchResultWeight> l) {
		SearchResultWeight[] arr = new SearchResultWeight[l.size()];
		for (int i = 0; i < l.size(); i++) {
			arr[i] = l.get(i);
		}
		SearchResultWeight[] sarr = sort(arr);
		List<SearchResultWeight> res = new ArrayList<SearchResultWeight>();
		for (int i = 0; i < sarr.length; i++) {
			res.add(sarr[sarr.length - 1 - i]);
		}
		return res;
	}

	private static SearchResultWeight[] sort(SearchResultWeight[] arr) {
		if (arr == null || arr.length == 0) {
			return null;
		}
		int len = arr.length;
		return quicksort(0, len - 1, arr);
	}

	private static SearchResultWeight[] quicksort(int low, int high, SearchResultWeight[] arr) {
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		double pivot = arr[low + (high - low) / 2].getWeight();

		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is smaller then the pivot
			// element then get the next element from the left list
			while (arr[i].getWeight() < pivot) {
				i++;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list
			while (arr[j].getWeight() > pivot) {
				j--;
			}

			// If we have found a values in the left list which is larger then
			// the pivot element and if we have found a value in the right list
			// which is smaller then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				exchange(i, j, arr);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j) {
			quicksort(low, j, arr);
		}
		if (i < high) {
			quicksort(i, high, arr);
		}
		return arr;
	}

	private static void exchange(int i, int j, SearchResultWeight[] arr) {
		SearchResultWeight temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	}

	/**
	 * Sorts the specified map in ascending order.
	 */
	public static Map<String, Double> sortMapAsc(Map<String, Double> m) {
		return sortMap(m, new AscValComparator(m));
	}

	/**
	 * Sorts the specified map in descending order.
	 */
	public static Map<String, Double> sortMapDesc(Map<String, Double> m) {
		return sortMap(m, new DescValComparator(m));
	}

	/**
	 * Generic util method for sorting a map according to the comparator type.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<String, Double> sortMap(Map<String, Double> m, Comparator c) {
		Map<String, Double> sortedMap = new TreeMap(c);
		sortedMap.putAll(m);
		return sortedMap;
	}

	/**
	 * Sorts the map by key.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<Double, List<Integer>> sortMapByKey(Map<Double, List<Integer>> m, Comparator c) {
		Map<Double, List<Integer>> sortedMap = new TreeMap<Double, List<Integer>>(c);
		sortedMap.putAll(m);
		return sortedMap;
	}

}
