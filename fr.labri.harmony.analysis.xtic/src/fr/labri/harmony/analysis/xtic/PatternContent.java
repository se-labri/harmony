package fr.labri.harmony.analysis.xtic;


public class PatternContent {

	private String value;
	private boolean presence;
	private String direction;

	public PatternContent(String value, boolean presence, String direction) {
		super();
		this.value = value;
		this.presence = presence;
		this.direction = direction;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isPresence() {
		return presence;
	}
	public void setPresence(boolean presence) {
		this.presence = presence;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}

	public boolean checkPattern(String oldText, String newText) {
		if((direction.equals("source") || direction.equals("both"))) 
			if(this.presence != oldText.contains(value))
				return false;
		if((direction.equals("target") || direction.equals("both"))) 
			if(this.presence != newText.contains(value))
				return false;
		return true;
	}

}
