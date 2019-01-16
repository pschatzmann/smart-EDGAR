package ch.pschatzmann.edgar.reporting;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Defines the relationship between tables. This information is used to generate
 * the join condition. The relationships are directed from a table leading to
 * another table.
 * 
 * @author pschatzmann
 *
 */

public class DBRelation implements Comparable<DBRelation> {
	private Collection<JoinCondition> joinConditions = new ArrayList<JoinCondition>();
	private String name;
	private DBTable fromTable;
	private DBTable toTable;
	private int seq;
	
	public DBRelation(String name) {
		this.name = name;
	}

	public DBRelation addJoinCondition(JoinCondition jc) {
		this.joinConditions.add(jc);
		fromTable = jc.getFromField().getTable();
		toTable = jc.getToField().getTable();
		
		toTable.addRelation(this);
		return this;
	}

	/**
	 * Returns the from table
	 * @return
	 */
	public DBTable getFromTable() {
		return fromTable;
	}

	/**
	 * Returns the to table
	 * @return
	 */
	public DBTable getToTable() {
		return toTable;
	}

	/**
	 * Returns the field level join conditions 
	 * @return
	 */
	public Collection<JoinCondition> getJoinConditions() {
		return joinConditions;
	}

	/**
	 * Determines if the field is on join condition on the from table
	 * @param relationFromField
	 * @return
	 */
	public boolean isRelationField(String relationFromField) {
		for (JoinCondition jc : joinConditions) {
			if (jc.getFromField().getFieldName().equalsIgnoreCase(relationFromField)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("table: ");
		sb.append(this.getFromTable());
		sb.append("->");
		sb.append(this.getToTable());
		sb.append("/");
		sb.append(this.getJoinConditions());
		return sb.toString();
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	public String getName() {
		return this.name;
	}

	/**
	 * Comparator based on seq, from table and to table
	 */
	@Override
	public int compareTo(DBRelation o) {
		int result =  this.getSeq().compareTo(o.getSeq());
		if (result == 0) {
			result = this.getFromTable().getTableName().compareTo(o.getFromTable().getTableName());
		}
		if (result == 0) {
			result = this.getToTable().getTableName().compareTo(o.getToTable().getTableName());
		}
		return result;
	}
	
}
