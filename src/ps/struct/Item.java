package ps.struct;

import java.util.List;

/**
 * Defines an experiment item structure.
 */
public class Item {

	private int order;
	private String name;
	private List<Param> parameters;

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Param> getParameters() {
		return parameters;
	}

	public void setParameters(List<Param> parameters) {
		this.parameters = parameters;
	}
}
