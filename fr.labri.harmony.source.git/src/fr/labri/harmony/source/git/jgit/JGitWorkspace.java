package fr.labri.harmony.source.git.jgit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;

import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.source.AbstractLocalWorkspace;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.WorkspaceException;

public class JGitWorkspace extends AbstractLocalWorkspace {

	protected Git git;

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
		initNewWorkspaceByUrl(getUrl());
	}

	protected void initNewWorkspaceByUrl(String url) {
		try {
			ProcessBuilder b = new ProcessBuilder("git", "clone", url, getPath());
			Process p = b.start();
			p.waitFor();
			git = Git.open(new File(getPath()));
		} catch (Exception e) {
			try {
				HarmonyLogger.info("Native git not found, cloning with JGit");
				git = Git.cloneRepository().setURI(url).setDirectory(new File((getPath()))).call();
			} catch (Exception e1) {
				try {
					FileUtils.deleteDirectory(new File(getPath()));
				} catch (IOException e2) {
					throw new WorkspaceException(e2);
				}
				throw new WorkspaceException(e1);
			}
			
			
		}
	}

	@Override
	public void initExistingWorkspace() {
		try {
			// check if index.lock is here, and remove it
			Files.deleteIfExists(Paths.get(getPath(), ".git", "index.lock"));
			git = Git.open(new File(getPath()));
			//git.pull().call();
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
				try {
					git.reset().setMode(ResetType.HARD).setRef(e.getNativeId()).call();
				} catch (GitAPIException e1) {
					throw new WorkspaceException(e1);
				}
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