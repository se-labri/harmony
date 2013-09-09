package fr.labri.harmony.source.hg4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.hg.core.HgCloneCommand;
import org.tmatesoft.hg.core.HgRepoFacade;
import org.tmatesoft.hg.core.HgRepositoryNotFoundException;
import org.tmatesoft.hg.repo.HgLookup;
import org.tmatesoft.hg.repo.HgRemoteRepository;

import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.source.AbstractLocalWorkspace;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.WorkspaceException;

//UPDATE_SITE : http://mercurialeclipse.eclipselabs.org.codespot.com/hg.wiki/update_site/stable

public class Hg4JWorkspace extends AbstractLocalWorkspace {

	private HgRepoFacade repoFacade;

	public Hg4JWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);
		repoFacade = new HgRepoFacade();
	}
	
	public HgRepoFacade getRepoFacade() {
		return repoFacade;
	}

	
	@Override
	public void update(Event e) throws WorkspaceException {
		// TODO Implement
		
	}
	
	@Override
	public boolean isInitialized() {
		// We attempt to create a façade on the local directory. If it fails then it means
		// that the clone of the repo hasn't been done yet
		HgRepoFacade hgTestRepo = new HgRepoFacade();
		try {
			hgTestRepo.initFrom(new File(getPath()));
		} catch (HgRepositoryNotFoundException e) {
			return false;
		}
		return true;
	}
	
	@Override
	//TODO add authentification management : URL url = new URL("http://user:passwd@localhost:8000/hello");
	public void initNewWorkspace() {
		HgRemoteRepository hgRemote;
		try {
			
			hgRemote = new HgLookup().detect(new URL(getUrl()));
			HgCloneCommand cmd = new HgCloneCommand();
			cmd.source(hgRemote);
			cmd.destination(new File(getPath()));
			cmd.execute();
			
			repoFacade.initFrom(new File(getPath()));
			
		}
		catch (HgRepositoryNotFoundException e) {
			 throw new WorkspaceException("Harmony was not able to connect to newly cloned mercurial repository named: "+getUrl());
			}
		catch (Exception e) {
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
			repoFacade.initFrom(new File(getPath()));	
		} catch (HgRepositoryNotFoundException e) {
			// Should not happen as we just checked in the isInitialized() method that repository was accessible
			throw new WorkspaceException("Harmony was not able to connect to the local copy of the mercurial repository named: "+getUrl());
		} 
		
	}

}
