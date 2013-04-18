package fr.labri.harmony.core.model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;


public class Metadata implements Data {
	
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

	private String name;
	
	private String value;
	
	public Metadata() {
	}
	
	public Metadata(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int getElementId() {
		return elementId;
	}

	@Override
	public void setElementId(int sourceId) {
		this.elementId = sourceId;
	}

}
