package ps.algorithm;

import java.util.ArrayList;
import java.util.List;

import ps.struct.SearchResultWeight;

public class Sorter {

    public static void main(String[] args) {
        List<SearchResultWeight> l = new ArrayList<SearchResultWeight>();
        l.add(new SearchResultWeight(2, 5.0));
        l.add(new SearchResultWeight(3, 6.0));
        l.add(new SearchResultWeight(1, 4.0));

        printResult(reverseSort(l));
    }

    private static void printResult(List<SearchResultWeight> l) {
        for (SearchResultWeight s : l) {
            System.out.println("id : " + s.getId() + ", weight = " + s.getWeight());
        }
    }

    public static List<SearchResultWeight> reverseSort(List<SearchResultWeight> l) {
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

}
