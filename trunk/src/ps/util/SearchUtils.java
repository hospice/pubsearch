package ps.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ps.struct.AcmResult;
import ps.struct.AcmTopic;

/**
 * Provides search functionality. 
 */
public class SearchUtils {
    
	/**
	 * Fetches the n most common topics for the specified query.
	 */
    public static List<String> findMostCommonTopicsForQuery(String query, int numOfResults, int n)
            throws Exception {
        List<AcmResult> acmResults = CrawlUtils.fetchAcmResults(query, numOfResults);
        Map<String, List<AcmTopic>> publicationUrlTopics = extractTopicsFromAllPublications(acmResults);
        List<String> mostCommonTopics = fetchCommonTopics(publicationUrlTopics, n);
        return mostCommonTopics;
    }

    /**
     * Constructs search query with most common topics.
     */
    public static String constructQuery(List<String> mostCommonTopics, Map<String, String> map) {
        String query = "";
        for (String t : mostCommonTopics) {
            query += t + " - " + map.get(t) + " ";
        }
        return query.trim();
    }

    /**
     * Returns a list with common topics.
     */
    public static List<String> fetchCommonTopics(Map<String, List<AcmTopic>> m, int n) {
        Map<String, Integer> topicFrequencyMap = new HashMap<String, Integer>();
        Iterator<String> it = m.keySet().iterator();
        while (it.hasNext()) {
            String url = it.next();
            List<AcmTopic> topics = m.get(url);
            for (AcmTopic topic : topics) {
                Integer freq = topicFrequencyMap.get(topic.getCode());
                if (freq == null) {
                    topicFrequencyMap.put(topic.getCode(), 1);
                } else {
                    topicFrequencyMap.put(topic.getCode(), ++freq);
                }
            }
        }
        return sortTopicsByFrequency(topicFrequencyMap, n);
    }

    /**
     * Returns the top n topics.
     */
    private static List<String> sortTopicsByFrequency(Map<String, Integer> topicFrequencyMap, int n) {
        List<String> mostCommonTopics = new ArrayList<String>();
        Map<Integer, List<String>> m = new TreeMap<Integer, List<String>>();
        Iterator<String> it = topicFrequencyMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Integer val = topicFrequencyMap.get(key);
            List<String> l = m.get(val);
            if (l == null) {
                l = new ArrayList<String>();
            }
            l.add(key);
            m.put(val, l);
        }
        for (int i = 0; i < n; i++) {
            Iterator<Integer> it2 = m.keySet().iterator();
            while (it2.hasNext()) {
                Integer key = it2.next();
                List<String> l = m.get(key);
                for (String s : l) {
                    mostCommonTopics.add(s);
                }
            }
        }
        List<String> mostCommonTopicsUpd = new ArrayList<String>();
        for (int i = 0; i < n; i++) {
            mostCommonTopicsUpd.add(mostCommonTopics.get(mostCommonTopics.size() - 1 - i));
        }
        return mostCommonTopicsUpd;
    }

    /**
     * Extracts all topics of all specific publications.
     */
    public static Map<String, List<AcmTopic>> extractTopicsFromAllPublications(List<AcmResult> acmResults)
            throws Exception {
        Map<String, List<AcmTopic>> publicationUrlTopics = new HashMap<String, List<AcmTopic>>();
        for (AcmResult acmResult : acmResults) {
            String url = acmResult.getUrl();
            publicationUrlTopics.put(url, CrawlUtils.extractTopicsFromPublication(url));
        }
        return publicationUrlTopics;
    }

}
