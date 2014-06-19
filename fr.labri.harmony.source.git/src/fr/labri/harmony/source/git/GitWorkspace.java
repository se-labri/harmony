package fr.labri.harmony.source.git;

import java.io.IOException;
import java.util.List;

import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.source.AbstractLocalWorkspace;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.WorkspaceException;
import fr.labri.harmony.core.util.ProcessExecutor;

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
			List<String> out = new ProcessExecutor("git", "rev-parse", "--is-inside-work-tree").setDirectory(getPath()).run().getOutput();
			return out.get(0).equals("true");
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}

	@Override
	public void initNewWorkspace() {
		try {
			new ProcessExecutor("git", "clone", getUrl(), getPath()).run();
		} catch (IOException | InterruptedException e) {
			throw new WorkspaceException(e);
		}
	}

	@Override
	public void initExistingWorkspace() {
		try {
			new ProcessExecutor("git", "pull").run();
		} catch (IOException | InterruptedException e) {
			throw new WorkspaceException(e);
		}
	}

	@Override
	public void update(Event event) throws WorkspaceException {
		try {
			new ProcessExecutor("git", "reset", "--hard", event.getNativeId()).setDirectory(getPath()).run();
		} catch (IOException | InterruptedException e) {
			throw new WorkspaceException(e);
		}
	}

	@Override
	public String getFileContentAfter(Action arg0) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not Implemented.");
	}

	@Override
	public String getFileContentBefore(Action arg0) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not Implemented.");
	}

}
