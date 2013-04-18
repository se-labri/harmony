package fr.labri.harmony.core.source;

import fr.labri.utils.file.FileUtils;


public abstract class AbstractLocalWorkspace extends AbstractWorkspace {

	protected String path;
	
	public void init() throws WorkspaceException {
		path = FileUtils.createTmpFolder("workspace", getTmpPath());
		LOGGER.info("Created folder for local workspace in: " + getPath());
	}

	@Override
	public void clean() throws WorkspaceException {
		FileUtils.deleteFolder(path);
		LOGGER.info("Deleted folder for local workspace in: " + getPath());
	}

	public String getPath() {
		return path;
	}

}
