package fr.labri.harmony.core.model;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

/**
 * An event is performed on a source, by one or several authors (only one in
 * most cases), and is composed by a set of actions. <br>
 * Typically, in a VCS an event is a commit/changeset.
 * 
 */
@Entity
public class Event extends SourceElement {

	@ManyToMany
	private List<Author> authors;

	@ManyToMany
	private Set<Event> parents;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "event")
	private List<Action> actions;

	@Basic
	private long timestamp;

	@ElementCollection
	private Set<String> tags;

	public Event() {
		super();
		authors = new ArrayList<Author>();
		parents = new HashSet<Event>();
		actions = new ArrayList<Action>();
		tags = new HashSet<String>();
	}

	/**
	 * 
	 * @param source
	 * @param nativeId
	 * @param timestamp
	 *            The timestamp of the commit, in milliseconds
	 * @param parents
	 * @param authors
	 */
	public Event(Source source, String nativeId, long timestamp, Set<Event> parents, List<Author> authors) {
		this();
		this.source = source;
		this.nativeId = nativeId;
		this.timestamp = timestamp;
		this.parents = parents;
		this.authors = authors;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * 
	 * @param timestamp
	 *            The timestamp in milliseconds
	 */
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
		return new ArrayList<>(parents);
	}

	public void setParents(Set<Event> parents) {
		this.parents = parents;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
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

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public String getTimestampAsString() {
		DateFormat f = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		return f.format(new Date(timestamp));
	}

}
