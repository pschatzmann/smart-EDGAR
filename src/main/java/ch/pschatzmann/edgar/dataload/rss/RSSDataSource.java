package ch.pschatzmann.edgar.dataload.rss;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import ch.pschatzmann.edgar.base.XBRL;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Access EDGAR RSS information in order to identify the available data
 * 
 * @author pschatzmann
 *
 */

public class RSSDataSource {
	private static final Logger LOG = Logger.getLogger(RSSDataSource.class);
	private String rss;
	private String monthlyRSS;

	/**
	 * Default Constructor
	 */
	private RSSDataSource(){}
	
	
	/**
	 * Determines all relevant Edgar RSS url sources
	 * 
	 * @return
	 */
	protected Collection<String> getRss() {
		String minPeriod = Utils.getProperty("minPeriod", "2005-04");

		Collection<String> result = new ArrayList();
		result.add(rss);

		if (monthlyRSS!=null) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));

			int year = 0;
			while (true) {
				try {
					year = cal.get(Calendar.YEAR);
					int month = cal.get(Calendar.MONTH);
					String period = year + "-" + String.format("%02d", month + 1);
					// we stop the processing if the date is before april 2005 or
					// the indicated start date
					if (period.compareTo(minPeriod) < 0 || period.compareTo("2005-04") < 0) {
						break;
					}
					LOG.info("period " + period);
					rss = monthlyRSS;
					result.add(rss.replace("%1", period));
					cal.add(Calendar.MONTH, -1);
				} catch (Exception ex) {
					LOG.error(ex, ex);
				}
			}
		}
		LOG.debug(result);
		return result;
	}

	/**
	 * Provides the collection of FeedInfoRecord objects which can be downloaded
	 * from Edgar to the local file system.
	 * 
	 * @param formRegex
	 * @return
	 * @throws IllegalArgumentException
	 * @throws MalformedURLException
	 * @throws FeedException
	 * @throws IOException
	 */
	public Collection<FeedInfoRecord> getData(String formRegex)
			throws IllegalArgumentException, MalformedURLException, FeedException, IOException {
		Collection<FeedInfoRecord> result = new TreeSet();

		for (String feedUrl : this.getRss()) {
			SyndFeedInput input = new SyndFeedInput();
			LOG.info("Reading data from "+feedUrl);
			SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));

			for (SyndEntry entry : feed.getEntries()) {
				FeedInfoRecord info = new FeedInfoRecord(entry);
				String form = info.getForm();
				if (form.matches(formRegex)) {
					result.add(info);
				} else {
					LOG.trace("Igored because '" + form + "' does not match with '" + formRegex + "'");
				}
			}
		}
		return result;
	}
	
	/**
	 * Loads the selection into a XBRL object
	 * @param regex
	 * @return
	 * @throws IOException 
	 * @throws FeedException 
	 * @throws MalformedURLException 
	 * @throws IllegalArgumentException 
	 */
	public XBRL getXBRL(String regex) throws IllegalArgumentException, MalformedURLException, FeedException, IOException {
		XBRL xbrl = new XBRL();
		this.getData(regex).forEach(fir -> fir.loadXBRL(xbrl));
		return xbrl;
	}
	
	
	public String getRssUrl() {
		return rss;
	}

	public void setRssUrl(String rss) {
		this.rss = rss;
	}

	public String getMonthlyRSSUrl() {
		return monthlyRSS;
	}

	public void setMonthlyRSSUrl(String monthlyRSS) {
		this.monthlyRSS = monthlyRSS;
	}

	/**
	 * Provides the latest changes (for all companies)
	 * @param history
	 * @return
	 */
	public static RSSDataSource createForChanges(boolean history) {
		RSSDataSource result = new RSSDataSource();
		result.setRssUrl(Utils.getProperty("rssSource", "https://www.sec.gov/Archives/edgar/xbrlrss.all.xml"));
		if (history) {
			result.setMonthlyRSSUrl(Utils.getProperty("monthlyRSS", "https://www.sec.gov/Archives/edgar/monthly/xbrlrss-%1.xml"));
		}
		return result;
	}
	
	/**
	 * Provides the filings from the indicated URL
	 * @param url
	 * @return
	 */
	public static RSSDataSource createForUrl(String url) {
		RSSDataSource result = new RSSDataSource();
		result.setRssUrl(url);
		return result;
	}

	/**
	 * Provides the filings for the indicated company
	 * @param companyNumber
	 * @return
	 */
	public static RSSDataSource createForCompanyNumber(String companyNumber) {
		RSSDataSource result = new RSSDataSource();
		String url = "https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=%cik&type=10-&dateb=&owner=exclude&start=0&count=100&output=atom";
		url = url.replaceAll("%cik", companyNumber);
		result.setRssUrl(url);
		return result;
	}
	
}
