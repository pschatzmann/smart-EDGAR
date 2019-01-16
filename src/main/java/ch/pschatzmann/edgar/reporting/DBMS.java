package ch.pschatzmann.edgar.reporting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.pschatzmann.edgar.utils.Utils;

/**
 * Access to database. Implemented as singleton
 * 
 * @author pschatzmann
 *
 */

public class DBMS {
	private static final Logger LOG = Logger.getLogger(DBMS.class);
	private static DBMS instance = null;
	private Connection connection = null;
	
	/**
	 * Enforce access via singleton
	 */
	private DBMS(){}
	
	/**
	 * Creates an singleton instance
	 * @return
	 */
	public static synchronized DBMS getInstance() {
		if (instance == null) {
			instance = new DBMS();
		}
		return instance;
	}

	/**
	 * Executes the SQL command against the database and adds all found records
	 * 
	 * @param sql
	 * @param tab
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void execute(String sql, Table tab) throws SQLException, ClassNotFoundException {
		openConnection();
		Statement statement = connection.createStatement();
		LOG.info("executeQuery");
		ResultSet rs = statement.executeQuery(sql);
		LOG.info("executeQuery completed");
		try {
			while (rs.next()) {
				tab.putRecord(rs);
			}
			LOG.info("records added to Table");
		} finally {
			rs.close();
			statement.close();
		}
	}

	protected void openConnection() throws SQLException, ClassNotFoundException {
		if (connection == null || connection.isClosed()) {
			LOG.info("Opening connection to database");
			Class.forName(Utils.getProperty("jdbcDriver", "org.postgresql.Driver"));
			String url = Utils.getProperty("jdbcURL", "jdbc:postgresql://nuc.local:5432/edgar");
			String user = Utils.getProperty("jdbcUser", "edgar");
			String password = Utils.getProperty("jdbcPassword", "edgar");
			connection = DriverManager.getConnection(url, user, password);
			connection.setAutoCommit(false);
		}
		connection.rollback();
	}

	public List<String> getFieldValues(String tableName, String fieldName, String like) throws SQLException, ClassNotFoundException {
		StringBuffer sb = new StringBuffer();
		sb.append("select ");
		sb.append(fieldName);
		sb.append(" from ");
		sb.append(tableName);
		if (!Utils.isEmpty(like)){
			sb.append(" where ");
			sb.append(fieldName);			
			sb.append(" ilike '");
			sb.append(like.trim());
			sb.append("'");
		}
		sb.append(" group by 1");
		sb.append(" order by 1");
		
		List<String> result = new ArrayList();
		openConnection();
		Statement statement = connection.createStatement();
		LOG.info(sb.toString());
		ResultSet rs = statement.executeQuery(sb.toString());
		try {
			while (rs.next()) {
				String value = rs.getString(1);
				if (value!=null) {
					result.add(value.trim());
				}
			}
		} finally {
			rs.close();
			statement.close();
		}
		LOG.info("->done");
		return result;
	}


}
