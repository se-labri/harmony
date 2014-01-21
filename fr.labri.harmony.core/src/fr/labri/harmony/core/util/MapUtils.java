package fr.labri.harmony.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtils {

	/**
	 * Increments an integer in a map, in a null safe manner.
	 * @param map
	 * @param key
	 * @param valueToAdd
	 */
	public static <K> void addIntegerValue(Map<K, Integer> map, K key, int valueToAdd) {
		Integer i = map.get(key);
		if (i == null) i = 0;
		i += valueToAdd;
		map.put(key, i);
	}
	
	/**
	 * Adds an element to a list in a map, in a null safe manner.
	 * @param map
	 * @param key
	 * @param elementToAdd
	 */
	public static <K,E> void addElementToList(Map<K,List<E>> map, K key, E elementToAdd) {
		List<E> list = map.get(key);
		if (list == null) list = new ArrayList<>();
		list.add(elementToAdd);
		map.put(key, list);
	}
}
