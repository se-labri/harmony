package fr.labri;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;


public class Counters<K> implements Iterable<Entry<K, AtomicLong>> {
	Map<K, AtomicLong> _counters = new LinkedHashMap<>();
	
	public long increment(K key) {
		return getCounter(key).getAndIncrement();
	}

	public long decrement(K key) {
		return getCounter(key).getAndDecrement();
	}
	
	public long add(K key, long delta) {
		return getCounter(key).getAndAdd(delta);
	}
	
	public long get(K key) {
		return getCounter(key).get();
	}
	
	public void dump(PrintStream out) {
		for (Entry<K, AtomicLong> e : _counters.entrySet()) {
			out.printf("%s: %d\n", e.getKey().toString(), e.getValue().get());
		}
	}
	
	private AtomicLong getCounter(K key) {
		AtomicLong l = _counters.get(key);
		if(l == null) {
			l = new AtomicLong();
			_counters.put(key, l);
		}
		return l;
	}

	public Long total() {
		long res = 0;
		for (Entry<K, AtomicLong> e : _counters.entrySet())
			res += e.getValue().get();
		return res;
	}

	@Override
	public Iterator<Entry<K, AtomicLong>> iterator() {
		return _counters.entrySet().iterator();
	}
}
