package ch.pschatzmann.edgar.reporting.marketshare;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ch.pschatzmann.common.table.ITableEx;
import ch.pschatzmann.common.table.Value;
import ch.pschatzmann.edgar.base.errors.DataException;
import ch.pschatzmann.edgar.reporting.EdgarModel;
import ch.pschatzmann.edgar.reporting.Table;
import ch.pschatzmann.edgar.table.CombinedKey;
import ch.pschatzmann.edgar.table.Key;

/**
 * Calculates the total sales per company and year
 * 
 * @author pschatzmann
 *
 */
public class ValuesByCompanyAndYear implements ITableEx<Value> {
	private static final long serialVersionUID = 1L;
	private Table table;

	public ValuesByCompanyAndYear(List<String> parameters) throws DataException, ClassNotFoundException, SQLException {
		EdgarModel model = new EdgarModel();
		model.setParameterAsPriorityAlternatives(true);
		model.create();
		model.getNavigationField("values", "unitref").setFilterValues("USD");
		model.getNavigationField("values", "segment").setFilterValues("");
		model.getNavigationField("values", "segmentdimension").setFilterValues("");
		model.getNavigationField("values", "form").setFilterValues("10-K");
		model.getNavigationField("values", "parameterName").setFilterValues(parameters);
		model.getNavigationField("values", "numberOfMonths").setFilterValues("12");

		this.table = new Table();
		table.setValueField(model.getTable("values").getValueField());
		table.addColumn(model.getNavigationField("values", "year", null));
		table.addRow(model.getNavigationField("company", "companyName", null));
		table.addRow(model.getNavigationField("values", "identifier", null));
		table.addRow(model.getNavigationField("company", "sicCode", null));
		table.addRow(model.getNavigationField("company", "sicDescription", null));
		table.addRow(model.getNavigationField("company", "tradingSymbol", null));
		table.execute(model);		
	}
	
	public ValuesByCompanyAndYear() throws DataException, ClassNotFoundException, SQLException {
		this(Arrays.asList("Revenues","SalesRevenueNet"));
	}

	@Override
	public List getRowFieldNames() {
		return table.getRowFieldNames();
	}

	@Override
	public int getColumnCount() {
		return table.getColumnCount();
	}

	@Override
	public String getColumnTitle(int col) {
		return table.getColumnTitle(col);
	}

	@Override
	public int getRowCount() {
		return table.getRowCount();
	}

	@Override
	public List getRowValue(int row) {
		return table.getRowValue(row);
	}

	@Override
	public Value getValue(int col, int row) {
		return new Value(this.table.getValue(col, row));
	}

	@Override
	public Table getBaseTable() {
		return this.table;
	}

	/**
	 * We return the sales for the requested SIC code and year. Please note that if
	 * we do not have the data for the full year available we report the last full
	 * year instead
	 * 
	 * @param year
	 * @param companyName
	 * @return
	 * @throws ParseException
	 */
	public Double getTotalSales(String year, String companyName) throws ParseException {
		CombinedKey key = new CombinedKey(new Key(Arrays.asList(year)), new Key(Arrays.asList(companyName)));
		return table.getValue(key).doubleValue();
	}

	@Override
	public ITableEx<Value> addColumnKey(String parameterName) {
		return this.table.addColumnKey(parameterName);
	}
	

}
