package ch.pschatzmann.edgar.table;

import java.util.Comparator;

/**
 * Used for sorting the rows and columns
 * 
 * @author pschatzmann
 *
 */
public class KeyComparator implements Comparator<Key> {
	@Override
	public int compare(Key o1, Key o2) {
		int result = ((Integer) o1.size()).compareTo(o2.size());
		if (result == 0) {
			for (int j = 0; j < o1.size(); j++) {
				result = o1.get(j).compareTo(o2.get(j));
				if (result != 0) {
					return result;
				}
			}
		}
		return 0;
	}

}

