package ch.pschatzmann.edgar.reporting.company;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import ch.pschatzmann.common.table.FormatException;
import ch.pschatzmann.common.table.ITable;
import ch.pschatzmann.common.table.TableFormatterCSV;
import ch.pschatzmann.common.table.TableFormatterHtml;
import ch.pschatzmann.common.table.TableFormatterJson;
import ch.pschatzmann.edgar.base.errors.DataException;

/**
 * Result which supports different formats
 * 
 * @author pschatzmann
 *
 */
public interface ICompanyInfoResult extends Serializable {
	/**
	 * Convert the List to an ArrayList
	 * @param useArrayList
	 * @return
	 */

	public ICompanyInfoResult setUseArrayList(boolean useArrayList);
	
	/**
	 * Provides the result as CSV String
	 * @return
	 * @throws FormatException
	 * @throws DataException
	 */
	
	default String toCsv() throws DataException,FormatException {
		return new TableFormatterCSV().format(getTable());
	}


	/**
	 * Provides the result as Html table
	 * @return
	 * @throws FormatException
	 * @throws DataException
	 */
	default String toHtml() throws  DataException,FormatException {
		return new TableFormatterHtml().format(getTable());
	}

	/**
	 * Provides the result as Json
	 * @return
	 * @throws FormatException
	 * @throws DataException
	 */
	default String toJson() throws  DataException, FormatException {
		return new TableFormatterJson().format(getTable());
	}


	/**
	 * Provides the result as List
	 * @return
	 * @throws DataException
	 */
	public List<Map<String, ?>> toList() throws DataException;

	/**
	 * Provides the result as ITable
	 * @return
	 * @throws DataException
	 */
	public ITable getTable() throws DataException;

	/**
	 * Determines the number of records
	 * @return
	 * @throws DataException
	 */
	default long size() throws  DataException {
		return this.getTable()==null? 0 : this.getTable().getRowCount();
	}
	
	/**
	 * Returns true if there is no data
	 * @return
	 * @throws DataException
	 */
	default boolean isEmpty() throws DataException {
		return this.size()==0;
	}

}
