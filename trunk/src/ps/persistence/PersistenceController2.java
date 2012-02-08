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

import ps.enumerators.TableNameEnum;

/**
 * Provides persistence related functionality.
 */
public class PersistenceController2 {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		// String descr = "information retrieval";
		// List<String> acrList = findAcronymsForDescr(descr);
		// for (String acr : acrList) {
		// System.out.println(acr);
		// }

		Map<String, List<String>> m = fetchCompleteAcronymsMap();
//		Iterator<String> it = m.keySet().iterator();
//		while (it.hasNext()) {
//			String acronym = it.next();
//			System.out.println("ACRONYM: " + acronym);
//			List<String> descriptionList = m.get(acronym);
//			for (String descr : descriptionList) {
//				System.out.println(" --> " + descr);
//			}
//		}
		System.out.println("SIZE OF MAP = " + m.size());
	}

	/**
	 * Returns the total TF.
	 */
	public static int findTf(int queryId, int queryResultId) throws SQLException, ClassNotFoundException, IOException {
		Connection connection = ConnectionController.getConnection();
		String q = "select tf from results_tf where query_id = " + queryId + " and query_result_id = " + queryResultId;
		PreparedStatement ps = connection.prepareStatement(q);
		ResultSet rs = ps.executeQuery();
		int tf = 0;
		while (rs.next()) {
			tf += rs.getInt(1);

		}
		PersistenceUtils.closeRsPs(rs, ps);
		return tf;
	}

	/**
	 * Returns the normalized TF divided by the total number of tokens in the document.
	 */
	public static double findNormalizedTf(int queryId, int queryResultId) throws SQLException, ClassNotFoundException,
			IOException {
		Connection connection = ConnectionController.getConnection();
		String q = "select tf, total_tokens from results_tf where query_id = " + queryId + " and query_result_id = "
				+ queryResultId;
		PreparedStatement ps = connection.prepareStatement(q);
		ResultSet rs = ps.executeQuery();
		int tf = 0;
		int totalTokens = 0;
		while (rs.next()) {
			tf += rs.getInt(1);
			if (totalTokens == 0) {
				totalTokens = rs.getInt(2);
			}
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return (double) tf / (double) totalTokens;
	}

	/**
	 * Returns the title default result id.
	 */
	public static String getDefaultResultTitleForId(int id) throws ClassNotFoundException, SQLException, IOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection connection = ConnectionController.getConnection();
			String q = "select title from query_results_default where id = " + id;
			ps = connection.prepareStatement(q);
			rs = ps.executeQuery();
			rs.next();
			return rs.getString(1);
		} finally {
			PersistenceUtils.closeRsPs(rs, ps);
		}
	}

	/**
	 * Saves an empty term frequency result.
	 */
	public static void saveEmptyTFResult(int queryId, List<Integer> pubsNotDownloaded, List<String> queryTokens)
			throws ClassNotFoundException, SQLException, IOException, NamingException {
		Connection connection = ConnectionController.getConnection();
		PreparedStatement ps = null;
		try {
			for (Integer pubId : pubsNotDownloaded) {
				for (String queryToken : queryTokens) {
					int id = PersistenceController.getNextId(TableNameEnum.RESULTS_TF);
					String q = "insert into results_tf values(" + id + "," + queryId + "," + "'" + queryToken
							+ "', 0, " + pubId + ", 0, true" + ")";
					ps = connection.prepareStatement(q);
					ps.executeUpdate();
				}
			}
		} finally {
			PersistenceUtils.closeRsPs(null, ps);
		}
	}

	public static void saveYearCitation(Integer queryResId, Map<Integer, Integer> m) throws ClassNotFoundException,
			SQLException, IOException, NamingException {
		Connection connection = ConnectionController.getConnection();
		PreparedStatement ps = null;
		try {
			Iterator<Integer> it = m.keySet().iterator();
			while (it.hasNext()) {
				Integer year = it.next();
				Integer citations = m.get(year);
				int id = PersistenceController.getNextId(TableNameEnum.YEAR_CITATION);
				String q = "insert into year_citation values(" + id + "," + queryResId + "," + year + "," + citations
						+ ")";
				ps = connection.prepareStatement(q);
				ps.executeUpdate();
			}
		} finally {
			PersistenceUtils.closeRsPs(null, ps);
		}
	}

	/**
	 * Fetches the query with the specified id.
	 */
	public static String fetchQueryForId(int queryId) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection connection = ConnectionController.getConnection();
			String query = "select query from queries where id = " + queryId;
			ps = connection.prepareStatement(query);
			rs = ps.executeQuery();
			rs.next();
			return rs.getString(1);
		} finally {
			PersistenceUtils.closeRsPs(rs, ps);
		}
	}

	/**
	 * Calculate the number of occurrences of the specified ignore flag value for the specified query id.
	 */
	public static Integer calcIgnoreFlagsForQueryId(int queryId, boolean flag) throws ClassNotFoundException,
			SQLException, IOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection connection = ConnectionController.getConnection();
			String query = "select count(*) from results_tf where query_id = " + queryId + " and ignore_flag = " + flag;
			ps = connection.prepareStatement(query);
			rs = ps.executeQuery();
			rs.next();
			return rs.getInt(1);
		} finally {
			PersistenceUtils.closeRsPs(rs, ps);
		}
	}

	/**
	 * Fetches all query result ids for the specified query id and ignore flag value.
	 */
	public static List<Integer> fetchAllResultsToIgnore(int queryId, boolean flag) throws SQLException,
			ClassNotFoundException, IOException {
		List<Integer> l = new ArrayList<Integer>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection connection = ConnectionController.getConnection();
			String query = "select distinct(query_result_id) from results_tf where query_id = " + queryId
					+ " and ignore_flag = " + flag;
			ps = connection.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				l.add(rs.getInt(1));
			}
			return l;
		} finally {
			PersistenceUtils.closeRsPs(rs, ps);
		}
	}

	// FIXME: FIX THE FOLLOWING QUERIES!!!

	public static Map<Integer, Integer> fetchCitationDistForQueryRes(int queryResId) throws ClassNotFoundException,
			SQLException, IOException {
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection connection = ConnectionController.getConnection();
			String query = "FIXME!!! RETURN YEAR(1) AND ANNUAL CITATION COUNT(2)!!!" + queryResId;
			ps = connection.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				int year = rs.getInt(1);
				int count = rs.getInt(2);
				m.put(year, count);
			}
			return m;
		} finally {
			PersistenceUtils.closeRsPs(rs, ps);
		}
	}

	/**
	 * Saves in persistence all acronyms and their descriptions from the provided map.
	 */
	public static void saveAllAcronyms(Map<String, List<String>> acronymsMap) throws ClassNotFoundException,
			IOException, NamingException, SQLException {
		Connection connection = ConnectionController.getConnection();
		PreparedStatement ps = null;
		try {
			Iterator<String> it = acronymsMap.keySet().iterator();
			while (it.hasNext()) {
				String acr = it.next();
				List<String> descrList = acronymsMap.get(acr);
				for (String descr : descrList) {
					int id = PersistenceController.getNextId(TableNameEnum.ACRONYMS);
					String q = "insert into acronyms values(" + id + ",'" + acr + "','" + descr + "');";
					ps = connection.prepareStatement(q);
					try {
						ps.executeUpdate();
					} catch (SQLException e) {

						System.err.println("PROBLEM WITH ACRONYM: " + acr + " AND DESCR = " + descr);
					}
				}
			}
		} finally {
			PersistenceUtils.closeRsPs(null, ps);
		}
	}

	/**
	 * Finds all acronyms with description matching the one of the specified string. If the query fetches an acronym
	 * with description exactly the same as the one in the specified string, then it returns the acronym itself.
	 * Alternative, it returns all acronyms containing descriptions that partially match the specified string.
	 */
	public static List<String> findAcronymsForDescr(String descr) throws ClassNotFoundException, SQLException,
			IOException {
		List<String> acrList = new ArrayList<String>();
		Connection connection = ConnectionController.getConnection();
		// i. attempts to find acronyms with exact matching
		String q = "select acronym from acronyms where description = '" + descr + "';";
		PreparedStatement ps = connection.prepareStatement(q);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			acrList.add(rs.getString(1));
		}
		// ii. attempts to find acronyms with partial matching in case acronyms with exact matching are not found
		if (acrList.size() == 0) {
			q = "select acronym from acronyms where description LIKE '%" + descr + "%';";
			ps = connection.prepareStatement(q);
			rs = ps.executeQuery();
			while (rs.next()) {
				acrList.add(rs.getString(1));
			}
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return acrList;
	}

	public static Map<String, List<String>> fetchCompleteAcronymsMap() throws ClassNotFoundException, SQLException,
			IOException {
		Map<String, List<String>> m = new TreeMap<String, List<String>>();
		Connection connection = ConnectionController.getConnection();
		String q = "select acronym, description from acronyms group by acronym";
		PreparedStatement ps = connection.prepareStatement(q);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String acronym = rs.getString(1);
			String description = rs.getString(2);
			List<String> descriptionList = m.get(acronym);
			if (descriptionList == null) {
				descriptionList = new ArrayList<String>();
			}
			descriptionList.add(description);
			m.put(acronym, descriptionList);
		}
		return m;
	}

}