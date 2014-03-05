package fr.labri;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import fr.labri.Timer.Aggregator;

public class Timer<K>  implements Iterable<Entry<K, Aggregator>>{
	Map<K, Aggregator> _timers = new LinkedHashMap<>();
	AggregatorFactory _factory;

	public Timer() {
		this(cumulativeSafeFactory());
	}
	
	public Timer(AggregatorFactory defaultFactory) {
		_factory = defaultFactory;
	}
	
	public void profile(K id, Runnable task) {
		long start = System.nanoTime();
		final Aggregator aggregator = getAggregator(id);
		task.run();
		long stop = System.nanoTime();
		aggregator.add(stop - start);
	}

	public TimerToken start(K id) {
		final long start = System.nanoTime();
		final Aggregator aggregator = getAggregator(id);
		return new TimerToken() {
			@Override
			public void stop() {
				long stop = System.nanoTime();
				aggregator.add(stop - start);
			}
		};
	}

	public long get(K key) {
		return toMili(getAggregator(key).total());
	}
	
	public void dump(PrintStream out) {
		for (Entry<K, Aggregator> e : _timers.entrySet()) {
			out.printf("%s: %d\n", e.getKey().toString(), toMili(e.getValue().total()));
		}
	}
	
	private long toMili(long val) {
		return val / (1000*1000);
	}

	private Aggregator getAggregator(K id) {
		return getAggregator(id, _factory);
	}
	
	private Aggregator getAggregator(K id, AggregatorFactory factory) {
		Aggregator a = _timers.get(id);
		if (a == null) {
			a = factory.create();
			_timers.put(id, a);
		}
		return a;
	}

	public void add(String id, AggregatorFactory factory) {
		if (_timers.containsKey(id))
			throw new RuntimeException("Timer '" + id
					+ "' is already existing!");
	}

	public static AggregatorFactory cumulativeFactory() {
		return new AggregatorFactory() {
			public Aggregator create() {
				return new CummulativeAggregator<>(new LinkedList<Long>());
			}
		};
	}
	public static AggregatorFactory simpleFactory() {
		return new AggregatorFactory() {
			public Aggregator create() {
				return new SimpleAggregator();
			}
		};
	}
	public static AggregatorFactory simpleSafeFactory() {
		return new AggregatorFactory() {
			public Aggregator create() {
				return new SimpleSafeAggregator();
			}
		};
	}
	
	public static AggregatorFactory cumulativeSafeFactory() {
		return new AggregatorFactory() {
			public Aggregator create() {
				return new CummulativeAggregator<>(Collections.synchronizedList(new LinkedList<Long>()));
			}
		};
	}
	
	interface Aggregator {
		void add(long time);

		long total();
	}

	public interface AggregatorFactory {
		Aggregator create();
	}
	
	public interface TimerToken {
		void stop();
	}

	static class CummulativeAggregator<L extends List<Long>> implements Aggregator {
		L _values;
		
		public CummulativeAggregator(L list) {
			_values = list;
		}

		@Override
		public void add(long time) {
			_values.add(time);
		}

		@Override
		public long total() {
			long total = 0;
			for (long v : _values)
				total += v;
			return total;
		}
	}
	
	static class SimpleAggregator implements Aggregator {
		long _value;

		@Override
		public void add(long time) {
			_value += time;
		}

		@Override
		public long total() {
			return _value;
		}
	}
	
	static class SimpleSafeAggregator implements Aggregator {
		AtomicLong _value = new AtomicLong();

		@Override
		public void add(long delta) {
			_value.addAndGet(delta);
		}

		@Override
		public long total() {
			return _value.get();
		}
	}
	
	public void main(String argv[]) {
		Timer<String> timer = new Timer<>();
		timer.profile("test", new Runnable() {
			@Override public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		});
	}

	@Override
	public Iterator<Entry<K, Aggregator>> iterator() {
		return _timers.entrySet().iterator();
	}
}