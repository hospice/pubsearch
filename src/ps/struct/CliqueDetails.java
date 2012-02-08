package ps.struct;

import java.util.List;

/**
 * Clique details structure.
 */
public class CliqueDetails {

	private List<Integer> topicsList;
	private Double value;

	public List<Integer> getTopicsList() {
		return topicsList;
	}

	public void setTopicsList(List<Integer> topicsList) {
		this.topicsList = topicsList;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
