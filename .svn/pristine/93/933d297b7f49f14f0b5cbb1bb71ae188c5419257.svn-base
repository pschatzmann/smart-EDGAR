package ch.pschatzmann.edgar.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ch.pschatzmann.edgar.base.Fact.Type;
import ch.pschatzmann.edgar.utils.Utils;

/**
 * Index to find facts by attribute values. All attributes are indexed
 * 
 * @author pschatzmann
 *
 */

public class IndexAPI  implements Serializable {
	private Map<String, List<Fact>> index = new HashMap();

	public void add(Fact fact) {
		add(fact.getType().name(),fact);
		for (String value : fact.getAttributes().values()) {
			add(value, fact);
		}
	}

	public void add(String value, Fact fact) {
		if (!Utils.isEmpty(value)) {
			add1(value, fact);
			if (!value.startsWith("<") && !value.startsWith("#")) {
				String sa[] = value.split("#|_");
				if (sa.length > 1) {
					for (String str : sa) {
						add1(str, fact);
					}
				}
			}
		}
	}

	protected void add1(String value, Fact fact) {
		List<Fact> list = index.get(value);
		if (list == null) {
			list = new ArrayList();
			index.put(value, list);

		}
		if (!list.contains(fact)) {
			list.add(fact);
		}
	}

	/**
	 * Find all facts which contain the indicated string
	 * 
	 * @param value
	 * @return
	 */
	public List<Fact> find(String value) {
		return index.get(value);
	}

	/**
	 * Find all facts which contain the indicated value and are of the correct
	 * type.
	 * 
	 * @param value
	 * @param types
	 * @return
	 */
	public Collection<Fact> find(String value, Collection<Type> types) {
		Collection<Fact> result = new TreeSet();
		Set<String> typesSet = new HashSet(types);
		Collection<Fact> indexResult = index.get(value);
		if (indexResult != null) {
			for (Fact f : indexResult) {
				if (f != null && (typesSet.contains(f.getType()))) {
					result.add(f);
				}
			}
		}
		return result;

	}

	/**
	 * Finds the first instance. Returns null if nothing is matching
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	public Fact find1(String value, Collection<Type> type) {
		return find1(value, type, null);
	}
	
	public Fact find1(String value, Collection<Type> type, Fact defaultValue) {
		Collection<Fact> result = find(value, type);
		return result == null ? defaultValue : result.isEmpty() ? defaultValue : result.iterator().next();
	}

	/**
	 * Clears the index
	 */
	public void clear() {
		index.clear();
	}
	
	public boolean isEmpty() {
		return this.index.isEmpty();
	}
}
