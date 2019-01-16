package ch.pschatzmann.edgar.test;

import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.common.table.FormatException;
import ch.pschatzmann.edgar.base.errors.DataException;
import ch.pschatzmann.edgar.reporting.company.CompanySearch;

public class TestCompanySearch {

	
	@Test
	public void testCompanySearch() throws ClassNotFoundException, DataException, SQLException, FormatException {
		CompanySearch companySearch = new CompanySearch("Apple%");
		Assert.assertTrue(companySearch.size()>0);
		System.out.println(companySearch.toCsv());
	}

	@Test
	public void testCompanySearchList() throws ClassNotFoundException, DataException, SQLException, FormatException {
		CompanySearch companySearch = new CompanySearch("companyName",Arrays.asList("A%","B%"));
		Assert.assertTrue(companySearch.size()>0);
		System.out.println(companySearch.toCsv());
	}
	
	@Test
	public void testCompanySearchAllTicker() throws ClassNotFoundException, DataException, SQLException, FormatException {
		CompanySearch companySearch = new CompanySearch().onlyCompaniesWithTradingSymbol(true);
		Assert.assertFalse(companySearch.toList().isEmpty());
		System.out.println(companySearch.toCsv());
	}
	
}
