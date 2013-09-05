package fr.labri.harmony.core.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.eclipse.persistence.annotations.Index;


@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class SourceElement {
	
	@Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    protected int id;

	@ManyToOne
	@JoinColumn(nullable=false,name="sourceId")
	protected Source source;
	
	@Basic 
	@Index
	protected String nativeId;
	
	@ElementCollection(fetch=FetchType.LAZY)
	protected Map<String, String> metadata;
	
	
	protected SourceElement(){
		this.metadata = new HashMap<String,String>();
	}
	
	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getNativeId() {
		return nativeId;
	}

	public void setNativeId(String nativeId) {
		this.nativeId = nativeId;
	}
	
	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ", nativeId: " + nativeId;
	}
	
}
