package ch.pschatzmann.edgar.reporting;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import ch.pschatzmann.edgar.utils.Utils;

/**
 * Field information which contains enough context information so that we are
 * able to generate the complete SQL command: The relation information is
 * necessary so that we know on which join path the field is located.
 * 
 * @author pschatzmann
 *
 */

@XmlRootElement
public class NavigationField extends DBField {
	private DBRelation relation;

	public NavigationField() {
	}

	/**
	 * Constructor
	 * 
	 * @param t
	 * @param fieldName
	 * @param toRelation
	 */
	public NavigationField(DBTable t, String fieldName, DBRelation toRelation) {
		super(t, fieldName);
		this.relation = toRelation;
		setupGroup(t, fieldName);
	}

	/**
	 * Minimum Constructor
	 * 
	 * @param field
	 * @param relation
	 */
	public NavigationField(DBField field, DBRelation relation) {
		super(field.getTable(), field.getFieldName(), field.getFieldNameSQL());
		this.relation = relation;
		setupGroup(field.getTable(), field.getFieldName());
	}

	private void setupGroup(DBTable t, String fieldName) {
		if (relation != null && relation.getFromTable() != t) {
			StringBuffer sb = new StringBuffer();
			if (relation.getFromTable().getToRelations().size() >= 1) {
				String title = relation.getJoinConditions().iterator().next().getFromField().getFieldName();
				sb.append(title);
				sb.append(" ");
			}
			sb.append(this.getTable().getTableName());
			this.setGroup(Utils.capitalize(sb.toString()));
		} else {
			this.setGroup(Utils.capitalize(this.getTable().getTableName()));
		}
	}

	/**
	 * The join path on which the field is located.
	 * 
	 * @return
	 */

	public DBRelation getToRelation() {
		return relation;
	}

	/**
	 * Determines all the relations which are originating from the current table
	 * in whcih the field is located
	 * 
	 * @return
	 */
	public Collection<DBRelation> getFromRelations() {
		return this.getTable().getFromRelations();
	}
	
	/**
	 * Defines the values which are used to restrict the result via a where condition
	 * @param filterValues
	 */
	public NavigationField setFilterValues(List<String> filterValues) {
		super.setFilterValues(filterValues);
		return this;
	}
	/**
	 * Defines the values which are used to restrict the result via a where condition
	 * @param filterValues
	 */
	public NavigationField setFilterValues(String ...filterValues) {
		return this.setFilterValues(Arrays.asList(filterValues));
	}


}
