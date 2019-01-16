package ch.pschatzmann.edgar.base;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.pschatzmann.edgar.base.Fact.Attribute;
import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * API for the presentation layer. This is represented by a hierarchical tree of
 * Presentation objects.
 * 
 * @author pschatzmann
 *
 */

public class PresentationAPI implements Serializable {
	private static final Logger LOG = Logger.getLogger(PresentationAPI.class);
	private XBRL xbrl;
	private Presentation root = new Presentation(this, "ROOT", "ROOT");
	private boolean suppressEmptyRows = false;
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Default constructor
	 * 
	 * @param xbrl
	 */
	protected PresentationAPI(XBRL xbrl) {
		this.xbrl = xbrl;
		this.open();
	}

	/**
	 * Returns a reference to the main XBRL
	 * 
	 * @return
	 */

	public XBRL getXBRL() {
		return this.xbrl;
	}

	/**
	 * Load the data into the model
	 */
	protected void open() {
		// add presentation link nodes
		root = getNode("ROOT", "ROOT", null);
		//Map<String, String> roleMap = getRoleMap();
		for (Fact presentationLink : xbrl.find(Type.presentationLink)) {
			// add presentationArc
			processPresentationArcs(presentationLink);
		}
		sort(root);
		extendWithFacts();
	}

	protected void processPresentationArcs(Fact presentationLink) {
		// collect information from loc entries
		Map<String, String> labelMap = this.getLocatorLabelMap(presentationLink);
		Map<String, Presentation> presentationMap = new HashMap();

		// process presentationArc
		String role = Utils.lastPath(presentationLink.getAttribute(Attribute.role));
		Set<Presentation> nodes = new HashSet();
		Map<String, String> refLabel = getLabels(presentationLink);
		processPresentationArcs(presentationLink, labelMap, presentationMap, role, nodes, refLabel);

		// add presentation to root if necessary
		for (Presentation n : nodes) {
			Presentation localRoot = n.getRoot();
			if (localRoot != this.root) {
				this.root.addChild(localRoot);
			}
		}
	}

	protected void processPresentationArcs(Fact presentationLink, Map<String, String> labelMap,
			Map<String, Presentation> presentationMap, String role, Set<Presentation> nodes,
			Map<String, String> refLabel) {
		for (Fact child : presentationLink.getChildren()) {
			if (child.getType() == Type.presentationArc) {
				String fromID = child.getAttribute(Attribute.from);
				String from = labelMap.get(fromID);
				String toID = child.getAttribute(Attribute.to);
				String to = labelMap.get(toID);
				String order = Utils.lastPath(child.getAttribute(Attribute.order));
				String priority = Utils.lastPath(child.getAttribute(Attribute.priority));
				String preferredLabel = Utils.lastPath(child.getAttribute(Attribute.preferredLabel));
				Presentation fromNode = getNode(from, fromID, presentationMap);
				fromNode.setLabelID(refLabel.get(fromID));
				fromNode.setRole(role);
				if (!Utils.isEmpty(preferredLabel)) {
					fromNode.setLabelRole(preferredLabel);
				}
				nodes.add(fromNode);

				Presentation toNode = getNode(to, toID, presentationMap);
				toNode.setLabelID(refLabel.get(toID));
				toNode.setOrder(Double.valueOf(order));
				toNode.setPriority(Utils.isEmpty(priority) ? 0.0 : Double.valueOf(priority));
				toNode.setRole(role);
				fromNode.addChild(toNode);

			}
		}
	}

	protected Map<String, String> getLabels(Fact presentationLink) {
		Map<String, String> refLabel = new HashMap();
		for (Fact child : presentationLink.getChildren()) {
			if (child.getType() == Type.loc) {
				String ref = Utils.lastPath(child.getAttribute(Attribute.href), "#");
				String label = child.getAttribute(Attribute.label);
				refLabel.put(label, ref);
			}
		}
		return refLabel;
	}

	protected void sort(Presentation node) {
		Collections.sort(node.getChildren());
		Collections.sort(node.getFacts());

		for (Presentation p : node.getChildren()) {
			sort(p);
		}
	}

	protected Presentation getNode(String name, String id, Map<String, Presentation> presentationMap) {
		Presentation result = presentationMap == null ? null : presentationMap.get(id);
		if (result == null) {
			result = new Presentation(this, id, name);
			// result.setParent(this.defaultParent);
			if (presentationMap != null) {
				presentationMap.put(id, result);
			}
		}
		return result;
	}

	/**
	 * Returns all exploded rows in inorder sequence starting from the root.
	 * 
	 * @return
	 */

	public List<Presentation> getChildrenEx() {
		return this.getRoot().getChildrenEx();
	}

	/**
	 * Get all direct children
	 * 
	 * @return
	 */
	public List<Presentation> getChildren() {
		return this.getRoot().getChildren();
	}

	/**
	 * Finds the Presentation by the name
	 * 
	 * @param viewName
	 * @return
	 */
	public Presentation getPresentation(String viewName) {
		for (Presentation p : this.getRoot().getChildrenEx()) {
			if (p.getName().equals(viewName)) {
				return p;
			}
		}
		return null;
	}

	protected Map<String, String> getRoleMap() {
		Map<String, String> result = new HashMap();
		for (Fact roleRef : xbrl.find(Type.roleRef)) {
			result.put(Utils.lastPath(roleRef.getAttribute(Attribute.href)),
					Utils.lastPath(roleRef.getAttribute(Attribute.roleURI)));
		}
		return result;
	}

	protected Map<String, String> getLocatorLabelMap(Fact presentationLink) {
		Map<String, String> map = new HashMap();
		for (Fact loc : presentationLink.getChildren()) {
			if (loc.getType() == Type.loc) {
				map.put(loc.getAttribute(Attribute.label), Utils.lastPath(loc.getAttribute(Attribute.href)));
			}
		}
		return map;
	}

	/**
	 * Determines the presentations for a parameter
	 * 
	 * @param parameterName
	 * @return
	 */
	public Collection<Presentation> findPresentationsForParameter(String parameterName) {
		Collection<Presentation> result = new HashSet();
		for (Presentation p : this.getChildrenEx()) {
			for (Fact fact : p.getFacts()) {
				if (fact.getParameterName().equals(parameterName)) {
					result.add(p);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the Root for all Presentations
	 * 
	 * @return
	 */

	public Presentation getRoot() {
		return root;
	}

	/**
	 * Determines if empty rows should be suppressed
	 * 
	 * @return
	 */
	public boolean isSuppressEmptyRows() {
		return suppressEmptyRows;
	}

	/**
	 * Defines that the empty rows should be suppressed
	 * 
	 * @param suppressEmptyRows
	 */
	public void setSuppressEmptyRows(boolean suppressEmptyRows) {
		this.suppressEmptyRows = suppressEmptyRows;
	}

	protected void extendWithFacts() {
		for (Presentation p : this.getChildrenEx()) {
			if (p.isLeaf()) {
				Collection<String> parameters = p.getParameterNamesFromFacts(p.getContexts());
				if (parameters.size() > 1) {
					// reclassify facts and create Presentations
					p.resetFacts();
					for (String par : parameters) {
						Presentation pNew = new Presentation(this, par, par);
						pNew.setLabelID(par);
						p.addChild(pNew);
					}
				}
			}
		}
	}

	public String toJson() throws JsonProcessingException {
		return mapper.writeValueAsString(this.getRoot());
	}

	/**
	 * Release memory from collections
	 */
	public void close() {
		root.clear();
	}

}
