package fr.labri.harmony.source.git.svntojgit;


import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.ModelPersister;
import fr.labri.harmony.source.git.jgit.JGitSourceExtractor;

public class SvnToJGitSourceExtractor extends JGitSourceExtractor {

	public SvnToJGitSourceExtractor() {
		super();
	}

	public SvnToJGitSourceExtractor(SourceConfiguration config, ModelPersister modelPersister) {
		super(config, modelPersister);
	}


	@Override
	public void initializeWorkspace() {
		workspace = new SvnToJGitWorkspace(this);
		workspace.init();
	}


}
