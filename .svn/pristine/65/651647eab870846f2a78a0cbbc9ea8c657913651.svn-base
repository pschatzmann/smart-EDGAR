package ch.pschatzmann.edgar.test;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.common.table.TableFormatterCSV;
import ch.pschatzmann.edgar.reporting.marketshare.MarketShare;
import ch.pschatzmann.edgar.reporting.marketshare.ValuesByCompanyAndYear;

public class TestMarketShare {


	@Test
	public void testSalesByCompany() throws Exception {
		ValuesByCompanyAndYear sales = new ValuesByCompanyAndYear();
		System.out.println(new TableFormatterCSV().format(sales));
	}	
	
	@Test
	public void testMarketShare() throws Exception {
		MarketShare sales = new MarketShare();
		System.out.println(new TableFormatterCSV().format(sales));		
	}	
	
	@Test
	public void testList() throws Exception {
		MarketShare sales = new MarketShare();
		System.out.println(new ArrayList(sales.toList().stream().filter(f -> f.get("2017").doubleValue() > 0.0).collect(Collectors.toList())));		
	}	

	@Test
	public void testApple() throws Exception {
		MarketShare sales = new MarketShare();
		Number result = sales.getMarketShare("0000320193", "2015");
		System.out.println("apple 2015: "+result);
		Assert.assertEquals(96.61, result.doubleValue(),0.1);
	}	


}
