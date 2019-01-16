package ch.pschatzmann.edgar.test;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import ch.pschatzmann.edgar.dataload.DownloadProcessorJDBC;

public class TestDatabase {
	@Test
	public void test() throws Exception {
		DownloadProcessorJDBC db = new DownloadProcessorJDBC();
		db.loadToDatabase(new File("src/test/resources/22872/22872-10-K-20120330.zip").toURI().toURL(),true);
		db.close();
	}
	
}
