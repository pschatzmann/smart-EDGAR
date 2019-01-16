package ch.pschatzmann.edgar.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parameters which are used in the Post Request of the values web-service
 * @author pschatzmann
 *
 */
public class Query {
	static ObjectMapper mapper = new ObjectMapper();

	private List<WSField> rows; 
	private List<WSField> cols;
	private List<WSField> filter;
	private List<String> parameters;
	private int topN = 20;
	private boolean parameterAsPriorityAlternatives = true;

	public List<WSField> getRows() {
		return rows;
	}
	public void setRows(List<WSField> rows) {
		this.rows = rows;
	}
	public List<WSField> getCols() {
		return cols;
	}
	public void setCols(List<WSField> cols) {
		this.cols = cols;
	}
	public List<WSField> getFilter() {
		return filter;
	}
	public void setFilter(List<WSField> filter) {
		this.filter = filter;
	}
	public List<String> getParameters() {
		return parameters;
	}
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	public int getTopN() {
		return topN;
	}
	public void setTopN(int topN) {
		this.topN = topN;
	}
	public boolean isParameterAsPriorityAlternatives() {
		return parameterAsPriorityAlternatives;
	}
	public void setParameterAsPriorityAlternatives(boolean parameterAsPriorityAlternatives) {
		this.parameterAsPriorityAlternatives = parameterAsPriorityAlternatives;
	}
	public synchronized String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return "";
		}
	}
	
}
