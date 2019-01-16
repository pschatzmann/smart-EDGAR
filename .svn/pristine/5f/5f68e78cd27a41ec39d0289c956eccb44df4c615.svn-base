package ch.pschatzmann.edgar.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ch.pschatzmann.common.table.ITable;
import ch.pschatzmann.edgar.base.errors.DataException;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Implementation which provides the join logic. Reporting data is usually
 * represented as star model which a central fact table. This implementation is
 * assuming that the relationships are a hierarchical tree structure starting
 * from a central fact table. We only generate the join conditins which are
 * necessary for the selected fields
 * 
 * @author pschatzmann
 *
 */

public class PostgresSQLModel implements ISQLModel {
	private static final Logger LOG = Logger.getLogger(PostgresSQLModel.class);
	private AbstractModel model;

	/**
	 * Default Constructor
	 */
	public PostgresSQLModel(AbstractModel edgarModel) {
		this.model = edgarModel;
	}

	/**
	 * Gemerate SQL command based on the selected rows, column and value field
	 * 
	 * @return
	 * @throws DataException 
	 */
	@Override
	public String toSQL(ITable itable) throws DataException {
		String result = "";
		if (itable instanceof Table) {
			Table table = (Table) itable;
			StringBuffer sb = new StringBuffer();
			sqlSelectFields(table, sb, table.getGroupFields(), new ArrayList());
			sqlFrom(table, sb);
			sqlJoin(table, sb);
			sqlWhere(sb);
			sqlGroupBy(table, table.getGroupFields(), sb, true);
			result = sb.toString();
			LOG.info(result);
		}
		return result;
	}

	protected void sqlFrom(Table table, StringBuffer sb) {
		sb.append(" FROM ");
		sb.append(getTableName(table));
	}
	
	protected String getTableName(Table table) {
		String result = "values";
		if (table.getValueField()!=null) {
			result = table.getValueField().getTable().getTableName();
		} else {
			result = table.getRows().get(0).getTable().getTableName();
		}
		return result;
	}
	

	protected void sqlSelectFields(Table table, StringBuffer sb, Collection<NavigationField> groupFields,
			Collection<NavigationField> distinct) {
		sqlSelectFields(table, sb, groupFields, distinct, true);
	}

	protected void sqlSelectFields(Table table, StringBuffer sb, Collection<NavigationField> groupFields,
			Collection<NavigationField> distinct, boolean withSQL) {
		sb.append("SELECT ");
		if (!distinct.isEmpty()) {
			sb.append("DISTINCT ON (");
			boolean first = true;
			for (NavigationField fld : distinct) {
				if (!first)
					sb.append(",");
				first = false;
				sb.append(fld.getFieldName());
			}
			sb.append(" ) ");
		}

		List<DBField> groupFieldsList = new ArrayList(groupFields);
		DBField last = groupFieldsList.get(groupFieldsList.size()-1);
		for (DBField fld : groupFieldsList) {
			if (fld.isCalculated() && withSQL) {
				sb.append(fld.getFieldNameSQL());
				sb.append(" AS ");
				sb.append(fld.getFieldName());
			} else {
				sb.append(withSQL ? fld.getFieldNameExt() : fld.getFieldName());
			}
			
			if (fld != last){			
			  sb.append(", ");
			} 
		}
		
		if (table.getValueField()!=null){
			sb.append(", ");
			if (table.getValueField().isCalculated()) {
				sb.append(table.getValueField().getFieldNameSQL());
				sb.append(" AS ");
			}
			sb.append(table.getValueField().getFieldName());
		}
	}

	protected void sqlGroupBy(Table table,Collection<NavigationField> fields, StringBuffer sb, boolean withTable) {
		boolean first = true;
		if (getTables(fields).size()>=1) {
			sb.append(" GROUP BY ");
			for (DBField fld : fields) {
				if (!first)
					sb.append(", ");
				first = false;
				sb.append(fld.isCalculated() || !withTable ? fld.getFieldName() : fld.getFieldNameExt());
			}
			// group by value field if no function has been selected
			ValueField vf = (ValueField) table.getValueField();
			if (vf!=null) {
				if (Utils.isEmpty(vf.getSelectedFunction())) {
					sb.append(", ");
					sb.append(vf.getFieldNameSQL());
				}
			}
		}		
	}

	protected Collection<String> getTables(Collection<NavigationField> fields) {
		return fields.stream().map(f -> f.getTable().getTableName()).collect(Collectors.toSet());
	}

	protected void sqlWhere(StringBuffer sb) {
		boolean first;
		Collection<NavigationField> where = model.getFilterFields();
		if (!where.isEmpty()) {
			first = true;
			sb.append(" WHERE ");
			for (DBField fld : where) {
				if (!first)
					sb.append(" AND ");
				first = false;
				if (!wildCardSearch(fld)) {
					sqlWhereIn(sb, fld);
				} else {
					sqlWhereLike(sb, fld);
				}
			}
		}
	}

	protected void sqlWhereIn(StringBuffer sb, DBField fld) {
		sb.append(fld.isCalculated() ? fld.getFieldNameSQL(): fld.getFieldNameExt());
		if (!fld.isFilterEquals()) {
			sb.append(" NOT ");
		}
		sb.append(" IN (");
		sb.append(Utils.toString(fld.getFilterValues(), "'", ","));
		sb.append(")");
	}

	protected void sqlWhereLike(StringBuffer sb, DBField fld) {
		sb.append(" ( ");
		boolean firstFilter = true;
		for (String value : fld.getFilterValues()) {
			if (!firstFilter) {
				sb.append(" OR ");
			}
			sb.append(fld.isCalculated() ? fld.getFieldNameSQL(): fld.getFieldNameExt());
			sb.append(value.contains("%") ? " ILIKE " : " = ");
			sb.append("'");
			sb.append(value);
			sb.append("'");
			firstFilter = false;
		}
		sb.append(" ) ");
	}
	
	protected boolean wildCardSearch(DBField fld) {
		boolean result = false;
		if (fld.isSupportWildCardFilter()) {
			for (String value : fld.getFilterValues()) {
				if (value.contains("%")) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Construt the inner joins.
	 */

	protected void sqlJoin(Table table, StringBuffer sb) {
		if (table.getValueField()!=null) {
			Collection<DBRelation> relations = new TreeSet<DBRelation>();
			Collection<NavigationField> all = new ArrayList<NavigationField>(table.getGroupFields());
			all.add(table.getValueField());
			all.addAll(model.getFilterFields());
			explode(table.getValueField().getFromRelations(), all, relations, 0);
	
			for (DBRelation r : relations) {
				sb.append(" INNER JOIN ");
				sb.append(r.getToTable().getTableName());
				sb.append(" ON (");
				String from = r.getFromTable().getTableName();
				String to = r.getToTable().getTableName();
				boolean first = true;
				for (JoinCondition jc : r.getJoinConditions()) {
					if (!first)
						sb.append(" AND ");
					first = false;
					sb.append(from);
					sb.append(".");
					sb.append(jc.getFromField().getFieldNameSQL());
					sb.append(" = ");
					sb.append(to);
					sb.append(".");
					sb.append(jc.getToField().getFieldNameSQL());
				}
				sb.append(" ) ");
			}
		}
	}

	/**
	 * We explode the relationships and add the relationships to the result
	 * which are used by the groupfields or which are on the path to the current
	 * node.
	 * 
	 * @param fromRelations
	 * @param fields
	 * @param relations
	 * @param level
	 * @return
	 */
	protected boolean explode(Collection<DBRelation> fromRelations, Collection<NavigationField> fields,
			Collection<DBRelation> relations, int level) {

		boolean result = false;
		for (DBRelation r : fromRelations) {
			boolean isValid = false;
			if (explode(r.getToTable().getFromRelations(), fields, relations, level + 1)) {
				isValid = true;
			}
			if (isValidRelation(r, fields)) {
				isValid = true;
			}
			if (isValid) {
				relations.add(r);
				r.setSeq(level);
			}

			result = result || isValid;
		}

		return result;
	}

	protected boolean isValidRelation(DBRelation r, Collection<NavigationField> groupFields) {
		for (NavigationField f : groupFields) {
			if ((f.getTable() == r.getToTable() && (f.getToRelation() == r || f.getToRelation() == null))) {
				return true;
			}
		}
		return false;
	}

	protected AbstractModel getModel() {
		return this.model;
	}

}
