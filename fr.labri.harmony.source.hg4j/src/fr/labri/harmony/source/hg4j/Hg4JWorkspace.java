package fr.labri.harmony.source.hg4j;

import java.io.File;
import java.net.URL;

import org.tmatesoft.hg.core.HgCheckoutCommand;
import org.tmatesoft.hg.core.HgCloneCommand;
import org.tmatesoft.hg.core.HgException;
import org.tmatesoft.hg.core.HgRepoFacade;
import org.tmatesoft.hg.core.HgRepositoryNotFoundException;
import org.tmatesoft.hg.core.Nodeid;
import org.tmatesoft.hg.repo.HgLookup;
import org.tmatesoft.hg.repo.HgRemoteRepository;
import org.tmatesoft.hg.util.CancelledException;

import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.source.AbstractLocalWorkspace;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.WorkspaceException;

//UPDATE_SITE : http://mercurialeclipse.eclipselabs.org.codespot.com/hg.wiki/update_site/stable

public class Hg4JWorkspace extends AbstractLocalWorkspace {

	private HgRepoFacade repoFacade;
	
	private Event lastEvent;

	public Hg4JWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);
		repoFacade = new HgRepoFacade();
	}
	
	public HgRepoFacade getRepoFacade() {
		return repoFacade;
	}

	
	@Override
	public void update(Event e) throws WorkspaceException {
		try {
				Nodeid node = Nodeid.fromAscii(e.getNativeId());
				new HgCheckoutCommand(repoFacade.getRepository()).clean(true).changeset(node).execute();
				lastEvent = e;
		} catch (NumberFormatException | HgException | CancelledException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Override
	public void update(Event e, Item i) throws WorkspaceException {
		if(lastEvent==null || lastEvent!=e)
			update(e);
	}
	
	@Override
	public boolean isInitialized() {
		// We attempt to create a fa�ade on the local directory. If it fails then it means
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
