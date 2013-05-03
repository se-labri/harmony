package fr.labri.harmony.source.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import fr.labri.harmony.core.model.*;
import fr.labri.harmony.core.source.*;

public class GitWorkspace extends AbstractLocalWorkspace {
	
	public GitWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);
	}

	@Override
	public void init() {
		super.init();
	}
	
	@Override
	public boolean isInitialized() {
		try {
			ProcessBuilder b = new ProcessBuilder("git", "rev-parse", "--is-inside-work-tree");
			b.directory(new File(getPath()));
			b.redirectErrorStream(true);
			Process p = b.start();
			
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = r.readLine();
			p.waitFor();
			r.close();
			return line.equals("true");
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public void initNewWorkspace() {
		try {
			ProcessBuilder b = new ProcessBuilder("git", "clone", getUrl(), getPath());
			Process p = b.start();
			p.waitFor();
		} catch (Exception e) {
			throw new WorkspaceException(e);
		}
	}
	
	@Override
	public void initExistingWorkspace() {
		try {
			ProcessBuilder b = new ProcessBuilder("git", "pull");
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
