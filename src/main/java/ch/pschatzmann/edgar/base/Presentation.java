package ch.pschatzmann.edgar.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.pschatzmann.edgar.base.Fact.Attribute;
import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Unfortunately the reporting tree structure is not matching the xml tag
 * hierarchy but is specified with from to nodes. We translate this into a
 * proper tree structure.
 * 
 * @author pschatzmann
 *
 */
public class Presentation implements Serializable, Comparable<Presentation> {
	private static final Logger LOG = Logger.getLogger(Presentation.class);
	private String name = "";
	private List<Presentation> children = null;
    @JsonIgnore
	private Presentation parent;
	private List<Fact> facts;
	private String labelRole = "label";
	private String labelID;
    @JsonIgnore
	private PresentationAPI presentationAPI;
	private Double priority = 0.0;
	private Double order = 0.0;
	private String role = "";
	private String id;

	Presentation(PresentationAPI presentationAPI, String id, String name) {
		this.presentationAPI = presentationAPI;
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name == null ? "" : name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getParentName() {
		return (this.getParent()!=null) ? this.getParent().getName():"";
	}

    @JsonIgnore
	public List<Presentation> getChildren() {
    		if (children==null) {
    			children = new ArrayList();
    		}
		return children;
	}

	public void addChild(Presentation child) {
		if (!this.getChildren().contains(child)) {
			this.children.add(child);
			child.setParent(this);
		}
	}

	public void setParent(Presentation parent) {
		this.parent = parent;
	}

	@JsonIgnore
	public List<Fact> getFacts() {
		if (facts == null) {
			facts = new ArrayList(this.presentationAPI.getXBRL().find(this.getName(), Type.value));
		}
		return facts;
	}
	
	public boolean isFactsAvailable() {
		return !getFacts().isEmpty();
	}
	
	public boolean isLeaf() {
		return this.getChildren().isEmpty();
	}
	
	
	protected void resetFacts() {
		facts.clear();
	}

	public List<FactValue> getFacts(Collection<Context> ctx) {
		List<FactValue> facts = new ArrayList();
		for (Fact f : getFacts()) {
			if (f instanceof FactValue) {
				if (ctx.contains(((FactValue) f).getContext())) {
					facts.add((FactValue) f);
				}
			}
		}
		return facts;
	}
		
	protected Collection<String> getParameterNamesFromFacts(Collection<Context> ctx){
		Set<String> result = new TreeSet();
		for (FactValue fc : getFacts(ctx)) {
			result.add(fc.getAttribute(Attribute.parameterName));
		}
		return result;
	}

	protected String getLabelRole() {
		return labelRole == null ? "" : labelRole;
	}

	protected void setLabelRole(String labelRole) {
		this.labelRole = labelRole;
	}

	protected String getLabelID() {
		return labelID;
	}

	protected void setLabelID(String labelID) {
		this.labelID = labelID;
		if (labelID == null) {
			LOG.info(this.id);
		}
	}

	public String getLabel() {
		String id = this.getLabelID();
		String label = this.presentationAPI.getXBRL().getLabelAPI().getLabel(id, this.getLabelRole()).getLabel();
		if (label.equals(id)) {
			id = Utils.lastPath(id);
			label = this.presentationAPI.getXBRL().getLabelAPI().getLabel(id, this.getLabelRole()).getLabel();
		}
		return label;
	}

	public int getSequence() {
		return this.getParent() == null ? 0 : this.getParent().getChildren().indexOf(this);
	}

	public Integer getLevel() {
		int result = 0;
		Presentation par = this.getParent();
		while (par != null) {
			result++;
			par = par.getParent();
		}
		return result;
	}

	/**
	 * Returns the name of the parent node which is just below the root node
	 * 
	 * @return
	 */
	public String getViewName() {
		Presentation par = this;
		String result = null;
		while (par != this.presentationAPI.getRoot()) {
			result = par.getName();
			par = par.getParent();
		}
		return result;
	}

	/**
	 * Determines the parent node
	 * 
	 * @return
	 */
    @JsonIgnore
	public Presentation getParent() {
		return parent;
	}

	/**
	 * Determines the actual root
	 * 
	 * @return
	 */
    @JsonIgnore
	public Presentation getRoot() {
		Presentation result = this;
		while (result.getParent() != null) {
			result = result.getParent();
		}
		return result;
	}

	/**
	 * Defines the priority from the xml
	 * 
	 * @param priority
	 */
	protected void setPriority(double priority) {
		this.priority = priority;
	}

	/**
	 * Defines the order from the xml
	 * 
	 * @param order
	 */
	protected void setOrder(double order) {
		this.order = order;
	}

	/**
	 * Returns the exploded list of children
	 * 
	 * @return
	 */
  
    @JsonIgnore
	public List<Presentation> getChildrenEx() {
		List<Presentation> result = new ArrayList();
		collectChildren(this, result);
		return result;
	}

	public List<Presentation> getChildrenEx(List<Context> ctxList) {
		List<Presentation> result = new ArrayList();
		for (Presentation p : getChildrenEx()) {
			if (p.hasFactsEx(ctxList)) {
				result.add(p);
			}
		}
		return result;
	}

	protected boolean hasFactsEx(List<Context> ctxList) {
		for (Presentation p : this.getChildrenEx()) {
			if (p.hasFacts(ctxList)) {
				return true;
			}
		}
		return false;
	}

	protected boolean hasFacts(List<Context> ctxList) {
		for (Fact f : this.getFacts()) {
			if (f instanceof FactValue) {
				FactValue fv = (FactValue) f;
				if (ctxList.contains(fv.getContext())) {
					return true;
				}
			}
		}
		return false;
	}

	protected void collectChildren(Presentation node, List<Presentation> result) {
		if (node != null) {
			result.add(node);
			for (Presentation child : node.getChildren()) {
				collectChildren(child, result);
			}
		}
	}
	
	/**
	 * Determines the list of all contexts (= horizontal dimensions of the table)
	 * 
	 * @return
	 */
	public List<Context> getContexts(boolean onSpecialAxis) {
		Set<Context> contextReferences = new TreeSet();
		for (Presentation p : getChildrenEx()) {
			for (Fact f : p.getFacts()) {
				Context ctx = ((FactValue) f).getContext();
				if (onSpecialAxis && ctx.isWithSegments() || !onSpecialAxis && !ctx.isWithSegments()) {
					contextReferences.add(ctx);
				}
			}
		}
		return new ArrayList(contextReferences);
	}
	
	public List<String> getDimensions() {
		Set<String> result = new HashSet();
		for (Presentation p : getChildrenEx()) {
			for (Fact f : p.getFacts()) {
				Context ctx = ((FactValue) f).getContext();
				for (Segment segment : ctx.getSegments()) {
						result.add(segment.getDimension());
					
				}
			}
		}
		return new ArrayList(result);
	}
	
	public List<Context> getContexts(String dimension) {
		Set<Context> result = new TreeSet();
		for (Presentation p : getChildrenEx()) {
			for (Fact f : p.getFacts()) {
				Context ctx = ((FactValue) f).getContext();
				for (Segment segment : ctx.getSegments()) {
					if (segment.getDimension().equals(dimension)) {
						result.add(ctx);
					}
				}
			}
		}
		return new ArrayList(result);
	}

	@JsonIgnore
	public List<Context> getContexts() {
		Set<Context> contextReferences = new TreeSet();
		for (Presentation p : getChildrenEx()) {
			for (Fact f : p.getFacts()) {
				Context ctx = ((FactValue) f).getContext();
				contextReferences.add(ctx);				
			}
		}
		return new ArrayList(contextReferences);
	}
	
	
	public List<String> getContextIds() {
		Set<String> contextReferences = new TreeSet();
		for (Presentation p : getChildrenEx()) {
			for (Fact f : p.getFacts()) {
				Context ctx = ((FactValue) f).getContext();
				contextReferences.add(ctx.getID());				
			}
		}
		return new ArrayList(contextReferences);
	}



	/**
	 * Determines the value for the indicated context
	 * 
	 * @param context
	 * @return
	 */
	public String getValue(Context context) {
		Collection<String> values = new TreeSet();
		StringBuffer sb = new StringBuffer();
		List<FactValue> facts = this.getFacts(Arrays.asList(context));
		switch (facts.size()) {
		case 0:
			break;
		case 1:
			sb.append(facts.get(0).getValue());
			break;
		default:
			break;
		}
		return sb.toString();
	}
	
	protected List<String> getUOMs(Collection<Context> ctxList) {
		Set<String> uoms = new TreeSet();
		for (Fact f : this.getFacts(ctxList)) {
			String uom = f.getAttribute(Attribute.unitRef);
			if (uom!=null) {
				uoms.add(presentationAPI.getXBRL().getLabelAPI().getUOMLabel(uom));
			}
		}
		return new ArrayList(uoms);
	}
	
	public String getUOM(Collection<Context> ctxList) {
		List<String> list = getUOMs(ctxList);
		return list.size()==1 ? list.get(0) : "";
	}

	/**
	 * Determines the role
	 * 
	 * @return
	 */
	public String getRole() {
		return role == null ? "" : role;
	}

	/**
	 * Sets the role which was defined in the Presentation xml with the roleRef tag
	 * 
	 * @param role
	 */
	public void setRole(String role) {
		this.role = role;
	}

	public String toHTML() {
		StringBuffer sb = new StringBuffer();
		List<Context> ctxList = this.getContexts(false);
		toHtml(sb, ctxList, "");

		for (String dimension : this.getDimensions()) {		
			List<Context> ctxList1 = this.getContexts(dimension);
			toHtml( sb, ctxList1, dimension);
		}
		return sb.toString();

	}

	protected void toHtml(StringBuffer sb, List<Context> ctxList, String dimension) {
		boolean first = true;
		if (!ctxList.isEmpty()) {
			if (first) {
				sb.append("<h2>");
				sb.append(this.getLabel());
				sb.append("</h2>");
				first = false;
			}

			sb.append("<h3>");
			String title = presentationAPI.getXBRL().getLabelAPI().getLabel(dimension).getLabel();
			sb.append(title);
			sb.append("</h3>");

			sb.append("<table border='1' style='width:100%'>");
			sb.append(Utils.NL);
			sb.append("<tr>");
			sb.append("<th/>");
			sb.append("<th/>");
			for (Context ctx : ctxList) {
				sb.append("<th>");
				String segmentTitle = Utils.toString(ctx.getSegments().stream().map(c -> c.getDescription()).collect(Collectors.toList()),"","/");
				sb.append(segmentTitle);
				sb.append(ctx.getDateDescription());
				sb.append("</th>");
			}
			sb.append("</tr>");
			sb.append(Utils.NL);

			for (Presentation p : this.getChildrenEx()) {
				if (!this.presentationAPI.isSuppressEmptyRows() || p.hasFactValues(ctxList)) {
					sb.append("<tr>");
					sb.append("<td>");
					sb.append(Utils.repeat("-", p.getLevel() - 1));
					sb.append(p.getLabel());
					sb.append("</td>");
					sb.append("<td>");
					sb.append(p.getUOM(ctxList));
					sb.append("</td>");
					
					
					for (Context ctx : ctxList) {
						sb.append("<td>");
						sb.append(p.getValue(ctx));
						sb.append("</td>");
					}
					sb.append("</tr>");
					sb.append(Utils.NL);
				}
			}
			sb.append("</table>");
			sb.append(Utils.NL);
		}
	}

	protected List<String> getSegmentLabels(List<Context> ctxList) {
		Set set = new TreeSet();
		for (Context ctx : ctxList) {
			set.addAll(ctx.getSegments());
		}
		return new ArrayList(set);
	}

	protected Double getPriority() {
		return this.priority;
	}

	protected Double getOrder() {
		return this.order;
	}


	protected Collection<FactValue> getFactValues(List<Context> ctxList) {
		Collection<FactValue> result = new ArrayList();
		for (Fact f : this.getFacts()) {
			FactValue vf = (FactValue) f;
			if (ctxList.contains(vf.getContext())) {
				result.add(vf);
			}
		}
		return result;
	}

	protected boolean hasFactValues(List<Context> ctxList) {
		for (Fact f : this.getFacts()) {
			FactValue vf = (FactValue) f;
			if (ctxList.contains(vf.getContext())) {
				return true;
			}
		}

		for (Presentation p : this.getChildrenEx()) {
			for (Fact f : p.getFacts()) {
				FactValue vf = (FactValue) f;
				if (ctxList.contains(vf.getContext())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getName());
		sb.append(":");
		sb.append(this.getRole());
		sb.append(" ");
		sb.append(this.getLevel());
		sb.append("/");
		sb.append(this.getSequence());
		return sb.toString();
	}

	/**
	 * Clears the data
	 */
	public void clear() {
		this.children = null;
		this.facts = null;
	}

	/**
	 * Make sure that we sort by priority and order
	 */
	@Override
	public int compareTo(Presentation o) {
		if (this.equals(o)) {
			return 0;
		}

		int result = this.getOrder().compareTo(o.getOrder());
		// }
		return result;
	}

	public boolean equals(Presentation o) {
		return this.id.equals(o.id);
	}

	@JsonIgnore
	public Presentation getLevel1() {
		Presentation current = this;		
		while (current.getLevel()>1) {
			current = current.getParent();
		}
		return current.getLevel()==1 ? current : null;
	}

}
