package fr.labri.harmony.core.source;

public class SourceExtractorException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public SourceExtractorException(Exception e) {
		super(e);
	}
	
	public SourceExtractorException(String message) {
		super(message);
	}

}
