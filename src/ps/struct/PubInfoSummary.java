package ps.struct;

import java.util.List;

public class PubInfoSummary {

	private int queryResultId;
	private String title;
	private String authors;
	private List<String> authList;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public List<String> getAuthList() {
		return authList;
	}

	public void setAuthList(List<String> authList) {
		this.authList = authList;
	}
	
	public int getQueryResultId() {
		return queryResultId;
	}

	public void setQueryResultId(int queryResultId) {
		this.queryResultId = queryResultId;
	}

	public PubInfoSummary(int queryResultId, String title, String authors){
		this.queryResultId = queryResultId;
		this.title = title;
		this.authors = authors;
	}
	
}
