package ch.pschatzmann.edgar.base;

import java.io.Serializable;

import ch.pschatzmann.edgar.utils.Utils;

/**
 * Label information
 * 
 * @author pschatzmann
 *
 */
public class Label implements Serializable, Comparable<Label> {
	private String label;
	private String role;

	Label(String label, String role) {
		this.label = label.trim();
		this.role = Utils.lastPath(role);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label.trim();
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int length() {
		return this.label.length();
	}

	public String getAttriuteName() {
		return this.getRole();
	}

	@Override
	public int compareTo(Label o) {
		return Integer.valueOf(length()).compareTo(o.length());
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(role);
		sb.append(":");
		sb.append(this.getLabel());
		return sb.toString();
	}

}
