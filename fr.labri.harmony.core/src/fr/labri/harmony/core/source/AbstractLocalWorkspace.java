package fr.labri.harmony.core.source;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;


public abstract class AbstractLocalWorkspace extends AbstractWorkspace {

	public AbstractLocalWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);
	}

	protected String path;
	
	@Override
	public void init() {
		try {
			URL url = new URL(getUrl());
			File workspaceDir = new File(getTmpPath() + "/" + url.getHost() + url.getPath());
			FileUtils.forceMkdir(workspaceDir);
			path = workspaceDir.getAbsolutePath();

			if (isInitialized()) {
				HarmonyLogger.info("Initializing existing local workspace at: " + getPath());
				initExistingWorkspace();
			} else {
				HarmonyLogger.info("Initializing new local workspace at: " + getPath());
				FileUtils.cleanDirectory(workspaceDir);
				initNewWorkspace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public abstract boolean isInitialized();
	public abstract void initNewWorkspace();
	public abstract void initExistingWorkspace();

	@Override
	public void clean() throws WorkspaceException {
		/*FileUtils.deleteFolder(path);
		LOGGER.info("Deleted folder for local workspace in: " + getPath());*/
	}

	public String getPath() {
		return path;
	}
	
	@Override
	public String getFileContentAfter(Action action) {
		if (action.getKind().equals(ActionKind.Delete)) return null;
		update(action.getEvent(), action.getItem());
		String itemPath = getPath() + "/" + action.getItem().getNativeId();
		try {
			return FileUtils.readFileToString(new File(itemPath));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public String getFileContentBefore(Action action) {
		if (action.getKind().equals(ActionKind.Create)) return null;
		update(action.getParentEvent(), action.getItem());
		String itemPath = getPath() + "/" + action.getItem().getNativeId();
		try {
			return FileUtils.readFileToString(new File(itemPath));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
