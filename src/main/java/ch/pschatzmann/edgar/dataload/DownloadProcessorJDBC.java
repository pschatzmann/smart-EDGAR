package ch.pschatzmann.edgar.dataload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.rometools.rome.io.FeedException;

import ch.pschatzmann.edgar.base.EdgarCompany;
import ch.pschatzmann.edgar.base.Fact;
import ch.pschatzmann.edgar.base.Fact.DataType;
import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.base.XBRL;
import ch.pschatzmann.edgar.base.errors.DataException;
import ch.pschatzmann.edgar.base.errors.DataLoadException;
import ch.pschatzmann.edgar.base.errors.NoNewDataException;
import ch.pschatzmann.edgar.base.errors.XBRLException;
import ch.pschatzmann.edgar.dataload.background.IProcess;
import ch.pschatzmann.edgar.dataload.background.ProcessorTimerTask;
import ch.pschatzmann.edgar.dataload.online.TradingSymbol;
import ch.pschatzmann.edgar.dataload.rss.DataDownload;
import ch.pschatzmann.edgar.dataload.rss.FeedInfoRecord;
import ch.pschatzmann.edgar.dataload.rss.RSSDataSource;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Main program which downloads loads the information from Edgar into the file
 * system and stores the numeric values in a jdbc database with the following
 * fields:
 * 
 * date*; identifier*; companyName; parameterName*; segment*; numberOfMonths*;
 * label; dateLabel; file; form; contextRef; sicCode; incorpration; location;
 * id; tradingSymbol; value; segmentDimension; sicDescription; unitRef; decimals
 * 
 * @author pschatzmann
 *
 */

public class DownloadProcessorJDBC implements IProcess {
	private static final Logger LOG = Logger.getLogger(DownloadProcessorJDBC.class);
	private TableFactory tableFactory = new TableFactory();
	private List<String> companyFields = Arrays.asList("identifier", "companyName", "sicCode", "sicDescription",
			"tradingSymbol", "incorporation", "location");
	private List<String> valueFields = Arrays.asList("date", "identifier", "parameterName", "label", "numberOfMonths",
			"dateLabel", "file", "form", "segment", "value", "segmentDimension", "unitRef", "decimals");
	private List<String> statesFields = Arrays.asList("id", "state", "country");
	private String destinationFolder = null;
	private DateFormat df = new SimpleDateFormat("yyyyMMdd");
	private boolean history; // also load history rrs

	public DownloadProcessorJDBC() throws IOException, TimeoutException, ClassNotFoundException, SQLException,
			InterruptedException, ParseException {
		destinationFolder = Utils.getProperty("destinationFolder", "/usr/local/bin/smart-edgar/data/");
		LOG.info("destination folder: "+destinationFolder);
		
		openConnection();
		createTables();

	}


	public static void main(String[] args) {
		try {
			DownloadProcessorJDBC main = new DownloadProcessorJDBC();

			String minutes = Utils.getProperty("timer", "");
			if (Utils.isEmpty(minutes)) {
				main.process();
			} else {
				LOG.info("Load will be repeated every " + minutes + " minutes");
				Timer timer = new Timer(true);
				timer.scheduleAtFixedRate(new ProcessorTimerTask(main), 0, 60 * 1000 * Integer.parseInt(minutes));
				Utils.waitForever();
			}
			LOG.info("*** END ***");
			System.exit(0);
		} catch (NoNewDataException ex) {
			LOG.info("*** END ***");
		} catch (Exception ex) {
			LOG.error(ex, ex);
			System.exit(-1);
		}
	}

	public void process() throws Exception {
		try {
			File historyFlag = new File(destinationFolder+"history");
			this.history = "true".equalsIgnoreCase(Utils.getProperty("history", !historyFlag.exists()+""));
			LOG.info("history = " + history);

			String formsRegex = Utils.getProperty("formsRegex", "10-Q.*|10-K.*"); // ".*"; "10-Q.*|10-K.*"
			LOG.info("formsRegex = " + formsRegex);

			Collection<FeedInfoRecord> data = RSSDataSource.createForChanges(history).getData(formsRegex);
			LOG.info("Total number of files: " + data.size());
			data = getUnprocessedRecords(data);
			LOG.info("Number of unprocessed files: " + data.size());

			for (FeedInfoRecord info : data) {
				try {
					this.process(info);
				} catch(Exception ex) {
					LOG.error(ex,ex);
				}
			}
			
			// create history file to prevent a subsequent load of history data
			historyFlag.createNewFile();
			
		} finally {
			this.close();
			LOG.info("The index was updated");
		}
	}

	private Collection<FeedInfoRecord> getUnprocessedRecords(Collection<FeedInfoRecord> input)
			throws ClassNotFoundException, SQLException, DataLoadException {
		Collection<FeedInfoRecord> result = new ArrayList();
		Set<String> processedFiles = new HashSet(tableFactory.getList("select distinct file from values"));
		int count = 0;
		for (FeedInfoRecord rec : input) {
			if (!processedFiles.contains(rec.getFile("").getName())) {
				result.add(rec);
			} else {
				count++;
			}
		}
		LOG.info("Number of already processed files " + count);
		return result;
	}

	protected void openConnection() throws InterruptedException {
		boolean open = false;
		int count = 0;
		while (!open) {
			try {
				tableFactory.openConnection();
				count++;
				open = true;
				if (count > 10) {
					LOG.error("Could not open the connection");
					System.exit(-1);
				}
			} catch (Exception ex) {
				LOG.error(ex);
				Thread.sleep(10000);
			}
		}
	}

	/**
	 * Main processing
	 * 
	 * @param info
	 * @throws XBRLException
	 * @throws NoNewDataException
	 * @throws SQLException
	 */
	public void process(FeedInfoRecord info) throws XBRLException, NoNewDataException, SQLException {
		try {
			DataDownload loader = new DataDownload(info.getUriXbrl());
			File file = info.getFile(destinationFolder);
			if (!loader.isValidDownloadFile(file)) {
				LOG.info("+" + info);
				loader.load(file);
			} else {
				LOG.info("-" + info);
			}

			loadToDatabase(file.toURI().toURL());

		} catch (Exception ex) {
			LOG.error(ex, ex);
			tableFactory.rollback();
		}

	}
	public void loadToDatabase(URL currentURL) throws SAXException, IOException, ParserConfigurationException, SQLException, ParseException, ClassNotFoundException, FeedException, DataException {
		loadToDatabase(currentURL, false);
	}

	public void loadToDatabase(URL currentURL, boolean reload) throws SAXException, IOException, ParserConfigurationException,
			SQLException, ParseException, ClassNotFoundException, FeedException, DataException {
		LOG.info("Processing " + currentURL);
		String fileName = Utils.lastPath(currentURL.toString());
		if (reload || !tableFactory.hasNext("select file from values where file='" + fileName + "' limit 1", false)) {
			XBRL xbrl = new XBRL();
			xbrl.load(currentURL);
			// update company file
			saveCompany(xbrl);
			saveXBRL(xbrl);

		} else {
			LOG.info("File was already laoded");
		}
	}

	
	public void saveXBRL(XBRL xbrl) throws SQLException, ParseException, ClassNotFoundException {
		Collection<Fact> facts = xbrl.find(Type.value);
		if (!facts.isEmpty()) {			
			int count = addValues(facts);
			LOG.info("-> Saving facts for "+xbrl.getFilingInfo().getFileName()+": "+count);
			tableFactory.close("values");
			tableFactory.commit();

		} else {
			LOG.info("No facts available");
		}
	}

	public void saveCompany(XBRL xbrl) throws SQLException, ParseException, ClassNotFoundException, MalformedURLException, IOException, FeedException, DataException {
		String tradingSymbol = xbrl.getCompanyInfo().getTradingSymbol();
		if (Utils.isEmpty(tradingSymbol)) {
			// we determine the ticker symbol from the ownership filings
			((EdgarCompany)xbrl.getCompanyInfo()).setTradingSymbol(new TradingSymbol(xbrl.getCompanyNumber()).getTradingSymbol());
		}
		updateCompanyFile(xbrl);
		saveCompanyInDB(xbrl);
	}

	public void saveCompanyInDB(XBRL xbrl) throws SQLException, ParseException, ClassNotFoundException {
		String id = xbrl.getCompanyNumber();
		if (!tableFactory.hasNext("select identifier from company where identifier = '" + id + "' limit 1",
				false)) {
			addCompanyRecord(xbrl);
		} else {
			updateCompanyRecord(xbrl);
		}
	}

	protected void updateCompanyFile(XBRL xbrl) throws MalformedURLException, IOException  {
		EdgarCompany c = new EdgarCompany();
		c.setCompanyNumber(xbrl.getCompanyNumber());
		c.scrape();
		c.setupTradingSymbol();
		c.saveFile();
	}

	protected int addValues(Collection<Fact> facts) throws SQLException {
		List<String> attributes = new ArrayList(valueFields);
		int count = 0;
		for (Fact f : facts) {
			if (f.getDataType() == DataType.number) {
				try {
					count++;
					tableFactory.addRecord("values", attributes, f.getAttributes(), "valueskey");
				} catch (SQLException ex) {
					LOG.error(ex, ex.getNextException());
					tableFactory.commit();
				} catch (Exception ex) {
					LOG.error(ex);
					tableFactory.commit();
				}
			}
		}
		return count;
	}

	protected void addCompanyRecord(XBRL xbrl) throws SQLException, ParseException, ClassNotFoundException {
		Fact first = xbrl.first((xbrl.find(Type.value)));
		if (first != XBRL.EMPTY) {
			tableFactory.addRecord("company", companyFields, first.getAttributes(), "companykey");
			tableFactory.commit();
		}
	}

	protected void updateCompanyRecord(XBRL xbrl) throws SQLException {
		Fact first = xbrl.first((xbrl.find(Type.value)));
		if (first != XBRL.EMPTY) {
			tableFactory.updateCompany( companyFields, first.getAttributes());
			tableFactory.commit();
		}		
	}
	
	protected void createTables()
			throws SQLException, InterruptedException, IOException, ParseException, ClassNotFoundException {
		if (!tableFactory.hasNext("select * from company limit 1", false)) {
			LOG.info("Creating company");
			createTableCompany();
		}

		if (!tableFactory.hasNext("select * from values limit 1", false)) {
			LOG.info("Creating values");
			createTableValues();
		}

		if (!tableFactory.hasNext("select id from states limit 1", false)) {
			LOG.info("Creating states");
			createTableStates();
		}
		
		tableFactory.commit();
	}

	protected void createTableValues()
			throws SQLException, InterruptedException, IOException, ParseException, ClassNotFoundException {

		tableFactory.createTable("values", valueFields);
		
		String idx = Utils.getProperty("valuesIndex",
				"ALTER TABLE values ADD CONSTRAINT valueskey PRIMARY KEY (date, identifier, parameterName, segment, numberOfMonths, form)");
		tableFactory.addIndex(idx);
		tableFactory.addIndex("create index idx_values1 on values(parametername,segment,segmentdimension,unitref)");
		tableFactory.addIndex("create index idx_values2 on values(file)");
		tableFactory.addIndex("create index idx_values3 on values(identifier,parametername,segment,segmentdimension,unitref)");
		tableFactory.addIndex("create index idx_values4 on values(form,numberofmonths,unitref,segment,segmentdimension)");
	}
	

	protected void createTableCompany() throws SQLException {
		String idx;
		tableFactory.createTable("company", companyFields);

		idx = Utils.getProperty("companyIndex","ALTER TABLE company ADD CONSTRAINT companykey PRIMARY KEY (identifier)");
		tableFactory.addIndex(idx);
		tableFactory.addIndex("create index idx_company1 on company(tradingsymbol)");
		tableFactory.addIndex("create index idx_company2 on company(location)");
		tableFactory.addIndex("create index idx_company3 on company(incorporation)");
	}

	/**
	 * Create and load the states database table
	 * 
	 * @throws InterruptedException
	 * @throws SQLException
	 * @throws IOException
	 * @throws ParseException
	 * @throws ClassNotFoundException
	 */

	protected void createTableStates()
			throws InterruptedException, SQLException, IOException, ParseException, ClassNotFoundException {
		String idx;
		// create table
		tableFactory.createTable("states", statesFields);

		// create index
		idx = Utils.getProperty("statesIndex", "ALTER TABLE states ADD CONSTRAINT stateskey PRIMARY KEY (id)");
		tableFactory.addIndex(idx);

		// add values
		BufferedReader b = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/STATES.csv")));
		String readLine = "";
		Map record = new HashMap();
		while ((readLine = b.readLine()) != null) {
			String[] sa = readLine.split(";");
			record.put("id", sa[0]);
			record.put("state", sa[1]);
			record.put("country", sa[2]);
			LOG.info("adding record "+record);
			tableFactory.addRecord("states", statesFields, record);
			tableFactory.commit();
		}
		
	}
	
	public void setDestinationFolder(String folder) {
		this.destinationFolder = folder;
	}

	public void close() throws XBRLException {
		try {
			tableFactory.close();
		} catch (SQLException e) {
			throw new XBRLException(e);
		}
	}

}
