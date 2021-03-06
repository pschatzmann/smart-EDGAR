package ch.pschatzmann.edgar.table.forecast;

/**
 * Individual Numerical Value which is used in the forecasting
 * 
 * @author pschatzmann
 *
 */
public class ForecastValue {
	public int index;
	public int seasonIndex=0;
	public double valueCumulated=0;
	public double value=0;
	public double valueRegression=0;
	public ForecastValue prior=null;
	
	public ForecastValue( int index) {
		this.index = index;
	}
	
	public String toString() {
		return Double.toString(value);
	}
	
}
