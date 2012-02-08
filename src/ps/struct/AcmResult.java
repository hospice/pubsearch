package ps.struct;

public class AcmResult {

	private String title;

	private String url;

	private Integer yearOfPublication;

	public AcmResult(String title, String url, int yearOfPublication) {
		super();
		this.title = title;
		this.url = url;
		this.yearOfPublication = yearOfPublication;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public AcmResult() {
		super();
	}

	public Integer getYearOfPublication() {
		return yearOfPublication;
	}

	public void setYearOfPublication(Integer yearOfPublication) {
		this.yearOfPublication = yearOfPublication;
	}

}
