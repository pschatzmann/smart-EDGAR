package ch.pschatzmann.edgar.reporting.company;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import ch.pschatzmann.common.table.ITable;
import ch.pschatzmann.common.utils.Tuple;

/**
 * Provides the reported parameter values for a company by date. Per default we
 * use the QuarterlyCumulated filter
 * 
 * @author pschatzmann
 *
 */
public interface ICompanyInfo extends ICompanyInfoResult {
	/**
	 * Defines the Parameters that should be returned in the table
	 * @param parameterNames
	 * @return
	 */
	default ICompanyInfo setParameterNames(String... parameterNames) {
		return setParameterNames(Arrays.asList(parameterNames));
	}
	
	/**
	 * Defines the Parameters that should be returned in the table
	 * @param parameterNames
	 * @return
	 */
	public ICompanyInfo setParameterNames(Collection<String> parameterNames);
	

	/**
	 * Defines the Parameters that should be removed from the final result
	 * @param removeParameterNames
	 * @return
	 */
	
	default ICompanyInfo removeParameterNames(String...removeParameterNames ) {
		return removeParameterNames(Arrays.asList(removeParameterNames));
	}

	/**
	 * Defines the Parameters that should be removed from the final result
	 * @param removeParameterNames
	 * @return
	 */
	public ICompanyInfo removeParameterNames(List<String>removeParameterNames );

	/**
	 * Defines the unit which should be returned
	 * @param unitRef
	 * @return
	 */
	public ICompanyInfo setUnitRef(String... unitRef);


	/**
	 * Defines if we the missing parameters should be returned as empty column
	 * @param addMissingParameters
	 * @return
	 */
	public ICompanyInfo setAddMissingParameters(boolean addMissingParameters);

		
	/**
	 * The date field is usually represented as String. We can convert it to a date instead
	 * @param flag
	 * @return
	 */
	public ICompanyInfo setAddTime(boolean flag);
	
	/**
	 * Adds a calculated column
	 * 
	 * @param parameterName
	 * @param formula
	 * @return
	 */
	public ICompanyInfo addFormula(String parameterName, String formula);
	
	/**
	 * Adds many calculated columns
	 * @param formulas
	 * @return
	 */
	public ICompanyInfo addFormulas(List<Tuple<String, String>> formulas);


	/**
	 * Defines a row filter
	 * @param filter
	 * @return
	 */
	public ICompanyInfo setFilter(BiFunction<ITable, Integer, Boolean> filter);


}
