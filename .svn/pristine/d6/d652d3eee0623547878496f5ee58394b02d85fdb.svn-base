package ch.pschatzmann.edgar.parsing;

import org.jsoup.Jsoup;

/**
 * Returns the text value. If the field contains html we convert it to plain
 * text
 * 
 * @author pschatzmann
 *
 */
public class HtmlToTextFormatter implements IValueFormatter {

	@Override
	public String format(String value) {
		value = Jsoup.parse(value).text();
		return value;
	}

}
