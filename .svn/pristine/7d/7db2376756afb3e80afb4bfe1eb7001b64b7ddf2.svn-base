package ch.pschatzmann.edgar.dataload.online;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.pschatzmann.edgar.base.XBRL;

/**
 * An individual Filing
 * 
 * @author pschatzmann
 *
 */
public class FilingEntry implements Serializable, Comparable<FilingEntry> {
	private static final Logger LOG = Logger.getLogger(FilingEntry.class);
	private Node node;
	private List<String> linksToDocuments;
	private CompanyInformation ci;
	private Optional<XBRL> xbrl;
	
	public FilingEntry(CompanyInformation ci, Node node){
		this.node = node;
		this.ci = ci;
		LOG.info(this.toString());
	}
	
	protected synchronized String xpath(String xpathStr)  {
		try {
			XPath xpath = ci.getXPathFactory().newXPath();
			XPathExpression expr = xpath.compile(xpathStr);
			return (String)expr.evaluate(node, XPathConstants.STRING);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected synchronized Node xpathNode(String xpathStr)  {
		try {
			XPath xpath = ci.getXPathFactory().newXPath();
			XPathExpression expr = xpath.compile(xpathStr);
			return (Node)expr.evaluate(node, XPathConstants.NODE);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public String getAccessionNumber() {
		return xpath("content/accession-nunber");
	}

	public String getFilingDate() {
		return xpath("content/filing-date");
	}

	public String getUpdated() {
		return xpath("updated");
	}
	
	public String getFilingNumber() {
		return xpath("content/file-number");
	}

	public String getForm() {
		return xpath("content/filing-type");
	}

	public String getFormName() {
		return xpath("content/form-name");
	}

	public String getDescription() {
		return xpath("content/items-desc");
	}
	
	public String getTitle() {
		return xpath("title");
	}

	public String getLink() {
		return xpathNode("link").getAttributes().getNamedItem("href").getTextContent();
	}
	
	public String toString() {
		return this.getAccessionNumber()+":"+this.getForm();
	}
	
	public String getLinkBase() {
		return this.ci.getLinkBase();
	}
	
	synchronized public Stream<String> getLinksToDocuments()  {
		try {
			if (linksToDocuments==null) {
				linksToDocuments = new ArrayList();
				StringBuffer sb = new StringBuffer();
				sb.append(getLinkBase());
				sb.append("/Archives/edgar/data/");
				sb.append(this.ci.getCompanyNumber());
				sb.append("/");
				sb.append(this.getAccessionNumber().replaceAll("-", ""));
				sb.append("/index.xml");
				LOG.info(sb.toString());
				
				Document doc = ci.getDocumentBuilder().parse(sb.toString());
				XPath xpath = ci.getXPathFactory().newXPath();
				XPathExpression expr = xpath.compile("//href");
				NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				for (int j=0;j<list.getLength();j++) {
					linksToDocuments.add(list.item(j).getTextContent());
				}
			}
			return linksToDocuments.stream();
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public Stream<String> getLinksToDocuments(String regex)  {
		return this.getLinksToDocuments().filter(f -> f.matches(regex));
	}
	
	public Optional<String> getLinkToXbrl() {
		return getLinksToDocuments(".*.zip").findFirst();
	}
	
	public XBRL loadXBRL(XBRL xbrl) {
		if (this.getForm().matches("10-K.*|10-Q.*")) {
			Optional<String> l = getLinkToXbrl();
			if (l.isPresent()) {
				String url = this.getLinkBase()+l.get();
				LOG.info(url);
				try {
					xbrl.load(new URL(url));
					xbrl.getFilingInfo().setForm(this.getForm());
				} catch(Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		return xbrl;
	}
	
	public synchronized Optional<XBRL> getXBRL() {
		if (this.xbrl==null) {
			XBRL xbrl = new XBRL();
			xbrl.setCompanyInfo(this.ci);
			this.loadXBRL(xbrl);
			this.xbrl = xbrl.isEmpty() ? Optional.empty():Optional.of(xbrl);
		}
		return this.xbrl;
	}

	@Override
	public int compareTo(FilingEntry o) {
		int result = this.getFilingDate().compareTo(o.getFilingDate());
		if (result==0){
			result = this.getForm().compareTo(o.getForm());
		}
		return result;
	}

	
}
