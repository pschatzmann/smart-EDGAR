package ch.pschatzmann.edgar.base;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import ch.pschatzmann.edgar.base.Fact.Attribute;
import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Access to the label information
 * 
 * @author pschatzmann
 *
 */

public class LabelAPI implements Serializable {
	private XBRL xbrl;
	static Label emptyLabel = new Label("", "");
	private Map<String, String> unitMap = new HashMap();

	public LabelAPI(XBRL xbrl) {
		this.xbrl = xbrl;
	}

	/**
	 * Determines the Labels for the indicated parameter: Within a labelLink we find
	 * the loc with the help of the parameter name. From there we find the
	 * corresponding labelArc and then the related label
	 * 
	 * @param parameter
	 * @return
	 */
	public Collection<Label> getLabels(String parameter) {
		Collection<Label> result = new HashSet();
		for (Fact labelLinks : xbrl.find(parameter, Type.labelLink)) {
			for (Fact f : labelLinks.getChildren(Type.loc)) {
				String ref = Utils.lastPath(f.getAttribute(Attribute.href));
				if (ref.equals(parameter)) {
					String label = f.getAttribute(Attribute.label);
					for (Fact l : xbrl.find(label, Type.labelArc)) {
						String to = l.getAttribute(Attribute.to);
						for (Fact l1 : xbrl.find(to, Type.label)) {
							result.add(new Label(l1.getAttribute(Attribute.label), l1.getAttribute(Attribute.role)));
						}
					}
				}
			}
		}

		if (result.isEmpty()) {
			for (Fact l1 : this.xbrl.find(parameter, Type.label)) {
				result.add(new Label(l1.getAttribute(Attribute.label), l1.getAttribute(Attribute.role)));
			}
		}

		return result;
	}

	/**
	 * Determines the default label
	 * 
	 * @param parameter
	 * @return
	 */
	public Label getLabel(String parameter) {
		return getLabel(parameter, Attribute.label.name());
	}

	/**
	 * Determines the label for the preferred role
	 * 
	 * @param parameter
	 * @param preferredRole
	 * @return
	 */
	public Label getLabel(String parameter, String preferredRole) {
		if (Utils.isEmpty(parameter)) {
			return emptyLabel;
		}

		Collection<Label> labels = getLabels(parameter);
		Label result = emptyLabel;
		if (parameter != null) {
			// find standard label
			Iterator<Label> it = labels.stream().filter(l -> l.getRole().equals(preferredRole)).iterator();
			if (it.hasNext()) {
				result = it.next();
			} else {
				// find shortest entry if no standard label could be found
				for (Label l : labels) {
					if (result == emptyLabel || result.length() > l.length()) {
						result = l;
					}
				}
			}
			// use the parameter if no label information is available
			if (result == emptyLabel && parameter != null) {
				result = new Label(parameter, Attribute.label.name());
			}
		}

		result.setLabel(result.getLabel().replaceAll("\\[Member\\]", ""));
		result.setLabel(result.getLabel().replaceAll("\\[Abstract\\]", ""));
		result.setLabel(result.getLabel().replaceAll("\\[Table\\]", ""));
		result.setLabel(result.getLabel().replaceAll("\\[Axis\\]", ""));
		result.setLabel(result.getLabel().replaceAll("\\[Domain\\]", ""));
		result.setLabel(result.getLabel().replaceAll("\\[Text Block\\]", ""));
		result.setLabel(result.getLabel().replaceAll("\\[Line Items\\]", ""));

		return result;
	}

	/**
	 * Determines the UOM label
	 * 
	 * @param unitRef
	 * @return
	 */
	public String getUOMLabel(String unitRef) {
		String resultStr = ""; 
		if (!Utils.isEmpty(unitRef)) {
			resultStr = unitMap.get(unitRef);
			if (resultStr == null) {
				StringBuffer result = new StringBuffer();

				Fact measure = XBRL.EMPTY;
				Fact unit = xbrl.first(xbrl.find(unitRef, Type.unit));
				if (unit != XBRL.EMPTY) {
					measure = xbrl.first(unit.getFacts(Type.measure));
					if (measure != XBRL.EMPTY) {
						result.append(Utils.lastPath(measure.getAttribute(Type.measure.name())));
					} else {
						Fact numerator = xbrl.first(unit.getFacts(Type.unitNumerator));
						if (numerator != XBRL.EMPTY) {
							measure = xbrl.first(numerator.getFacts(Type.measure));
							result.append(Utils.lastPath(measure.getAttribute(Type.measure.name())));

							Fact deNumerator = xbrl.first(unit.getFacts(Type.unitDenominator));
							measure = xbrl.first(deNumerator.getFacts(Type.measure));
							result.append(" / ");
							result.append(Utils.lastPath(measure.getAttribute(Type.measure.name())));
						} else {
							result.append(Utils.lastPath(unitRef));
						}
					}
				} else {
					result.append(Utils.lastPath(unitRef));
				}
				resultStr = result.toString().toUpperCase();
				unitMap.put(unitRef, resultStr);				
			}
		}

		return resultStr;
	}

	public void close() {
	}

}
