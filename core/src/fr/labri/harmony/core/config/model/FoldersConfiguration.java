package fr.labri.harmony.core.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import static fr.labri.harmony.core.config.ConfigProperties.*;

public class FoldersConfiguration {

	String tmpFolder;
	String outFolder;

	public FoldersConfiguration(String tmpFolder, String outFolder) {
		super();
		this.tmpFolder = tmpFolder;
		this.outFolder = outFolder;
	}

	public FoldersConfiguration() {
		tmpFolder = DEFAULT_TMP_FOLDER;
		outFolder = DEFAULT_OUT_FOLDER;
	}

	public String getTmpFolder() {
		return tmpFolder;
	}

	@JsonProperty(TMP)
	public void setTmpFolder(String tmpFolder) {
		this.tmpFolder = tmpFolder;
	}

	public String getOutFolder() {
		return outFolder;
	}

	@JsonProperty(OUT)
	public void setOutFolder(String outFolder) {
		this.outFolder = outFolder;
	}

}
