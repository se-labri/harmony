package fr.labri.harmony.analysis.xtic.report.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.ListTimedScore;
import fr.labri.harmony.analysis.xtic.TimedScore;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.PatternAptitudeComparator;
import fr.labri.utils.collections.Pair;

public class ReportHTMLAptInternCoHappening extends ReportHTMLCoHappening {

	int totalEvents = 0;

	public ReportHTMLAptInternCoHappening(String path, int events) {
		this(path);
		this.totalEvents = events;
	}

	public ReportHTMLAptInternCoHappening(String path) {
		super(path);
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
		header(ps);
		
		ps.println("<h1>Co-Happening of Pattern Aptitudes</h1>");
		ps.println("The following reports highlight the co-happening of pattern aptitudes per event.");
		ps.println("The <b>Venn</b> diagram provide an indication of how often two pattern aptitudes are expressed in the same commit");
		
		for(Aptitude apt : aptitudes) {
			Map<PatternAptitude, ListTimedScore> score_apt = new HashMap<>(); 
			//Need to uniformize the dataset. we compute the union of each timestamp found.
			List<PatternAptitude> list_patterns = new ArrayList<PatternAptitude>();
			for(PatternAptitude ap : apt.getPatterns()) {
				score_apt.put(ap, new ListTimedScore());
				for(Developer dev : developers) {
					if(dev.getScore().get(ap) != null) 
						if(!dev.getScore().get(ap).getList().isEmpty()) {
							for(TimedScore ts : dev.getScore().get(ap).getList()) {
								score_apt.get(ap).addValues(ts.getTimestamp(), ts.getValue());
							}
						}
				}
				if(!score_apt.get(ap).getList().isEmpty()) {
					list_patterns.add(ap);
				}
			}

			Collections.sort(list_patterns, new PatternAptitudeComparator());

			
			ps.println("<hr>");
			
			ps.println("<h1>"+apt.getIdName()+"</h1>");
			ps.println(apt.getDescription());
			ps.println("<h2>Visualization</h2>");
			printVennDiagram(ps, list_patterns, score_apt, "venn_global_apt"+apt.getId());

			ps.println("<h2>Details</h2>");
			double nbEvents = (double)this.totalEvents;
			Map<Pair<PatternAptitude, PatternAptitude>, List<Double>> index = new HashMap<Pair<PatternAptitude,PatternAptitude>, List<Double>>();

			computeTable(ps, list_patterns, score_apt, index, true, nbEvents, apt.getId()+"_global");

			//Now we do the same job but for developers
			ps.println("<h2>Developers</h2>");
			ps.println("The following data are specific to each developer. The variations +/- indicated are computed against the general trends of the project");

			for(Developer dev : developers) {
				score_apt = new HashMap<>(); 
				//Need to uniformize the dataset. we compute the union of each timestamp found.
				list_patterns = new ArrayList<PatternAptitude>();
				ps.println("<h3>"+dev.getName()+"</h3>");

				for(PatternAptitude ap : apt.getPatterns()) {
					score_apt.put(ap, new ListTimedScore());
					if(dev.getScore().get(ap) != null) 
						if(!dev.getScore().get(ap).getList().isEmpty()) {
							for(TimedScore ts : dev.getScore().get(ap).getList()) {
								score_apt.get(ap).addValues(ts.getTimestamp(), ts.getValue());
							}
						}
					if(!score_apt.get(ap).getList().isEmpty()) {
						list_patterns.add(ap);
					}
				}	

				Collections.sort(list_patterns, new PatternAptitudeComparator());
				ps.println("<h4>Visualization</h4>");
				printVennDiagram(ps, list_patterns, score_apt, apt.getId()+"_"+dev.getId());

				ps.println("<h4>Details</h4>");
				nbEvents = dev.getNbCommit();
				computeTable(ps, list_patterns, score_apt, index, false, nbEvents, apt.getId()+"_"+dev.getId());
			}
		}
		footer(ps);
		ps.close();
	}

}
