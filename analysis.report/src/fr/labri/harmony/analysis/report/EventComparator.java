package fr.labri.harmony.analysis.report;

import java.util.Comparator;

import fr.labri.harmony.core.model.Event;

public class EventComparator implements Comparator<Event> {

	@Override
	public int compare(Event e1, Event e2) {
		return Long.compare(e1.getTimestamp(), e2.getTimestamp());
	}
	

}
