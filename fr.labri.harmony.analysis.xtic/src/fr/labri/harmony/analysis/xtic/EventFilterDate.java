package fr.labri.harmony.analysis.xtic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.labri.harmony.core.model.Event;

public class EventFilterDate implements IEventFilter {

	private  int START_YEAR = 2010;
	
	EventFilterDate(int start) {
		START_YEAR = start;
	}
	
	@Override
	public List<Event> filterEvents(List<Event> events) {
		Calendar cal = Calendar.getInstance();
		List<Event> toReturn = new  ArrayList<Event>();
		for(Event e : events) {
			cal.setTime(new Date(e.getTimestamp()));
			if(cal.get(Calendar.YEAR) >= START_YEAR)
				toReturn.add(e);
		}
		return toReturn;
	}

}
