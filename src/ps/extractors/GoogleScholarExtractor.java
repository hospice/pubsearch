package ps.extractors;

import java.util.ArrayList;
import java.util.List;

import ps.struct.PublicationInfo;
import ps.util.CrawlUtils;
import ps.util.IOUtils;
import ps.util.StringUtils;

/**
 * Performs the extraction of all publication results from Google Scholar.
 */
public class GoogleScholarExtractor {

	public static void main(String[] args) throws Exception {
		String pathname = "C:/gs_test.html";
		String html = IOUtils.readFileFromPath(pathname);
		String title = "Web Information Retrieval";
		String author = "Lewandowski";
		String specificHtml = identifySpecificResHtml(html, title, author);
		System.out.println(specificHtml);
	}

	/**
	 * Extracts all publication results related information from the HTML of the results page.
	 */
	public static List<PublicationInfo> extractPublicationResults(String query) throws Exception {
		List<PublicationInfo> results = new ArrayList<PublicationInfo>();

		// ******* FIXME ***********
		// String html = CrawlUtils.fetchHtmlCodeForUrl(constructQuery(query));
		String path = "C:/gs_test5.html";
		String html = IOUtils.readFileFromPath(path);
		// **************************

		int from = html.indexOf(ExtrConstants.GS_RES_SECTION);
		if (from > -1) {
			int i = 0;
			while (i < ExtrConstants.GS_TOTAL_RES_PER_PAGE) {
				String section = "";
				int beg = html.indexOf(ExtrConstants.GS_RES_BEG);
				int end = 0;
				if (beg > -1) {
					beg = beg + ExtrConstants.GS_RES_BEG.length();
					end = html.indexOf(ExtrConstants.GS_RES_END, beg);
					if (end > -1) {
						section = html.substring(beg, end);
					} else {
						if (i == ExtrConstants.GS_TOTAL_RES_PER_PAGE - 1) {
							end = html.indexOf(ExtrConstants.GS_RES_PAGE_END, beg);
							section = html.substring(beg, end);
						} else {
							throw new Exception("ENDING OF RESULT NOT FOUND!");
						}
					}
				} else {
					throw new Exception("BEGINNING OF RESULT NOT FOUND!");
				}
				String url = extractUrlFromContent(section);
				Integer year = extractYearOfPublicationFromContent(section);
				Integer citations = extractNumOfCitationsFromContent(section);
				String resStart = ExtrConstants.GS_RES_TITLE_BEG_PREF + i + ExtrConstants.GS_RES_TITLE_BEG_SUFF;
				int resultStart = section.indexOf(resStart);
				if (resultStart > -1) {
					resultStart = resultStart + resStart.length();
					int resultEnd = section.indexOf(ExtrConstants.A_HREF_END, resultStart);
					if (resultEnd > -1) {
						String title = StringUtils.stripTextFromHtml(section.substring(resultStart, resultEnd));
						PublicationInfo p = new PublicationInfo(title, url, citations, year, section);
						results.add(p);
					} else {
						throw new Exception("END OF RESULTS NOT FOUND!");
					}

					html = html.substring(end);
					i++;
				} else {
					throw new Exception("BEGINNING OF RESULTS NOT FOUND!");
				}
			}
		} else {
			throw new Exception("NO RESULTS SECTION FOUND IN HTML!");
		}
		return results;
	}

	/**
	 * Constructs the query to be submitted to GoogleScholar.
	 */
	public static String constructQuery(String orgQuery) {
		return ExtrConstants.GS_QUERY_PREF + orgQuery.replaceAll(" ", "+") + ExtrConstants.GS_QUERY_SUFF;
	}

	/**
	 * Updates the provided publication info with details fetched from Google Scholar.
	 */
	public static void updatePublicationCitationsAndDate(PublicationInfo publicationInfo) throws Exception {
		String html = CrawlUtils.fetchHtmlCodeForUrl(constructQuery(publicationInfo.getTitle()));
		int from = html.indexOf(ExtrConstants.GS_RES_SECTION);
		int i = 0;
		if (from > -1) {
			while (i < ExtrConstants.GS_TOTAL_RES_PER_PAGE) {
				String section = "";
				int beg = html.indexOf(ExtrConstants.GS_RES_BEG);
				int end = 0;
				if (beg > -1) {
					beg = beg + ExtrConstants.GS_RES_BEG.length();
					end = html.indexOf(ExtrConstants.GS_RES_END, beg);
					if (end > -1) {
						section = html.substring(beg, end);
					} else {
						if (i == (ExtrConstants.GS_TOTAL_RES_PER_PAGE - 1)) {
							end = html.indexOf(ExtrConstants.GS_RES_PAGE_END, beg);
							section = html.substring(beg, end);
						} else {
							throw new Exception("END OF RESULTS NOT FOUND!");
						}
					}
				} else {
					throw new Exception("BEGINNING OF RESULTS NOT FOUND!");
				}
				String titleSection = ExtrConstants.GS_RES_TITLE_BEG_PREF + i + ExtrConstants.GS_RES_TITLE_BEG_SUFF;
				int titleBeg = section.indexOf(titleSection);
				if (titleBeg > -1) {
					titleBeg = titleBeg + titleSection.length();
					int titleEnd = section.indexOf(ExtrConstants.A_HREF_END, titleBeg);
					if (titleEnd > -1) {
						String title = StringUtils.stripTextFromHtml(section.substring(titleBeg, titleEnd));
						if (title.trim().equalsIgnoreCase(publicationInfo.getTitle().trim())) {
							publicationInfo.setYearOfPublication(extractYearOfPublicationFromContent(section));
							publicationInfo.setNumOfCitations(extractNumOfCitationsFromContent(section));
							return;
						}
					} else {
						throw new Exception("END OF TITLE NOT FOUND!");
					}
					html = html.substring(end);
					i++;
				} else {
					throw new Exception("BEGINNING OF TITLE NOT FOUND!");
				}
			}
		} else {
			throw new Exception("NO RESULTS SECTION FOUND IN HTML!");
		}
	}

	/**
	 * Extracts the URL from the specified HTML.
	 */
	private static String extractUrlFromContent(String html) {
		int urlFrom = html.indexOf(ExtrConstants.A_HREF_BEG) + ExtrConstants.A_HREF_BEG.length();
		int urlTo = html.indexOf("\"", urlFrom);
		return html.substring(urlFrom, urlTo);
	}

	/**
	 * Extracts the publication date from the specified HTML.
	 */
	private static Integer extractYearOfPublicationFromContent(String html) {
		int dateFrom = html.indexOf(ExtrConstants.GS_DATE_BEG) + ExtrConstants.GS_DATE_BEG.length();
		int dateTo = html.indexOf(ExtrConstants.CLOSE_DIV, dateFrom);
		Integer yearOfPublication = StringUtils.extractYearFromText(html.substring(dateFrom, dateTo));
		if (yearOfPublication != null) {
			if (yearOfPublication.compareTo(1900) == -1 || yearOfPublication.compareTo(2021) == 1) {
				yearOfPublication = null;
			}
		}
		return yearOfPublication;
	}

	/**
	 * Extracts the number of citations from the specified HTML.
	 */
	private static Integer extractNumOfCitationsFromContent(String html) {
		Integer numOfCitations = null;
		int from = html.indexOf(ExtrConstants.GS_CITED_BY);
		if (from > -1) {
			from = from + ExtrConstants.GS_CITED_BY.length();
			int to = html.indexOf("</a>", from);
			String citAsText = html.substring(from, to).trim();
			try {
				numOfCitations = Integer.parseInt(citAsText);
			} catch (NumberFormatException e) {
				// do nothing, numOfCitations is already null...
			}
		}
		return numOfCitations;
	}

	/**
	 * Extracts the publication HTML section from Google Scholar, by identifying the result that has a matching title
	 * with the provided title.
	 */
	public static String extractPublicationHtml(String title, String author) throws Exception {
		List<PublicationInfo> res = GoogleScholarExtractor.extractPublicationResults(title);
		String html = null;
		for (PublicationInfo p : res) {
			if (p.getTitle().equalsIgnoreCase(title)) {
				for (String a : p.getAuthors()) {
					if (a.equalsIgnoreCase(author)) {
						html = p.getHtml();
						break;
					}
				}
			}
		}
		if (html == null) {
			throw new Exception("The publication information for : " + title + " has not been found in Google Scholar!");
		}
		return html;
	}

	/**
	 * Identifies the HTML section for the specific title and author. The author name is provided for disambiguation
	 * among different publications with the same title.
	 * 
	 * @param html
	 *            , the Google Scholar results HTML
	 * @param title
	 *            , the publication title we are searching for
	 * @param author
	 *            , the name of the first author of the publication
	 * @return the HTML section from the results HTML page containing all information for the specific publication
	 * @throws Exception
	 */
	public static String identifySpecificResHtml(String html, String title, String author) throws Exception {
		String copy = new String(html);
		String htmlSection = "";
		int from = copy.indexOf(ExtrConstants.GS_RES_SECTION);
		if (from > -1) {
			int i = 0;
			while (i < ExtrConstants.GS_TOTAL_RES_PER_PAGE) {
				String section = "";
				int beg = copy.indexOf(ExtrConstants.GS_RES_BEG);
				int end = 0;
				if (beg > -1) {
					beg = beg + ExtrConstants.GS_RES_BEG.length();
					end = copy.indexOf(ExtrConstants.GS_RES_END, beg);
					if (end > -1) {
						section = copy.substring(beg, end);
					} else {
						if (i == ExtrConstants.GS_TOTAL_RES_PER_PAGE - 1) {
							end = copy.indexOf(ExtrConstants.GS_RES_PAGE_END, beg);
							section = copy.substring(beg, end);
						} else {
							throw new Exception("ENDING OF RESULT NOT FOUND!");
						}
					}
				} else {
					throw new Exception("BEGINNING OF RESULT NOT FOUND!");
				}
				String resStart = ExtrConstants.GS_RES_TITLE_BEG_PREF + i + ExtrConstants.GS_RES_TITLE_BEG_SUFF;
				int resultStart = section.indexOf(resStart);
				if (resultStart > -1) {
					resultStart = resultStart + resStart.length();
					int resultEnd = section.indexOf(ExtrConstants.A_HREF_END, resultStart);
					if (resultEnd > -1) {
						String titleFound = StringUtils.stripTextFromHtml(section.substring(resultStart, resultEnd)
								.trim());
						if (titleFound.equalsIgnoreCase(title)) {
							String authorsLastName = extractFirstAuthorLastName(section);
							if (authorsLastName.length() > 1 && author.contains(authorsLastName)) {
								System.out.println("AUTHOR NAME: " + authorsLastName);
								return section;
							}
						}
					} else {
						throw new Exception("END OF RESULTS NOT FOUND!");
					}

					copy = copy.substring(end);
					i++;
				} else {
					throw new Exception("BEGINNING OF RESULTS NOT FOUND!");
				}
			}
		} else {
			throw new Exception("NO RESULTS SECTION FOUND IN HTML!");
		}
		return htmlSection;
	}

	/**
	 * Extracts the first author's last name in the provided HTML corresponding to the result section HTML code.
	 */
	private static String extractFirstAuthorLastName(String resSectionHtml) {
		String html = new String(resSectionHtml);
		String firstAuth = "";
		int authSectionBeg = html.indexOf(ExtrConstants.GS_AUTH_BEG);
		if (authSectionBeg > -1) {
			authSectionBeg += ExtrConstants.GS_AUTH_BEG.length();
			String authUrl = "<a href=\"/citations?";
			int authorUrlPos = html.indexOf(authUrl);
			int from = 0;
			int to = 0;
			// case where citation information exists in Google Scholar (so we encounter a link)
			if(authorUrlPos > -1){
				from = html.indexOf(">", authorUrlPos) + 1;
				to = html.indexOf("<", from);
			}else{
				from = authSectionBeg;
				to = html.indexOf(" -", authorUrlPos);
			}
			firstAuth = html.substring(from, to);
		}
		String lastName = firstAuth.split(" ")[1];
		lastName = StringUtils.stripTextFromNonChars(lastName);
		return lastName;
	}

}
