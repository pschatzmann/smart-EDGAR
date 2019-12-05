package ch.pschatzmann.edgar.test;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.edgar.dataload.online.CompanyInformation;
import ch.pschatzmann.edgar.dataload.online.FilingEntry;

public class TestOnline {

	@Test
	public void testCompanyInformation() throws Exception {
		CompanyInformation info = new CompanyInformation("AAPL");
		System.out.println(info.getCompanyNumber());
		System.out.println(info.getFiscalYearEnd());
		System.out.println(info.getCompanyName());
		System.out.println(info.getSIC());
		System.out.println(info.getSICDescription());
		System.out.println(info.getLocationState());
		System.out.println(info.getIncorporationState());

		Assert.assertEquals("0000320193",info.getCompanyNumber());
		Assert.assertEquals("0928",info.getFiscalYearEnd());
		Assert.assertEquals("Apple Inc.",info.getCompanyName());
		Assert.assertEquals("3571",info.getSICCode());
		Assert.assertEquals("ELECTRONIC COMPUTERS",info.getSICDescription());
		Assert.assertEquals("CA", info.getLocationState());
		Assert.assertEquals("CA", info.getIncorporationState());
	}
	
	
	@Test
	public void testCompanyInformationList() throws Exception {
		CompanyInformation info = new CompanyInformation("AAPL");
		Assert.assertFalse(info.entries().isEmpty());
		
		System.out.println(info.entries());
		FilingEntry e = info.entries().get(7);
		System.out.println(e.getAccessionNumber());
		System.out.println(e.getFilingDate());
		System.out.println(e.getFilingNumber());
		System.out.println(e.getForm());
		System.out.println(e.getFormName());
		System.out.println(e.getDescription());
		System.out.println(e.getTitle());
		System.out.println(e.getUpdated());
		System.out.println(e.getLink());
		System.out.println(e.getLinksToDocuments());
		
	}

	@Test
	public void testCompany10K() throws Exception {
		CompanyInformation info = new CompanyInformation("AAPL");
		Assert.assertFalse(info.entries("10-K").isEmpty());
		
		System.out.println(info.entries("10-K"));
		FilingEntry e = info.entries("10-K").get(0);
		System.out.println("AccessionNumber:"+e.getAccessionNumber());
		System.out.println("FilingDate:"+e.getFilingDate());
		System.out.println("FilingNumber:"+e.getFilingNumber());
		System.out.println("Form:"+e.getForm());
		System.out.println("FormName:"+e.getFormName());
		System.out.println("Description:"+e.getDescription());
		System.out.println("Title:"+e.getTitle());
		System.out.println("Updated:"+e.getUpdated());
		System.out.println("Link"+e.getLink());
		System.out.println("LinksToDocuments"+e.getLinksToDocuments().collect(Collectors.toList()));
		System.out.println("LinkToXbrl:"+e.getLinkToXbrl());
			
	}

	@Test
	public void testCompany10Fast() throws Exception {
		CompanyInformation info = new CompanyInformation("AAPL","10-K","10-Q");
		Assert.assertFalse(info.entries().isEmpty());
		
		System.out.println(info.entries());
		FilingEntry e = info.entries().get(0);
		System.out.println("AccessionNumber:"+e.getAccessionNumber());
		System.out.println("FilingDate:"+e.getFilingDate());
		System.out.println("FilingNumber:"+e.getFilingNumber());
		System.out.println("Form:"+e.getForm());
		System.out.println("FormName:"+e.getFormName());
		System.out.println("Description:"+e.getDescription());
		System.out.println("Title:"+e.getTitle());
		System.out.println("Updated:"+e.getUpdated());
		System.out.println("Link"+e.getLink());
		System.out.println("LinksToDocuments"+e.getLinksToDocuments().collect(Collectors.toList()));
		System.out.println("LinkToXbrl:"+e.getLinkToXbrl());
			
	}

	@Test
	public void testXBRL() throws Exception {
		CompanyInformation info = new CompanyInformation("AAPL","10-K");
		Assert.assertFalse(info.getXBRL().isEmpty());
		System.out.println(info.entries());
		
		List<String> years = info.entries().stream().filter(e -> e.getXBRL().isPresent()).map(e -> e.getFilingDate().substring(0, 4)).collect(Collectors.toList());

		System.out.println(years);
		Assert.assertTrue(years.contains("2010"));
		
	}

}
