package ch.pschatzmann.edgar.reporting;

import java.util.Arrays;
import java.util.Collection;

/**
 * Defines the joins relationships on the field level and is used to generate
 * the join statement. This is used by the relation
 * 
 * @author pschatzmann
 *
 */
public class JoinCondition {
	private DBField fromField;
	private DBField toField;

	/**
	 * Default Constructor
	 * @param fromField
	 * @param toField
	 */
	public JoinCondition(DBField fromField, DBField toField) {
		this.fromField = fromField;
		this.toField = toField;
	}

	/**
	 * Checks if the table is used in the from or to field
	 * @param table
	 * @return
	 */
	public boolean isValid(DBTable table) {
		return fromField.getTable() == table || toField.getTable() == table;
	}

	/**
	 * Returns the from and to fields 
	 * 
	 * @return
	 */
	public Collection<DBField> getFields() {
		return Arrays.asList(fromField, toField);
	}

	/**
	 * Determines the from field
	 * @return
	 */
	public DBField getFromField() {
		return fromField;
	}

	/**
	 * Determines the to field
	 * @return
	 */
	public DBField getToField() {
		return toField;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("fields: ");
		sb.append(this.getFromField());
		sb.append("->");
		sb.append(this.getToField());
		return sb.toString();
	}

}
