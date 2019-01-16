package ch.pschatzmann.edgar.base;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides information about a Filing Company
 * 
 * @author pschatzmann
 *
 */
public interface ICompany extends Serializable {
	public String getCompanyName();
	public String getCompanyNumber();
	public String getIncorporationState();
	public String getLocationState();
	public String getSICCode();
	public String getSICDescription();
	public String getTradingSymbol();

    @JsonIgnore
	default String getSIC() {
		StringBuffer result = new StringBuffer();
		result.append(this.getSICCode());
		result.append(" ");
		result.append(this.getSICDescription());
		return result.toString();
	}

	@JsonIgnore
	default String getFolderName() {
		return getCompanyNumber().replaceFirst("^0+(?!$)", "");
	}
	

}
