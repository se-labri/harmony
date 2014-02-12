package fr.labri.harmony.analysis.cloc;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class ClocEntries {
	
	@Id @GeneratedValue
	private int id;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<ClocEntry> entries;

	public ClocEntries() {
		entries = new ArrayList<>();
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public List<ClocEntry> getEntries() {
		return entries;
	}
	
	public ClocEntry getEntry(String language) {
		for (ClocEntry entry: getEntries()) if (language.equalsIgnoreCase(entry.getLanguage())) return entry;
		return null;
	}

	public void setEntries(List<ClocEntry> entries) {
		this.entries = entries;
	}
	
}
