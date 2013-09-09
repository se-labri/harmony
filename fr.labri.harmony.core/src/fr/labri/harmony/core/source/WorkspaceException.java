package fr.labri.harmony.core.source;

public class WorkspaceException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public WorkspaceException(Exception e) {
		super(e);
	}
	
	public WorkspaceException(String message) {
		super(message);
	}

}
