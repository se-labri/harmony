package fr.labri.harmony.core.model;

import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

@Entity
public class Event extends SourceElement {

	@Id
	@GeneratedValue
	private int id;

	@ManyToMany
	private List<Author> authors;

	@ManyToMany
	private List<Event> parents;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "event")
	private List<Action> actions;

	@Basic
	private long timestamp;

	public Event() {
		super();
		authors = new ArrayList<Author>();
		parents = new ArrayList<Event>();
		actions = new ArrayList<Action>();
	}

	public Event(Source source, String nativeId, long timestamp, List<Event> parents, List<Author> authors) {
		this.source = source;
		this.nativeId = nativeId;
		this.timestamp = timestamp;
		this.parents = parents;
		this.authors = authors;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	public List<Event> getParents() {
		return parents;
	}

	public void setParents(List<Event> parents) {
		this.parents = parents;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * 
	 * @param parent
	 * @return The set of action having the given parent event
	 */
	public Set<Action> getActions(Event parent) {
		if (parent == null) throw new IllegalArgumentException("null parent");
		Set<Action> result = new HashSet<>();
		for (Action a : actions) {
			if (a.getParentEvent() != null && a.getParentEvent().equals(parent)) result.add(a);
		}
		return result;
	}

	public String getTimestampAsString() {
		DateFormat f = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		return f.format(new Date(timestamp));
	}

	/*
	 * public String getTimestampAsString() { DateFormat f = new UTCSimpleDateFormat("y M d H"); return f.format(new Date(timestamp)); }
	 */

}
