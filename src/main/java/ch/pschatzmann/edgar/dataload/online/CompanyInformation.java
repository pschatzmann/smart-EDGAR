package ch.pschatzmann.edgar.dataload.online;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.pschatzmann.edgar.base.ICompany;
import ch.pschatzmann.edgar.base.XBRL;
import ch.pschatzmann.edgar.utils.Utils;


/**
 * CompanyInformation is using the https://www.sec.gov/cgi-bin/browse-edgar Online information to retrieve the
 * company information and filings for the company.
 * 
 * @author pschatzmann
 *
 */

public class CompanyInformation implements ICompany {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(CompanyInformation.class);
	private List<FilingEntry> entries;
	private XPathFactory xPathfactory;
	DocumentBuilderFactory factory;
	private String tickerOrCik;	
	private String forms[];
	private TradingSymbol tradingSymbol;
	private String stateOfIncorporation;
	private String stateLocation;
	private String companyName;
	private String sicCode;
	private String sicDescription;
	private String companyNumber;
	private String fiscalYearEnd;

	/**
	 * Constructor
	 * @param tickerOrCik
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public CompanyInformation(String tickerOrCik, String...forms)
			throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		this.tickerOrCik = tickerOrCik;
		this.forms = forms;
		setup();
	}
	
	public CompanyInformation(String tickerOrCik)
			throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		this.tickerOrCik = tickerOrCik;
		this.forms = new String[] {""};
		setup();
	}


	private synchronized void setup() {
		try {
			if (Utils.isEmpty(this.tickerOrCik)){
				throw new RuntimeException("The tickerOrCik must not be empty");
			}
			
			if (companyNumber==null) {
				factory = DocumentBuilderFactory.newInstance();
		
				String url = getUrl(tickerOrCik,"----");
				LOG.info(url);
				Document doc = this.getDocumentBuilder().parse(url);		

				XPathExpression exprCompany = this.getXPathFactory().newXPath().compile("/feed/company-info");
				Node companyInfo = (Node) exprCompany.evaluate(doc, XPathConstants.NODE);	

				companyNumber = xpath("cik",companyInfo);				
				stateOfIncorporation = xpath("state-of-incorporation",companyInfo);
				stateLocation = xpath("state-location",companyInfo);
				companyName =xpath("conformed-name",companyInfo);
				sicCode = xpath("assigned-sic",companyInfo);
				sicDescription =xpath("assigned-sic-desc",companyInfo);
				fiscalYearEnd = xpath("fiscal-year-end",companyInfo);	
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	protected void setupList() {
		try {
			if (entries==null) {
				entries = new ArrayList();
				XPathExpression expr = this.getXPathFactory().newXPath().compile("/feed/entry");
				for (String form : forms) {
					String nextUrl = this.getUrl(this.tickerOrCik, form);
					DocumentBuilder builder = this.getDocumentBuilder();
					LOG.info(nextUrl);
					Document doc = builder.parse(nextUrl);		
					
					// process subsequent pages
					while(nextUrl != null) {
						LOG.info(nextUrl);
						doc = builder.parse(nextUrl);
						processEntries(expr, doc);					
						nextUrl = getNextUrl(doc);
					}
				}
			} 	
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private NodeList processEntries(XPathExpression expr, Document doc) throws XPathExpressionException {
		NodeList currentEntries = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int j=0; j<currentEntries.getLength();j++) {
			entries.add(new FilingEntry(this, currentEntries.item(j)));
		}
		return currentEntries;
	}


	protected String getUrl(String tickerOrCik, String form) {
		String url = "https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=%companyNr&type=%form&dateb=&owner=include&count=100&output=atom";
		url = url.replace("%companyNr", tickerOrCik);
		url = url.replace("%form", form);
		return url;
	}

	protected String getNextUrl(Document doc) throws XPathExpressionException {
		String url;
		XPathExpression nextExpr = xPathfactory.newXPath().compile("/feed/link[@rel='next']");
		Node next = (Node) nextExpr.evaluate(doc, XPathConstants.NODE);
		if (next!=null) {
			url = next.getAttributes().getNamedItem("href").getTextContent();
			url = url.replace("http://", "https://");
		} else {
			url = null;
		}
		return url;
	}
	

	protected synchronized String xpath(String xpathStr, Node companyInfo)  {
		try {
			XPath xpath = this.getXPathFactory().newXPath();
			XPathExpression expr = xpath.compile(xpathStr);
			return (String)expr.evaluate(companyInfo, XPathConstants.STRING);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public String getIncorporationState() {
		return this.stateOfIncorporation;
	}
	
	public String getLocationState() {
		return this.stateLocation;
	}

	public String getCompanyName() {
		return this.companyName;
	}

	public String getSICCode() {
		return this.sicCode;
	}

	public String getSICDescription() {
		return this.sicDescription;
	}

	public String getCompanyNumber() {
		return this.companyNumber;
	}
	
	public String getFiscalYearEnd() {
		return this.fiscalYearEnd; //xpath("fiscal-year-end");
	}
	
	public String getLinkBase() {
		return "https://www.sec.gov";
	}
	
	public Stream<FilingEntry> stream(String formRegex) {
		return entries().stream().filter(p -> p.getForm().matches(formRegex));
	}

	public List<FilingEntry> entries(String formRegex) {
		return stream(formRegex).collect(Collectors.toList());
	}

	public List<FilingEntry> entries() {
		setup();
		setupList();
		return entries;
	}
	
	public Optional<FilingEntry> firstEntry(String regex) {
		return stream(regex).findFirst();
	}
	
	protected synchronized XPathFactory getXPathFactory() {
		xPathfactory = XPathFactory.newInstance();
		return this.xPathfactory;
	}
	
	protected DocumentBuilder getDocumentBuilder() {
		try {
			return factory.newDocumentBuilder();
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public XBRL getXBRL(String regex) {
		XBRL xbrl = new XBRL();
		this.entries(regex).forEach(e -> e.loadXBRL(xbrl));
		return xbrl;
	}
	
	public XBRL getXBRL() {
		XBRL xbrl = new XBRL();
		xbrl.setCompanyInfo(this);
		this.entries().forEach(e -> e.loadXBRL(xbrl));
		return xbrl;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getCompanyName());
		sb.append(" (");
		sb.append(this.getCompanyNumber());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String getTradingSymbol() {
		if (this.tradingSymbol==null) {
			this.tradingSymbol = new TradingSymbol(this.getCompanyNumber());
		}
		return this.tradingSymbol.getTradingSymbol();
	}

}
