package fr.labri.harmony.core.source;

import fr.labri.harmony.core.config.ConfigProperties;

public abstract class AbstractWorkspace implements Workspace {
	
	protected SourceExtractor<?> sourceExtractor;
	
	public AbstractWorkspace() {
	}
	
	@Override
	public void setSourceExtractor(SourceExtractor<?> sourceExtractor) {
		this.sourceExtractor = sourceExtractor;
	}
	
	public String getTmpPath() {
		return sourceExtractor.getConfig().get(ConfigProperties.FOLDERS).get(ConfigProperties.TMP).asText();
	}
	
	public String getOutPath() {
		return sourceExtractor.getConfig().get(ConfigProperties.FOLDERS).get(ConfigProperties.OUT).asText();
	}
	
	public String getUrl() {
		return sourceExtractor.getConfig().get(ConfigProperties.URL).asText();
	}
	
}