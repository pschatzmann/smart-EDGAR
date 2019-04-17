package ch.pschatzmann.edgar.reporting.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;

import ch.pschatzmann.common.table.ITable;
import ch.pschatzmann.common.utils.Tuple;
import ch.pschatzmann.edgar.base.ICompany;
import ch.pschatzmann.edgar.base.errors.DataException;
import ch.pschatzmann.edgar.service.CompanyValues;
import ch.pschatzmann.edgar.service.EdgarDBService.PeriodFilter;
import ch.pschatzmann.edgar.table.TableFromMaps;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Provides the reported parameter values for a company by date from the REST
 * API. Per default we use the Yearly filter
 * 
 * @author pschatzmann
 */

public class CompanyEdgarValuesRest implements ICompanyInfo {
	private final static Logger LOG = Logger.getLogger(CompanyEdgarValuesRest.class);
	private static final long serialVersionUID = 1L;
	private List<Tuple<String, String>> formulas = new ArrayList();
	private boolean setAddTime;
	private boolean addMissingParameter;
	private String[] unitRef ={ "USD" };
	private List<String> removeParameterNames;
	private List<String> parameterNames;
	private boolean useArrayList;
	private TableFromMaps iTable;
	private CompanySelection companySelection;
	private BiFunction<ITable, Integer, Boolean> periods = new FilterYearly();
	private String[] headers;
	private List<Map<String, String>> records = new ArrayList();

	public CompanyEdgarValuesRest(CompanySelection companySelection) {
		this.companySelection = companySelection;
	}
	
	public CompanyEdgarValuesRest(ICompany company) {
		this.companySelection = new CompanySelection().setCompanyNumber(company.getCompanyNumber());
	}
	
	public CompanyEdgarValuesRest(String companyNumber) {
		this.companySelection = new CompanySelection().setCompanyNumber(companyNumber);
	}


	@Override
	public CompanyEdgarValuesRest setUseArrayList(boolean useArrayList) {
		this.useArrayList = useArrayList;
		return this;
	}

	@Override
	public CompanyEdgarValuesRest setParameterNames(Collection<String> parameterNames) {
		this.parameterNames = new ArrayList(parameterNames);
		return this;
	}

	@Override
	public CompanyEdgarValuesRest removeParameterNames(List<String> removeParameterNames) {
		this.removeParameterNames = removeParameterNames;
		return this;
	}

	@Override
	public CompanyEdgarValuesRest setUnitRef(String... unitRef) {
		this.unitRef = unitRef;
		return this;
	}

	@Override
	public CompanyEdgarValuesRest setAddMissingParameters(boolean addMissingParameters) {
		this.addMissingParameter = addMissingParameters;
		return this;
	}

	@Override
	public CompanyEdgarValuesRest setAddTime(boolean flag) {
		this.setAddTime = flag;
		return this;
	}

	@Override
	public CompanyEdgarValuesRest addFormula(String parameterName, String formula) {
		this.formulas.add(new Tuple(parameterName, formula));
		return this;
	}

	@Override
	public CompanyEdgarValuesRest addFormulas(List<Tuple<String, String>> formulas) {
		this.formulas.addAll(formulas);
		return this;
	}

	@Override
	public List<Map<String, ?>> toList() throws DataException {
		return this.useArrayList ? new ArrayList(getTable().toList()) : getTable().toList();
	}

	@Override
	public ITable getTable() throws DataException {
		setup();
		return this.iTable;
	}

	protected void setup() {
		if (this.iTable == null) {
			CompanyValues q = new CompanyValues();
			q.setCompanySelection(this.companySelection);
			q.setFormulas(toListOfString(this.formulas));
			q.setParameters(this.parameterNames);
			q.setPeriods(getPeriodFilter());
			q.setRemoveParameterNames(removeParameterNames);
			q.setUnitRef(this.unitRef);

			try {
				int count = 0;
				for(String inputLine: postRest(q)) {
					processLine(count++, inputLine);
				}
				this.iTable = new TableFromMaps(Arrays.asList("date", "companyName", "tradingSymbol", "identifier",
						"incorporation", "location", "sicDescription"), this.addMissingParameter);
				this.iTable.addValues(this.records);

			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	protected List<String> toListOfString(List<Tuple<String, String>> formulas) {
		return formulas.stream().map(t -> t.x+": "+t.y).collect(Collectors.toList());
	}

	/**
	 * Gets the CSV data from a REST call
	 * @param q
	 * @return
	 * @throws Exception
	 */
	protected List<String> postRest(CompanyValues q) throws Exception {
	    ClientConfig config = new ClientConfig();
	    config.register(JacksonJsonProvider.class);
	    Client client = ClientBuilder.newClient(config);
	    String url = Utils.getProperty("restURL","https://pschatzmann.ch/edgar/db/companyValues");
	    LOG.info("restURL:"+url);
		WebTarget target = client.target(url);
		Response response = target.request().accept(MediaType.TEXT_PLAIN).buildPost(Entity.entity(q,MediaType.APPLICATION_JSON)).invoke();
		String responseString = response.readEntity(String.class);
		response.close();
		if (response.getStatus()!=200) {
			throw new RuntimeException(responseString);
		}
		String sa[] =responseString.split(System.lineSeparator());
		LOG.info("Number of lines: "+sa.length);
 		return Arrays.asList(sa);
		
	}

	/**
	 * Converts the line to a record and adds it to the records list
	 * @param row
	 * @param inputLine
	 */
	protected void processLine(int row, String inputLine) {
		if (row == 0) {
			this.headers = inputLine.trim().split(";");
		} else {
			String data[] = inputLine.trim().split(";");
			Map<String, String> record = new TreeMap();
			for (int j = 0; j < Math.min(this.headers.length, data.length); j++) {
				record.put(headers[j], data[j]);
			}
			records.add(record);
		}
	}

	/**
	 * Determines the Periods Filter Enum which can be handled by the REST service
	 * @return
	 */
	protected PeriodFilter getPeriodFilter() {
		PeriodFilter result = null;
		try {
			result = PeriodFilter.valueOf(((IRowFilter)this.periods).getRestKey());
		} catch (Exception ex) {
			LOG.warn("The PeriodFilter could not be determined for " + this.periods.getClass().getSimpleName());
		}
		return result;
	}

	@Override
	public ICompanyInfo setFilter(BiFunction<ITable, Integer, Boolean> filter) {
		this.periods = filter;
		return this;
	}

}
