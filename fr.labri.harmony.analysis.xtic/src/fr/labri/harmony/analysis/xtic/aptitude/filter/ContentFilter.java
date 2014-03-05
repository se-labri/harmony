package fr.labri.harmony.analysis.xtic.aptitude.filter;


public class ContentFilter extends Filter {

	private String value;

	public ContentFilter(String value, boolean presence, String direction) {
		super(presence, direction);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public int executeFilter(String oldElement, String newElement) {
		if((direction.equals("source") || direction.equals("both"))) 
			if(oldElement != null && this.presence != oldElement.contains(value))
				return 0;
		if((direction.equals("target") || direction.equals("both"))) 
			if(newElement != null && this.presence != newElement.contains(value))
				return 0;
		return 1;
	}

}
