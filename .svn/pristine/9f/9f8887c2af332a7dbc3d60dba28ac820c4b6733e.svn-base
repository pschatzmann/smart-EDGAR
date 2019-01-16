package ch.pschatzmann.edgar.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.pschatzmann.common.utils.Tuple;
import ch.pschatzmann.edgar.reporting.company.CompanySelection;
import ch.pschatzmann.edgar.service.EdgarDBService.PeriodFilter;

/**
 * Information which is necessary to request the companyValues Webservice
 * 
 * @author pschatzmann
 *
 */
public class CompanyValues {
	private CompanySelection companySelection;
	private String format;
	private PeriodFilter periods;
	private List<String> parameters;
	private List<String> removeParameterNames;
	private List<String> formulas;
	private String[] unitRef  = { "USD" };

	public CompanyValues() {
	}

	public CompanySelection getCompanySelection() {
		return companySelection;
	}

	public void setCompanySelection(CompanySelection companySelection) {
		this.companySelection = companySelection;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public PeriodFilter getPeriods() {
		return periods;
	}

	public void setPeriods(PeriodFilter periods) {
		this.periods = periods;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public List<String> getFormulas() {
		return formulas;
	}

	public void setFormulas(List<String> formulas) {
		this.formulas = formulas;
	}

	public List<String> getRemoveParameterNames() {
		return removeParameterNames;
	}

	public void setRemoveParameterNames(List<String> removeParameterNames) {
		this.removeParameterNames = removeParameterNames;
	}

	public String[] getUnitRef() {
		return this.unitRef;
	}
	
	public void setUnitRef(String ...unitRef) {
		this.unitRef = unitRef;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CompanyValuesRequest:");
		sb.append(this.getPeriods());
		sb.append("/");
		sb.append(this.getCompanySelection());
		sb.append("/");
		sb.append(this.getParameters());
		sb.append("/");
		sb.append(this.getFormulas());
		sb.append("/");
		sb.append(this.getRemoveParameterNames());
		return sb.toString();
	}

}
