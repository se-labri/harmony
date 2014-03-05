package fr.labri.harmony.analysis.xtic.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.FileUtils;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.report.csv.ReportCSVMatrix;
import fr.labri.harmony.analysis.xtic.report.csv.ReportCSVTime;
import fr.labri.harmony.analysis.xtic.report.csv.ReportCSVTimeForR;
import fr.labri.harmony.analysis.xtic.report.html.ReportHTML;
import fr.labri.harmony.analysis.xtic.report.html.ReportHTMLAptCoEvolution;
import fr.labri.harmony.analysis.xtic.report.html.ReportHTMLAptCoHappening;
import fr.labri.harmony.analysis.xtic.report.html.ReportHTMLAptInternCoHappening;
import fr.labri.harmony.analysis.xtic.report.html.ReportHTMLDevelopers;
import fr.labri.harmony.analysis.xtic.report.html.ReportHTMLEvolutionAptitudes;
import fr.labri.harmony.analysis.xtic.report.html.ReportHTMLIndex;
import fr.labri.harmony.analysis.xtic.report.html.ReportHTMLMatrix;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Source;

public class GenerateReport {

	public static void createReport(File directory, Source source, List<Aptitude> aptitudes, List<Developer> developers,  Dao dao) {
		try {
			File html = new File(directory+"/html");
			FileUtils.deleteDirectory(html);
			html.mkdir();
			File csv = new File(directory+"/csv");
			FileUtils.deleteDirectory(csv);
			csv.mkdir();

			List<Report> reports = new ArrayList<Report>();
			//CSV
			reports.add(new ReportCSVMatrix(csv+"/summary.csv"));
			reports.add(new ReportCSVTime(csv+"/summary_evolution.csv"));
			reports.add(new ReportCSVTimeForR(csv+"/summary_evolution_ruby.csv"));
			//HTML
			ReportHTML.initHTML(html);
			reports.add(new ReportHTMLIndex(source, html+"/index.html"));
			reports.add(new ReportHTMLDevelopers(html+"/developers_evolution.html"));
			reports.add(new ReportHTMLEvolutionAptitudes(html+"/aptitudes_evolution.html"));
			reports.add(new ReportHTMLMatrix(html+"/matrix.html"));
			reports.add(new ReportHTMLAptCoEvolution(html+"/aptitudes_co_evolution.html"));
			reports.add(new ReportHTMLAptCoHappening(html+"/aptitudes_co_happening.html", source.getEvents().size()));
			reports.add(new ReportHTMLAptInternCoHappening(html+"/aptitudes_co_happening_intern.html", source.getEvents().size()));
			
			Collections.sort(aptitudes, new AptitudeComparator());
			for(Report r : reports) {
				HarmonyLogger.info("Reporting - "+r.getClass().getSimpleName());
				developers = resetDevs(developers, dao, source);
				r.printReport(aptitudes, developers);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			return;
		}

	}
	

	
	private static List<Developer> resetDevs(List<Developer> developers, Dao dao, Source source) {
		developers = dao.getData("xtic", Developer.class, source);
		UtilsDeveloper.cleanDevelopers(developers);
		Collections.sort(developers, new DeveloperNameSorter());
		return developers;
	}


}
