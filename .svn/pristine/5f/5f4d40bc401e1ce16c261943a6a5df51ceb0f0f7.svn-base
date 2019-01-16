package ch.pschatzmann.edgar.dataload.rss;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.rometools.rome.feed.synd.SyndEntry;

import ch.pschatzmann.edgar.base.XBRL;
import ch.pschatzmann.edgar.base.errors.DataLoadException;
import ch.pschatzmann.edgar.base.errors.XBRLException;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Individual Record provided from the RRS feed which is used to load the data from EDGAR and
 * create the related file (with the correct name)
 * 
 * @author pschatzmann
 *
 */

public class FeedInfoRecord implements Comparable<FeedInfoRecord> {
	private static final Logger LOG = Logger.getLogger(FeedInfoRecord.class);
	private static DateFormat df = new SimpleDateFormat("yyyyMMdd");
	private Date publishedDate;
	private String form;
	private String title;
	private String cikNumber;
	private String uriXbrl;
	private String urlHttp;
	private String period;
	protected static String DEL = ";";

	enum UrlType {
		htm, xbrl
	};

	private String destinationFile = Utils.getProperty("destinationFolder", "/usr/local/bin/SmartEdgar/data/");

	/**
	 * Default Constructor
	 */
	public FeedInfoRecord() {
		DEL = Utils.getProperty("delimiter", DEL);
	}

	/**
	 * Constructor based on the SyndEntry from the RRS feed
	 * 
	 * @param entry
	 */
	public FeedInfoRecord(SyndEntry entry) {
		this();
		form = entry.getDescription().getValue();
		title = entry.getTitle().replaceAll(DEL, "");
		cikNumber = getCIKNumber(entry);
		publishedDate = entry.getPublishedDate();
		urlHttp = entry.getLink().replace("http://", "https://");
		uriXbrl = entry.getUri().replace("http://", "https://");
		if (!uriXbrl.startsWith("http")) {
			// we try to deduce the xbrl url
			uriXbrl = urlHttp.replace("-index.htm", "-xbrl.zip");
			form = title.substring(0, title.indexOf(" ")).trim();
		}
		
	}

	/**
	 * Constructor using a CSV string to pass in the attribute values
	 * 
	 * @param str
	 * @throws ParseException
	 */
	public FeedInfoRecord(String str) throws ParseException {
		String sa[] = str.split(DEL);
		setForm(sa[0]);
		setCIKNumber(sa[1]);
		setPeriod(sa[2]);
		setPublishedDate(df.parse(sa[3]));
		setTitle(sa[4]);
		setUriXbrl(sa[5]);
		setUrlHttp(sa[6]);
	}

	/**
	 * Determines the filer number
	 * 
	 * @param entry
	 * @return
	 */
	private String getCIKNumber(SyndEntry entry) {
		String number = entry.getLink().replace("http://www.sec.gov/Archives/edgar/data/", "");
		number = number.substring(0, number.indexOf("/"));
		return number;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCIKNumber() {
		return cikNumber;
	}

	public void setCIKNumber(String filerNumber) {
		this.cikNumber = filerNumber;
	}

	public String getUriXbrl() {
		return uriXbrl;
	}

	public void setUriXbrl(String uriXbrl) {
		this.uriXbrl = uriXbrl;
	}

	public String getUrlHttp() {
		return urlHttp;
	}

	public void setUrlHttp(String urlHttp) {
		this.urlHttp = urlHttp;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	/**
	 * Creates a CSV representation of the attribute values
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(form);
		sb.append(DEL);
		sb.append(cikNumber);
		sb.append(DEL);
		sb.append(period);
		sb.append(DEL);
		sb.append(df.format(publishedDate));
		sb.append(DEL);
		sb.append(title);
		sb.append(DEL);
		sb.append(uriXbrl);
		sb.append(DEL);
		sb.append(urlHttp);
		return sb.toString();
	}

	/**
	 * Determines the File with which the data is supposed to be available
	 * 
	 * @param destinationFolder
	 * @return
	 */
	public File getFile(String destinationFolder) {
		String fileName = getFileName(destinationFolder, this.getCIKNumber(), this.getForm(), this.getPublishedDate());
		File result = new File(fileName);
		return result;
	}

	/**
	 * Determines the file name
	 * 
	 * @param destinationFolder
	 * @param filerNumber
	 * @param form
	 * @param date
	 * @return
	 */
	private String getFileName(String destinationFolder, String filerNumber, String form, Date date) {
		StringBuffer sb = new StringBuffer(destinationFolder);
		if (!destinationFolder.isEmpty()) {
			sb.append(File.separatorChar);
		}
		sb.append(filerNumber);
		sb.append(File.separatorChar);
		sb.append(filerNumber);
		sb.append("-");
		sb.append(form.replaceAll("/", "-"));
		if (date!=null) {
			sb.append("-");
			sb.append(df.format(date));
		}
		sb.append(".zip");
		return sb.toString();
	}

	/**
	 * Returns a key which is used to check if the data has been loaded
	 * 
	 * @return
	 */
	public String getKey() {
		StringBuffer sb = new StringBuffer();
		sb.append(cikNumber);
		sb.append(DEL);
		sb.append(form);
		sb.append(DEL);
		sb.append(period);
		return sb.toString();
	}

	public void download() throws XBRLException, DataLoadException {
		File file = this.getFile(destinationFile);
		DataDownload loader = new DataDownload(this.getUriXbrl());

		if (!loader.isValidDownloadFile(file)) {
			LOG.info("+" + this);
			file.delete();
			loader.load(file);
		} else {
			LOG.info("-" + this);
		}
	}

	/**
	 * Loads the XBRL file from the file system (if it exists) - otherwise directly
	 * from the Edgar URL
	 * 
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws MalformedURLException 
	 */
	public XBRL getXBRL() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		XBRL xbrl = new XBRL();
		return loadXBRL(xbrl);
	}

	/**
	 * Loads the data into an existing XBRL object
	 * @param xbrl
	 * @return
	 */
	
	public XBRL loadXBRL(XBRL xbrl) {
		try {
			File file = this.getFile(this.destinationFile);
			if (file.exists())  xbrl.load(file); else xbrl.load(new URL(this.getUriXbrl()));
		} catch(Exception ex) {
			LOG.error(ex,ex);;
		}
		return xbrl;
	}


	@Override
	public int compareTo(FeedInfoRecord o) {
		return this.getFile("").compareTo(o.getFile(""));
	}
;
}
