package fr.labri.harmony.analysis.xtic.report;


import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.List;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.core.model.Source;

public abstract class Report {
	
	protected PrintStream ps;
	
	public Report(String path) {
		try {
			ps = new PrintStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void printReport(List<Aptitude> aptitudes, List<Developer> developers);
	
	public abstract void printReport(List<Source> sources, List<Aptitude> aptitudes, List<Developer> developers);
	
	protected String formatDouble(double s) {
		return new DecimalFormat("#.##").format(s);
	}
}
