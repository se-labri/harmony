package fr.labri.harmony.analysis.xtic;

import java.util.List;

import fr.labri.harmony.core.model.Event;

public interface IEventFilter {
	public List<Event> filterEvents(List<Event> events);
}
