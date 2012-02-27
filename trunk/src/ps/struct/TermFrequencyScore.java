package ps.struct;

/**
 * Represents the term frequency score.
 */
public class TermFrequencyScore {

	private double title;
	private double abstractText;
	private double body;

	public double getTitle() {
		return title;
	}

	public void setTitle(double title) {
		this.title = title;
	}

	public double getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(double abstractText) {
		this.abstractText = abstractText;
	}

	public double getBody() {
		return body;
	}

	public void setBody(double body) {
		this.body = body;
	}

	public TermFrequencyScore() {
		this.title = 0d;
		this.abstractText = 0d;
		this.body = 0d;
	}

	public TermFrequencyScore(double title, double abstractText, double body) {
		this.title = title;
		this.abstractText = abstractText;
		this.body = body;
	}

}
