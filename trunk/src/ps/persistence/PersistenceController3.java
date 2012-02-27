package ps.persistence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import ps.enumerators.TableNameEnum;
import ps.struct.PubInfoSummary;
import ps.tmp.SearchEngineEnum;
import ps.util.CollectionUtils;

/**
 * Note: Contains latest persistence utility related with multi-academic engine functionality  
 * 
 * @author Manolis
 *
 */
public class PersistenceController3 {
	
	public static void main(String[] args) {
		int queryId = 24;
		SearchEngineEnum se = SearchEngineEnum.MISCROSOFT_ACADEMIC_SEARCH;
		try {
			// *** TEST SAVE:
			List<Integer> publicationIdList = new ArrayList<Integer>();
			publicationIdList.add(1);
			publicationIdList.add(2);
			publicationIdList.add(3);
			publicationIdList.add(4);
			savedRankedResultsForEngine(queryId, se, publicationIdList);
			
			// *** TEST RETRIEVE:
			List<Integer> rankedResList = fetchRankedResultsForEngine(queryId, se);
			Integer[] rankedResArr = CollectionUtils.convertIntegerListToArray(rankedResList);
			for (int i = 0; i < rankedResArr.length; i++) {
				System.out.println(rankedResArr[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Fetches all results for the specific query for the specific engine order by the rank in which they appear.
	 */
	public static List<Integer> fetchRankedResultsForEngine(int queryId, SearchEngineEnum se) throws SQLException,
			ClassNotFoundException, IOException {
		Connection connection = ConnectionController.getConnection();
		String q = "select publication_id from rank where query_id = " + queryId + " and engine  = '" + se.getShortName() + "' order by rank asc";
		PreparedStatement ps = connection.prepareStatement(q);
		ResultSet rs = ps.executeQuery();
		List<Integer> l = new ArrayList<Integer>();
		while (rs.next()) {
			int id = rs.getInt(1);
			l.add(id);
		}
		PersistenceUtils.closeRsPs(rs, ps);
		return l;
	}
	
	/**
	 * Saves the specified ranked list for the specific query and the specific search engine.
	 */
	public static void savedRankedResultsForEngine(int queryId, SearchEngineEnum se, List<Integer> publicationIdList)
			throws ClassNotFoundException, SQLException, IOException, NamingException {
		Connection connection = ConnectionController.getConnection();
		PreparedStatement ps = null;
		try {
			int rank = 1;
			for (Integer pubId : publicationIdList) {
				int id = PersistenceController.getNextId(TableNameEnum.RANK);
				String q = "insert into rank values(" + id + ", " + queryId + ", " + "'" + se.getShortName() + "', "
						+ pubId + ", " + rank + ")";
				ps = connection.prepareStatement(q);
				ps.executeUpdate();
				rank++;
			}
		} finally {
			PersistenceUtils.closeRsPs(null, ps);
		}
	}

}
