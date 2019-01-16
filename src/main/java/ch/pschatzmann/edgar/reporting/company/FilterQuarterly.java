package ch.pschatzmann.edgar.reporting.company;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.log4j.Logger;

import ch.pschatzmann.common.table.ITable;

/**
 * Returns all 10-Q entries for 3 months (P and L) or 0 months (Balance Sheet
 * values)
 * 
 * @author pschatzmann
 *
 */
public class FilterQuarterly implements IRowFilter {
	private static final Logger LOG = Logger.getLogger(FilterQuarterly.class);
	private List<Map<String, ?>> list;
	private PeriodsDetermination periods;

	public FilterQuarterly() {
	}
	
	@Override
	public Boolean apply(ITable table, Integer row) {
		setup(table);
		Map<String,?> record = list.get(row);
		return periods.isValidQuarterly(record.get("date").toString(), record.get("form").toString(), record.get("numberOfMonths").toString());
	}

	protected void setup(ITable table) {
		if (this.list != table) {
			this.list = table.toList();
			periods = new PeriodsDetermination(table);
		}
	}
	
	public String getFileNameRegex() {
		return ".*10-Q.*";
	}

	@Override
	public String getRestKey() {
		return "Q";
	}


}
