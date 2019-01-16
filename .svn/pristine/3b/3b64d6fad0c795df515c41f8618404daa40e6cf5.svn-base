package ch.pschatzmann.edgar.reporting;

import java.util.Arrays;
import java.util.Collection;

/**
 * The value field which is not grouped needs to have an aggregate function
 * defined. We use the SUM as default function
 * 
 * @author pschatzmann
 *
 */

public class ValueField extends NavigationField {
	public Collection<String> functions = Arrays.asList("SUM(%fld)", "COUNT(%fld)", "MIN(%fld)", "MAX(%fld)","AVG(%fld)");
	public String selectedFunction = functions.iterator().next();

	/**
	 * Minimum Constructor based on a NavigationField
	 * @param field
	 */
	public ValueField(NavigationField field) {
		super(field, field.getToRelation());
		this.setCalculated(true);
	}

	/**
	 * Basic constructor which is accepting all relevant information
	 * @param field
	 * @param relation
	 * @param functions
	 */
	public ValueField(NavigationField field, DBRelation relation, Collection<String> functions) {
		super(field, relation);
		this.setCalculated(true);
		this.functions = functions;
		if (!functions.isEmpty()) {
			selectedFunction = functions.iterator().next();
		}
	}

	/**
	 * Determines the currently defined functions. The field current name can be specified with the %fld variable.
	 * e.g. sum(%fld)
	 * @return
	 */
	public Collection<String> getFunctions() {
		return functions;
	}

	/**
	 * Defines the supported functinos
	 * @param functions
	 */
	public void setFunctions(Collection<String> functions) {
		this.functions = functions;
	}

	/**
	 * Determines the currently selected function
	 * @return
	 */
	public String getSelectedFunction() {
		return selectedFunction;
	}

	/**
	 * Defines the currently selected function
	 * 
	 * @param selectedFunction
	 */
	public void setSelectedFunction(String selectedFunction) {
		this.selectedFunction = selectedFunction;
	}

	/**
	 * The field which is used to generate the SQL 
	 */
	@Override
	public String getFieldNameSQL() {
		String result =  this.getSelectedFunction();
		if (result!=null) {
			result = result.replaceAll("%fld", super.getFieldNameSQL());
		} else {
			result = super.getFieldNameSQL();
		}
		return result;
	}


}
