package ch.pschatzmann.edgar.base;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import ch.pschatzmann.common.table.ITableEx;
import ch.pschatzmann.common.table.Value;
import ch.pschatzmann.edgar.base.Fact.Attribute;
import ch.pschatzmann.edgar.base.Fact.DataType;
import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.parsing.DefaultValueFormatter;
import ch.pschatzmann.edgar.parsing.HtmlToTextFormatter;
import ch.pschatzmann.edgar.parsing.IValueFormatter;
import ch.pschatzmann.edgar.parsing.RemoveValueFormatter;
import ch.pschatzmann.edgar.parsing.SaxHtmlDocumentHandler;
import ch.pschatzmann.edgar.parsing.SaxXmlDocumentHandler;
import ch.pschatzmann.edgar.table.ValueTable;
import ch.pschatzmann.edgar.utils.Utils;
import ch.pschatzmann.edgar.utils.WontCloseBufferedInputStream;

/**
 * XBRL Parser. All XBRL information is loaded from a file or URL into a hierarchical List of Facts
 * where the root represents the top level of each "file" For each fact row we
 * store the attribute values in a key-value map.
 * 
 * The name of the XML tag is stored in the type field. The exception are the
 * parameter values of the value xml. We store the parameter value in the
 * "value" attribute and we name the type field as "value" as well, so that we
 * can easily filter for all parameter records.
 * 
 * All attribute values and the type field are indexed so that we can perform
 * quick searches.
 * 
 * @author pschatzmann
 *
 */

public class XBRL implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum SegmentScope {
		WithoutSegments, WithSegments, All
	}

	private static final Logger LOG = Logger.getLogger(XBRL.class);
	public static Fact EMPTY = new Fact(null, Type.ROOT, 0, 0);
	public static List EMPTYList = new ArrayList();
	private IndexAPI index = new IndexAPI();
	private Fact root = new Fact(this, Type.ROOT, 0, 0);
	private Set<URL> loadedURLs = new HashSet();
	private boolean isSchemaRef;
	private boolean isLinkbaseRef;
	private boolean isImport;
	private List<String> valueAttributes = null;
	private String companyNumber = "";
	private LabelAPI labelAPI = null;
	private PresentationAPI presentationAPI = null;
	private ICompany companyInfo;
	private boolean setupValueAttributes = true;
	private boolean extendedCompanyInformation = true;
	private boolean postProcessingDone = false;
	private int maxFieldSize = 1000000;
	private Map<DataType, IValueFormatter> formatters = new HashMap();
	private EdgarFiling lastFilingInfo = null;

	/**
	 * Default constructor
	 */
	public XBRL() {
		this.isSchemaRef = false;
		this.isLinkbaseRef = false;
		this.isImport = false;
	}

	/**
	 * Basic class which represents all XBRL data and provides the basic access to
	 * the data. It can be loaded from an url or a file.
	 * 
	 * @param isSchemaRef
	 * @param isLinkbaseRef
	 * @param isImport
	 */

	public XBRL(boolean isSchemaRef, boolean isLinkbaseRef, boolean isImport) {
		this();
		this.isSchemaRef = isSchemaRef;
		this.isLinkbaseRef = isLinkbaseRef;
		this.isImport = isImport;
	}

	/**
	 * Access to the LabelAPI
	 * 
	 * @return
	 */
	public LabelAPI getLabelAPI() {
		if (this.labelAPI == null) {
			this.labelAPI = new LabelAPI(this);
		}
		return this.labelAPI;
	}

	/**
	 * Access to PresentationAPI
	 * 
	 * @return
	 */
	public PresentationAPI getPresentationAPI() {
		if (this.presentationAPI == null) {
			this.presentationAPI = new PresentationAPI(this);
		}
		return this.presentationAPI;
	}

	/**
	 * Provides access to the index
	 * 
	 * @return
	 */
	public IndexAPI getIndex() {
		return index;
	}

	/**
	 * Loads a file or files in a directory
	 * 
	 * @param file
	 */
	public void load(File file)  {
		try {
			setImportFileName(file.toString());
			if (file.isDirectory()) {
				Collection<Exception> errors = new ArrayList();
				Arrays.asList(file.listFiles()).stream().forEach(f -> tryLoad(f, errors));
				if (!errors.isEmpty()) {
					LOG.error(errors);
				}
				this.postProcessing();
			} else {
				// just load the file
				load(file.toURI().toURL());
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Try to load a file. Does not throw any exceptions
	 * 
	 * @param f
	 * @param errors
	 */
	public void tryLoad(File f, Collection<Exception> errors) {
		try {
			LOG.info("Loading " + f);
			load(f);
		} catch (Exception ex) {
			if (errors != null)
				errors.add(ex);
		}
	}

	/**
	 * Loads the data from the URL
	 * 
	 * @param url
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public boolean load(URL url) throws SAXException, IOException, ParserConfigurationException {
		boolean result = false;
		setImportFileName(url.toString());
		if (url.getPath().endsWith(".zip")) {
			result = loadZip(url);
		} else {
			if (url.toExternalForm().endsWith(".htm")) {
				LOG.warn("html files are not supported: " + url.toExternalForm());
			} else {
				result = loadFile(url);
			}
		}
		resolve(url);
		postProcessing();
		return result;
	}

	/**
	 * We support the import of multiple files. Here we allocate a new FilingInfo
	 * for each filing
	 * 
	 * @param string
	 */
	protected void setImportFileName(String string) {
		String fileName = getImportFileName(string);
		if (this.lastFilingInfo == null || !fileName.equals(this.lastFilingInfo.getFileName())) {
			this.lastFilingInfo = new EdgarFiling(fileName);
		}

	}

	/**
	 * Returns the import file name for the indicated path
	 * 
	 * @param fileWithPath
	 * @return
	 */
	public String getImportFileName(String fileWithPath) {
		int start = fileWithPath.lastIndexOf("/");
		return fileWithPath.substring(start + 1);
	}

	
	protected void resolve(URL url) throws SAXException, IOException, ParserConfigurationException {
		while (resolveReferences(url)) {
			LOG.info("References were loaded");
		}
	}

	/**
	 * After the import we define the form and company number
	 */
	protected void postProcessing() {
		// Setup Company Number
		if (Utils.isEmpty(this.getCompanyNumber())) {
			Iterator<Fact> it = this.find("EntityCentralIndexKey", Arrays.asList(Type.value)).iterator();
			while (it.hasNext() && Utils.isEmpty(this.getCompanyNumber())) {
				String companyNr = ((FactValue) it.next()).getValue();
				this.setCompanyNumber(companyNr);
				LOG.info("Setting Company Number from EntityCentralIndexKey: "+companyNr);				
			}
			if (Utils.isEmpty(this.getCompanyNumber())) {
				int start = this.getFilingInfo().getFileName().indexOf("-");
				if (start>0) {
					this.setCompanyNumber(this.getFilingInfo().getFileName().substring(0,start));
					LOG.info("Setting Company Number from Filename: "+this.getCompanyNumber());
				}
			}
		}

		// Determine document type
		// Last resort to set the form name. 1.st We try to get the form name from the
		// file name.
		// 2nd. we try to set it in the parser.
		if (Utils.isEmpty(this.getFilingInfo().getForm())) {
			Iterator<Fact> it = this.find("DocumentType", Arrays.asList(Type.value)).iterator();
			if (it.hasNext()) {
				String form = (((FactValue) it.next()).getValue());
				this.getFilingInfo().setForm(form);
			}
		}
		// setup company info
		this.getCompanyInfo();

		this.setPostProcessingDone(true);
	}

	protected boolean loadFile(URL url) throws SAXException, IOException, ParserConfigurationException {
		boolean result = load1(url, true);
		return result;
	}

	protected boolean loadZip(URL url) throws IOException, ParserConfigurationException, SAXException {
		boolean result = false;
		if (!loadedURLs.contains(url)) {
			ZipInputStream zis = new ZipInputStream(url.openStream());
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String name = ze.getName();
				LOG.info(" -> " + name);
				if (!name.endsWith("htm")) {
					load1(zis, isFactFile(name), true);
				} else {
					// we use a try catch block to handle the case where we have xmls and
					// additional html exhibits
					try {
						loadiXbrl(zis, name);
					} catch (Exception ex) {
						LOG.error("Could not load " + name + " " + ex);
					}
				}
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();
			loadedURLs.add(url);
			result = true;
		}
		return result;
	}

	protected void loadiXbrl(ZipInputStream zis, String name)
			throws IOException, ParserConfigurationException, SAXException {
		// make the input stream re-readable and get the first line
		BufferedInputStream is = new BufferedInputStream(zis);
		is.mark(10000000);
		BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String firstLine = r.readLine();
		// We process only xml documents
		if (firstLine.toLowerCase().startsWith("<?xml")) {
			is.reset();
			load1(is, isFactFile(name), false);
		} else {
			LOG.info("    " + name + " -> ignored because it is not a avalid iXBRL document: " + firstLine);
		}
	}

	private boolean containsXML(ZipInputStream zis) throws IOException {
		ZipEntry ze = zis.getNextEntry();
		while (ze != null) {
			String name = ze.getName();
			if (name.endsWith(".xml")) {
				return true;
			}
			ze = zis.getNextEntry();
		}
		return false;
	}

	public String getCompanyNumber() {
		return companyNumber;
	}

	public void setCompanyNumber(String cik) {
		this.companyNumber = cik;
	}

	protected boolean isFactFile(String name) {
		return !name.contains("_") && name.endsWith(".xml") || name.endsWith(".htm");
	}

	protected boolean load1(URL url, boolean fact) throws ParserConfigurationException, SAXNotRecognizedException,
			SAXNotSupportedException, SAXException, IOException {
		boolean result = false;
		if (url != null && !loadedURLs.contains(url)) {
			LOG.info("loading " + url.toExternalForm() + "...");
			InputStream is = url.openStream();
			load1(is, fact, url.toString().endsWith("xml"));
			is.close();
			loadedURLs.add(url);
			result = true;
		}
		return result;
	}

	protected void load1(InputStream is, boolean fact, boolean xml)
			throws ParserConfigurationException, IOException, SAXException {
		InputStream wis = new WontCloseBufferedInputStream(is);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		factory.setValidating(false);
		factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
		factory.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
		SAXParser saxParser = factory.newSAXParser();
		DefaultHandler h = xml ? new SaxXmlDocumentHandler(this, root, fact)
				: new SaxHtmlDocumentHandler(this, root, fact);
		saxParser.parse(wis, h);
	}

	protected boolean resolveReferences(URL url) throws SAXException, IOException, ParserConfigurationException {
		boolean result = false;
		result = resolveSchemaReferences(url, result);
		result = resolveLinkBaseReferences(url, result);
		result = resolveImports(result);
		return result;
	}

	protected boolean resolveImports(boolean result) throws ParserConfigurationException, SAXNotRecognizedException,
			SAXNotSupportedException, SAXException, IOException, MalformedURLException {
		if (isImport) {
			for (Fact fact : find(Type.importX)) {
				String ref = fact.getAttribute("schemaLocation");
				if (ref.startsWith("http")) {
					if (load1(new URL(ref), false)) {
						result = true;
					}
				}
			}
		}
		return result;
	}

	protected boolean resolveLinkBaseReferences(URL url, boolean result) throws ParserConfigurationException,
			SAXNotRecognizedException, SAXNotSupportedException, SAXException, IOException, MalformedURLException {
		if (isLinkbaseRef) {
			for (Fact fact : find(Type.linkbaseRef)) {
				String ref = fact.getAttribute("href");
				if (load1(toURL(url, ref), false)) {
					result = true;
				}
			}
		}
		return result;
	}

	protected boolean resolveSchemaReferences(URL url, boolean result) throws ParserConfigurationException,
			SAXNotRecognizedException, SAXNotSupportedException, SAXException, IOException, MalformedURLException {
		if (isSchemaRef) {
			for (Fact fact : find(Type.schemaRef)) {
				String ref = fact.getAttribute("href");
				if (load1(toURL(url, ref), false)) {
					result = true;
				}
			}
		}
		return result;
	}

	protected URL toURL(URL url, String ref) throws MalformedURLException {
		String str = url.toExternalForm();
		int lastIndex = str.lastIndexOf("/");
		str = str.substring(0, lastIndex + 1);

		return ref == null ? null : new URL(str + ref);
	}

	/**
	 * Finds all matching child facts
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	public Collection<Fact> find(String value, Type type) {
		return find(value, Arrays.asList(type));
	}

	/**
	 * Find w/o setting up the value attributes
	 * 
	 * @param value
	 * @param types
	 * @return
	 */
	public Collection<Fact> find(String value, Collection<Type> types) {
		Collection<Fact> result = index.find(value, types);
		return result == null ? EMPTYList : result;
	}

	/**
	 * Find multiple values
	 * 
	 * @param values
	 * @param types
	 * @return
	 */
	public Collection<Fact> find(Collection<String> values, Collection<Type> types) {
		Collection<Fact> result = null;
		for (String value : values) {
			if (!value.isEmpty()) {
				if (result == null) {
					result = index.find(value, types);
				} else {
					result.retainAll(index.find(value, types));
				}
			}
		}
		return result;
	}

	/**
	 * Search text in the index
	 * 
	 * @param value
	 * @return
	 */
	public Collection<Fact> find(String value) {
		return new HashSet(index.find(value));
	}

	/**
	 * Finds all child facts
	 * 
	 * @return
	 */
	public List<Fact> find() {
		return root.getFacts();
	}

	/**
	 * Finds all child value facts
	 * 
	 * @return
	 */
	public List<FactValue> findValues() {
		return (List) this.find(Type.value);
	}

	/**
	 * Finds child value facts
	 * 
	 * @return
	 */
	public List<FactValue> findValues(String value) {
		return new ArrayList(this.find(value, Type.value));
	}

	/**
	 * Finds child value facts of the indicated data type
	 * 
	 * @return
	 */
	public List<FactValue> findValues(DataType type) {
		return this.findValues().stream().filter(f -> f.getDataType() == type).filter(f -> !Utils.isEmpty(f.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * Concatenates all Texts for the same date into a single text. We ignore the
	 * text entries which have segment information
	 * 
	 * @return
	 */
	public List<FactValue> getCombinedTextValues() {
		Map<String, List<FactValue>> facts = this.findValues().stream().filter(f -> f.getDataType() == DataType.string)
				.filter(f -> f.getContext().getSegments().isEmpty()).filter(f -> !Utils.isEmpty(f.getValue()))
				.collect(Collectors.groupingBy(FactValue::getDate));

		return facts.entrySet().stream().map(e -> toFactValue(e)).collect(Collectors.toList());

	}

	/**
	 * Combined all texts
	 * 
	 * @param entry
	 * @return
	 */
	protected FactValue toFactValue(Entry<String, List<FactValue>> entry) {
		FactValue value = new FactValue(this, Type.typedMember.value, 0, 0);
		value.put("date", entry.getKey());
		value.getAttributes().entrySet().stream()
				.filter(e -> e.getKey().matches(
						"location|sicDescription|date|identifier|incorporation|sicCode|companyName|tradingSymbol"))
				.filter(e -> !Utils.isEmpty(e.getValue())).forEach(e -> value.put(e.getKey(), e.getValue()));
		StringBuffer sb = new StringBuffer();
		entry.getValue().forEach(e -> sb.append(e.getLabel() + ": " + e.getValue() + System.lineSeparator()));
		value.setValue(sb.toString());
		return value;
	}

	/**
	 * Determines the parameter names with the indicated type
	 * 
	 * @param dataType
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public Set<String> getParameterNames(DataType dataType)
			throws IOException, SAXException, ParserConfigurationException {
		return this.findValues().stream().filter(p -> !p.getDataType().equals(dataType)).map(p -> p.getParameterName())
				.collect(Collectors.toSet());
	}

	/**
	 * Determines all parameter names
	 * 
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public Set<String> getParameterNames() throws IOException, SAXException, ParserConfigurationException {
		return this.findValues().stream().map(p -> p.getParameterName()).collect(Collectors.toSet());
	}

	/**
	 * Finds the child facts
	 * 
	 * @param type
	 * @return
	 */
	public List<Fact> find(Type type) {
		return find(Arrays.asList(type));
	}

	/**
	 * Finds the child facts
	 * 
	 * @param types
	 * @return
	 */
	public List<Fact> find(Collection<Type> types) {
		return root.getFacts(types, true, 0, Integer.MAX_VALUE);
	}

	/**
	 * Returns all urls which were loaded either via the load method or indirectly
	 * by the system because the content of the url contains references to other
	 * urls.
	 * 
	 * @return
	 */
	public Collection<URL> getLoadedURLs() {
		return this.loadedURLs;
	}

	/**
	 * Returns the first fact form the collection. If the collection is empty we
	 * return an empty fact node.
	 * 
	 * @param facts
	 * @return
	 */
	public Fact first(Collection<Fact> facts) {
		return facts.isEmpty() ? EMPTY : facts.iterator().next();
	}

	/**
	 * We make sure that we do not store any html information to save on memory.
	 * 
	 * @param ignore
	 */
	public void setIgnoreHtml(boolean ignore) {
		this.setValueFormatter(DataType.html, ignore ? new RemoveValueFormatter() : new DefaultValueFormatter());
	}

	/**
	 * We convert any html to text
	 */
	public void setConvertHtmlToText(boolean convert) {
		this.setValueFormatter(DataType.html, convert ? new HtmlToTextFormatter() : new DefaultValueFormatter());
	}

	public void setValueFormatter(DataType type, IValueFormatter formatter) {
		this.formatters.put(type, formatter);
	}

	public IValueFormatter getValueFormatter(DataType type) {
		IValueFormatter result = this.formatters.get(type);
		return result == null ? new DefaultValueFormatter() : result;
	}

	/**
	 * Returns the collection of all attribute names
	 * 
	 * @return
	 */
	public List<String> getValueAttributes() {
		if (valueAttributes == null) {
			Set set = new HashSet();
			List<FactValue> values = this.findValues();
			set.addAll(values.get(values.size() - 1).getAttributes().keySet());
			set.addAll(values.get(values.size() / 2).getAttributes().keySet());
			valueAttributes = new ArrayList(set);
		}

		return valueAttributes;
	}

	protected void addAttributes(String name) {
		if (!valueAttributes.contains(name)) {
			valueAttributes.add(name);
		}
	}

	protected void setIndex(IndexAPI index) {
		this.index = index;
	}

	/**
	 * Provides the value elements as csv
	 * 
	 * @return
	 */
	public String toValueCSV() {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String attribute : this.getValueAttributes()) {
			if (!first)
				sb.append(Utils.DEL);
			sb.append(attribute);
			first = false;
		}
		sb.append(Utils.NL);
		for (Fact fact : this.find(Type.value)) {
			first = true;
			if (!Utils.isEmpty(fact.getAttribute(Attribute.value))) {
				for (String attribute : this.getValueAttributes()) {
					if (!first)
						sb.append(Utils.DEL);
					sb.append(Utils.notNull(Utils.escapeCSV(fact.getAttribute(attribute))));
					first = false;
				}
				sb.append(Utils.NL);
			}
		}
		return sb.toString();
	}

	public ICompany getCompanyInfo() {
		if (this.companyInfo == null) {
			this.companyInfo = new EdgarCompany(this);
		}
		return companyInfo;
	}
	
	public XBRL setCompanyInfo(ICompany company) {
		this.companyInfo = company;
		this.setCompanyNumber(company.getCompanyNumber());
		return this;
	}

	public List<Context> getContext(SegmentScope scope) {
		List<Context> result = new ArrayList();
		for (Fact ctxFact : this.find(Arrays.asList(Type.context))) {
			Context ctx = new Context(this, ctxFact.getAttribute(Attribute.id));
			switch (scope) {
			case WithSegments:
				if (ctx.isWithSegments()) {
					result.add(ctx);
				}
				break;
			case WithoutSegments:
				if (!ctx.isWithSegments()) {
					result.add(ctx);
				}
				break;
			default:
				result.add(ctx);
				break;
			}
		}
		return result;
	}

	/**
	 * Determines the contexts for the indicated dimension
	 * 
	 * @param dimension
	 * @return
	 */
	public List<Context> getContext(String dimension) {
		List<Context> result = new ArrayList();
		for (Fact ctxFact : this.find(Arrays.asList(Type.context))) {
			Context ctx = new Context(this, ctxFact.getAttribute(Attribute.id));
			for (Segment segment : ctx.getSegments()) {
				if (segment.getDimension().equals(dimension)) {
					result.add(ctx);
				}
			}
		}
		return result;
	}

	/**
	 * Determines all existing dimensions
	 * 
	 * @return
	 */
	public Collection<String> getDimensions() {
		Set<String> result = new HashSet();
		for (Fact ctxFact : this.find(Arrays.asList(Type.context))) {
			Context ctx = new Context(this, ctxFact.getAttribute(Attribute.id));
			for (Segment segment : ctx.getSegments()) {
				String dimension = segment.getDimension();
				if (!Utils.isEmpty(dimension)) {
					result.add(dimension);
				}
			}
		}
		return result;
	}

	public boolean isSetupValueAttributes() {
		return setupValueAttributes;
	}

	public void setSetupValueAttributes(boolean setupValueAttributes) {
		this.setupValueAttributes = setupValueAttributes;
	}

	public boolean isExtendedCompanyInformation() {
		return extendedCompanyInformation;
	}

	public void setExtendedCompanyInformation(boolean extendedCompanyInformation) {
		this.extendedCompanyInformation = extendedCompanyInformation;
	}

	public boolean isPostProcessingDone() {
		return postProcessingDone;
	}

	public void setPostProcessingDone(boolean postProcessingDone) {
		this.postProcessingDone = postProcessingDone;
	}

	public int getMaxFieldSize() {
		return maxFieldSize;
	}

	public void setMaxFieldSize(int maxFieldSize) {
		this.maxFieldSize = maxFieldSize;
	}

	/**
	 * Converts all the values with an empty segment into a ITable
	 * 
	 * @return
	 */
	public ITableEx<Value> toTable() {
		return toTable(a -> a.getDataType() == DataType.number && a.getUnit().equals("USD")
				&& a.getContext().getSegments().isEmpty());
	}

	/**
	 * Converts all the values with an empty segment into a ITable
	 * 
	 * @return
	 */
	public ITableEx<Value> toTable(Predicate<FactValue> p) {
		ValueTable table = new ValueTable("value", "parameterName", Arrays.asList("prefix", "label", "uri", "dateLabel",
				"contextRef", "decimals", "segment", "segmentDimension", "file", "id"));
		List<Map<String, String>> records = this.findValues().stream().filter(a -> p.test(a))
				.map(a -> a.getAttributes()).collect(Collectors.toList());
		table.addValues(records);
		return table;
	}

	/**
	 * Check if we have any data available. Returns true if there is no data
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return this.index.isEmpty();
	}

	/**
	 * Determines the information from the last filing (file name and form)
	 * 
	 * @return
	 */
	public EdgarFiling getFilingInfo() {
		return this.lastFilingInfo;
	}

	/**
	 * Closes all related collectioins
	 */
	public void close() {
		this.index.clear();
		this.root.clear();
		this.loadedURLs.clear();

		if (this.labelAPI != null) {
			this.labelAPI.close();
			this.labelAPI = null;
		}
		if (this.presentationAPI != null) {
			this.presentationAPI.close();
			this.presentationAPI = null;
		}
		if (this.valueAttributes != null) {
			this.valueAttributes.clear();
			this.valueAttributes = null;
		}
		this.companyInfo = null;

	}


}
