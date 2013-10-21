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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.eclipse.persistence.annotations.Index;

/**
 * Superclass of the elements contained in a {@link Source}. <br>
 * It mainly provides the common fields of these elements, such as the native id of the element and tne metadata associated to it.
 *
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class SourceElement implements HarmonyModelElement {
	
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
	@Lob
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nativeId == null) ? 0 : nativeId.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SourceElement other = (SourceElement) obj;
		if (nativeId == null) {
			if (other.nativeId != null) return false;
		} else if (!nativeId.equals(other.nativeId)) return false;
		if (source == null) {
			if (other.source != null) return false;
		} else if (!source.equals(other.source)) return false;
		return true;
	}	
	
}
