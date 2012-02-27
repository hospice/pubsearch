package ps.extractors;

/**
 * Contains all constants used in the extractor classes.
 */
public class ExtrConstants {

	// HTML
	public final static String A_HREF_BEG = "<a href=\"";
	public final static String A_HREF_END = "</a>";
	public final static String H3_TAG = "<h3>";
	public final static String OPEN_A_TAG = "<a";
	public final static String RIGHT_TAG_CHAR = ">";
	public final static String URL_ENC_SPACE = "%20";
	public final static String CLOSE_DIV = "</div>";
	
	// ARNETMINER
	public final static String ARN_QUERY_PREF = "http://arnetminer.org/pubsearch.do?keyword=";
	public final static String ARN_RES_ITEM = "<div id=\"publist\">";
	public final static String ARN_AUTH_START = "<a href=\"expertisesearch.do";
	public final static String ARN_AUTH_URL_PREF = "viewpub.do?pid=";
	public final static String ARN_AUTH_SECT_START = "<span>Authors: </span>";
	public final static String ARN_PUBLISH_YEAR = "<span>Published year:</span> ";

 	// GOOGLE SCHOLAR
	public final static String GS_RES_SECTION = "Results <b>";
	public final static String GS_RES_TITLE_BEG_PREF = "'res','";
	public final static String GS_RES_TITLE_BEG_SUFF = "')\">";
	public final static String GS_RES_BEG = "<h3 class=\"gs_rt\">";
	public final static String GS_RES_END = "<div class=gs_r>";
	public final static String GS_RES_PAGE_END = "Create email alert";
	public final static String GS_DATE_BEG = "<div class=gs_a>";
	public final static String GS_AUTH_BEG = "<div class=gs_a>";
	public final static String GS_CITED_BY = "Cited by";
	public final static String GS_QUERY_PREF = "http://scholar.google.gr/scholar?hl=en&q=";
	public final static String GS_QUERY_SUFF = "&btnG=%C1%ED%E1%E6%DE%F4%E7%F3%E7&as_ylo=&as_vis=0";
	public final static int GS_TOTAL_RES_PER_PAGE = 10;

	// MICROSOFT ACADEMIC SEARCH
	public static final String MS_RES_BEG = "<li class=\"paper-item\">";
	public static final String MS_RES_END = "</li>";
	public static final String MS_URL_PREF = "http://academic.research.microsoft.com";
	public static final String MS_AUTH_PREF = "http://academic.research.microsoft.com/Author/";
	public static final String MS_QUERY_PREF = "http://academic.research.microsoft.com/Search?query=";
	public static final String PUBLICATION_URL_PART = "Publication/";
	
}
