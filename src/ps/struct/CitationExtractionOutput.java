package ps.struct;

public class CitationExtractionOutput {

	private Integer numOfCitations;
	private String citedByUrl;

	public Integer getNumOfCitations() {
		return numOfCitations;
	}

	public void setNumOfCitations(Integer numOfCitations) {
		this.numOfCitations = numOfCitations;
	}

	public String getCitedByUrl() {
		return citedByUrl;
	}

	public void setCitedByUrl(String citedByUrl) {
		this.citedByUrl = citedByUrl;
	}

	public CitationExtractionOutput(Integer numOfCitations, String citedByUrl) {
		this.numOfCitations = numOfCitations;
		this.citedByUrl = citedByUrl;
	}

}
