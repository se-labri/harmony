package fr.labri.harmony.core.execution;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SourceExecutionReport {

	private static final int STACK_TRACE_LENGTH = 1000;
	
	@Id @GeneratedValue
	private int id;
	
	private String sourceUrl;
	
	private boolean executedWithoutError;
	
	@Column(length=STACK_TRACE_LENGTH)
	private String stackTrace;
	
	private long executionTimeMillis;
	
	public SourceExecutionReport() {
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public boolean isExecutedWithoutError() {
		return executedWithoutError;
	}

	public void setExecutedWithoutError(boolean executedWithoutError) {
		this.executedWithoutError = executedWithoutError;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public long getExecutionTimeMillis() {
		return executionTimeMillis;
	}

	public void setExecutionTimeMillis(long executionTimeMillis) {
		this.executionTimeMillis = executionTimeMillis;
	}
	
	public void setException(Throwable e) {
		stackTrace = getStackTrace(e);
		if (stackTrace.length() > STACK_TRACE_LENGTH) stackTrace = stackTrace.substring(0, STACK_TRACE_LENGTH);
	}

	private String getStackTrace(Throwable e) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		return result.toString();
	}

}
