package ch.pschatzmann.edgar.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormatSymbols;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * Basic utility function
 * 
 * @author pschatzmann
 *
 */
public class Utils {
	private static final Logger LOG = Logger.getLogger(Utils.class);
	private static DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols.getInstance();
	public static final String NL = "\r\n";
	public static Object DEL = ";";

	/**
	 * Checks if the string is empty (empty string or null)
	 * 
	 * @param str String with or without value
	 * @return true if empty
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	/**
	 * Returns the indicated environment variable or if it does not exist the system
	 * property.
	 * 
	 * @param property     property name
	 * @param defaultValue value if no property is defined
	 * @return
	 */
	public static String getProperty(String property, String defaultValue) {
		String value = System.getenv(property);
		return value == null ? System.getProperty(property, defaultValue) : value;
	}

	/**
	 * Returns the last part of a string after any of the following delimiting
	 * characters (_,#,/,:)
	 * 
	 * @param url
	 * @return
	 */

	public static String lastPath(String url) {
		String result = url;
		if (url != null) {
			if (url.contains("_")) {
				result = url.substring(url.lastIndexOf("_") + 1, url.length());
			} else if (url.contains("#")) {
				result = url.substring(url.lastIndexOf("#") + 1, url.length());
			} else if (url.contains("/")) {
				result = url.substring(url.lastIndexOf("/") + 1, url.length());
			} else if (url.contains(":")) {
				result = url.substring(url.lastIndexOf(":") + 1, url.length());
			}
		} else {
			result = "";
		}
		return result;
	}

	public static String lastPath(String url, String delim) {
		String result = url;
		if (url != null) {
			if (url.contains(delim)) {
				result = url.substring(url.lastIndexOf(delim) + 1, url.length());
			} else {
				result = url;
			}
		} else {
			result = "";
		}
		return result;
	}

	/**
	 * Converts a null string to an empty spaces - otherwise the input is returned
	 * 
	 * @param value
	 * @return
	 */
	public static String notNull(String value) {
		return value == null ? "" : value;
	}

	/**
	 * Converts a null string to an defaultValue - otherwise the input is returned
	 * 
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	public static String notNull(Object value, String defaultValue) {
		return value == null ? defaultValue : value.toString();
	}

	/**
	 * Creates a string by repeating the input string n times
	 * 
	 * @param s input string
	 * @param n
	 * @return
	 */
	public static String repeat(String s, int n) {
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < n; j++) {
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * Executes repeated thread sleep for 1 minutue
	 * 
	 * @throws InterruptedException
	 */
	public static void waitForever() throws InterruptedException {
		while (true) {
			Thread.sleep(1000 * 60);
		}
	}

	/**
	 * Returns true if at least 1 field in any is contained in fields
	 * 
	 * @param fields
	 * @param any
	 * @return
	 */
	public static boolean containsAny(Collection fields, Collection any) {
		for (Object fld : fields) {
			for (Object fld1 : any) {
				if (fld.equals(fld1)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Convert collection to String
	 * 
	 * @param filterValues
	 * @param stringDel
	 * @param delimiter
	 * @return
	 */
	public static String toString(Collection filterValues, String stringDel, String delimiter) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (Object str : filterValues) {
			if (!first)
				sb.append(delimiter);
			first = false;
			sb.append(stringDel);
			sb.append(str.toString());
			sb.append(stringDel);
		}
		return sb.toString();
	}

	/**
	 * Count the occurrences of the findString
	 * 
	 * @param str
	 * @param findStr
	 * @return
	 */
	public static long wordCount(String str, String findStr) {
		int lastIndex = 0;
		int count = 0;

		while (lastIndex != -1) {
			lastIndex = str.indexOf(findStr, lastIndex);

			if (lastIndex != -1) {
				count++;
				lastIndex += findStr.length();
			}
		}

		return count;
	}

	/**
	 * Capitalize the first characters to upper case
	 * 
	 * @param string
	 * @return
	 */
	public static String capitalize(String string) {
		String[] arr = string.split(" ");
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < arr.length; i++) {
			sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1)).append(" ");
		}
		return sb.toString().trim();

	}

	/**
	 * Collect the runtime
	 * 
	 * @param context
	 * @param start
	 * @return
	 */
	public static String runtime(String context, long start) {
		StringBuffer sb = new StringBuffer();
		sb.append(context);
		sb.append(" completed in ");
		sb.append((System.currentTimeMillis() - start) / 1000.0);
		sb.append(" sec");
		return sb.toString();
	}

	/**
	 * Return true if the str is a number
	 * 
	 * @param str
	 * @param resultIfEmpty
	 * @return
	 */
	public static Boolean isNumber(String str, Boolean resultIfEmpty) {
		char localeMinusSign = currentLocaleSymbols.getMinusSign();
		if (Utils.isEmpty(str))
			return resultIfEmpty;
		if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != localeMinusSign)
			return false;

		boolean isDecimalSeparatorFound = false;
		char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();

		for (char c : str.substring(1).toCharArray()) {
			if (!Character.isDigit(c)) {
				if (c == localeDecimalSeparator && !isDecimalSeparatorFound) {
					isDecimalSeparatorFound = true;
					continue;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Makes sure that we do not have any null. If the object is null it is converted to an empty string
	 * @param value
	 * @return string value of value - if null we return an empty string
	 */
	public static String str(Object value) {
		return value == null ? "" : value.toString();
	}

	/**
	 * Converts a URL to a ByteArrayOutputStream
	 * @param url
	 * @return ByteArrayOutputStream
	 * @throws IOException
	 */
	public static ByteArrayOutputStream urlToByteArray(URL url) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			is = url.openStream();
			byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
			int n;

			while ((n = is.read(byteChunk)) > 0) {
				baos.write(byteChunk, 0, n);
			}
		} catch (IOException e) {
			LOG.error(e, e);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return baos;
	}

	/**
	 * Writes the string to a temporary file
	 * @param str
	 * @return
	 * @throws IOException 
	 */
	public static File createTempFile(String str) throws IOException {
		File tmpFile = File.createTempFile("edgar", ".tmp");
		tmpFile.deleteOnExit();

		FileWriter writer = new FileWriter(tmpFile);
		writer.write(str);
		writer.close();
		return tmpFile;

	}
	
	/**
	 * Defines the CSV Delimiter
	 * @param del
	 */
	public static void setCSVDelimiter(String del) {
		DEL = del;
	}

	/**
	 * Escapes a CSV value so that we handle text which contains " and new lines correctly
	 * @param value
	 * @return
	 */
	public static String escapeCSV(String value) {
		if (value==null) return "";
		if (!Utils.isNumber(value, false)) {
			StringBuffer sb = new StringBuffer();
			sb.append("\"");
			String result = value.replaceAll("\"", "\"\"");
			result = result.replaceAll(System.lineSeparator(), " ");
			sb.append(result);
			sb.append("\"");
			return sb.toString();
		} else {
			return value;
		}
	}

	/**
	 * Replaces the escaped html characters whith the proper characters
	 * @param str
	 * @return
	 */
	public static String cleanHtmlEscapes(String str) {
		String result = str.replaceAll("&amp;", "&");
		result = result.replaceAll("&#39;", "'");
		result = result.replaceAll("&quot;", "\"");
		result = result.replaceAll("&lt;", "<");
		result = result.replaceAll("&gt;", ">");
		return result;
	}
	
	/**
	 * Determines the data folder
	 * @return
	 */
	public static String getDestinationFolder() {
		return Utils.getProperty("destinationFolder","/data/SmartEdgar");
	}

	/**
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean equals(String s1, String s2) {
		return str(s1).equals(str(s2));
	}
	
	/**
	 * Converts a String to a Double
	 * @param in
	 * @return
	 */
	public static double toDouble(String in) {
		if (in==null) return 0.0;
		String value = in.trim();
		if (value.endsWith("-")) {
			// move trailing - to start
			value = value.replaceAll("-","");
			if (Utils.isEmpty(value)) {
				value = "0";
			}
			value = "-"+ value;
		}
		return Double.valueOf(value); 
	}

}
