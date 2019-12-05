package ch.pschatzmann.edgar.base;

import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;

import ch.pschatzmann.edgar.base.Fact.DataType;
import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Additional processing for each fact node. We try to reconstruct any missing
 * quarterly values for Value nodes based on the reported 6, 9 and 12 months
 * 
 * @author pschatzmann
 *
 */
public class PostProcessing {
	private static final Logger LOG = Logger.getLogger(PostProcessing.class);
	private XBRL xbrl;

	public PostProcessing(XBRL xbrl) {
		this.xbrl = xbrl;
	}

	public void process(Fact fact) {
		if (fact instanceof FactValue) {
			FactValue factValue = (FactValue) fact;
			if (factValue.getDataType() == DataType.number) {
				String year = factValue.getYear();

				if (factValue.getMonths() == 6 || factValue.getMonths() == 9 || factValue.getMonths() == 12) {
					FactValue search = new FactValue(factValue);
					// value to update
					Collection<Fact> found = xbrl.find(Arrays.asList(year, "3", search.getLabel(), search.getDate()),
							Arrays.asList(Type.value));
					if (found.isEmpty()) {
						try {
							Collection<Fact> fromValueList;
							double value = 0;
							String parameter = search.getParameterName();
							String date = search.getDate();
							String segments = search.getContext().getSegmentDescription();
							// add missing value
							int fromMonths = factValue.getMonths() - 3;
							fromValueList = xbrl.find(Arrays.asList(year, "" + fromMonths, parameter, date, segments),
									Arrays.asList(Type.value));

							if (!fromValueList.isEmpty()) {
								value = getValue(fromValueList, factValue);
								LOG.info(value);
							} else {
								LOG.warn("Calculated Quarterly Value could not be determined: "+parameter+" "+date+" "+fromMonths+" months");
							}

						} catch (Exception ex) {
							LOG.error(ex, ex);
						}
					} else {
						LOG.info(found);
						LOG.info("info complete");
					}

				}
			}

		}

	}

	private double getValue(Collection<Fact> from, FactValue factValue) throws Exception {
		if (from.size() != 1) {
			throw new Exception("The number of found values is " + from.size());
		}
		FactValue fromValue = (FactValue) from.iterator().next();
		return Utils.toDouble(factValue.getValue()) - Utils.toDouble(fromValue.getValue());
	}
}
