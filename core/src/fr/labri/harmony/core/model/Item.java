package fr.labri.harmony.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Item extends SourceElement {

	@Id @GeneratedValue
	private int id;
	
	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="item")
	private List<Action> actions;

	public Item() {
		super();
		this.actions = new ArrayList<Action>();
	}
	
	public Item(Source source, String path) {
		super();
		setSource(source);
		setNativeId(path);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}
	
	public Set<Author> getAuthors() {
		Set<Author> authors = new HashSet<>();
		for (Action ac: getActions()) authors.addAll(ac.getEvent().getAuthors());
		return authors;
	}
	
}
