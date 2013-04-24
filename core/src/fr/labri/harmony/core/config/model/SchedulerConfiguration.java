package fr.labri.harmony.core.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SchedulerConfiguration {
	private int numberOfThreads = 1;
	private int cloneTimeOut = 100000;

	public SchedulerConfiguration() {
	};

	public SchedulerConfiguration(int numberOfThreads, int cloneTimeOut) {
		this(numberOfThreads);
		this.cloneTimeOut = cloneTimeOut;
	}

	public SchedulerConfiguration(int numberOfThreads) {
		super();
		
		if(numberOfThreads>0){
			this.numberOfThreads = numberOfThreads;
		}
		else{
			// A negative or null number of threads was requested, the value was left to default (1 thread)
		}
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	@JsonProperty("threads")
	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public int getCloneTimeOut() {
		return cloneTimeOut;
	}

	@JsonProperty("timeout")
	public void setCloneTimeOut(int cloneTimeOut) {
		this.cloneTimeOut = cloneTimeOut;
	}

}
