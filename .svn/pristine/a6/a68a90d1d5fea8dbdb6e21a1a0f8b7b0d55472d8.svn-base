package ch.pschatzmann.edgar.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.edgar.base.Fact;
import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.base.IndexAPI;
import ch.pschatzmann.edgar.base.XBRL;

/**
 * Tests for the class IndexAPI
 * @author pschatzmann
 *
 */
public class TestIndexAPI {
		
	@Test
	public void testIndex() throws Exception {
		XBRL x = new XBRL();
		IndexAPI api = x.getIndex();
		Fact f = new Fact(x,Type.value, 0, 0l);
		api.add("a", f);
		api.add("a", f);
		api.add("b", f);
		
		Assert.assertEquals(1, api.find("a").size());
	}
	
	@Test
	public void testIndexValue() throws Exception {
		XBRL x = new XBRL();
		IndexAPI api = x.getIndex();
		Fact f = new Fact(x,Type.value, 0, 0l);
		api.add("a", f);
		api.add("a", f);
		api.add("b", f);
		
		Assert.assertEquals(1, api.find("a", Arrays.asList(Type.value)).size());
		Assert.assertEquals(0, api.find("a", Arrays.asList(Type.appinfo)).size());
	}

	@Test
	public void testIndexNotFoundType() throws Exception {
		XBRL x = new XBRL();
		IndexAPI api = x.getIndex();
		Fact f = new Fact(x,Type.value, 0, 0l);
		api.add("a", f);
		api.add("a", f);
		api.add("b", f);
		
		Assert.assertEquals(0, api.find("a", Arrays.asList(Type.appinfo)).size());
	}
	
	@Test
	public void testIndexNotFoundValue() throws Exception {
		XBRL x = new XBRL();
		IndexAPI api = x.getIndex();
		Fact f = new Fact(x,Type.value, 0, 0l);
		api.add("a", f);
		api.add("a", f);
		api.add("b", f);
		
		Assert.assertEquals(0, api.find("aa", Arrays.asList(Type.value)).size());
	}

	
	@Test
	public void testIndexFirst() throws Exception {
		XBRL x = new XBRL();
		IndexAPI api = x.getIndex();
		Fact f1 = new Fact(x,Type.value, 0, 0l);
		Fact f2 = new Fact(x,Type.value, 0, 0l);
		api.add("a", f1);
		api.add("a", f2);
		api.add("b", f1);
		
		Assert.assertEquals(f1, api.find1("a", Arrays.asList(Type.value)));
	}
}
