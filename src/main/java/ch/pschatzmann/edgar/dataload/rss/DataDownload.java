package ch.pschatzmann.edgar.dataload.rss;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.pschatzmann.edgar.base.errors.DataLoadException;
import ch.pschatzmann.edgar.base.errors.XBRLException;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Logic to download the data from Edgar
 * 
 * @author pschatzmann
 *
 */
public class DataDownload {
	private static final Logger LOG = Logger.getLogger(DataDownload.class);
	private String uriXbrl;

	public DataDownload(String url) {
		this.uriXbrl = url;
	}

	/**
	 * Checks if the Download File is available and a valid zip file
	 * 
	 * @param zip
	 * @return
	 */
	public boolean isValidDownloadFile(File zip) {
		boolean result = false;
		if (zip.exists()) {
			ZipFile zf;
			try {
				zf = new ZipFile(zip);
				result = true;
			} catch (Exception ex) {
			}
		}
		return result;
	}

	/**
	 * Loads the data from Edgar to the indicated output file
	 * 
	 * @param outputFile
	 * @throws XBRLException
	 * @throws DataLoadException
	 */
	public void load(File outputFile) throws XBRLException, DataLoadException {
		if (outputFile.exists()) {
			outputFile.delete();
		}

		if (this.uriXbrl.endsWith(".zip")) {
			loadZip(outputFile);
		} else if (this.uriXbrl.endsWith(".htm") || this.uriXbrl.endsWith(".html")) {
			loadHttp(outputFile);
		} else {
			LOG.error("Unsupported download format " + this.getUriXbrl());
		}
	}

	/**
	 * Loads the data from the indicated uri to the indicated output folder
	 * 
	 * @param outputFolder
	 * @throws XBRLException
	 */
	public void loadZip(File outputFolder) throws XBRLException {
		File parent = outputFolder.getParentFile();
		if (parent==null || parent.exists() || parent.mkdirs()) {
			try {
				URL website = new URL(this.uriXbrl.replace("http://", "https://"));
				InputStream in = website.openStream();
				Files.copy(in, outputFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
				in.close();
				LOG.info("File created :" + outputFolder.getAbsolutePath());
			} catch (Exception ex) {
				throw new XBRLException(ex);
			}

		} else {
			throw new XBRLException("The path could not be created: " + outputFolder.getParentFile());
		}
	}

	/**
	 * Creates a zip file if the link is a htm file
	 * 
	 * @param ouputZipFile
	 * @throws DataLoadException
	 */
	private void loadHttp(File ouputZipFile) throws DataLoadException {
		try {
			File parent = ouputZipFile.getParentFile();
			if (parent==null || parent.exists() || parent.mkdirs()) {
				String urlStr = this.getUriXbrl();
				urlStr = urlStr.replace("http://", "https://");
				String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
				ouputZipFile.createNewFile();
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(ouputZipFile));
				// get all files which are in /FilingSummary/InputFiles/File
				for (String xmlFileName : getDocuments(urlStr,fileName)) {
					String contentURL = urlStr.replaceAll(fileName, xmlFileName);
					LOG.info("adding to zip " + contentURL);
					URL source = new URL(contentURL);
					ZipEntry e = new ZipEntry(xmlFileName);
					out.putNextEntry(e);
					byte[] data = Utils.urlToByteArray(source).toByteArray();
					out.write(data, 0, data.length);
					out.closeEntry();
				}
				out.close();
				LOG.info("File created :" + ouputZipFile.getAbsolutePath());
			}
		} catch (Exception ex) {
			throw new DataLoadException(ex);
		}
	}

	private String getUriXbrl() {
		return this.uriXbrl;
	}

	private List<String> getDocuments(String urlStr, String fileName) throws IOException, ParserConfigurationException, SAXException {
		URL index = new URL(urlStr.replaceAll(fileName, "FilingSummary.xml"));
		List<String> result = new ArrayList();
		if (exists(index)) {
			// parse xml
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(index.openStream());

			NodeList nList = doc.getElementsByTagName("File");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node e = nList.item(temp);
				String text = e.getTextContent();
				result.add(text);
			}
		} else {
			// parse html
			org.jsoup.nodes.Document doc = Jsoup.connect(urlStr).get();
			Elements links = doc.select("a[href]"); 
			Iterator<Element> it = links.iterator();
			while(it.hasNext()) {
				Element el = it.next();
				String txt = el.text();
				File f = new File(txt);
				String name = f.getName();
				if (name.endsWith("xml") || name.endsWith("xsd")) {
					result.add(name);
				}
			}			
		}

		return result;
	}
	
	private boolean exists(URL url) {
		boolean result = false;
		HttpURLConnection huc;
		try {
			huc = ( HttpURLConnection )  url.openConnection ();
			huc.setInstanceFollowRedirects(true);
			huc.setRequestMethod ("GET"); 
			huc.connect () ; 
			result = huc.getResponseCode() == 200 ;

		} catch (IOException e) {
			LOG.warn(e,e);
		} 
		return result;
	}

}
