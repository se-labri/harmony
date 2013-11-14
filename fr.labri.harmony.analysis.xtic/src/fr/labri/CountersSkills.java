package fr.labri;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.labri.harmony.analysis.xtic.ListTimedScore;

public class CountersSkills<K> implements Iterable<Entry<K, ListTimedScore>> {
	
	Map<K, ListTimedScore> _counters = new LinkedHashMap<>();

	public boolean add(K key, Long timestamp, Long value) {
		return getCounter(key).addValues(timestamp,value);
	}
	
	public ListTimedScore get(K key) {
		return getCounter(key);
	}
	
	public void dump(PrintStream out) {
		for (Entry<K, ListTimedScore> e : _counters.entrySet()) {
			out.printf("%s: %d\n", e.getKey().toString(), e.getValue().getScore());
		}
	}
	
	private ListTimedScore getCounter(K key) {
		ListTimedScore l = _counters.get(key);
		if(l == null) {
			l = new ListTimedScore();
			_counters.put(key, l);
		}
		return l;
	}

	public Long total() {
		long res = 0;
		for (Entry<K, ListTimedScore> e : _counters.entrySet())
			res += e.getValue().getScore();
		return res;
	}

	@Override
	public Iterator<Entry<K, ListTimedScore>> iterator() {
		return _counters.entrySet().iterator();
	}
}
