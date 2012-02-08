package ps.struct;

import java.util.List;

/**
 * Defines an experiment session structure.
 */
public class Session {

	private List<Item> items;

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
}
