package ps.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ps.struct.Graph;
import ps.struct.Link;
import ps.struct.Node;
import ps.util.GraphUtils;

@SuppressWarnings("rawtypes")
public class CliqueFinder {

	/**
	 * Finds all maximal cliques in the intersection of the specified graphs
	 */
	public static Map<List<Integer>, Double> findMaxCliquesForIntersection(Graph g1, Graph g2, Map<Integer, String> topicsMap, double minVal){
		Graph clone = g1.clone();
		GraphUtils.graphIntersection(g2, clone);
		return CliqueFinder.findMaxCliques(clone, topicsMap, minVal);
	}
	
	/**
	 * Finds all maximal cliques
	 */
	public static Map<List<Integer>, Double> findMaxCliques(Graph g, Map<Integer, String> topicsMap, double minVal) {
		Map<List<Integer>, Double> maxCliques = new HashMap<List<Integer>, Double>();
		int[] nodemap = new int[g.getNumNodes()];
		for (int i = 0; i < nodemap.length; i++)
			nodemap[i] = i;
		Set cliques = g.getAllMaximalCliques(minVal);
		Iterator iter = cliques.iterator();
		while (iter.hasNext()) {
			Set c = (Set) iter.next();
			double val = evaluate(c, g);
			if (val < minVal){
				continue; // don't show "light-weight" clique	
			}
			List<Integer> topicsList = new ArrayList<Integer>();
			Iterator it2 = c.iterator();
			while (it2.hasNext()) {
				Integer id = (Integer) it2.next();
				int topicId = nodemap[id.intValue()] + 1;
				topicsList.add(topicId);
			}
			Collections.sort(topicsList);
			maxCliques.put(topicsList, val);
		}
		return maxCliques;
	}

	private static double evaluate(Set c, Graph g) {
		double res = 0;
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Integer nid = (Integer) iter.next();
			Node n = g.getNode(nid.intValue());
			Set inarcs = n.getInLinks();
			Iterator itin = inarcs.iterator();
			while (itin.hasNext()) {
				Integer lid = (Integer) itin.next();
				Link l = g.getLink(lid.intValue());
				int inid = l.getStart();
				if (c.contains(new Integer(inid)))
					res += l.getWeight();
			}
			Set outarcs = n.getOutLinks();
			Iterator itout = outarcs.iterator();
			while (itout.hasNext()) {
				Integer lid = (Integer) itout.next();
				Link l = g.getLink(lid.intValue());
				int outid = l.getStart();
				if (c.contains(new Integer(outid)))
					res += l.getWeight();
			}
		}
		return res / 2.0; // divide by 2, as each arc's weight was counted twice
	}


}
