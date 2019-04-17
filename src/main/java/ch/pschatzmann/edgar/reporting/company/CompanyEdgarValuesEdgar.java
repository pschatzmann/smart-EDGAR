package ch.pschatzmann.edgar.reporting.company;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ch.pschatzmann.edgar.base.EdgarCompany;
import ch.pschatzmann.edgar.base.Fact.DataType;
import ch.pschatzmann.edgar.base.FactValue;
import ch.pschatzmann.edgar.base.ICompany;
import ch.pschatzmann.edgar.base.XBRL;
import ch.pschatzmann.edgar.base.errors.DataException;
import ch.pschatzmann.edgar.dataload.online.CompanyInformation;
import ch.pschatzmann.edgar.table.ValueTable;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Provides the reported parameter values for a company directly from EDGAR. Per default we
 * use the QuarterlyCumulated filter
 * 
 * @author pschatzmann
 *
 */
public class CompanyEdgarValuesEdgar extends CompanyEdgarValuesBase implements ICompanyInfo {
	private static final long serialVersionUID = 1L;
	private Predicate<FactValue> pedicate = a -> a.getDataType() == DataType.number && a.getUnit().equals("USD")
			&& a.getContext().getSegments().isEmpty() && !Utils.isEmpty(a.getDate());
	private String identifier;
	private Collection<String> parameterNames;

	/**
	 * Select the values with the help of the EdgarCompany 
	 * 
	 * @param company
	 * @throws DataException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */

	public CompanyEdgarValuesEdgar(ICompany company) throws DataException, ClassNotFoundException, SQLException {
		this.identifier = company.getCompanyNumber();
	}
	
	/**
	 * Select the values with the help of the company identifier
	 * @param companyNumber
	 */
	public CompanyEdgarValuesEdgar(String companyNumber)  {
		this.identifier = companyNumber;
	}

	/**
	 * Constructor which allows the specification of a custom filtering of values
	 * 
	 * @param company
	 * @param pedicate
	 * @throws DataException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public CompanyEdgarValuesEdgar(EdgarCompany company, Predicate<FactValue> pedicate)
			throws DataException, ClassNotFoundException, SQLException {
		this.identifier = company.getCompanyNumber();
		this.pedicate = pedicate;
	}

	/**
	 * Setup of File based Table
	 */
	protected void setup() throws DataException {
		try {
			if (table == null) {
				if (parameterNamesArray != null) {
					
					ValueTable table = new ValueTable("value", "parameterName", Arrays.asList("prefix", "label", "uri", "dateLabel",
					    "contextRef", "decimals", "segment", "segmentDimension", "file", "id"));
					this.table = table;

					IRowFilter f = (IRowFilter) this.filter;
					String regex = getRegex(f);
					// process all XBRL filings
					new CompanyInformation(identifier,regex.replaceAll("\\.\\*", "")).stream(regex)
					    .parallel()
						.filter(e -> e.getXBRL().isPresent())
						.map(e -> e.getXBRL().get())
						.forEach(e -> this.addValues(e, table));

					// add requested parameter as column even if it does not exist
					if (this.isAddMissingParameters() && parameterNamesArray != null) {
						new ArrayList<String>(Arrays.asList(parameterNamesArray))
								.forEach(parameter -> table.addColumnKey(parameter));
					}
	
					// Adds the defined filter
					setFilter(filter, consolidated);
				}
			}
		} catch(Exception ex) {
			throw new DataException(ex);
		}

	}

	/**
	 * Adds the data from the xbrl to the table
	 * @param xbrl
	 * @param table
	 */
	protected void addValues(XBRL xbrl ,ValueTable table) {
		List<Map<String, String>> records = xbrl.findValues().stream()
		.filter(pedicate)
		.filter(p -> isValidParameter(p.getParameterName()))
		.map(a -> a.getAttributes())
		.collect(Collectors.toList());
		table.addValues(records);
	}

	protected String getRegex(IRowFilter f) {
		String regex = null;
		if (this.filter instanceof IRowFilter) {
			regex = f.getFileNameRegex();
		}
		if (Utils.isEmpty(regex)|| regex.equals(".*")) {
			regex = ".*10-.*";
		}
		return regex;
	}

	/**
	 * Remove the invalid parameter names
	 * 
	 * @param parameterName
	 * @return
	 */
	protected boolean isValidParameter(String parameterName) {
		if (parameterNames == null) {
			parameterNames = new HashSet(Arrays.asList(this.parameterNamesArray));
		}
		return parameterNames.isEmpty() || parameterNames.contains(parameterName);
	}
}
