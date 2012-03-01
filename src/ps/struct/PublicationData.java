package ps.struct;

import ps.util.AcronymExtractorUtils;
import ps.util.StringUtils;
import ps.util.TermFrequencyUtils;

public class PublicationData {

	private String title;
	private String[] pubSections;
	private String abstractText;
	private String body;
	private String[] keywordsArr;
	private String[] acronymArr;
	private String[] queryTokens;

	public PublicationData(String query, String pubTitle, String publicationText) throws Exception {
		this.title = pubTitle.toLowerCase();
		this.pubSections = TermFrequencyUtils.extractAbstractBodyIndexTerms(publicationText);
		this.abstractText = pubSections[0];
		this.body = pubSections[1];
		this.keywordsArr = TermFrequencyUtils.tokenizeKeywords(pubSections[2]);
		//this.acronymArr = AcronymExtractorUtils.findAcronym(query, title, abstractText, body);
		this.queryTokens = StringUtils.splitStringIntoTokens(query);
	}
	
	public PublicationData(String[] queryTokens, String pubTitle, String abstractText, String body){
		this.queryTokens = queryTokens;
		this.title = pubTitle.toLowerCase();
		this.abstractText = abstractText.toLowerCase();
		this.body = body.toLowerCase();
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String[] getPubSections() {
		return pubSections;
	}
	public void setPubSections(String[] pubSections) {
		this.pubSections = pubSections;
	}
	public String getAbstractText() {
		return abstractText;
	}
	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String[] getAcronymArr() {
		return acronymArr;
	}
	public void setAcronymArr(String[] acronymArr) {
		this.acronymArr = acronymArr;
	}
	public String[] getQueryTokens() {
		return queryTokens;
	}
	public void setQueryTokens(String[] queryTokens) {
		this.queryTokens = queryTokens;
	}
	public String[] getKeywordsArr() {
		return keywordsArr;
	}
	public void setKeywordsArr(String[] keywordsArr) {
		this.keywordsArr = keywordsArr;
	}
}
