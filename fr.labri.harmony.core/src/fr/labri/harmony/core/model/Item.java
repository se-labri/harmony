package fr.labri.harmony.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * An Item is an element that can be modified over time, such as a file, or a bug report.
 */
@Entity
public class Item extends SourceElement {

	public static final String METADATA_PATHS = "paths";
	
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


	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}
	
	/**
	 * use Dao.getAuthors(Item) instead
	 */
	@Deprecated
	public Set<Author> getAuthors() {
		Set<Author> authors = new HashSet<>();
		for (Action ac: getActions()) authors.addAll(ac.getEvent().getAuthors());
		return authors;
	}
	
	/**
	 * @return The paths the items has in the source.
	 */
	public List<String> getPaths() {
		String pathsMetadata = getMetadata().get(METADATA_PATHS);
		// if there is no metadata for the paths, we return the nativeId
		if (pathsMetadata == null) return Arrays.asList(new String[]{nativeId});
		return Arrays.asList(pathsMetadata.split("\n"));
	}
	
}
