package ch.pschatzmann.edgar.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import ch.pschatzmann.common.utils.Utils;
import ch.pschatzmann.edgar.base.Context;
import ch.pschatzmann.edgar.base.Fact;
import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.base.Presentation;
import ch.pschatzmann.edgar.base.PresentationAPI;
import ch.pschatzmann.edgar.base.XBRL;

/**
 * Tests for the class PresentationAPI
 * 
 * @author pschatzmann
 *
 */
public class TestPresentationAPI {
	private static XBRL xbrl;

	@BeforeClass
	public static void setup() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		xbrl = new XBRL();
//		xbrl.setIgnoreHtml(false);
		xbrl.load(new File("./src/test/resources/22872/22872-10-K-20120330.zip"));
	}

	@Test
	public void testAPI() throws Exception {
		PresentationAPI api = xbrl.getPresentationAPI();
		List<Presentation> lines = api.getChildrenEx();
		System.out.println(lines);
		Assert.assertFalse(lines.isEmpty());
	}

	@Test
	public void testList() throws Exception {
		PresentationAPI api = xbrl.getPresentationAPI();
		for (Presentation pview : api.getChildren()) {
			System.out.println(pview);
			for (Presentation p : pview.getChildrenEx()) {
				System.out.println(Utils.repeat("-", p.getLevel()) + p.getLabel() + " : " + p.getRole());
			}
		}
	}

	@Test
	public void testHtml() throws Exception {
		PresentationAPI api = xbrl.getPresentationAPI();
		api.setSuppressEmptyRows(true);

		StringBuffer sb = new StringBuffer();
		for (Presentation p : api.getChildren()) {
			sb.append(p.toHTML());
		}
		String html = sb.toString();
		Files.write(Paths.get("./src/test/resources/test.html"), html.getBytes());
		Assert.assertNotNull(html);
	}

	@Test
	public void testHtmlSplit() throws Exception {
		PresentationAPI api = xbrl.getPresentationAPI();
		api.setSuppressEmptyRows(false);

		for (Presentation p : api.getChildren()) {
			String name = p.getName();
			StringBuffer sb = new StringBuffer();
			sb.append(p.toHTML());
			String html = sb.toString();
			Files.write(Paths.get("./src/test/resources/" + name + ".html"), html.getBytes());
			Assert.assertNotNull(html);
		}
	}
	
	@Test
	public void testHtmlStatementOfStockholdersEquityAbstract() throws Exception {
		PresentationAPI api = xbrl.getPresentationAPI();
		api.setSuppressEmptyRows(false);

		Presentation p = api.getPresentation("StatementOfStockholdersEquityAbstract");
		String name = p.getName();
		StringBuffer sb = new StringBuffer();
		sb.append(p.toHTML());
		String html = sb.toString();
		Files.write(Paths.get("./src/test/resources/" + name + ".html"), html.getBytes());
	}
	
	
//
//	@Test
//	public void testSegments() throws Exception {
//		PresentationAPI api = xbrl.getPresentationAPI();
//		api.setSuppressEmptyRows(false);
//
//		StringBuffer sb = new StringBuffer();
//		for (Presentation p : api.getChildren()) {
//			System.out.println(p.getName() + ": " + p.getContexts(false) + "/" + p.getContexts(true) + "//"
//					+ p.getContextSegments());
//		}
//
//	}

	@Test
	public void testCompletnessOfAssignments() throws Exception {
		Collection<Fact> facts = new ArrayList(xbrl.find(Type.value));

		PresentationAPI api = xbrl.getPresentationAPI();
		for (Presentation p : api.getChildrenEx()) {
			facts.removeAll(p.getFacts());
		}
		System.out.println(facts);
		Assert.assertTrue(facts.isEmpty());
	}
}
