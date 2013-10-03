package fr.labri.harmony.source.git.jgit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
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
			git = Git.cloneRepository().setURI(getUrl()).setDirectory(new File((getPath()))).call();
		} catch (Exception e) {
			try {
				FileUtils.deleteDirectory(new File(getPath()));
			} catch (IOException e1) {
				throw new WorkspaceException(e1);
			}
			throw new WorkspaceException(e);
		}
	}

	@Override
	public void initExistingWorkspace() {
		try {
			git = Git.open(new File(getPath()));
			git.pull().call();
		} catch (Exception e) {
			//FIXME : allow harmony to work offline
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