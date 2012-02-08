package ps.struct;

/**
 * Period structure.
 */
public class Period {

	private Integer from;
	private Integer to;

	public Integer getFrom() {
		return from;
	}

	public void setFrom(Integer from) {
		this.from = from;
	}

	public Integer getTo() {
		return to;
	}

	public void setTo(Integer to) {
		this.to = to;
	}

	public Period(int from, int to) {
		super();
		this.from = from;
		this.to = to;
	}
}
