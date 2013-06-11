package fr.labri.harmony.core.config;

public class ConfigProperties {

	public static final String DATABASE = "database";
	
	/**
	 * The use of manage-create-sources is deprecated, but kept for compatibility
	 * Use scheduler instead.
	 */
	public static final String MANAGE_CREATE_SOURCES = "manage-create-sources";
	public static final String SCHEDULER = "scheduler";
	public static final String NUM_THREADS = "threads";
	public static final String TIMEOUT = "timeout";
	
	public static final String OUT = "out";
	public static final String TMP = "tmp";
	public static final String FOLDERS = "folders";
	
	public static final String URL = "url";
	
	public static final String CLASS = "class";
	public static final String OPTIONS = "options";
	
	public static final String ANALYSES = "source-analyses";
	public static final String POST_PROCESSING_ANALYSES = "post-processing-analyses";
	
	public static final String DEFAULT_TMP_FOLDER = "tmp";
	public static final String DEFAULT_OUT_FOLDER = "out";

	public static final int DEFAULT_NUM_THREADS = 1;
	public static final int DEFAULT_TIMEOUT = 100000;
	
}
