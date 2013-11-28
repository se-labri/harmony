package fr.labri.harmony.source.git.jgit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
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
			throw new WorkspaceException(e);
		}
	}

	@Override
	public void update(Event e) throws WorkspaceException {
		try {
			git.checkout().setStartPoint(e.getNativeId()).addPath(".").setForce(true).call();
			git.clean().setCleanDirectories(true).setIgnore(true).call();
		} catch (Exception ex) {
			throw new WorkspaceException(ex);
		}
	}
	
	@Override
	public void update(Event e, Item item) throws WorkspaceException {
		try {
			git.checkout().setStartPoint(e.getNativeId()).addPath(item.getNativeId()).setForce(true).call();
		}
		catch (Exception ex) {
			throw new WorkspaceException(ex);
		}
	}

}