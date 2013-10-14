package fr.labri.harmony.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Index;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.source.Workspace;

@Entity
public class Source {

	@Id @GeneratedValue
	private int id;

	@Basic @Index
	private String url;

	@OneToMany(cascade=CascadeType.REMOVE,fetch=FetchType.LAZY,mappedBy="source")
	private List<Item> items;

	@OneToMany(cascade=CascadeType.REMOVE,fetch=FetchType.LAZY,mappedBy="source")
	private List<Event> events;

	@OneToMany(cascade=CascadeType.REMOVE,fetch=FetchType.LAZY,mappedBy="source")
	private List<Author> authors;

	@OneToMany(cascade=CascadeType.REMOVE,fetch=FetchType.LAZY,mappedBy="source")
	private List<Action> actions;

	@Transient
	private Workspace workspace;
	
	@Transient
	private SourceConfiguration config;

	public Source() {
		super();
		events = new ArrayList<Event>();
		authors = new ArrayList<Author>();
		items = new ArrayList<Item>();
		actions = new ArrayList<>();
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Workspace getWorkspace() {
		if (workspace == null) throw new RuntimeException("No workspace available for this source");
		return workspace;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	public SourceConfiguration getConfig() {
		return config;
	}

	public void setConfig(SourceConfiguration config) {
		this.config = config;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return "Source: " + url;
	}

}
