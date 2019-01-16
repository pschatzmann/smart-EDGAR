package ch.pschatzmann.edgar.dataload;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import ch.pschatzmann.edgar.base.EdgarCompany;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Update the company information in the sql database
 * 
 * @author pschatzmann
 *
 */

public class UpdateCompanyInfoJDBC {
	private static final Logger LOG = Logger.getLogger(DownloadProcessorJDBC.class);
	private static TableFactory tableFactory = new TableFactory();

	public static void main(String[] args) {
		try {
			tableFactory.openConnection();
			update();

		} catch (Exception ex) {
			LOG.error(ex, ex);
		}
	}

	private static void update() throws ClassNotFoundException, SQLException {
		String where = Utils.getProperty("where", "sicdescription like '%amp%'");

		
		for (String cik : tableFactory
				.getList("select identifier from company where "+where)) {
			EdgarCompany ci = new EdgarCompany(cik);
			String sql = "update company set companyname='%cn', siccode='%sic', sicdescription='%sicdesc', incorporation='%inc', location='%loc' where identifier = '%id' ";
			sql = sql.replace("%id", ci.getCompanyNumber());
			sql = sql.replace("%cn", ci.getCompanyName());
			sql = sql.replace("%sicdesc", ci.getSICDescription());
			sql = sql.replace("%sic", ci.getSICCode());
			sql = sql.replace("%inc", ci.getIncorporationState());
			sql = sql.replace("%loc", ci.getLocationState());
			LOG.info(sql);
			tableFactory.execute(sql);
			tableFactory.commit();
		}
	}

}
