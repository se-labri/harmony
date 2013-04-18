package fr.labri.harmony.analysis.cloc;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import fr.labri.harmony.core.model.Data;

@Entity
public class ClocEntries implements Data {
	
	@Id @GeneratedValue
	private int id;

	private int elementId;

	private int elementKind;
	
	public int getElementKind() {
		return elementKind;
	}

	public void setElementKind(int elementKind) {
		this.elementKind = elementKind;
	}

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

	public int getElementId() {
		return elementId;
	}

	public void setElementId(int elementId) {
		this.elementId = elementId;
	}
	
	public List<ClocEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<ClocEntry> entries) {
		this.entries = entries;
	}
	
}
