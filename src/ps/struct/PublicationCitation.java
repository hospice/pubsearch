package ps.struct;

public class PublicationCitation {

	private int resultId;

	private int citationCount;

	private int yearOfPublication;

	public PublicationCitation(int pubId, int citationCount, int yearOfPublication) {
		super();
		this.resultId = pubId;
		this.citationCount = citationCount;
		this.yearOfPublication = yearOfPublication;
	}

	public int getResultId() {
		return resultId;
	}

	public void setResultId(int pubId) {
		this.resultId = pubId;
	}

	public int getCitationCount() {
		return citationCount;
	}

	public void setCitationCount(int citationCount) {
		this.citationCount = citationCount;
	}

	public int getYearOfPublication() {
		return yearOfPublication;
	}

	public void setYearOfPublication(int yearOfPublication) {
		this.yearOfPublication = yearOfPublication;
	}

}
