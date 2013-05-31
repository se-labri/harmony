package fr.labri.harmony.source.svnkit;

import java.io.File;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.source.AbstractLocalWorkspace;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.WorkspaceException;


public class SvnKitWorkspace extends AbstractLocalWorkspace {
	
	private SVNClientManager svnClientManager;
	private SVNURL surl;


	public SvnKitWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);
		
		// TODO manage login/pwd here
		svnClientManager = SVNClientManager.newInstance(new DefaultSVNOptions(), "", "");			

		
		// Initialize factories
		FSRepositoryFactory.setup();
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();	
	}

	@Override
	public boolean isInitialized() {
		try {
			svnClientManager.getStatusClient().doStatus(new File(getPath()), false);
			return true;
		} catch (SVNException e) {
			return false;
		}	
		
	}

	@Override
	public void initNewWorkspace() {
		try {
			
		
			surl =  SVNURL.parseURIEncoded(getUrl());
			svnClientManager.createRepository(surl, false);
			svnClientManager.getUpdateClient().doCheckout(surl, new File(getPath()), SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);

		} catch (SVNException e) {
			throw new WorkspaceException(e);
		}
		
	}

	
	@Override
	public void initExistingWorkspace() {
		
		try {
			surl =  SVNURL.parseURIEncoded(getUrl());
		} catch (SVNException e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public void update(Event e) throws WorkspaceException {
		try {
			long rev = Long.parseLong(e.getNativeId());
			svnClientManager.getUpdateClient().doUpdate(new File(getPath()), SVNRevision.create(rev), SVNDepth.INFINITY, true, true);
		} catch (SVNException ex) {
			throw new WorkspaceException(ex);
		}
	}
	
	public SVNURL getSurl() {
		return surl;
	}

	public SVNClientManager getSvnClientManager() {
		return svnClientManager;
	}


}
