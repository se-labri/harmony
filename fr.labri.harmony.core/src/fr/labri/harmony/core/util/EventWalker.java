package fr.labri.harmony.core.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import fr.labri.harmony.core.model.Event;

/**
 * Uses topological walk of the events DAG to get the events between two given events.
 */
public class EventWalker {
	
	private HashMap<Event, Set<Event>> eventsChildren;
	private Event oldEvent;
	private Event newEvent;

	public EventWalker() {
	}
	
	public ArrayList<Event> getEventsBetween(Event oldEvent, Event newEvent) {
		eventsChildren = new HashMap<>();
		this.oldEvent = oldEvent;
		this.newEvent = newEvent;
		
		HashSet<Event> eventsBefore = dfs();
		HashSet<Event> eventsAfter = reverseDfs();
		ArrayList<Event> events = new ArrayList<>();
		for (Event e : eventsBefore) {
			if (eventsAfter.contains(e)) events.add(e);
		}
		return events;
	}

	
	
	private HashSet<Event> dfs() {
		// Classical DFS structures
		ArrayDeque<Event> eventsToVisit = new ArrayDeque<>();
		HashSet<Event> discoveredEvents = new HashSet<>();
		eventsChildren = new HashMap<>();

		eventsToVisit.push(newEvent);

		while (!eventsToVisit.isEmpty()) {
			Event e = eventsToVisit.pop();
			if (discoveredEvents.add(e) && !e.equals(oldEvent)) { // e was not discovered
				for (Event parentEvent : e.getParents()) {
					MapUtils.addElementToSet(eventsChildren, parentEvent, e);
					eventsToVisit.push(parentEvent);
				}
			}
		}
		return discoveredEvents;
	}

	private HashSet<Event> reverseDfs() {
		// Classical DFS structures
		ArrayDeque<Event> eventsToVisit = new ArrayDeque<>();
		HashSet<Event> discoveredEvents = new HashSet<>();

		eventsToVisit.push(oldEvent);

		while (!eventsToVisit.isEmpty()) {
			Event e = eventsToVisit.pop();
			if (discoveredEvents.add(e) && !e.equals(newEvent)) { // e was not discovered
				Set<Event> childrenEvents = eventsChildren.get(e);
				if (childrenEvents != null) {
					for (Event childEvent : childrenEvents) {
						eventsToVisit.push(childEvent);
					}
				}
			}
		}
		return discoveredEvents;
	}
}
