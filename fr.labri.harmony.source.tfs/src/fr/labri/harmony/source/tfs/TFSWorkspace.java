package fr.labri.harmony.source.tfs;

import java.net.URI;
import java.net.URISyntaxException;
import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissionProfile;
import com.microsoft.tfs.core.clients.versioncontrol.exceptions.WorkspaceNotFoundException;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.DefaultNTCredentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.CredentialsUtils;

import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.source.AbstractLocalWorkspace;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.WorkspaceException;



public class TFSWorkspace extends AbstractLocalWorkspace {

	public static final String CREDENTIALS_NODE_NAME = "credentials";
	public static final String TFS_WORKSPACE_NAME = "HarmonyTmpTFSWorkspace";

	protected String serverPath;
	protected VersionControlClient tfsClient;
	protected Credentials credentials;
	protected Workspace tfsWorkspace;
	
	/**
	 * 
	 * @param sourceExtractor
	 *            The associated Source
	 */
	public TFSWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);

     
        String USERNAME = sourceExtractor.getConfig().getUsername();
		String PASSWORD = sourceExtractor.getConfig().getPassword();

        // In case no username is provided and the current platform supports
        // default credentials, use default credentials
        if ((USERNAME == null || USERNAME.length() == 0) && CredentialsUtils.supportsDefaultCredentials())
        {
            credentials = new DefaultNTCredentials();
        }
        else
        {
            credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD);
        }

        URI httpProxyURI = null;

        if (getUrl() != null && getUrl().length() > 0)
        {
            try
            {
                httpProxyURI = new URI(getUrl());
            }
            catch (URISyntaxException e)
            {
                // Do Nothing
            }
        }
        
        TFSTeamProjectCollection connection = new TFSTeamProjectCollection(httpProxyURI, credentials);   
        connection.authenticate();
        tfsClient = connection.getVersionControlClient();	
		
	}
	
	

	@Override
	public void init() throws WorkspaceException {

		
	}

	public VersionControlClient getTFSClient() {
		return tfsClient;
	}

	@Override
	public void update(Event e) throws WorkspaceException {
		ChangesetVersionSpec version = new ChangesetVersionSpec(Integer.parseInt(e.getNativeId()));
		tfsWorkspace.get(version, GetOptions.OVERWRITE);
	}

	public Changeset[] getChangeset() {
		return tfsClient.queryHistory(serverPath, LatestVersionSpec.INSTANCE, 0, RecursionType.FULL, "", null, LatestVersionSpec.INSTANCE, Integer.MAX_VALUE, true, false, false, true);
	}

	@Override
	public void clean() throws WorkspaceException {
		tfsClient.deleteWorkspace(tfsWorkspace);
		super.clean();
	}


	@Override
	public boolean isInitialized() {
		// TODO Manage existing clone
		return false;
	}

	@Override
	public void initNewWorkspace() {
		try {
			tfsWorkspace = tfsClient.getRepositoryWorkspace(TFS_WORKSPACE_NAME, sourceExtractor.getConfig().getUsername());
		} catch (WorkspaceNotFoundException e) {
			try {

				tfsWorkspace = tfsClient.createWorkspace(null, TFS_WORKSPACE_NAME, "Harmony temporary workspace", WorkspaceLocation.SERVER, null, WorkspacePermissionProfile.getPrivateProfile());

				// Map the workspace
				WorkingFolder workingFolder = new WorkingFolder(serverPath, path);
				tfsWorkspace.createWorkingFolder(workingFolder);

			} catch (Exception e1) {
				throw new WorkspaceException(e1);
			}
		}
	
	}

	@Override
	public void initExistingWorkspace() {
		// TODO Manage existing clone
		
	}

}
