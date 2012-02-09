package ps.tmp;

public enum SearchEngineEnum {

	GOOGLE_SCHOLAR("gs"), ARNETMINER("am"), MISCROSOFT_ACADEMIC_SEARCH("ms");

	private String shortName;

	private SearchEngineEnum(String sn) {
		this.shortName = sn;
	}

	public String getShortName() {
		return this.shortName;
	}

}
