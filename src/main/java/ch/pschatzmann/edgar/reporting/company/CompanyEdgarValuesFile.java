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
import ch.pschatzmann.edgar.base.XBRL;
import ch.pschatzmann.edgar.base.errors.DataException;
import ch.pschatzmann.edgar.table.ValueTable;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Provides the reported parameter values for a company by date from the xbrl zip files. Per default we
 * use the QuarterlyCumulated filter
 * 
 * @author pschatzmann
 *
 */
public class CompanyEdgarValuesFile extends CompanyEdgarValuesBase implements ICompanyInfo {
	private static final long serialVersionUID = 1L;
	private Predicate<FactValue> pedicate = a -> a.getDataType() == DataType.number && a.getUnit().equals("USD")
			&& a.getContext().getSegments().isEmpty() && !Utils.isEmpty(a.getDate());
	private String identifier;
	private Collection<String> parameterNames;

	/**
	 * Select the values with the help of the company identifier
	 * 
	 * @param company
	 * @throws DataException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */

	public CompanyEdgarValuesFile(EdgarCompany company) throws DataException, ClassNotFoundException, SQLException {
		this.identifier = company.getCompanyNumber();
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
	public CompanyEdgarValuesFile(EdgarCompany company, Predicate<FactValue> pedicate)
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
					XBRL xbrl;
					if (this.filter instanceof IRowFilter) {
						IRowFilter f = (IRowFilter) this.filter;
						xbrl = new EdgarCompany(identifier).getXBRL(f.getFileNameRegex());					
						
					} else {
						xbrl = new EdgarCompany(identifier).getXBRL();					
					}
	
					ValueTable table = new ValueTable("value", "parameterName", Arrays.asList("prefix", "label", "uri", "dateLabel",
							"contextRef", "decimals", "segment", "segmentDimension", "file", "id"
							//,"incorporation","companyName","sicCode","location","tradingSymbol","sicDescription"
							));
					this.table = table;
					List<Map<String, String>> records = xbrl.findValues().stream()
							.filter(pedicate)
							.filter(p -> isValidParameter(p.getParameterName()))
							.map(a -> a.getAttributes())
							.collect(Collectors.toList());
					table.addValues(records);
	
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
