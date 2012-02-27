package ps.persistence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.NamingException;

import ps.enumerators.QueryResultEnum;
import ps.enumerators.StatusEnum;
import ps.enumerators.TableNameEnum;
import ps.struct.AcmTopic;
import ps.struct.CliqueDetails;
import ps.struct.CliquesPerGraph;
import ps.struct.GraphDetails;
import ps.struct.Period;
import ps.struct.PublicationCitation;
import ps.struct.PublicationInfo;
import ps.struct.Query;
import ps.util.CrawlUtils;

/**
 * Provides persistence related functionality.
 */
public class PersistenceController {
	private static final Map<String, Integer> TOPICS_MAP = loadTopics();

	/**
	 * Loads all topics.
	 */
	public static Map<String, Integer> loadTopics() {
		Map<String, Integer> m = new HashMap<String, Integer>();
		Connection connection;
		try {
			connection = ConnectionController.getConnection();
			String query = "SELECT * FROM TOPICS;";
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Integer id = rs.getInt(1);
				String code = rs.getString(2);
				m.put(code, id);
			}
			PersistenceUtils.closeRsPs(rs, ps);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return m;
	}

	/**
	 * Gets all query results for the specified query.
	 */
	public static List<Integer> getAllQueryResultsForQuery(int queryId) throws ClassNotFoundException, SQLException,
			IOException {
		List<Integer> l = new ArrayList<Integer>();
		Connection connection = ConnectionController.getConnection();
		String query = "select id from query_results where query_id = " + queryId;
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			l.add(rs.getInt(1));
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return l;
	}

	/**
	 * Fetches all query results information for the specified query.
	 */
	public static List<PublicationCitation> getAllQueryResultsInfoForQuery(int queryId) throws ClassNotFoundException,
			SQLException, IOException {
		List<PublicationCitation> res = new ArrayList<PublicationCitation>();
		Connection connection = ConnectionController.getConnection();
		String query = "select id, num_of_citations, year_of_publication from query_results where query_id = "
				+ queryId + " and is_eligible = 1";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int pubId = rs.getInt(1);
			int citationCount = rs.getInt(2);
			int year = rs.getInt(3);
			PublicationCitation publicationCitation = new PublicationCitation(pubId, citationCount, year);
			res.add(publicationCitation);
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return res;
	}

	/**
	 * Fetches all default query results.
	 */
	public static List<PublicationCitation> getAllDefaultQueryResults(int queryId) throws ClassNotFoundException,
			SQLException, IOException {
		List<PublicationCitation> res = new ArrayList<PublicationCitation>();
		Connection connection = ConnectionController.getConnection();
		String query = "select id, num_of_citations, year_of_publication from query_results_default where query_id = "
				+ queryId + " and is_eligible = 1";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int pubId = rs.getInt(1);
			int citationCount = rs.getInt(2);
			int year = rs.getInt(3);
			PublicationCitation publicationCitation = new PublicationCitation(pubId, citationCount, year);
			res.add(publicationCitation);
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return res;
	}

	/**
	 * Filters the specified cliques.
	 */
	public static List<CliquesPerGraph> filterCliques(List<CliquesPerGraph> orgList, List<Integer> topicsList)
			throws ClassNotFoundException, SQLException, IOException {
		List<CliquesPerGraph> filteredList = new ArrayList<CliquesPerGraph>();
		for (CliquesPerGraph orgClique : orgList) {
			boolean isPartOfClique = true;
			List<Integer> orgCliqueTopics = orgClique.getCliqueDetails().getTopicsList();
			for (Integer t : topicsList) {
				if (!orgCliqueTopics.contains(t)) {
					isPartOfClique = false;
				}
			}
			if (isPartOfClique) {
				filteredList.add(orgClique);
			}
		}
		return filteredList;
	}

	/**
	 * Loads all cliques in persistence.
	 */
	public static List<CliquesPerGraph> loadAllCliques() throws ClassNotFoundException, SQLException, IOException {
		List<CliquesPerGraph> l = new ArrayList<CliquesPerGraph>();
		Connection connection = ConnectionController.getConnection();
		String query = "select * from cliques_per_graph";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			// create new cliques per graph object
			CliquesPerGraph cliquesPerGraph = new CliquesPerGraph();
			// clique details-related
			int cliqueDetailsId = rs.getInt(2);
			CliqueDetails cliqueDetails = new CliqueDetails();
			cliqueDetails = getCliqueDetailsForId(cliqueDetailsId);
			cliquesPerGraph.setCliqueDetails(cliqueDetails);
			// graph details-related
			int graphDetailsId = rs.getInt(3);
			GraphDetails graphDetails = new GraphDetails();
			graphDetails = getGraphDetailsForId(graphDetailsId);
			cliquesPerGraph.setGraphDetails(graphDetails);
			// add cliques per graph in list
			l.add(cliquesPerGraph);

		}
		PersistenceUtils.closeRsPs(rs, ps);
		return l;
	}

	/**
	 * Returns the clique details for the specified id.
	 */
	private static CliqueDetails getCliqueDetailsForId(Integer id) throws ClassNotFoundException, SQLException,
			IOException {
		CliqueDetails cliqueDetails = new CliqueDetails();
		Connection connection = ConnectionController.getConnection();
		String query = "select * from clique_details where id = " + id;
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		double value = rs.getDouble(2);
		cliqueDetails.setValue(value);
		int cliqueId = rs.getInt(3);
		List<Integer> topicsList = getAllTopicsForCliqueId(cliqueId);
		cliqueDetails.setTopicsList(topicsList);
		return cliqueDetails;
	}

	/**
	 * Returns all topics for the specified clique id.
	 */
	private static List<Integer> getAllTopicsForCliqueId(Integer id) throws ClassNotFoundException, SQLException,
			IOException {
		List<Integer> l = new ArrayList<Integer>();
		Connection connection = ConnectionController.getConnection();
		String query = "select * from clique where id = " + id;
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			l.add(rs.getInt(2));
		}
		return l;
	}

	/**
	 * Returns the graph details for the specified id.
	 */
	private static GraphDetails getGraphDetailsForId(Integer id) throws ClassNotFoundException, SQLException,
			IOException {
		GraphDetails graphDetails = new GraphDetails();
		Connection connection = ConnectionController.getConnection();
		String query = "select * from graph_details where id = " + id;
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		// Period-related
		Integer periodId = rs.getInt(2);
		Period[] pArr = getPeriodsForId(periodId);
		graphDetails.setPeriod1(pArr[0]);
		graphDetails.setPeriod2(pArr[1]);
		// Association case-related
		Integer assocCaseId = rs.getInt(3);
		Integer[] assocCaseArr = getAssocCasesForId(assocCaseId);
		graphDetails.setAssocCase1(assocCaseArr[0]);
		graphDetails.setAssocCase2(assocCaseArr[1]);
		return graphDetails;
	}

	/**
	 * Returns the association case details for the specified id.
	 */
	private static Integer[] getAssocCasesForId(Integer id) throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select * from assoc_case where id = " + id;
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		Integer case1 = rs.getInt(2);
		Integer case2 = rs.getInt(3);
		Integer[] assocCaseArr = new Integer[2];
		assocCaseArr[0] = case1;
		assocCaseArr[1] = case2;
		return assocCaseArr;
	}

	/**
	 * Returns the period details for the specified id.
	 */
	private static Period[] getPeriodsForId(Integer id) throws SQLException, ClassNotFoundException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select * from period where id = " + id;
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		Integer from1 = rs.getInt(2);
		Integer to1 = rs.getInt(3);
		Integer from2 = rs.getInt(4);
		Integer to2 = rs.getInt(5);
		Period p1 = new Period(from1, to1);
		Period p2 = null;
		if (from2 != null && to2 != null) {
			p2 = new Period(from2, to2);
		}
		Period[] pArr = new Period[2];
		pArr[0] = p1;
		pArr[1] = p2;
		return pArr;
	}

	/**
	 * Saves the specific clique for graph.
	 */
	public static void saveCliquesForGraph(CliquesPerGraph cliquesPerGraph) throws ClassNotFoundException,
			SQLException, IOException, NamingException {
		double value = cliquesPerGraph.getCliqueDetails().getValue();
		List<Integer> topicsList = cliquesPerGraph.getCliqueDetails().getTopicsList();
		Period p1 = cliquesPerGraph.getGraphDetails().getPeriod1();
		Period p2 = cliquesPerGraph.getGraphDetails().getPeriod2();
		Integer assocCase1 = cliquesPerGraph.getGraphDetails().getAssocCase1();
		Integer assocCase2 = cliquesPerGraph.getGraphDetails().getAssocCase2();
		saveCliquesPerGraph(value, topicsList, p1, p2, assocCase1, assocCase2);
	}

	/**
	 * Saves the specific cliques per graph details.
	 */
	private static Integer saveCliquesPerGraph(double value, List<Integer> topicsList, Period p1, Period p2,
			Integer assocCase1, Integer assocCase2) throws ClassNotFoundException, SQLException, IOException,
			NamingException {
		Integer cliquesPerGraphId = getNextId(TableNameEnum.CLIQUES_PER_GRAPH);
		Integer cliqueDetailsId = saveCliqueDetails(value, topicsList);
		Integer graphDetailsId = saveGraphDetails(p1, p2, assocCase1, assocCase2);
		Connection connection = ConnectionController.getConnection();
		String query = "insert into cliques_per_graph values (" + cliquesPerGraphId + ", " + cliqueDetailsId + ", "
				+ graphDetailsId + ")";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.executeUpdate();
		PersistenceUtils.closeRsPs(null, ps);
		return cliquesPerGraphId;
	}

	/**
	 * Saves the specific clique details.
	 */
	private static Integer saveCliqueDetails(double value, List<Integer> topicsList) throws ClassNotFoundException,
			SQLException, IOException, NamingException {
		Integer cliqueDetailsId = getNextId(TableNameEnum.CLIQUE_DETAILS);
		Integer cliqueId = saveClique(topicsList);
		Connection connection = ConnectionController.getConnection();
		String query = "insert into clique_details values (" + cliqueDetailsId + ", " + value + ", " + cliqueId + ")";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.executeUpdate();
		PersistenceUtils.closeRsPs(null, ps);
		return cliqueDetailsId;
	}

	/**
	 * Saves the specific clique.
	 */
	private static Integer saveClique(List<Integer> topicsList) throws ClassNotFoundException, SQLException,
			IOException, NamingException {
		if (topicsList == null || topicsList.isEmpty()) {
			return null;
		}
		Integer cliqueId = getNextId(TableNameEnum.CLIQUE);
		Connection connection = ConnectionController.getConnection();
		String queryPt1 = "insert into clique values (" + cliqueId;
		PreparedStatement ps = null;
		for (Integer topic : topicsList) {
			String queryPt2 = ", " + topic + ")";
			ps = connection.prepareStatement(queryPt1 + queryPt2);
			ps.executeUpdate();
		}
		PersistenceUtils.closeRsPs(null, ps);
		return cliqueId;
	}

	/**
	 * Saves the specific graph details.
	 */
	private static Integer saveGraphDetails(Period p1, Period p2, Integer assocCase1, Integer assocCase2)
			throws ClassNotFoundException, SQLException, IOException, NamingException {
		Integer periodId = savePeriod(p1, p2);
		Integer associationCaseId = saveAssociationCase(assocCase1, assocCase2);
		Integer graphDetailsId = getGraphDetailsId(periodId, associationCaseId);
		if (graphDetailsId == null) {
			graphDetailsId = getNextId(TableNameEnum.GRAPH_DETAILS);
			Connection connection = ConnectionController.getConnection();
			String query = "insert into graph_details values(" + graphDetailsId + ", " + periodId + ", "
					+ associationCaseId + ")";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.executeUpdate();
			PersistenceUtils.closeRsPs(null, ps);
		}
		return graphDetailsId;
	}

	/**
	 * Saves the specific association case in persistence.
	 */
	private static Integer saveAssociationCase(Integer assocCase1, Integer assocCase2) throws ClassNotFoundException,
			SQLException, IOException, NamingException {
		Integer assocCaseId = getAssocCaseId(assocCase1, assocCase2);
		if (assocCaseId == null) {
			assocCaseId = getNextId(TableNameEnum.ASSOC_CASE);
			Connection connection = ConnectionController.getConnection();
			String query = "insert into assoc_case values(" + assocCaseId + ", " + assocCase1;
			if (assocCase2 == null) {
				query += ", NULL)";
			} else {
				query += ", " + assocCase2 + ")";
			}
			PreparedStatement ps = connection.prepareStatement(query);
			ps.executeUpdate();
			PersistenceUtils.closeRsPs(null, ps);
		}
		return assocCaseId;

	}

	/**
	 * Saves the specified period in persistence.
	 */
	private static Integer savePeriod(Period p1, Period p2) throws ClassNotFoundException, SQLException, IOException,
			NamingException {
		Integer periodId = getPeriodId(p1, p2);
		if (periodId == null) {
			periodId = getNextId(TableNameEnum.PERIOD);
			Connection connection = ConnectionController.getConnection();
			String query = "";
			if (p2 != null) {
				query = "insert into period values(" + periodId + "," + p1.getFrom() + ", " + p1.getTo() + ", "
						+ p2.getFrom() + ", " + p2.getTo() + ")";
			} else {
				query = "insert into period values(" + periodId + "," + p1.getFrom() + ", " + p1.getTo()
						+ ", NULL, NULL)";
			}
			PreparedStatement ps = connection.prepareStatement(query);
			ps.executeUpdate();
			PersistenceUtils.closeRsPs(null, ps);
		}
		return periodId;
	}

	/**
	 * Returns the ID for the specified period.
	 */
	private static Integer getPeriodId(Period p1, Period p2) throws ClassNotFoundException, SQLException, IOException,
			NamingException {
		Connection connection = ConnectionController.getConnection();
		String query = "select id from period where from_1 = " + p1.getFrom() + " and to_1 = " + p1.getTo();
		if (p2 != null) {
			query += " and from_2 = " + p2.getFrom() + " and to_2 = " + p2.getTo();
		}
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		Integer id = rs.next() ? rs.getInt(1) : null;
		PersistenceUtils.closeRsPs(rs, ps);
		return id;
	}

	/**
	 * Returns the ID for the specified association case.
	 */
	private static Integer getAssocCaseId(Integer assocCase1, Integer assocCase2) throws ClassNotFoundException,
			SQLException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select id from assoc_case where case_1 = " + assocCase1;
		if (assocCase2 != null) {
			query += " and case_2 = " + assocCase2;
		}
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		Integer id = rs.next() ? rs.getInt(1) : null;
		PersistenceUtils.closeRsPs(rs, ps);
		return id;
	}

	/**
	 * Returns the ID for the specified graph details.
	 */
	private static Integer getGraphDetailsId(Integer periodId, Integer associationCaseId)
			throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select id from graph_details where period_id = " + periodId + " and assoc_case_id = "
				+ associationCaseId;
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		Integer id = rs.next() ? rs.getInt(1) : null;
		PersistenceUtils.closeRsPs(rs, ps);
		return id;
	}

	/**
	 * Fetches the next ID.
	 */
	public static Integer getNextId(TableNameEnum tableName) throws ClassNotFoundException, SQLException, IOException,
			NamingException {
		Connection connection = ConnectionController.getConnection();
		String query = "SELECT MAX(ID) FROM " + tableName.toString();
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		int i = rs.getInt(1) + 1;
		PersistenceUtils.closeRsPs(rs, ps);
		return i;
	}

	/**
	 * Prints all cliques.
	 */
	public static void printAllCliques(List<CliquesPerGraph> l) {
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - -");
		for (CliquesPerGraph c : l) {
			// GRAPH DETAILS:
			System.out.println("Assoc.Case [i]    : " + c.getGraphDetails().getAssocCase1());
			System.out.println("Assoc.Case [ii]   : " + c.getGraphDetails().getAssocCase2());
			System.out.println("Period From [i]   : " + c.getGraphDetails().getPeriod1().getFrom());
			System.out.println("Period To [i]     : " + c.getGraphDetails().getPeriod1().getTo());
			System.out.println("Period From [ii]  : " + c.getGraphDetails().getPeriod2().getFrom());
			System.out.println("Period To [ii]    : " + c.getGraphDetails().getPeriod2().getTo());
			System.out.println();
			// CLIQUE DETAILS:
			double value = c.getCliqueDetails().getValue();
			System.out.println("value = " + value);
			System.out.println();
			List<Integer> topicsList = c.getCliqueDetails().getTopicsList();
			for (Integer t : topicsList) {
				System.out.println("topic id = " + t);
			}
			System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - -");
		}
	}

	/**
	 * Returns map containing all code-description pairs for all ACM Portal CS topics
	 */
	public static Map<String, String> fetchAllTopicCodesAndDescr() throws ClassNotFoundException, SQLException,
			IOException {
		Map<String, String> m = new TreeMap<String, String>();
		Connection connection = ConnectionController.getConnection();
		String query = "SELECT * FROM TOPICS;";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String code = rs.getString(2);
			String descr = rs.getString(3);
			m.put(code, descr);
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return m;
	}

	/**
	 * Saves the publication information.
	 */
	public static void savePublicationInfo(PublicationInfo publicationInfo, int queryId, boolean saveDefault,
			Integer rank) throws Exception {

		// contains all publication topics
		List<AcmTopic> topicsList = publicationInfo.getTopics();

		// saves results in query results table (top 30)
		int qid = savePublicationInfo(publicationInfo, queryId, TableNameEnum.QUERY_RESULTS, null);

		// saves all publication topics
		saveTopicsForPublication(qid, topicsList);

		// saves results in DEFAULT query results table (top 10)
		if (saveDefault) {
			savePublicationInfo(publicationInfo, queryId, TableNameEnum.QUERY_RESULTS_DEFAULT, rank);
		}

	}

	/**
	 * Saves publication information: helper method.
	 */
	private static Integer savePublicationInfo(PublicationInfo publicationInfo, int queryId, TableNameEnum tableName,
			Integer rank) throws ClassNotFoundException, SQLException, IOException, NamingException {
		Connection connection = ConnectionController.getConnection();
		int id = getNextId(tableName);
		String query = "insert into " + tableName + " values (" + id + ", " + queryId + ", '"
				+ publicationInfo.getUrl() + "', '" + publicationInfo.getTitle() + "', ''," + rank + ", NULL, "
				+ publicationInfo.getNumOfCitations() + ",'" + createAuthorsStr(publicationInfo.getAuthors()) + "',"
				+ publicationInfo.getYearOfPublication() + ", " + publicationInfo.getIsEligible() + ")";
		System.out.println("query: " + query);
		PreparedStatement ps = connection.prepareStatement(query);
		ps.executeUpdate();
		PersistenceUtils.closeRsPs(null, ps);
		return id;
	}

	/**
	 * Saves topics for publication.
	 */
	public static void saveTopicsForPublication(int queryResultId, List<AcmTopic> topicsList) throws SQLException,
			ClassNotFoundException, IOException {
		Connection connection = ConnectionController.getConnection();
		PreparedStatement ps = null;
		for (AcmTopic acmTopic : topicsList) {
			Integer idx = TOPICS_MAP.get(acmTopic.getCode());
			if (idx != null) {
				String query = "insert into query_results_topics values(" + queryResultId + ", " + idx + ")";
				ps = connection.prepareStatement(query);
				ps.executeUpdate();
			}
		}
		PersistenceUtils.closeRsPs(null, ps);
	}

	/**
	 * Returns all topics for the specified publication.
	 */
	public static List<Integer> getAllTopicsForPublication(Integer queryResultId) throws ClassNotFoundException,
			SQLException, IOException {
		List<Integer> l = new ArrayList<Integer>();
		Connection connection = ConnectionController.getConnection();
		String query = "select * from query_results_topics where query_result_id = " + queryResultId;
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			Integer topicId = rs.getInt(2);
			l.add(topicId);
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return l;
	}

	/**
	 * Returns all topics per clique.
	 */
	public static Map<Integer, List<Integer>> getAllTopicsPerClique() throws ClassNotFoundException, SQLException,
			IOException {
		Map<Integer, List<Integer>> cliqueTopicIdMap = new HashMap<Integer, List<Integer>>();
		Connection connection = ConnectionController.getConnection();
		String query = "select * from cliques_per_graph;";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		int count = 1;
		System.out.print("Processing... ");
		while (rs.next()) {
			// System.out.println("currently processing: " + count);
			Integer cliquesPerGraphId = rs.getInt(1);
			cliqueTopicIdMap.put(cliquesPerGraphId, getTopicsForClique(rs.getInt(2)));
			count++;
		}
		System.out.print(" done.");
		System.out.println();
		PersistenceUtils.closeRsPs(rs, ps);
		return cliqueTopicIdMap;
	}

	/**
	 * Finds all topic for the specified clique.
	 */
	public static List<Integer> getTopicsForClique(Integer cliquesPerGraphId) throws ClassNotFoundException,
			SQLException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select c.topic_id from clique c where c.id in "
				+ "(select cd.clique_id from clique_details cd where cd.id in "
				+ "(select cg.clique_details_id from cliques_per_graph cg where id = " + cliquesPerGraphId + "))";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		List<Integer> topicsPerCliqueList = new ArrayList<Integer>();
		while (rs.next()) {
			topicsPerCliqueList.add(rs.getInt(1));
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return topicsPerCliqueList;
	}

	/**
	 * Finds all cliques that match based on the topics of the search result.
	 */
	public static List<Integer> getAllMatchingCliques(List<Integer> resultTopics,
			Map<Integer, List<Integer>> cliqueTopicsMap) {
		List<Integer> allMatchingCliqueIdList = new ArrayList<Integer>();
		Iterator<Integer> it = cliqueTopicsMap.keySet().iterator();
		while (it.hasNext()) {
			Integer cliqueId = it.next();
			List<Integer> cliqueTopics = cliqueTopicsMap.get(cliqueId);
			if (isMatch(cliqueTopics, resultTopics)) {
				allMatchingCliqueIdList.add(cliqueId);
			}
		}
		return allMatchingCliqueIdList;
	}

	/**
	 * Checks if there is a topic match.
	 */
	public static boolean isMatch(List<Integer> cliqueTopics, List<Integer> resultTopics) {
		for (Integer i : resultTopics) {
			if (!cliqueTopics.contains(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Saves cliques for graph.
	 */
	public static void saveCliquesForGraph(int queryResultId, List<Integer> cliqueIdList)
			throws ClassNotFoundException, SQLException, IOException, NamingException {
		Connection connection = ConnectionController.getConnection();
		PreparedStatement ps = null;
		for (Integer cliqueId : cliqueIdList) {
			String query = "insert into " + TableNameEnum.CLIQUES_PER_GRAPH_MATCHING + " values ("
					+ getNextId(TableNameEnum.CLIQUES_PER_GRAPH_MATCHING) + ", " + queryResultId + ", " + cliqueId + ")";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		}
		PersistenceUtils.closeRsPs(null, ps);
	}

	/**
	 * Fetches the weight value of the specified clique per graph id.
	 */
	public static double getWeightForCliquePerGraph(int cliquesPerGraphId) throws ClassNotFoundException, SQLException,
			IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select value from clique_details where id = (select clique_details_id from cliques_per_graph where id = "
				+ cliquesPerGraphId + ")";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		double w = rs.getDouble(1);
		PersistenceUtils.closeRsPs(rs, ps);
		return w;
	}

	/**
	 * Returns number of keywords for clique per graph.
	 */
	public static int getNumOfKeywordsForCliquePerGraph(int cliquesPerGraphId) throws ClassNotFoundException,
			SQLException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select count(c.topic_id) from clique c where c.id in "
				+ "(select cd.clique_id from clique_details cd where cd.id in "
				+ "(select cg.clique_details_id from cliques_per_graph cg where id = " + cliquesPerGraphId + "))";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		int count = rs.getInt(1);
		PersistenceUtils.closeRsPs(rs, ps);
		return count;
	}

	/**
	 * Returns the latest period that the specified clique belongs to.
	 */
	public static Period getPeriodForCliquePerGraph(int cliquesPerGraphId) throws ClassNotFoundException, SQLException,
			IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select * from period where id in (select period_id from graph_details where id in "
				+ "(select graph_details_id from cliques_per_graph where id = " + cliquesPerGraphId + "))";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		Integer from1 = rs.getInt(2);
		Integer to1 = rs.getInt(3);
		Integer from2 = rs.getInt(4);
		Integer to2 = rs.getInt(5);
		if (from2 > 0) {
			if (from2 > from1 && to2 > to1) {
				return new Period(from2, to2);
			}
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return new Period(from1, to1);
	}

	/**
	 * Returns the minimum association cases that the specified clique belongs to.
	 */
	public static Integer getAssocCaseForCliquePerGraph(int cliquesPerGraphId) throws ClassNotFoundException,
			SQLException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select * from assoc_case where id in (select assoc_case_id from graph_details where id in"
				+ " (select graph_details_id from cliques_per_graph where id = " + cliquesPerGraphId + "))";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		Integer assocCase1 = rs.getInt(2);
		Integer assocCase2 = rs.getInt(3);
		if (assocCase2 > 0) {
			if (assocCase2 < assocCase1) {
				return assocCase2;
			}
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return assocCase1;
	}

	/**
	 * Returns weight for specific association case.
	 */
	public static double getWeightForAssocCase(int assocType) throws Exception {
		if (assocType > 3 || assocType < 1) {
			throw new Exception("Unsupported association case value: " + assocType);
		}
		Connection connection = ConnectionController.getConnection();
		String query = "select value from weights where weight = 'assoc_type_" + assocType + "';";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		Double value = rs.getDouble(1);
		PersistenceUtils.closeRsPs(rs, ps);
		return value;
	}

	/**
	 * Returns the exponential smoothing weight.
	 */
	public static double getExponentialSmoothingWeight() throws SQLException, ClassNotFoundException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select value from weights where weight = 'exp_smoothing';";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		Double value = rs.getDouble(1);
		PersistenceUtils.closeRsPs(rs, ps);
		return value;
	}

	/**
	 * Saves all publication authors.
	 */
	public static void saveAllPublicationAuthors() throws Exception {
		List<String> publicationUrls = new ArrayList<String>();
		Connection connection = ConnectionController.getConnection();
		String query = "select url from query_results";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			publicationUrls.add(rs.getString(1));
		}
		for (String url : publicationUrls) {
			System.out.print("Currently processing URL: " + url);
			PublicationInfo p = CrawlUtils.extractAllPublicationInfo(url);
			String auths = createAuthorsStr(p.getAuthors());
			updateResultsTable(TableNameEnum.QUERY_RESULTS, auths, url);
			updateResultsTable(TableNameEnum.QUERY_RESULTS_DEFAULT, auths, url);
			System.out.print(" done.");
			System.out.println();
		}

	}

	/**
	 * Updates the results table.
	 */
	private static void updateResultsTable(TableNameEnum tableName, String auths, String url)
			throws ClassNotFoundException, SQLException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "update " + tableName + " set authors = '" + auths + "' where url = '" + url + "';";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.executeUpdate();
		PersistenceUtils.closeRsPs(null, ps);
	}

	/**
	 * Creates authors string.
	 */
	private static String createAuthorsStr(List<String> authList) {
		String res = "";
		for (String a : authList) {
			String auth = a.replaceAll("'", "\\\\'");
			res += auth + ", ";
		}
		if ("".equals(res)) {
			return "";
		}
		return res.substring(0, res.length() - 2);
	}

	/**
	 * Fetches next query id to process.
	 */
	public static int fetchNextQueryIdToProcess() throws Exception {
		Connection connection = ConnectionController.getConnection();
		String query = "select * from queries where status = 'TO_PROCESS';";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		Integer qid = rs.getInt(1);
		PersistenceUtils.closeRsPs(rs, ps);
		return qid;
	}

	/**
	 * Checks if there exists a query with pending status.
	 */
	public static boolean pendingQueryExists() throws Exception {
		Connection connection = ConnectionController.getConnection();
		String query = "select count(*) from queries where status = 'PENDING';";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		int count = rs.getInt(1);
		PersistenceUtils.closeRsPs(rs, ps);
		return count > 0;
	}
	
	/**
	 * Fetches next query to process.
	 */
	public static Query fetchNextQueryToProcess() throws Exception {
		Connection connection = ConnectionController.getConnection();
		String query = "select * from queries where status = 'PENDING';";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		rs.next();
		Query q = new Query(rs.getInt(1), rs.getString(3));
		PersistenceUtils.closeRsPs(rs, ps);
		return q;
	}

	/**
	 * Updates status.
	 */
	public static void updateStatus(StatusEnum status, int id) throws SQLException, ClassNotFoundException, IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "update queries set status =  '" + status + "' where id = " + id;
		PreparedStatement ps = connection.prepareStatement(query);
		ps.executeUpdate();
		PersistenceUtils.closeRsPs(null, ps);
	}

	/**
	 * Updates ranking.
	 */
	public static void updateRanking(List<Integer> rankList) throws ClassNotFoundException, SQLException, IOException {
		int rank = 1;
		Connection connection = ConnectionController.getConnection();
		String query = "";
		PreparedStatement ps = null;
		for (Integer searchResultId : rankList) {
			query = "update query_results set rank =  " + rank + " where id = " + searchResultId;
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
			rank++;
		}
		PersistenceUtils.closeRsPs(null, ps);
	}

	/**
	 * Fetches the feedback for query results.
	 */
	public static int[] getFeedbackForQueryResults(int queryId, QueryResultEnum queryResult) throws ClassNotFoundException,
			SQLException, IOException {
		int[] feedbackArr = new int[10];
		Connection connection = ConnectionController.getConnection();
		String query = "select * from " + queryResult + " where query_id = " + queryId + ";";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			Integer rank = rs.getInt(6);
			if (rank > 0) {
				Integer feedbackScore = rs.getInt(7);
				feedbackArr[rank - 1] = feedbackScore;
			}
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return feedbackArr;
	}

	/**
	 * Saves feedback.
	 */
	public static void saveFeedback(int queryId, double acmRes, double pubSearchRes) throws SQLException,
			ClassNotFoundException, IOException, NamingException {
		Integer feedbackId = getNextId(TableNameEnum.FEEDBACK);
		Connection connection = ConnectionController.getConnection();
		String query = "insert into feedback values (" + feedbackId + "," + queryId + "," + acmRes + ", "
				+ pubSearchRes + ");";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.executeUpdate();
		PersistenceUtils.closeRsPs(null, ps);
	}

	/**
	 * Fetches feedback for the results.
	 */
	public static Integer fetchFeedbackForResult(int queryResultId) throws SQLException, ClassNotFoundException,
			IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select feedback_score from query_results where id = " + queryResultId + ";";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return rs.getInt(1);
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return null;
	}

	/**
	 * Fetches feedback for the default results.
	 */
	public static Integer fetchFeedbackForResultDef(int queryResultId) throws SQLException, ClassNotFoundException,
			IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select feedback_score from query_results_default where id = " + queryResultId + ";";
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return rs.getInt(1);
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return null;
	}

	public static void saveFeedback2(int queryId, double pubSearchRes, double bucketRange) throws SQLException,
			ClassNotFoundException, IOException, NamingException {
		Integer feedbackId = getNextId(TableNameEnum.FEEDBACK);
		Connection connection = ConnectionController.getConnection();
		String query = "insert into feedback values (" + feedbackId + "," + queryId + "," + "0" + ", " + pubSearchRes
				+ "," + bucketRange + ");";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.executeUpdate();
		PersistenceUtils.closeRsPs(null, ps);
	}

	public static void saveFeedback3(int queryId, double acmRes, double pubSearchRes, double bucketRange)
			throws SQLException, ClassNotFoundException, IOException, NamingException {
		Integer feedbackId = getNextId(TableNameEnum.FEEDBACK);
		Connection connection = ConnectionController.getConnection();
		String query = "insert into feedback values (" + feedbackId + "," + queryId + "," + acmRes + ", "
				+ pubSearchRes + "," + bucketRange + ");";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.executeUpdate();
		PersistenceUtils.closeRsPs(null, ps);
	}

	/**
	 * Fetches the query result title form the specified query result id.
	 */
	public static String fetchQueryResultTitleFromId(int queryResultId) throws SQLException, ClassNotFoundException,
			IOException {
		Connection connection = ConnectionController.getConnection();
		String query = "select title from query_results where id = " + queryResultId;
		PreparedStatement ps = connection.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			String title = rs.getString(1);
			PersistenceUtils.closeRsPs(rs, ps);
			return title;
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return "";
	}

	/**
	 * Fetches average feedback for buckets (PubSearch).
	 */
	public static List<Double> fetchAverageFeedbackForBucketsPubSearch(int numOfBuckets) throws ClassNotFoundException,
			SQLException, IOException {
		List<Double> l = new ArrayList<Double>();
		Connection connection = ConnectionController.getConnection();

		for (int i = 1; i <= numOfBuckets; i++) {
			String q = "select avg(pub_search_res) from feedback where bucket_range = " + i;
			PreparedStatement ps = connection.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			rs.next();
			l.add(rs.getDouble(1));
		}
		return l;
	}

	/**
	 * Fetches average feedback for buckets (ACM).
	 */
	public static List<Double> fetchAverageFeedbackForBucketsACM(int numOfBuckets) throws ClassNotFoundException,
			SQLException, IOException {
		List<Double> l = new ArrayList<Double>();
		Connection connection = ConnectionController.getConnection();

		for (int i = 1; i <= numOfBuckets; i++) {
			String q = "select avg(acm_res) from feedback where bucket_range = " + i;
			PreparedStatement ps = connection.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			rs.next();
			l.add(rs.getDouble(1));
		}
		return l;
	}

	/**
	 * Fetches the default query results.
	 */
	public static List<Integer> fetchQueryResultsDefault(int queryId, int totalRes) throws ClassNotFoundException,
			SQLException, IOException {
		List<Integer> res = new ArrayList<Integer>();
		Connection connection = ConnectionController.getConnection();
		String q = "select id from query_results_default where query_id = " + queryId + " order by rank asc";
		PreparedStatement ps = connection.prepareStatement(q);
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while (rs.next()) {
			if (count < totalRes) {
				res.add(rs.getInt(1));
				count++;
			} else {
				PersistenceUtils.closeRsPs(rs, ps);
				return res;
			}
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return res;
	}

	/**
	 * Fetches feedback for all default query results.
	 */
	public static int fetchFeedbackForQueryResultDefault(int queryResultDefaultId) throws SQLException,
			ClassNotFoundException, IOException {
		Connection connection = ConnectionController.getConnection();
		String q = "select feedback_score from query_results_default where id = " + queryResultDefaultId + ";";
		PreparedStatement ps = connection.prepareStatement(q);
		ResultSet rs = ps.executeQuery();
		rs.next();
		int feedback = rs.getInt(1);
		PersistenceUtils.closeRsPs(rs, ps);
		return feedback;
	}

	/**
	 * Saves the search results' term frequency.
	 */
	public static void saveResultTf(int queryId, Map<String, Integer> tfMap, int queryResultId, int totalTokens)
			throws ClassNotFoundException, SQLException, IOException, NamingException {
		Connection connection = ConnectionController.getConnection();
		PreparedStatement ps = null;
		Integer id = null;
		String query = "";
		Iterator<String> it = tfMap.keySet().iterator();
		while (it.hasNext()) {
			id = getNextId(TableNameEnum.RESULTS_TF);
			String token = it.next();
			Integer tf = tfMap.get(token);
			query = "insert into results_tf values(" + id + "," + queryId + ",'" + token + "'," + tf + ","
					+ queryResultId + "," + totalTokens + ", false)";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		}
		PersistenceUtils.closeRsPs(null, ps);
	}

}