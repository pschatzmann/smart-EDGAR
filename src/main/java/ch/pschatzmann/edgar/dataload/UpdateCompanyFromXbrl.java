package ch.pschatzmann.edgar.dataload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.pschatzmann.edgar.base.EdgarCompany;
import ch.pschatzmann.edgar.base.XBRL;
import ch.pschatzmann.edgar.service.EdgarFileService;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Main class for program which updates the company table from the latest XBRL
 * records
 * 
 * @author pschatzmann
 *
 */
public class UpdateCompanyFromXbrl {

	/**
	 * Loads the latest XBRL records for all companies and updates the company table
	 * if the trading symbol is defined.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		EdgarFileService service = new EdgarFileService();
		DownloadProcessorJDBC update = new DownloadProcessorJDBC();

		List<String> list = new ArrayList(service.getCompanies());
		Collections.reverse(list);
		for (String companyNr : list) {
			try {
				List<String> filings = service.getFilings(companyNr, true);
				String lastFiling = filings.get(filings.size() - 1);
				System.out.println(lastFiling);
				XBRL xbrl = service.getXBRL(Arrays.asList(lastFiling), 10000000);
				xbrl.getCompanyInfo();

				String symbol =((EdgarCompany)xbrl.getCompanyInfo()).getTradingSymbolEx();
				if (!Utils.isEmpty(symbol)) {
					System.out.println("->" + symbol);
					update.updateCompanyRecord(xbrl);

				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("--- END ---");
		System.exit(0);
	}

}
