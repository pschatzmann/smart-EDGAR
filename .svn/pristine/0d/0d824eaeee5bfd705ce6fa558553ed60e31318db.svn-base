package ch.pschatzmann.edgar.reporting;

import ch.pschatzmann.common.table.ITable;
import ch.pschatzmann.edgar.base.errors.DataException;

/**
 * Interface which allows for the implementation of different models to
 * implement the construction of the SQL command
 * 
 * @author pschatzmann
 *
 */
public interface ISQLModel {
	/**
	 * Adds the join command to the string buffer
	 * 
	 * @param table
	 */
	public String toSQL(ITable table) throws DataException;

}
