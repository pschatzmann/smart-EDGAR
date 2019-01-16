package ch.pschatzmann.edgar.reporting.marketshare;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.pschatzmann.common.table.ITable;
import ch.pschatzmann.common.table.ITableEx;
import ch.pschatzmann.common.table.TableBackedListOfMap;
import ch.pschatzmann.common.table.TableConsolidated;
import ch.pschatzmann.common.table.TableConsolidated.ConsolidationOperation;
import ch.pschatzmann.edgar.base.errors.DataException;
import ch.pschatzmann.edgar.reporting.Table;
import ch.pschatzmann.common.table.Value;
/**
 * Calculates the company sales as percent of the sales of the SIC Sector
 * 
 * @author pschatzmann
 *
 */
public class MarketShare implements ITableEx<Value> {
	private static final long serialVersionUID = 1L;
	private ValuesByCompanyAndYear companySales;
	private TableConsolidated sectorSales;
	private Table table;
	private List<String> sicNumbers = new ArrayList();
	private List<String> years = new ArrayList();
	private List<String> companies;

	public MarketShare() throws ClassNotFoundException, DataException, SQLException {
		this("Revenues","SalesRevenueNet");
	}

	public MarketShare(String... parameters) throws ClassNotFoundException, DataException, SQLException {
		this(Arrays.asList(parameters));
	}

	
	public MarketShare(List<String> parameters) throws ClassNotFoundException, DataException, SQLException {
		this.companySales = new ValuesByCompanyAndYear(parameters);
		this.table = companySales.getBaseTable();
		
		this.sectorSales = new TableConsolidated(companySales,Arrays.asList("companyName","identifier","sicDescription","tradingSymbol"),ConsolidationOperation.Sum);
		sicNumbers =  IntStream.range(0,this.sectorSales.getRowCount())
				.mapToObj(row -> sectorSales.getRowValue(row).get(0).toString()).collect(Collectors.toList());
		
		years = IntStream.range(0,this.sectorSales.getColumnCount())
				.mapToObj(col -> sectorSales.getColumnTitle(col)).collect(Collectors.toList());
		
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
		Value result = new Value();
		try {
			Value companySales = new Value(this.table.getValue(col, row));
			if (companySales != null) {
				String year = this.getColumnTitle(col);
				String sicCode = (String) this.getRowValue(row).get(2);
				Number sicValue = this.getTotalSales(year, sicCode);
				result = new Value(Math.round(companySales.doubleValue() / sicValue.doubleValue() * 100 * 100) / 100.00);
			}
		} catch (Exception ex) {
		}
		return result;
	}

	private Number getTotalSales(String year, String sicCode) throws ParseException {
		String maxYear = getMaxYear(year);
		return sectorSales.getValue(years.indexOf(maxYear), sicNumbers.indexOf(sicCode)).doubleValue();
	}
	
	
	public Number getMarketShare(String companyNumber, String year) {
		Number result = null;
		if (this.companies==null) {
			this.companies = new ArrayList();
			for (int j=0;j<this.getRowCount();j++) {
				companies.add(this.getRowValue(j,"identifier"));
			}
		}
		int row = companies.indexOf(companyNumber);
		int col = this.years.indexOf(year);
		result = this.getValue(col, row);
		
		return result;
	}
	
	public String getRowValue(int row, String fieldName) {
		String result = "";
		int pos = this.getRowFieldNames().indexOf(fieldName);
		if (pos>=0) {
			result = (String) this.getRowValue(row).get(pos);
		}
		return result;
	}


	@Override
	public ITable<Value> getBaseTable() {
		return companySales;
	}
	
	/**
	 * Determines the max year where we have all data available 
	 * @param year
	 * @return
	 * @throws ParseException
	 */
	public static String getMaxYear(String year) throws ParseException {
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		int lastYear = currentYear - 1;
		int lastYear2 = currentYear - 2;
		int requestedYear = Integer.parseInt(year);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		// we can never report the current year
		// the last year is available from April of the current year
		String result = null;
		if (requestedYear < lastYear2) {
			result = year;
		} else {
			Date currentDate = new Date();
			int maxYear = 0;
			if (currentDate.after(df.parse(currentYear + "-04-05"))) {
				maxYear =  lastYear;
			} else {
				maxYear =  lastYear2;
			}
			result = requestedYear <= maxYear ? year : ""+maxYear;
		}
		return result;
	}

	@Override
	public ITableEx<Value> addColumnKey(String parameterName) {
		return table.addColumnKey(parameterName);
	}
	
	@Override
	public List<Map<String,Value>> toList() {
		return new TableBackedListOfMap<Value>(this);
	}


}
