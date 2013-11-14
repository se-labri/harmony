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
import fr.labri.harmony.analysis.xtic.report.AptitudeComparator;
import fr.labri.harmony.analysis.xtic.report.PatternAptitudeComparator;
import fr.labri.utils.collections.Pair;

public class ReportHTMLAptCoHappening extends ReportHTMLCoHappening {

	int totalEvents = 0;

	public ReportHTMLAptCoHappening(String path, int events) {
		this(path);
		this.totalEvents = events;
	}

	public ReportHTMLAptCoHappening(String path) {
		super(path);
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
		header(ps);
		
		Map<Aptitude, ListTimedScore> score_apt_global = new HashMap<>(); 
		//Need to uniformize the dataset. we compute the union of each timestamp found.
		List<Aptitude> list_aptitudes = new ArrayList<Aptitude>();
		List<PatternAptitude> list_patterns = new ArrayList<PatternAptitude>();
		for(Aptitude apt : aptitudes) {
			score_apt_global.put(apt, new ListTimedScore());
			for(PatternAptitude ap : apt.getPatterns()) {
				for(Developer dev : developers) {
					if(dev.getScore().get(ap) != null) 
						if(!dev.getScore().get(ap).getList().isEmpty()) {
							for(TimedScore ts : dev.getScore().get(ap).getList()) {
								score_apt_global.get(apt).addValues(ts.getTimestamp(), ts.getValue());
							}
						}
				}
			}
			if(!score_apt_global.get(apt).getList().isEmpty()) {
				list_aptitudes.add(apt);
			}
		}
		Collections.sort(list_patterns, new PatternAptitudeComparator());
		
		//Summary with visu and details
		ps.println("<h1>Co-Happening of Aptitudes</h1>");
		ps.println("The following reports highlight the co-happening of aptitudes per event.");
		ps.println("The <b>Venn</b> diagram provide an indication of how often two aptitudes are expressed in the same commit");

		ps.println("<h2>Visualization</h2>");
		printVennDiagram(ps, list_aptitudes, score_apt_global, "venn_global");
		
		ps.println("<h2>Details</h2>");
		double nbEvents = (double)this.totalEvents;
		Map<Pair<Aptitude, Aptitude>, List<Double>> index = new HashMap<Pair<Aptitude,Aptitude>, List<Double>>();
		computeTable(ps, list_aptitudes, score_apt_global, index, true, nbEvents, "global");

		//Now we do the same job but for developers
		ps.println("<h1>Developers</h1>");
		ps.println("The following data are specific to each developer. The variations +/- indicated are computed against the general trends of the project");

		for(Developer dev : developers) {
			score_apt_global = new HashMap<>(); 
			//Need to uniformize the dataset. we compute the union of each timestamp found.
			list_aptitudes = new ArrayList<Aptitude>();
			ps.println("<h2>"+dev.getName()+"</h2>");
			for(Aptitude apt : aptitudes) {
				score_apt_global.put(apt, new ListTimedScore());
				for(PatternAptitude ap : apt.getPatterns()) {
					if(dev.getScore().get(ap) != null) 
						if(!dev.getScore().get(ap).getList().isEmpty()) {
							for(TimedScore ts : dev.getScore().get(ap).getList()) {
								score_apt_global.get(apt).addValues(ts.getTimestamp(), ts.getValue());
							}
						}
				}	
				if(!score_apt_global.get(apt).getList().isEmpty()) {
					list_aptitudes.add(apt);
				}
			}
			Collections.sort(list_aptitudes, new AptitudeComparator());
			ps.println("<h3>Vizualization</h3>");
			printVennDiagram(ps, list_aptitudes, score_apt_global, String.valueOf(dev.getId()));
			
			ps.println("<h3>Details</h3>");
			nbEvents = dev.getNbCommit();
			computeTable(ps, list_aptitudes, score_apt_global, index, false, nbEvents, "global_"+dev.getId());
		}
		footer(ps);
		ps.close();
	}

}
