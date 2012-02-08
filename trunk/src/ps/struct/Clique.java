package ps.struct;

import java.util.List;
import java.util.Map;

public class Clique {

	private Map<List<Integer>, Double> maxCliques;

	private GraphSuper graphSuper;

	public Map<List<Integer>, Double> getMaxCliques() {
		return maxCliques;
	}

	public void setMaxCliques(Map<List<Integer>, Double> maxCliques) {
		this.maxCliques = maxCliques;
	}

	public GraphSuper getGraphSuper() {
		return graphSuper;
	}

	public void setGraphSuper(GraphSuper graphSuper) {
		this.graphSuper = graphSuper;
	}

}
