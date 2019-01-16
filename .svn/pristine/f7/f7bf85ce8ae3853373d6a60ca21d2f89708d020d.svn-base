package ch.pschatzmann.edgar.reporting;

import ch.pschatzmann.common.table.FormatException;
import ch.pschatzmann.common.table.ITable;
import ch.pschatzmann.common.table.ITableFormatter;
import ch.pschatzmann.edgar.base.errors.DataException;

/**
 * Provides the SQL query which is used to create the table
 * 
 * @author pschatzmann
 *
 */
public class TableFormatterSQL implements ITableFormatter {
	private EdgarModel model;

	public TableFormatterSQL(EdgarModel model) {
		this.model = model;
	}

	@Override
	public String format(ITable table) throws FormatException {
		try {
			return this.model.toSQL(table);
		} catch (DataException e) {
			throw new FormatException(e);
		}
	}

}
