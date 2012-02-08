package ps.util;

import ps.struct.Graph;
import ps.struct.Link;

/**
 * Provides graph-related functionality.
 */
public class GraphUtils {

	/**
	 * Performs graph intersection on the specified graphs and the clone graph ends up becoming the intersection.
	 */
	public static Graph graphIntersection(Graph g2, Graph clone) {
		Link[] arcs = clone.get_arcs();
		for (int i = 0; i < arcs.length; i++) {
			fetchLinkFromArray(arcs[i], g2.get_arcs(), clone);
		}
		return clone;
	}

	/**
	 * Fetches the link(arc) from array if it exists.
	 */
	private static Link fetchLinkFromArray(Link arc, Link[] arcArray, Graph g) {
		double weight = 0;
		for (int i = 0; i < arcArray.length; i++) {
			if (isMatch(arcArray[i], arc)) {
				weight = arc.getWeight() < arcArray[i].getWeight() ? arc.getWeight() : arcArray[i].getWeight();
				break;
			}
		}
		arc.set_weight(weight);
		return arc;
	}

	/**
	 * Checks if arc parameters match.
	 */
	private static boolean isMatch(Link arc1, Link arc2) {
		return arc1.getWeight() > 0 && arc2.getWeight() > 0 && arc1.getStart() == arc2.getStart()
				&& arc1.getEnd() == arc2.getEnd();
	}

}
