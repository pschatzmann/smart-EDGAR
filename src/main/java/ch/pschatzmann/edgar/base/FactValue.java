package ch.pschatzmann.edgar.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.model.Parameter.Source;
import org.jsoup.Jsoup;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.pschatzmann.edgar.utils.Utils;

/**
 * Fact which represents a node of type 'value'.
 * 
 * @author pschatzmann
 *
 */
public class FactValue extends Fact implements Serializable {
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = Logger.getLogger(FactValue.class);
	private Context ctx;
	private boolean isAttributesSetup = false;
	private String value = null;

	public FactValue(XBRL xbrl, Type type, int level, long line) {
		super(xbrl, type, level, line);
	}
	
	public FactValue(FactValue source) {
		super(source);
		this.ctx = source.ctx;
		this.isAttributesSetup = source.isAttributesSetup;
		this.value = source.value;
	}

	/**
	 * Returns the attribute/value map
	 * 
	 * @return
	 */
	@JsonIgnore
	public Map<String, String> getAttributes() {
		setupAttributes(this.getXBRL());
		return super.getAttributes();
	}

	protected void setupAttributes(XBRL xbrl) {
		if (!isAttributesSetup && xbrl.isPostProcessingDone()) {
			this.isAttributesSetup = true;

			this.put(Attribute.value.name(), this.getValue());
			// label
			this.put(Attribute.label.name(), this.getLabel());
			if (this.getAttribute(Attribute.unitRef) != null) {
				put(Attribute.unitRef.name(), this.getUnit());
			}
			// attributes form context
			Context ctx = this.getContext();
			if (ctx!=null) {
				this.put(Attribute.dateLabel.name(), ctx.getDateDescription());
				this.put(Attribute.date.name(), ctx.getDate());
				this.put(Attribute.segment.name(), ctx.getSegmentDescription());
				this.put(Attribute.segmentDimension.name(), ctx.getDimensionDescription());
				this.put(Attribute.identifier.name(), ctx.getCompanyIdentifier());
				this.put(Attribute.numberOfMonths.name(), "" + ctx.getMonths());
			}

			// additional attributes
			if (xbrl.isExtendedCompanyInformation()) {
				ICompany company = xbrl.getCompanyInfo();
				this.put(Attribute.companyName.name(), company.getCompanyName());
				this.put(Attribute.tradingSymbol.name(), company.getTradingSymbol());
				this.put(Attribute.incorporation.name(), company.getIncorporationState());
				this.put(Attribute.location.name(), company.getLocationState());
				this.put(Attribute.sicCode.name(), company.getSICCode());
				this.put(Attribute.sicDescription.name(), company.getSICDescription());
			}
			// filing information
			this.put(Attribute.form.name(), this.getFilingInfo().getForm());
			this.put(Attribute.file.name(), this.getFilingInfo().getFileName());
		}
	}

	/**
	 * Returns endDate attribute value. If it is not available on the node, we determine it from the context
	 * 
	 * @return
	 */
	public String getDate() {
		String result = Utils.notNull(this.getAttribute(Attribute.date));
		if (result.isEmpty()) {
			result = Utils.notNull(this.getContext().getDate());
		}
		return result;
	}
	
	/**
	 * URI of parameter
	 * @return
	 */
	public String getUri() {
		return Utils.notNull(this.getAttribute("uri"));
	}

	/**
	 * URI Prefix of paramter
	 * @return
	 */
	public String getUriPrefix() {
		return Utils.notNull(this.getAttribute("prefix"));		
	}

	/**
	 * Returns the value attribute value. For ixbrl we need to explode the child
	 * nodes to determine the content
	 * 
	 * @return
	 */
	public String getValue() {
		if (value == null) {
			StringBuffer sb = new StringBuffer(Utils.str(this.getAttribute("value")));
			String continuedAt = this.getAttribute("continuedAt");
			if (Utils.isEmpty(continuedAt)) {
				updateValuesFromChildren(sb);
			} else {
				updateValueFromContinuations(sb, continuedAt);
			}
			value = sb.toString();
		}
		return value;
	}
	
	
	protected void setValue(String value) {
		this.put("value", value);
		this.value = value;
	}
	
	private void updateValueFromContinuations(StringBuffer sb, String continuedAt) {
		while (!Utils.isEmpty(continuedAt)) {
			Fact f = getContinuationById(continuedAt);	
			for (Fact c : f.getChildren()) {
				if (c.getType()!=Type.continuation) {
					for (Fact c1 : c.getFacts()) {
						String str = Utils.str(c1.getAttribute("value"));
						if (!Utils.isEmpty(str)) {
							sb.append(str);
							sb.append(" ");
						}
					}
				}
			}
			continuedAt = f.getAttribute("continuedAt");
		}
		value = sb.toString();
		this.put("value", value);
	}

	private void updateValuesFromChildren(StringBuffer sb) {
		if (!Utils.isEmpty(this.getAttribute("parameterName"))) {
			for (Fact f : this.getFacts()) {
				if (f != this) {
					String str = f.getAttribute("value");
					if (!Utils.isEmpty(str)) {
						sb.append(str);
						sb.append(" ");
					}
				}
			}
			value = sb.toString();
			this.put("value", value);
		}
	}
	
	private Fact getContinuationById(String id) {
		for (Fact f : this.getXBRL().find(id)) {
			if (f.getType()==Type.continuation && f.getAttribute("id").equals(id)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * Determines the unit of measure label
	 * 
	 * @return
	 */
	public String getUnit() {
		String unit = this.getAttribute(Attribute.unitRef);
		return this.getXBRL().getLabelAPI().getUOMLabel(unit);
	}

	/**
	 * Returns the label of the parameter
	 * 
	 * @return
	 */
	public String getLabel() {
		return this.getXBRL().getLabelAPI().getLabel(getParameterName()).getLabel();
	}

	/**
	 * 
	 * @param preferredRole
	 * @return
	 */
	public String getLabel(String preferredRole) {
		return this.getXBRL().getLabelAPI().getLabel(getParameterName(), preferredRole).getLabel();
	}
	
	/**
	 * Returns true if the context has segments
	 * @return
	 */
	public boolean isContextWithSegments() {
		return this.getContext().isWithSegments();
	}
	

	/**
	 * Gets the information related to the context
	 * 
	 * @return
	 */
	@JsonIgnore
	public Context getContext() {
		if (ctx == null) {
			ctx = new Context(this.getXBRL(), this.getAttribute(Attribute.contextRef));
			if (!ctx.isValid()) {
				ctx = null;
			}
		}
		return ctx;
	}

	/**
	 * Returns the number of periods
	 * 
	 * @return
	 */
	public int getMonths() {
		return this.getContext().getMonths();
	}

	/**
	 * Returns the contextRef value
	 * 
	 * @return
	 */
	public String getContextID() {
		return this.getAttribute(Attribute.contextRef);
	}

	/**
	 * Returns all field values
	 * 
	 * @return
	 */
	@JsonIgnore
	public List<String> getAttributeValues() {
		List<String> result = new ArrayList();
		for (String att : this.getXBRL().getValueAttributes()) {
			result.add(this.getAttribute(att));
		}
		return result;
	}
	
	public String getYear() {
		String date = this.getContext().getDate();
		String year = date.length() < 4 ? "":date.substring(0, 4);
		return year;
	}
	

}
