package ch.pschatzmann.edgar.reporting.company;

import ch.pschatzmann.common.table.ITable;

/**
 * Returns all 10-K entries for 12 months or 0 months
 * 
 * @author pschatzmann
 *
 */
public class NoFilter implements IRowFilter {

	public NoFilter() {
	}

	@Override
	public Boolean apply(ITable table, Integer row) {
		return true;
	}

	@Override
	public String getRestKey() {
		return "";
	}
}
