package ch.pschatzmann.edgar.reporting.company;

import java.util.function.BiFunction;

import ch.pschatzmann.common.table.ITable;

/**
 * Dynamic Filter to select the valid filing rows
 * 
 * @author pschatzmann
 *
 */
public interface IRowFilter extends BiFunction<ITable, Integer, Boolean> {
	default String getFileNameRegex() {return ".*10-.*";}
	String getRestKey();
}
