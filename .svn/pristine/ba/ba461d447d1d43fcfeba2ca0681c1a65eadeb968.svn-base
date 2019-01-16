package ch.pschatzmann.edgar.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.pschatzmann.common.table.ITable;
import ch.pschatzmann.edgar.table.Key;

/**
 * Scenario1: select only 1 quarter values 
 * Scenario2: select 1,2,3,4 Q values (max Q)
 * 
 * @author pschatzmann
 *
 */
public class DateTable implements ITable {
	public enum Type {Quarter, MaxPeriods}
	private List<String> dates;
	private int datePos;
	private int numberOfMonths;
	private Table table;
	
	
	public DateTable(Table table, Type type) {
		this.table = table;
		datePos = table.getRowFieldNames().indexOf("date");
		numberOfMonths = table.getRowFieldNames().indexOf("numberOfMonths");
		Set<String> datesSet = IntStream.range(0, table.getRowCount())
			.mapToObj(row -> table.getRowValue(row).get(datePos))
			.collect(Collectors.toSet());
		dates = new ArrayList(datesSet);
		
		// correct dataRows
		List<Key> delete = new ArrayList();
		List<String> dates = new ArrayList();
		for (Key keys : this.table.getDataRows()){
			String date = keys.get(datePos);
			switch(type) {
			case Quarter:
				if (getPeriodsInQuarters(keys.get(numberOfMonths))!=1l){
					delete.add(keys);
				}
				break;
			case MaxPeriods:
				String maxPeriods = getMaxPeriodForDate(date);
				if (!maxPeriods.equals(keys.get(numberOfMonths))){
					delete.add(keys);
				}
			}
		}
		this.table.getDataRows().removeAll(delete);
	}
	
	protected List<String> getPeriodsForDate(String date){
		return IntStream.range(0, table.getRowCount())
			.mapToObj(row -> table.getRowValue(row).get(numberOfMonths))
			.filter(row -> date.equals(table.getRowValue(datePos)))
			.collect(Collectors.toList());	
	}

	protected String getMaxPeriodForDate(String date){
		List<String> periods = getPeriodsForDate(date);
		return periods.get(periods.size()-1);
	}
	
	
	protected long getPeriodsInQuarters(String numberOfMonths) {
		return Math.round(Double.valueOf(numberOfMonths) / 3.0);
	}

	protected String getDateByOffset(String date, int offset){
		String result=null;
		int pos = dates.indexOf(date)+offset;
		if (pos>=0 && pos<dates.size()) {
			result = dates.get(pos);
		}
		return result;
	}
	
	
	@Override
	public int getColumnCount() {
		return table.getColumnCount();
	}

	@Override
	public String getColumnTitle(int pos) {
		return table.getColumnTitle(pos);
	}

	@Override
	public int getRowCount() {
		return table.getRowCount();
	}

	@Override
	public List<String> getRowFieldNames() {
		return table.getRowFieldNames();
	}

	@Override
	public List<String> getRowValue(int pos) {
		return table.getRowValue(pos);
	}

	@Override
	public Number getValue(int col, int row) {
		return table.getValue(col, row);
	}

	@Override
	public ITable getBaseTable() {
		return this;
	}

}
