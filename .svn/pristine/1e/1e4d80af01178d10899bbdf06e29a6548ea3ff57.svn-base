package ch.pschatzmann.edgar.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The SAX parser closes the input stream. This prevents the loading from zip
 * files. We prevent the automatic closing and must close the streams ourself!
 * 
 * @author pschatzmann
 *
 */
public class WontCloseBufferedInputStream extends BufferedInputStream {
	public WontCloseBufferedInputStream(InputStream in) {
		super(in);
	}

	@Override
	public void close() {
		// Do nothing.
	}

	/**
	 * Close method which should be called externally
	 * @throws IOException
	 */
	public void reallyClose() throws IOException {
		super.close();
	}
}