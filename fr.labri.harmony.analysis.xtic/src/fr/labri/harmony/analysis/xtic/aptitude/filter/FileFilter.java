package fr.labri.harmony.analysis.xtic.aptitude.filter;


import java.util.regex.Pattern;

public class FileFilter extends Filter {

	private Pattern value;

	public FileFilter(String value, boolean presence, String direction) {
		super(presence, direction);
		this.value = Pattern.compile(value);
	}
	
	public Pattern getValue() {
		return value;
	}
	public void setValue(Pattern value) {
		this.value = value;
	}
	
	public int executeFilter(String oldElement, String newElement) {
		if((direction.equals("source") || direction.equals("both"))) 
			if(oldElement != null && this.presence != this.value.matcher(oldElement.trim()).find())
				return 0;
		if((direction.equals("target") || direction.equals("both"))) 
			if(newElement != null && this.presence != this.value.matcher(newElement.trim()).find())
				return 0;
		return 1;
	}

	
}
