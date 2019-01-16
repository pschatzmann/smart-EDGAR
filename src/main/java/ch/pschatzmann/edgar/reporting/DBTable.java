package ch.pschatzmann.edgar.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.pschatzmann.edgar.base.errors.DataException;

/**
 * Model for a single SQL database table which contains fields and which has
 * relationships to other tables.
 * 
 * @author pschatzmann
 *
 */

@JsonIgnoreProperties({ "fields", "valueField","relations","navigationFields" })
public class DBTable {
	private String tableName;
	private Collection<DBField> fields = new ArrayList<DBField>();
	private NavigationField valueField;
	private Collection<DBRelation> relations = new ArrayList<DBRelation>();
	private List<NavigationField> navigationFields = null;

	/**
	 * Minimum constructor. The fields must be defined separatly
	 * 
	 * @param tableName
	 */
	public DBTable(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Constructor which defines the table with its fields
	 * 
	 * @param tableName
	 * @param fieldNames
	 */
	public DBTable(String tableName, Collection<String> fieldNames) {
		this.tableName = tableName;
		for (String fld : fieldNames) {
			addField(fld);
		}
	}

	/**
	 * add a field
	 * 
	 * @param fieldName
	 * @return
	 */
	public DBField addField(String fieldName) {
		DBField fld = new DBField(this, fieldName);
		fields.add(fld);
		return fld;
	}

	/**
	 * Add a field.
	 * 
	 * @param fieldName
	 * @param expression
	 * @return
	 */
	public DBField addField(String fieldName, String expression) {
		DBField fld = new DBField(this, fieldName, expression);
		fields.add(fld);
		return fld;
	}

	/**
	 * Returns the field by name
	 * 
	 * @param name
	 * @return
	 */
	public DBField getField(String name) {
		for (DBField f : this.fields) {
			if (f.getFieldName().equalsIgnoreCase(name)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * Determines the table name
	 * 
	 * @return
	 */
	public String getTableName() {
		return this.tableName;
	}

	/**
	 * Returns all fields of the table
	 * 
	 * @return
	 */
	public Collection<DBField> getFields() {
		return fields;
	}

	/**
	 * Defines the default value field for this table
	 * 
	 * @param tableField
	 * @return
	 */
	public NavigationField setValueField(NavigationField tableField) {
		this.valueField = tableField;
		return tableField;
	}

	/**
	 * Determines the default value field
	 * 
	 * @return
	 */
	public NavigationField getValueField() {
		return valueField;
	}

	/**
	 * Adds a relationship information. The same information is also added to
	 * the To table so that we can navigate both top down and button up.
	 * 
	 * @param rel
	 * @return
	 */

	public DBRelation addRelation(DBRelation rel) {
		this.relations.add(rel);
		return rel;
	}

	/**
	 * Returns the currently defined relationships
	 * 
	 * @return
	 */
	public Collection<DBRelation> getRelations() {
		return relations;
	}

	/**
	 * A separate navigtion field is generated for each relationship which is
	 * leading to the field.
	 * 
	 * @return
	 */
	public Collection<NavigationField> getNavigationFields() {
		if (navigationFields == null) {
			navigationFields = new ArrayList<NavigationField>();
			for (DBField fld : this.getFields()) {
				for (DBRelation rel : this.getToRelations()) {
					if (fld != this.valueField) {
						navigationFields.add(new NavigationField(fld, rel));
					}
				}
				if (this.getToRelations().isEmpty()){
					if (fld != this.valueField) {
						navigationFields.add(new NavigationField(fld, null));
					}
				}
			}
		}
		return navigationFields;
	}

	/**
	 * Determines the navigation field by name. The relationFromField is
	 * optional and relevant only if there are multiple relationships
	 * 
	 * @param name
	 * @param relationFromField
	 * @return
	 * @throws DataException 
	 */
	public NavigationField getNavigationField(String name, String relationFromField) throws DataException {
		for (NavigationField fld : getNavigationFields()) {
			if (fld.getFieldName().equalsIgnoreCase(name)) {
				if (relationFromField == null || fld.getToRelation().getName().equals(relationFromField) || fld.getToRelation().isRelationField(relationFromField)) {
					return fld;
				}
			}
		}
		return null;
	}

	/**
	 * Determines all relationships which are leading from the current table
	 * 
	 * @return
	 */
	public Collection<DBRelation> getFromRelations() {
		return this.relations.stream().filter(p -> p.getFromTable() == this).collect(Collectors.toList());
	}

	/**
	 * Determines all relationships which are leading to the current table
	 * 
	 * @return
	 */
	public Collection<DBRelation> getToRelations() {
		return this.relations.stream().filter(p -> p.getToTable() == this).collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return this.getTableName();
	}

}
