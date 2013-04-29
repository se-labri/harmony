package fr.labri.harmony.source.git;

import java.io.File;

import fr.labri.harmony.core.model.*;
import fr.labri.harmony.core.source.*;

public class GitWorkspace extends AbstractLocalWorkspace {
	
	public GitWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);
	}

	@Override
	public void init() throws WorkspaceException {
		super.init();
		try {
			ProcessBuilder b = new ProcessBuilder("git", "clone", getUrl(), getPath());
			Process p = b.start();
			p.waitFor();
		} catch (Exception e) {
			throw new WorkspaceException(e);
		}
	}

	@Override
	public void update(Event e) throws WorkspaceException {
		try {
			ProcessBuilder b = new ProcessBuilder("git", "reset", "--hard", e.getNativeId());
			b.directory(new File(getPath()));
			Process p = b.start();
			p.waitFor();
		} catch (Exception ex) {
			throw new WorkspaceException(ex);
		}
	}

}
