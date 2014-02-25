package fr.labri.harmony.core.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Threads Configuration
 */
public class SchedulerConfiguration {
	private int numberOfThreads = 1;

	// Global timeout in second
	private int globalTimeOut = 108;

	public SchedulerConfiguration() {
	}

	public SchedulerConfiguration(int numberOfThreads, int globalTimeOut) {
		this(numberOfThreads);
		this.globalTimeOut = globalTimeOut;
	}

	public SchedulerConfiguration(int numberOfThreads) {
		super();

		if (numberOfThreads > 0) {
			this.numberOfThreads = numberOfThreads;
		}
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	@JsonProperty("threads")
	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public int getGlobalTimeOut() {
		return globalTimeOut;
	}

	@JsonProperty("timeout")
	public void setGlobalTimeOut(int globalTimeOut) {
		this.globalTimeOut = globalTimeOut;
	}

}
