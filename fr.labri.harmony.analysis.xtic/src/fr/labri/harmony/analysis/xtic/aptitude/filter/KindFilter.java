package fr.labri.harmony.analysis.xtic.aptitude.filter;

public class KindFilter extends Filter {
	
	private String value;
	
	public KindFilter(String value, boolean presence, String direction) {
		super(presence, direction);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public int executeFilter(String oldElement, String newElement) {
		if(value.equals("*"))
			return 1;
		String tokens[] = value.split("\\|");
		for(String tk : tokens) {
			if(newElement.equalsIgnoreCase(tk.trim()))
				return 1;
		}
		return 0;
	}

}
