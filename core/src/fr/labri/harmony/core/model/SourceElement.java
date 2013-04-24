package fr.labri.harmony.core.model;

import javax.persistence.Basic;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.eclipse.persistence.annotations.Index;

@MappedSuperclass
public abstract class SourceElement {

	@ManyToOne
	@JoinColumn(nullable=false,name="sourceId")
	protected Source source;
	
	@Basic 
	@Index
	protected String nativeId;
	
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
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ", nativeId: " + nativeId;
	}
	
}
