package fr.labri.harmony.analysis.xtic;

import java.util.Comparator;
import fr.labri.harmony.core.model.Event;

public class EventComparator implements Comparator<Event> {

	@Override
	public int compare(Event arg0, Event arg1) {
		return Long.compare(arg0.getTimestamp(), arg1.getTimestamp());
	}
	
}