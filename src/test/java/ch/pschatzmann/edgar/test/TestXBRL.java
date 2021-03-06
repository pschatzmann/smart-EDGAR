package ch.pschatzmann.edgar.test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.edgar.base.Fact;
import ch.pschatzmann.edgar.base.Fact.Attribute;
import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.base.FactValue;
import ch.pschatzmann.edgar.base.XBRL;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Tests for the functionality of the class XBRL
 * 
 * @author pschatzmann
 *
 */
public class TestXBRL {
	@Test
	public void testFile() throws Exception {
		XBRL x = new XBRL(true,true,false);
		File file = new File("./src/test/resources/ste-20170331.xml");
		x.load(file);
		Assert.assertFalse(x.find().isEmpty());
		Assert.assertFalse(x.find(Type.value).isEmpty());

	}

	@Test
	public void testURL() throws Exception {
		XBRL x = new XBRL();
		x.load(new URL(
				"https://www.sec.gov/Archives/edgar/data/1528396/000152839617000020/0001528396-17-000020-xbrl.zip"));
		Assert.assertFalse(x.find(Type.value).isEmpty());
		Assert.assertFalse(x.find().isEmpty());
		Assert.assertFalse("labelArc NOT FOUND",x.find(Arrays.asList(Type.labelArc)).isEmpty());
		Assert.assertFalse("labelLink NOT FOUND",x.find(Arrays.asList(Type.labelLink)).isEmpty());
		Assert.assertFalse("label NOT FOUND",x.find(Arrays.asList(Type.label)).isEmpty());
		Assert.assertFalse("calculationArc NOT FOUND",x.find(Arrays.asList(Type.calculationArc)).isEmpty());
		Assert.assertFalse("calculationLink NOT FOUND",x.find(Arrays.asList(Type.calculationLink)).isEmpty());
		Assert.assertFalse("definition NOT FOUND",x.find(Arrays.asList(Type.definition)).isEmpty());
		Assert.assertFalse("definitionLink NOT FOUND",x.find(Arrays.asList(Type.definitionLink)).isEmpty());
		Assert.assertFalse("presentationLink NOT FOUND",x.find(Arrays.asList(Type.presentationLink)).isEmpty());
		Assert.assertFalse("presentationArc NOT FOUND",x.find(Arrays.asList(Type.presentationArc)).isEmpty());
		
		System.out.println(x.getLabelAPI().getLabel("AvailableForSaleSecuritiesDebtMaturitiesCurrent"));
		//System.out.println(x.getLabelAPI().getLabel("AvailableForSaleSecuritiesDebtMaturitiesCurrent","documentation"));

	}
	


	@Test
	public void testFindParameter() throws Exception {
		XBRL x = new XBRL();
		x.load(new URL(
				"https://www.sec.gov/Archives/edgar/data/320193/000032019319000119/0000320193-19-000119-xbrl.zip"));
		Collection<FactValue> facts = x.findValues("NetIncomeLoss");
		System.out.println(facts);
		Assert.assertEquals("55256000000", facts.iterator().next().getValue());

	}

	
	
	
	@Test
	public void testCSV() throws Exception {
		XBRL x = new XBRL();
		x.load(new URL("https://www.sec.gov/Archives/edgar/data/1528396/000152839617000020/0001528396-17-000020-xbrl.zip"));		
		String csv = x.toValueCSV();
		System.out.println(csv);
		Files.write(Paths.get("./src/test/resources/test.csv"), csv.getBytes());

		Assert.assertTrue(csv.contains("contextRef"));
		Assert.assertTrue(csv.contains("date"));
		Assert.assertTrue(csv.contains("file"));
		Assert.assertTrue(csv.contains("form"));
		Assert.assertTrue(csv.contains("label"));
		Assert.assertTrue(csv.contains("parameterName"));
		Assert.assertTrue(csv.contains("segment"));
		Assert.assertTrue(csv.contains("value"));
		Assert.assertTrue(csv.contains("decimals"));
		Assert.assertTrue(csv.contains("id"));
		Assert.assertTrue(csv.contains("unitRef"));
		Assert.assertTrue(csv.contains("identifier"));
		Assert.assertTrue(csv.contains("sicDescription"));
		Assert.assertTrue(csv.contains("tradingSymbol"));
	}

	@Test
	public void testDirectory() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/test/resources/22872"));
		Assert.assertFalse(x.find().isEmpty());
		Assert.assertFalse(x.find(Type.value).isEmpty());
		Assert.assertFalse("labelArc NOT FOUND",x.find(Arrays.asList(Type.labelArc)).isEmpty());
		Assert.assertFalse("labelLink NOT FOUND",x.find(Arrays.asList(Type.labelLink)).isEmpty());
		Assert.assertFalse("label NOT FOUND",x.find(Arrays.asList(Type.label)).isEmpty());
		Assert.assertFalse("calculationArc NOT FOUND",x.find(Arrays.asList(Type.calculationArc)).isEmpty());
		Assert.assertFalse("calculationLink NOT FOUND",x.find(Arrays.asList(Type.calculationLink)).isEmpty());
		Assert.assertFalse("definition NOT FOUND",x.find(Arrays.asList(Type.definition)).isEmpty());
		Assert.assertFalse("definitionLink NOT FOUND",x.find(Arrays.asList(Type.definitionLink)).isEmpty());
		Assert.assertFalse("presentationLink NOT FOUND",x.find(Arrays.asList(Type.presentationLink)).isEmpty());
		Assert.assertFalse("presentationArc NOT FOUND",x.find(Arrays.asList(Type.presentationArc)).isEmpty());
	}

	@Test
	public void testZipFile() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/test/resources/22872/22872-10-K-20120330.zip"));

		Assert.assertFalse(x.find().isEmpty());
		Assert.assertFalse(x.find(Type.value).isEmpty());
		Assert.assertFalse("labelArc NOT FOUND",x.find(Arrays.asList(Type.labelArc)).isEmpty());
		Assert.assertFalse("labelLink NOT FOUND",x.find(Arrays.asList(Type.labelLink)).isEmpty());
		Assert.assertFalse("label NOT FOUND",x.find(Arrays.asList(Type.label)).isEmpty());
		Assert.assertFalse("calculationArc NOT FOUND",x.find(Arrays.asList(Type.calculationArc)).isEmpty());
		Assert.assertFalse("calculationLink NOT FOUND",x.find(Arrays.asList(Type.calculationLink)).isEmpty());
		Assert.assertFalse("definition NOT FOUND",x.find(Arrays.asList(Type.definition)).isEmpty());
		Assert.assertFalse("definitionLink NOT FOUND",x.find(Arrays.asList(Type.definitionLink)).isEmpty());
		Assert.assertFalse("presentationLink NOT FOUND",x.find(Arrays.asList(Type.presentationLink)).isEmpty());
		Assert.assertFalse("presentationArc NOT FOUND",x.find(Arrays.asList(Type.presentationArc)).isEmpty());

	}

	@Test
	public void testLabel() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/test/resources/22872"));
		Assert.assertFalse("labelArc NOT FOUND",x.find(Arrays.asList(Type.labelArc)).isEmpty());
		Assert.assertFalse("labelLink NOT FOUND",x.find(Arrays.asList(Type.labelLink)).isEmpty());
		Assert.assertFalse("label NOT FOUND",x.find(Arrays.asList(Type.label)).isEmpty());
		
	}
	
	@Test
	public void testCalculation() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/test/resources/22872"));
		Assert.assertFalse("calculationArc NOT FOUND",x.find(Arrays.asList(Type.calculationArc)).isEmpty());
		Assert.assertFalse("calculationLink NOT FOUND",x.find(Arrays.asList(Type.calculationLink)).isEmpty());
	}
	
	@Test
	public void testDefinition() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/test/resources/22872"));
		Assert.assertFalse("calculationArc NOT FOUND",x.find(Arrays.asList(Type.calculationArc)).isEmpty());
		Assert.assertFalse("definition NOT FOUND",x.find(Arrays.asList(Type.definition)).isEmpty());
		Assert.assertFalse("definitionLink NOT FOUND",x.find(Arrays.asList(Type.definitionLink)).isEmpty());
	}
	
	
	@Test
	public void testPresentation() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/test/resources/22872"));
		Assert.assertFalse("presentationLink NOT FOUND",x.find(Arrays.asList(Type.presentationLink)).isEmpty());
		Assert.assertFalse("presentationArc NOT FOUND",x.find(Arrays.asList(Type.presentationArc)).isEmpty());
	}
	
	
	@Test
	public void testDocumentation() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/main/resources/us-gaap-doc-2017-01-31.xml"));
		System.out.println(x.getLabelAPI().getLabel("AccountsReceivableRelatedParties").getLabel());
		Assert.assertEquals("documentation",x.getLabelAPI().getLabel("AccountsReceivableRelatedParties").getRole());		
		Assert.assertNotNull(x.getLabelAPI().getLabel("AccountsReceivableRelatedParties").getLabel());

	}	
	
	@Test
	public void testAllDocumentation() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/test/resources/22872/22872-10-K-20120330.zip"));
		x.load(new File("./src/main/resources/us-gaap-doc-2017-01-31.xml"));
		for (FactValue v  : x.findValues()) {
			Assert.assertNotNull(x.getLabelAPI().getLabel(v.getParameterName()).getLabel());
			Assert.assertNotNull(x.getLabelAPI().getLabel(v.getParameterName(),"documentation").getLabel());
			System.out.println(x.getLabelAPI().getLabel(v.getParameterName(),"documentation").getLabel());
			System.out.println(x.getLabelAPI().getLabel(v.getParameterName()).getLabel());
			System.out.println("-");
		}
	}
	
	@Test
	public void testiXBRL() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/test/resources/html/1104462-10-K-A-20170405.zip"));
		
		FactValue v1 = (FactValue) x.findValues("IncomeTaxDisclosureTextBlock").iterator().next();
		System.out.println(v1.getAttribute("parameterName")+": "+v1.getAttribute("value"));
		
		Collection<FactValue> values =  x.findValues();
		for (FactValue v  : values) {
			Assert.assertNotNull(x.getLabelAPI().getLabel(v.getParameterName()).getLabel());
			System.out.println(v.getAttribute("parameterName")+": "+v.getAttribute("value"));
		}
		
		long segments = values.parallelStream().filter(p -> !Utils.isEmpty( p.getAttribute(Attribute.segment))).count();
		Assert.assertTrue(segments>0l);
		
		long segmentDim = values.parallelStream().filter(p -> !Utils.isEmpty( p.getAttribute(Attribute.segmentDimension))).count();
		Assert.assertTrue(segmentDim>0l);
		
		Assert.assertFalse(values.isEmpty());
	}
	
	@Test
	public void testInvalidHtml() throws Exception {
		XBRL x = new XBRL();
		x.load(new File("./src/test/resources/0000801337-18-000086-xbrl.zip"));		
	}
	
	
}
