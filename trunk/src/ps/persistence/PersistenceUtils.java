package ps.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PersistenceUtils {
	/**
	 * Closes the specified result set and prepared statement
	 */
	public static void closeRsPs(ResultSet rs, PreparedStatement ps) throws SQLException {
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
	}

}
