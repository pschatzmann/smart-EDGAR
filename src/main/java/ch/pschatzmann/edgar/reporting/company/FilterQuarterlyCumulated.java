package ch.pschatzmann.edgar.reporting.company;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.pschatzmann.common.table.ITable;

/**
 * Returns all 10-Q and 10-K entries for 3,6,9 and 12 months or 0 months
 * 
 * @author pschatzmann
 *
 */
public class FilterQuarterlyCumulated implements IRowFilter {
	private static final Logger LOG = Logger.getLogger(FilterQuarterlyCumulated.class);
	private List<Map<String, ?>> list;
	private PeriodsDetermination periods;

	public FilterQuarterlyCumulated() {
	}
	
	@Override
	public Boolean apply(ITable table, Integer row) {
		setup(table);
		Map<String,?> record = list.get(row);
		Boolean result = periods.isValidQuarterlyCumulated(record.get("date").toString(), record.get("form").toString(), record.get("numberOfMonths").toString());
		return result;
	}

	protected void setup(ITable table) {
		if (this.list != table) {
			this.list = table.toList();
			periods = new PeriodsDetermination(table);
		}
	}

	@Override
	public String getRestKey() {
		return "QC";
	}

}
