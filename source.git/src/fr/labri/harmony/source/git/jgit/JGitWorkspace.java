package fr.labri.harmony.source.git.jgit;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;

import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.source.AbstractLocalWorkspace;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.WorkspaceException;


public class JGitWorkspace extends AbstractLocalWorkspace {

	private Git git;
	
	public JGitWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);
	}
	
	public Git getGit() {
		return git;
	}
	
	@Override
	public void init() {
		super.init();
	}

	@Override
	public boolean isInitialized() {
		try {
			Git.open(new File(getPath()));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public void initNewWorkspace() {
		try {
			git = Git.cloneRepository().setURI(getUrl()).setDirectory(new File((getPath()))).setTimeout(600).call();
		} catch (Exception e) {
			throw new WorkspaceException(e);
		}
	}

	@Override
	public void initExistingWorkspace() {
		try {
			git = Git.open(new File(getPath()));
		} catch (Exception e) {
			throw new WorkspaceException(e);
		}
	}

	@Override
	public void update(Event e) throws WorkspaceException {
		try {
			LOGGER.fine("Updating source: " + getUrl() + " to event: " + e + ".");
			git.reset().setMode(ResetType.HARD).setRef(e.getNativeId()).call();
		} catch (Exception ex) {
			throw new WorkspaceException(ex);
		}
	}
	
}