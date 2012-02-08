package ps.struct;

/**
 * Query structure.
 */
public class Query {

	private int id;
	private String text;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Query(int id, String text) {
		this.id = id;
		this.text = text;
	}
}
