package ch.pschatzmann.edgar.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Wrapper for Navigation field which contains the minumum information
 * 
 * @author pschatzmann
 *
 */
public class WSField {
	private String table;
	private String fieldName;
	private String relationName;
	private List<String> filterValues = new ArrayList<String>();
	private boolean equals = true;

	/**
	 * Constructor
	 */
	public WSField() {
	}
	
	public WSField(String table, String fieldName) {
		this.table = table;
		this.fieldName = fieldName;
	}

	/**
	 * Returns the name of the table
	 * @return
	 */
	public String getTable() {
		return table;
	}

	/**
	 * Defines the name of the table
	 * @param table
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * Determine the field name
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}
	
	/**
	 * Define the field name
	 * @param fieldName
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * Returns the name of the relation
	 * @return
	 */
	public String getRelationName() {
		return relationName;
	}

	/**
	 * Define the name the relation
	 * @param name
	 */
	public void setRelationName(String name) {
		this.relationName = name;
	}

	/**
	 * Convert from json string
	 * 
	 * @param jsonRepresentation
	 * @return
	 */
	public static WSField fromString(String jsonRepresentation) {
		ObjectMapper mapper = new ObjectMapper();
		WSField o = null;
		try {
			o = mapper.readValue(jsonRepresentation, WSField.class);
		} catch (Exception e) {
			throw new WebApplicationException();
		}
		return o;
	}

	/**
	 * Returns the filter values
	 * @return
	 */
	public List<String> getFilterValues() {
		return filterValues;
	}

	/**
	 * Defines the filter values
	 * @param filterValues
	 */
	public void setFilterValues(List<String> filterValues) {
		this.filterValues = filterValues;
	}
	
	/**
	 * Defines if the filter values should be contained or not
	 * @return
	 */
	public boolean isFilterEquals() {
		return equals;
	}

	/**
	 * Set the filter logic if the values should be contained or not
	 * @param equals
	 */
	public void setFilterEquals(boolean equals) {
		this.equals = equals;
	}

	/**
	 * Convert to String
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.table);
		sb.append("/");
		sb.append(this.fieldName);
		return sb.toString();
	}

}
