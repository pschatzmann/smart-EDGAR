package ch.pschatzmann.edgar.test;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.edgar.base.EdgarCompany;
/**
 * Tests for the class CompanyInfo
 * @author pschatzmann
 *
 */
public class TestCompanyInfo {
	@Test
	public void test() throws Exception {
		EdgarCompany ci = new EdgarCompany("0000320193");
		Assert.assertEquals("Apple Inc.", ci.getCompanyName());
		Assert.assertEquals("CA", ci.getIncorporationState());
		Assert.assertEquals("CA", ci.getLocationState());
		Assert.assertEquals("3571", ci.getSICCode());
		Assert.assertEquals("ELECTRONIC COMPUTERS", ci.getSICDescription());
		Assert.assertEquals("3571 ELECTRONIC COMPUTERS", ci.getSIC());
//		Assert.assertEquals("AAPL", ci.getTradingSymbol());
	}
	
	@Test
	public void testWoZeros() throws Exception {
		EdgarCompany ci = new EdgarCompany("320193");
		Assert.assertEquals("Apple Inc.", ci.getCompanyName());
		Assert.assertEquals("CA", ci.getIncorporationState());
		Assert.assertEquals("CA", ci.getLocationState());
		Assert.assertEquals("3571", ci.getSICCode());
		Assert.assertEquals("ELECTRONIC COMPUTERS", ci.getSICDescription());
		Assert.assertEquals("3571 ELECTRONIC COMPUTERS", ci.getSIC());
//		Assert.assertEquals("AAPL", ci.getTradingSymbol());
	}


}
