package ch.pschatzmann.edgar.reporting.company;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import ch.pschatzmann.common.table.FormatException;
import ch.pschatzmann.common.table.ITable;
import ch.pschatzmann.common.table.ITableEx;
import ch.pschatzmann.common.table.TableCalculated;
import ch.pschatzmann.common.table.TableConsolidated;
import ch.pschatzmann.common.table.TableFilteredOnCol;
import ch.pschatzmann.common.table.TableFilteredOnRow;
import ch.pschatzmann.common.table.TableFormatterCSV;
import ch.pschatzmann.common.table.TableFormatterHtml;
import ch.pschatzmann.common.table.TableFormatterJson;
import ch.pschatzmann.common.utils.Tuple;
import ch.pschatzmann.edgar.base.errors.DataException;
import ch.pschatzmann.edgar.reporting.EdgarModel;
import ch.pschatzmann.edgar.reporting.NavigationField;
import ch.pschatzmann.edgar.reporting.Table;
import ch.pschatzmann.edgar.reporting.ValueField;

/**
 * Provides the reported parameter values for a company by date from the sql database. Per default we
 * use the QuarterlyCumulated filter
 * 
 * @author pschatzmann
 *
 */
public class CompanyEdgarValuesDB extends CompanyEdgarValuesBase implements ICompanyInfo {
	private static final long serialVersionUID = 1L;
	private CompanySelection companySelection;

	/**
	 * Select the values with the help of the company identifier
	 * @param identifier
	 * @throws DataException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public CompanyEdgarValuesDB(String identifier) throws DataException, ClassNotFoundException, SQLException {
		this(new CompanySelection().setCompanyNumber(identifier));
	}
	
	/**
	 * Default Constructor: we provide a CompanySelection object 
	 * @param companySelection
	 * @throws DataException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */

	public CompanyEdgarValuesDB(CompanySelection companySelection)
			throws DataException, ClassNotFoundException, SQLException {
		if (!companySelection.isValid()) {
			throw new RuntimeException(
					"You must select a company by defining the identifier, companyName or tradingSymbol");
		}
		this.companySelection = companySelection;
	}

	protected void setup() throws DataException {
		try {
			if (table == null) {
				EdgarModel model = new EdgarModel();
				model.setParameterAsPriorityAlternatives(false);
				model.create();
				model.getNavigationField("values", "unitref").setFilterValues(Arrays.asList(unitRefArray));
				model.getNavigationField("values", "segment").setFilterValues(Arrays.asList(""));
				model.getNavigationField("values", "segmentdimension").setFilterValues(Arrays.asList(""));
				
				NavigationField companyName = model.getNavigationField("company", "companyName", null);
				NavigationField companyIdentifier = model.getNavigationField("values", "identifier", null);
				NavigationField companyTradingSymbol = model.getNavigationField("company", "tradingSymbol", null);
				NavigationField parameterNames = model.getNavigationField("values", "parameterName", null);
				ValueField valueFld = (ValueField) model.getTable("values").getValueField();
				valueFld.setSelectedFunction(null);
				
				if (!companySelection.getCompanyNumber().isEmpty())
					companyIdentifier.setFilterValues(Arrays.asList(companySelection.getCompanyNumber()));
				if (!companySelection.getCompanyName().isEmpty())
					companyName.setFilterValues(Arrays.asList(companySelection.getCompanyName()));
				if (!companySelection.getTradingSymbol().isEmpty())
					companyTradingSymbol.setFilterValues(Arrays.asList(companySelection.getTradingSymbol()));
				if (parameterNamesArray != null) {
					parameterNames.setFilterValues(Arrays.asList(parameterNamesArray));
				}
	
				Table table = new Table();
				table.setValueField(valueFld);
				table.addColumn(parameterNames);
				table.addRow(model.getNavigationField("values", "date", null));
				table.addRow(companyName);
				table.addRow(companyTradingSymbol);
				table.addRow(companyIdentifier);
				table.addRow(model.getNavigationField("company", "incorporation", null));
				table.addRow(model.getNavigationField("company", "location", null));
				table.addRow(model.getNavigationField("company", "sicDescription", null));
				table.addRow(model.getNavigationField("values", "form", null));
				table.addRow(model.getNavigationField("values", "numberOfMonths", null));
				table.execute(model);
				this.table = table;
				
				// add requested parameter as column even if it does not exist
				if (this.isAddMissingParameters() && parameterNamesArray!=null) {
					new ArrayList<String>(Arrays.asList(parameterNamesArray)).forEach(parameter -> table.addColumnKey(parameter));
				}
				
				// Adds the defined filter
				setFilter(filter, consolidated);
							
			}
		} catch(Exception ex) {
			throw new DataException(ex);
		}
	}

}
