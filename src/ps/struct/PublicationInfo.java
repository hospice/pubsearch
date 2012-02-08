package ps.struct;

import java.util.List;

/**
 * Publication info structure.
 */
public class PublicationInfo {

	private String url;

	private String title;

	private List<String> authors;

	private List<AcmTopic> topics;

	private String abstractTxt;

	private Integer numOfCitations;

	private Integer yearOfPublication;

	private Boolean isEligible;
	
	private String html;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public String getAbstractTxt() {
		return abstractTxt;
	}

	public void setAbstractTxt(String abstractTxt) {
		this.abstractTxt = abstractTxt;
	}

	public Integer getNumOfCitations() {
		return numOfCitations;
	}

	public void setNumOfCitations(Integer numOfCitations) {
		this.numOfCitations = numOfCitations;
	}

	public List<AcmTopic> getTopics() {
		return topics;
	}

	public void setTopics(List<AcmTopic> topics) {
		this.topics = topics;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getYearOfPublication() {
		return yearOfPublication;
	}

	public void setYearOfPublication(Integer yearOfPublication) {
		this.yearOfPublication = yearOfPublication;
	}

	public Boolean getIsEligible() {
		return isEligible;
	}

	public void setIsEligible(Boolean isEligible) {
		this.isEligible = isEligible;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}
	
	public PublicationInfo(String title, String url, Integer numOfCitations, Integer yearOfPublication, String html){
		this.title = title;
		this.url = url;
		this.numOfCitations = numOfCitations;
		this.yearOfPublication = yearOfPublication;
		this.html = html;
	}
	
	public PublicationInfo(String title, String url, List<String> authors, Integer yearOfPublication){
		this.title = title;
		this.url = url;
		this.authors = authors;
		this.yearOfPublication = yearOfPublication;
	}
	
	public PublicationInfo(String title, String url, List<String> authors){
		this.title = title;
		this.url = url;
		this.authors = authors;
	}
	
	public PublicationInfo(){
		super();
	}
	
}
