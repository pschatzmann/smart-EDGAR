package ch.pschatzmann.edgar.dataload;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.pschatzmann.edgar.utils.Utils;

/**
 * Basic SQL functionality
 * 
 * @author pschatzmann
 *
 */

public class TableFactory {
	private static final Logger LOG = Logger.getLogger(TableFactory.class);
	private Connection connection;
	private Map<String, PreparedStatement> psMap = new HashMap();
	private Map<String, String> typeMap = new HashMap();

	/**
	 * Creates a new table with the indicated fields
	 * 
	 * @param tableName
	 * @param attributes
	 * @return
	 * @throws SQLException
	 */
	public boolean createTable(String tableName, List<String> attributes) throws SQLException {
		boolean result = true;
		try {
			LOG.info("Create table " + tableName);
			openConnection();
			StringBuffer sb = new StringBuffer();
			sb.append("CREATE TABLE ");
			sb.append(tableName);
			sb.append(" (");
			boolean first = true;
			for (String field : attributes) {
				if (!first)
					sb.append(", ");
				sb.append(field);
				sb.append(" ");
				sb.append(getDataType(field));
				first = false;
			}
			sb.append(");");

			execute(sb.toString());
		} catch (Exception ex) {
			result = false;
			LOG.info(ex);
			connection.rollback();
		}
		return result;
	}

	public void openConnection() throws SQLException, ClassNotFoundException {
		openConnection(false);
	}

	public void openConnection(boolean autoCommit) throws SQLException, ClassNotFoundException {
		if (connection == null || connection.isClosed()) {
			Class.forName(Utils.getProperty("jdbcDriver", "org.postgresql.Driver"));
			String url = getUrl();
			String user = Utils.getProperty("jdbcUser", "edgar");
			String password = Utils.getProperty("jdbcPassword", "edgar");

			LOG.info(url);
			connection = DriverManager.getConnection(url, user, password);
			connection.setAutoCommit(autoCommit);
		}
		connection.rollback();
	}

	private String getUrl() {
		String url = Utils.getProperty("jdbcURL", "jdbc:postgresql://nuc.local:5432/edgar");
		return url;
	}

	private String getDataType(String field) {
		String result = this.typeMap.get(field);
		if (result == null) {
			result = Utils.getProperty("typeString", "VARCHAR(1000)");
			if (field.equals("value")) {
				result = Utils.getProperty("typeNumber", "DECIMAL(20,2)");
			} else if (field.equals("date")) {
				result = Utils.getProperty("typeDate", "DATE");
			}
		}
		return result;
	}

	public boolean execute(String sql) {
		boolean result = false;
		try {
			if (sql != null) {
				LOG.info(sql);
				openConnection();
				Statement stmt = connection.createStatement();
				stmt.executeUpdate(sql);
				stmt.close();
				connection.commit();
				result = true;
			}
		} catch (Exception ex) {
			LOG.error(ex);
		}
		return result;
	}

	public boolean hasNext(String sql, boolean defaultValue) {
		boolean result = false;
		try {
			if (sql != null) {
				openConnection();
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				result = rs.next();
				stmt.close();
			}
		} catch (Exception ex) {
			result = defaultValue;
		}
		return result;
	}

	public Collection<String> getList(String sql) throws SQLException, ClassNotFoundException {
		Collection<String> result = new ArrayList();
		openConnection();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {
			result.add(rs.getString(1));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ResultSet getResultSet(String sql) throws ClassNotFoundException, SQLException {
		openConnection(true);
		Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = stmt.executeQuery(sql);
		return rs;
	}

	public void addRecord(String tableName, List<String> attributes, Map<String, String> record)
			throws SQLException, ParseException, ClassNotFoundException {
		addRecord(tableName, attributes, record, "");
	}

	public void addRecord(String tableName, List<String> attributes, Map<String, String> record, String constraint)
			throws SQLException, ParseException, ClassNotFoundException {
		PreparedStatement ps = getPreparedStatement(tableName);
		if (ps == null || ps.isClosed()) {
			ps = this.openStatement(tableName, attributes, constraint);
		}

		ps.clearParameters();
		int j = 1;
		for (String attribute : attributes) {
			Object value = record.get(attribute);
			if (attribute.equals("date")) {
				ps.setDate(j, Date.valueOf((String) value));
			} else if (attribute.equals("value")) {
				try {
					if (!Utils.isEmpty((String) value)) {
						BigDecimal number = new BigDecimal((String) value);
						ps.setObject(j, number);
					}
				} catch (Exception ex) {
					LOG.error("Could not convert value to number " + value);
				}
			} else {
				ps.setString(j, Utils.str(value));
			}
			j++;
		}
		ps.execute();
	}

	private PreparedStatement getPreparedStatement(String tableName) {
		return this.psMap.get(tableName);
	}

	private PreparedStatement openStatement(String tableName, List<String> attributes, String constr)
			throws SQLException, ClassNotFoundException {
		LOG.debug("Creating statement");
		openConnection();
		String insert = createInsertStatement(tableName, attributes, constr);
		PreparedStatement ps = connection.prepareStatement(insert);
		this.psMap.put(tableName, ps);
		return ps;
	}

	private String createInsertStatement(String tableName, List<String> attributes, String constr) {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO ");
		sb.append(tableName);
		sb.append(" (");
		boolean first = true;
		for (String field : attributes) {
			if (!first)
				sb.append(", ");
			sb.append(field);
			first = false;
		}
		sb.append(") VALUES (");
		first = true;
		for (String field : attributes) {
			if (!first)
				sb.append(", ");
			sb.append("?");
			first = false;
		}
		sb.append(") ");
		if (!constr.isEmpty()) {
			sb.append(" ON CONFLICT ON CONSTRAINT ");
			sb.append(constr);
			sb.append(" DO NOTHING");
		}
		sb.append(";");
		return sb.toString();
	}

	public Map<String, String> getTypeMap() {
		return typeMap;
	}

	/**
	 * Provides the possibility to define the type for each attriute
	 * 
	 * @param attribute
	 * @param type
	 */
	public void putType(String attribute, String type) {
		this.typeMap.put(attribute, type);
	}

	/**
	 * Close the connection
	 * 
	 * @throws SQLException
	 */

	public void close() throws SQLException {
		for (PreparedStatement ps : this.psMap.values()) {
			ps.close();
		}
		psMap.clear();
		connection.commit();
		connection.close();
	}

	public void commit() throws SQLException {
		connection.commit();
	}

	public void rollback() throws SQLException {
		connection.rollback();
	}

	public void addIndex(String index) {
		execute(index);
	}
		

	public void close(String table) {
		try {
			this.psMap.get(table).close();
		} catch (Exception e) {
		}
	}

	public void updateCompany(List<String> companyFields, Map<String, String> attributes) {
		StringBuffer sb = new StringBuffer();
		sb.append("update company set ");
		boolean first = true;
		for (String fld : companyFields) {
			if (!fld.equals("identifier")) {
				String value = attributes.get(fld);
				if (!Utils.isEmpty(value)) {
					if (!first) {
						sb.append(", ");
					}
					sb.append(fld);
					sb.append(" = '");
					value = value.replaceAll("'", "");
					sb.append(value.trim());
					sb.append("' ");
					first = false;
				}
			}
		}
		String id = attributes.get("identifier");
		sb.append(" where identifier = '");
		sb.append(id);
		sb.append("'");

		if (!first) {
			String sql = sb.toString();
			this.execute(sql);
		}

	}

	/**
	 * Determines if the database exists
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public boolean databaseExists(String name) throws SQLException {
		ResultSet resultSet = connection.getMetaData().getCatalogs();

		// iterate each catalog in the ResultSet
		while (resultSet.next()) {
			// Get the database name, which is at position 1
			String databaseName = resultSet.getString(1);
			if (name.equalsIgnoreCase(databaseName)) {
				resultSet.close();
				return true;
			}
		}
		resultSet.close();
		return false;
	}
}
