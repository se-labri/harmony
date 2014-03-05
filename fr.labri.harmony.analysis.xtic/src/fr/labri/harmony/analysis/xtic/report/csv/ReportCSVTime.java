package fr.labri.harmony.analysis.xtic.report.csv;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.TimedScore;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.Report;
import fr.labri.harmony.core.model.Source;

public class ReportCSVTime extends Report {
	
	public ReportCSVTime(String path) {
		super(path);
	}
	
	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
		
		
		ps.println("Aptitude;PatternAptitude;Developer;Time;Score");
		
		Set<Long> timestamps = new HashSet<Long>();
		for(Aptitude apt : aptitudes) {
			for(PatternAptitude ap : apt.getPatterns()) {
				for(Developer dev : developers) 
					if(dev.getScore().get(ap) != null) 
						if(!dev.getScore().get(ap).getList().isEmpty()) {
							timestamps.addAll(dev.getScore().get(ap).getTimestamps());
							for(TimedScore ts : dev.getScore().get(ap).getList()) {
								ps.println(apt.getIdName()+";"+ap.getIdName()+";"+dev.getName()+";"+ts.getTimestamp()+";"+ts.getValue());
							}
						}
			}
		}

		ps.close();
	}

	@Override
	public void printReport(List<Source> sources, List<Aptitude> aptitudes,
			List<Developer> developers) {
		// TODO Auto-generated method stub
		
	}
}

