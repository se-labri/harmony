package fr.labri.harmony.analysis.xtic.aptitude.filter;

public abstract class Filter {
	
	protected boolean presence;
	protected String direction;
	
	public Filter(boolean _presence, String _direction) {
		this.presence = _presence;
		this.direction = _direction;
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
	
	public abstract int executeFilter(String oldElement, String newElement);
}
