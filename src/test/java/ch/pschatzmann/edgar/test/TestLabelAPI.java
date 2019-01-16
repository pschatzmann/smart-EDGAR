package ch.pschatzmann.edgar.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import ch.pschatzmann.edgar.base.XBRL;

/**
 * Test for the class LabelAPI 
 * 
 * @author pschatzmann
 *
 */
public class TestLabelAPI {
	private static XBRL xbrl;
	
	@BeforeClass
	public static void setup() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		xbrl = new XBRL();
		xbrl.load(new URL("https://www.sec.gov/Archives/edgar/data/1528396/000152839617000020/0001528396-17-000020-xbrl.zip"));
	}
	
	@Test
	public void testLabel() throws Exception {
		Assert.assertEquals("Capitalized Computer Software, Net", xbrl.getLabelAPI().getLabel("CapitalizedComputerSoftwareNet").getLabel());
	}

	@Test
	public void testLabelUndefinedRole() throws Exception {
		Assert.assertEquals("Capitalized Computer Software, Net", xbrl.getLabelAPI().getLabel("CapitalizedComputerSoftwareNet","NA").getLabel());
	}
	
	@Test
	public void testLabelRole() throws Exception {
		Assert.assertEquals("Capitalized software development costs", xbrl.getLabelAPI().getLabel("CapitalizedComputerSoftwareNet","terseLabel").getLabel());
	}
		
	@Test
	public void testUndefinedRole() throws Exception {
		Assert.assertEquals("XXX", xbrl.getLabelAPI().getLabel("XXX","terseLabel").getLabel());
	}
		
	@Test
	public void testUndefined() throws Exception {
		Assert.assertEquals("XXX", xbrl.getLabelAPI().getLabel("XXX").getLabel());
	}

}
