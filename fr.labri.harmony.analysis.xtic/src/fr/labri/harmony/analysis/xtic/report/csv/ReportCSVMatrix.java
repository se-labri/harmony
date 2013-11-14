package fr.labri.harmony.analysis.xtic.report.csv;

import java.util.ArrayList;
import java.util.List;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.Report;


public class ReportCSVMatrix extends Report {

	public ReportCSVMatrix(String path) {
		super(path);
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {

		List<PatternAptitude> patterns = new ArrayList<PatternAptitude>();
		for(Aptitude a : aptitudes) {
			patterns.addAll(a.getPatterns());
		}

		ps.print("Developer");
		for(Aptitude as : aptitudes) {
			for(PatternAptitude ap : as.getPatterns()) {
				ps.print(";"+ap.getIdName());
			}
			ps.print(";Total");
		}
		ps.println();
		
		for(Developer dev : developers) {
			ps.print(dev.getName());
			for(Aptitude as : aptitudes) {
				double scoreGlobal = 0D;
				for(PatternAptitude pa : as.getPatterns()) {
					if(dev.getScore().get(pa)==null) 
						ps.print(";0");
					else {
						ps.print(";"+dev.getScore().get(pa).getScore());
						scoreGlobal+=dev.getScore().get(pa).getScore();
					}
				}
				ps.print(";"+formatDouble(scoreGlobal));
			}
			ps.println();
		}
		ps.close();
	}
}

