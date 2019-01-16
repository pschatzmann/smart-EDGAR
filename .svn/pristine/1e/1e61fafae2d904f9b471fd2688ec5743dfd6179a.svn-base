package ch.pschatzmann.edgar.dataload;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;

import org.apache.log4j.Logger;

import ch.pschatzmann.edgar.base.Context;

/**
 * Correct the calculated number of months
 * 
 * @author pschatzmann
 *
 */

public class UpdateMonths {
	private static final Logger LOG = Logger.getLogger(DownloadProcessorJDBC.class);
	private static TableFactory tableFactory = new TableFactory();

	public static void main(String[] args) {
		try {
			tableFactory.openConnection();
			Collection<String> companies = tableFactory.getList("select identifier from values group by identifier");
			tableFactory.close();
			for (String company : companies) {
				LOG.info(company);
				update(company);
			}
	        LOG.info("---END---");

		} catch (Exception ex) {
			LOG.error(ex, ex);
		}
	}

	private static void update(String company) throws ClassNotFoundException, SQLException, ParseException {
		Context ctx = new Context(null, null);		
		tableFactory.openConnection();
		
		String sql = "select * from values where datelabel like '% to %' and identifier='"+company+"' and numberofmonths not in ('3','6','9','12')";
		ResultSet rs = tableFactory.getResultSet(sql);
        while (rs.next()) {
            String dateRange = rs.getString("datelabel");
            long monthsReported = Long.parseLong( rs.getString("numberofmonths"));
            LOG.info(dateRange+" "+monthsReported);
            
    			String[] sa = dateRange.split("to");
    			long monthsToBe = ctx.getMonths(ctx.getDate(sa[0]), ctx.getDate(sa[1]));
    			
    			if (monthsToBe!=monthsReported) {
    				LOG.info(" -> "+monthsToBe);	
                
                rs.updateString("numberofmonths", ""+monthsToBe);
                try {
                		rs.updateRow();
                     tableFactory.commit();
                } catch(Exception ex) {
                		LOG.error(ex,ex);
                    tableFactory.rollback();
                }
            }
        }
        LOG.info("---");

	}

}
