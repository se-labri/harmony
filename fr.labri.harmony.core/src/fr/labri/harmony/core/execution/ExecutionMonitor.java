package fr.labri.harmony.core.execution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.dao.HarmonyEntityManagerFactory;
import fr.labri.harmony.core.log.HarmonyLogger;

public class ExecutionMonitor {

	private HarmonyEntityManagerFactory emf;
	private ThreadTimes tt;

	public ExecutionMonitor(Dao dao) {
		emf = dao.getEntityManagerFactory(null);
		tt = new ThreadTimes(100);

	}

	/**
	 * @return the {@link ExecutionReport} id
	 */
	public int initMonitoring() {
		ExecutionReport report = new ExecutionReport();
		report.setExecutionDate(new Date());
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(report);
		em.getTransaction().commit();
		em.close();

		tt.start();
		return report.getId();

	}

	public void stopMonitoring(int executionReportId) {
		tt.interrupt();
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		ExecutionReport report = em.find(ExecutionReport.class, executionReportId);
		report.setTotalSystemTimeMillis(tt.getTotalSystemTime() / 1000000);
		report.setTotalUserTimeMillis(tt.getTotalUserTime() / 1000000);
		em.persist(report);
		em.getTransaction().commit();
	}

	public void addSourceExecutionReport(int executionReportId, SourceExecutionReport sourceExecutionReport) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(sourceExecutionReport);
		ExecutionReport report = em.find(ExecutionReport.class, executionReportId);
		report.add(sourceExecutionReport);
		em.persist(report);
		em.getTransaction().commit();
	}

	public void printExecutionReport(int executionReportId) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		ExecutionReport report = em.find(ExecutionReport.class, executionReportId);
		em.getTransaction().commit();
		Map<String, String> executionErrors = new HashMap<>();
		double totalTimeMilis = 0.0;

		for (SourceExecutionReport sourceExecutionReport : report.getSourceExecutionReports()) {
			if (sourceExecutionReport.isExecutedWithoutError()) {
				totalTimeMilis += sourceExecutionReport.getExecutionTimeMillis();
			} else {
				executionErrors.put(sourceExecutionReport.getSourceUrl(), sourceExecutionReport.getStackTrace());
			}
		}

		HarmonyLogger.info("Total execution time: " + totalTimeMilis / 1000 + "s");
		if (!executionErrors.isEmpty()) {
			HarmonyLogger.info(executionErrors.size() + " sources were analyzed with errors");
			HarmonyLogger.info("Stack Traces are available in the ErrorLog.txt file");
			try {
				FileWriter writer = new FileWriter(new File("ErrorLog.txt"));
				for (Map.Entry<String, String> error : executionErrors.entrySet()) {
					writer.write("Source Url: " + error.getKey() + "\n\n" + error.getValue() + "\n\n\n");
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			HarmonyLogger.info("All analyses finished without error");
		}
	}
}
