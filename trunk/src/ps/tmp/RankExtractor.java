package ps.tmp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ps.extractors.ArnetMinerExtractor;
import ps.extractors.GoogleScholarExtractor;
import ps.extractors.MicrosoftAcademicSearchExtractor;
import ps.persistence.PersistenceController2;
import ps.struct.PubInfoSummary;
import ps.struct.PublicationInfo;

public class RankExtractor {

	public static void main(String[] args) throws Exception {

		// COMMON STUFF GOES HERE:
		int queryId = 1125;
		List<PubInfoSummary> pubInfoSummaryList = PersistenceController2.fetchPubInfoSummaryForQuery(queryId);
		String query = PersistenceController2.fetchQueryForId(queryId);
		
//		// Microsoft Academic Search
//		List<PublicationInfo> msList = MicrosoftAcademicSearchExtractor.fetchPublicationInfoList(query);
//		Map<PublicationInfo, Integer> msMap = fillMap(msList, pubInfoSummaryList);
//		printMap(msMap);
		
		
//		// ArnetMiner
//		List<PublicationInfo> amList = ArnetMinerExtractor.fetchPublicationInfoList(query);
//		Map<PublicationInfo, Integer> amMap = fillMap(amList, pubInfoSummaryList);
//		printMap(amMap);
		
		// GoogleScholar
//		List<PublicationInfo> gsList = GoogleScholarExtractor.fetchPublicationInfoList(query);
//		Map<PublicationInfo, Integer> gsMap = fillMap(gsList, pubInfoSummaryList);
//		printMap(gsMap);
		
	}
	
	
	private static Map<PublicationInfo, Integer> fillMap(List<PublicationInfo> msList, List<PubInfoSummary> pubInfoSummaryList){
		Map<Integer, PublicationInfo> msRankMap = new HashMap<Integer, PublicationInfo>();
		int rank = 1;
		for (PublicationInfo p : msList) {
			msRankMap.put(rank, p);
			rank++;
		}
		return fillPublicationInfoQueryResIdMap(msRankMap, pubInfoSummaryList);
	}
	
	private static Map<PublicationInfo, Integer> fillPublicationInfoQueryResIdMap(Map<Integer, PublicationInfo> rankMap,
			List<PubInfoSummary> pubInfoSummaryList) {
		Map<PublicationInfo, Integer> pubInfoQueryResMap = new HashMap<PublicationInfo, Integer>();
		for (Iterator<Integer> iterator = rankMap.keySet().iterator(); iterator.hasNext();) {
			Integer r = (Integer) iterator.next();
			PublicationInfo p = rankMap.get(r);
			for (PubInfoSummary pubInfoSummary : pubInfoSummaryList) {
				String pubInfoSummaryTitle = pubInfoSummary.getTitle().trim();
				if (pubInfoSummaryTitle.equalsIgnoreCase(p.getTitle())) {
					List<String> pubInfoSummaryAuthList = pubInfoSummary.getAuthList();
					if (pubInfoSummaryAuthList != null && pubInfoSummaryAuthList.size() > 0) {
						String pubInfoSummaryAuth = pubInfoSummaryAuthList.get(0);
						String[] pubInfoSummaryAuthArr = pubInfoSummaryAuth.split(" ");
						if (pubInfoSummaryAuthArr.length > 1) {
							int lastElem = pubInfoSummaryAuthArr.length - 1;
							pubInfoSummaryAuth = pubInfoSummaryAuthArr[lastElem]; // take last name
						}
						for (String publicationAuth : p.getAuthors()) {
							String[] publicationAuthArr = publicationAuth.split(" ");
							if (publicationAuthArr.length > 1) {
								int lastElem = publicationAuthArr.length - 1;
								publicationAuth = publicationAuthArr[lastElem]; // take last name
							}
							if (pubInfoSummaryAuth.equalsIgnoreCase(publicationAuth)) {
								pubInfoQueryResMap.put(p, pubInfoSummary.getQueryResultId());
								break;
							}
						}
						pubInfoQueryResMap.put(p, pubInfoSummary.getQueryResultId());
						break;
					} else {
						pubInfoQueryResMap.put(p, pubInfoSummary.getQueryResultId());
						break;
					}
				}
			}
		}
		return pubInfoQueryResMap;
	}
	
	private static void printMap(Map<PublicationInfo, Integer> map) {
		Iterator<PublicationInfo> it = map.keySet().iterator();
		while (it.hasNext()) {
			PublicationInfo pubInfo = it.next();
			Integer queryResultsId = map.get(pubInfo);
			System.out.println(queryResultsId + " -> " + pubInfo.getTitle());
		}
	}

}
