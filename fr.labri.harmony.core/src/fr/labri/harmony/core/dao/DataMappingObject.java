package fr.labri.harmony.core.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.eclipse.persistence.annotations.Index;

@Entity
public class DataMappingObject {
	
	@Id
	@GeneratedValue
	private int id;

	@Index
	private String databaseName;

	@Index
	private String dataClassSimpleName;

	@Index
	private int dataId;

	@Index
	private int elementId;

	@Index
	private String elementType;

	public DataMappingObject() {
	}
	
	public DataMappingObject(String databaseName, String dataClassSimpleName, int dataId, int elementId, String elementType) {
		super();
		this.databaseName = databaseName;
		this.dataClassSimpleName = dataClassSimpleName;
		this.dataId = dataId;
		this.elementId = elementId;
		this.elementType = elementType;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDataClassSimpleName() {
		return dataClassSimpleName;
	}

	public void setDataClassSimpleName(String dataClassSimpleName) {
		this.dataClassSimpleName = dataClassSimpleName;
	}

	public int getDataId() {
		return dataId;
	}

	public void setDataId(int dataId) {
		this.dataId = dataId;
	}

	public int getElementId() {
		return elementId;
	}

	public void setElementId(int elementId) {
		this.elementId = elementId;
	}

	public String getElementType() {
		return elementType;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

}
