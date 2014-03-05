package fr.labri.harmony.analysis.xtic.report.html;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.ListTimedScore;
import fr.labri.harmony.analysis.xtic.TimedScore;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.UtilsDate;
import fr.labri.harmony.core.log.HarmonyLogger;

public class ReportHTMLEvolutionAptitudes extends ReportHTML {


	public ReportHTMLEvolutionAptitudes(String path) {
		super(path);
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {

		header(ps);

		String aptName = "summary_aptitudes_general";
		ps.println("<br><hr><h1>Evolution of Aptitudes</h1>");

		Map<Aptitude, ListTimedScore> score_apt_global = new HashMap<>(); 
		Map<PatternAptitude, ListTimedScore> score_apt = new HashMap<>(); 
		//Need to uniformize the dataset. we compute the union of each timestamp found.
		Set<Long> timestamps = new HashSet<Long>();
		for(Aptitude apt : aptitudes) {
			score_apt_global.put(apt, new ListTimedScore());
			for(PatternAptitude ap : apt.getPatterns()) {
				score_apt.put(ap, new ListTimedScore());
				for(Developer dev : developers) 
					if(dev.getScore().get(ap) != null) 
						if(!dev.getScore().get(ap).getList().isEmpty()) {
							timestamps.addAll(dev.getScore().get(ap).getTimestamps());
							for(TimedScore ts : dev.getScore().get(ap).getList()) {
								score_apt.get(ap).addValues(ts.getTimestamp(), ts.getValue());
								score_apt_global.get(apt).addValues(ts.getTimestamp(), ts.getValue());
							}
						}
			}
		}
		//On ajoute des valeurs vides aux devs pour les timestamps
		for(Aptitude apt : aptitudes)
			for(PatternAptitude ap : apt.getPatterns())
				for(Developer dev : developers) 
					if(dev.getScore().get(ap) != null) 
						if(!dev.getScore().get(ap).getList().isEmpty())
							for(long timestamp : timestamps) {
								score_apt.get(ap).addIfAbsent(timestamp);
								score_apt_global.get(apt).addIfAbsent(timestamp);
							}


		printHeaderChart(ps, aptName, "", true);
		boolean firstApt = true;
		for(Aptitude apt : aptitudes) {
			StringBuffer sb = new StringBuffer();
			if(!score_apt_global.get(apt).getList().isEmpty()) {
				if(!firstApt)
					ps.print(",");
				ps.print(" {"
						+ "		name: '"+apt.getIdName()+"',"
						+ "data : [ ");
				long inc = 0;
				boolean start=true;
				for(TimedScore ts : score_apt_global.get(apt).scoreSortedByTime()) {
					inc += ts.getValue();
					if(!start)
						ps.print(",");
					ps.print(" { x : "+UtilsDate.convertEpoch(ts.getTimestamp())+ ", y : "+inc+" }");
					sb.append(inc+"\n");
					start=false;
				}
				ps.println(" ], "
						+ "color: palette.color()"
						+ "} ");
				firstApt=false;
			}
		}
		printFooterChart(ps, aptName, true);

		ps.println("<br><hr><h1>Aptitudes Details</h1>");
		for(Aptitude apt : aptitudes) {
			ps.println("<h2>"+apt.getIdName()+"</h2>");
			ps.println("<p>"+apt.getDescription()+"</p>");
			ps.println("<h3>Evolution of Pattern Aptitudes</h3>");
			createJsAptitudeDetails(ps, apt, score_apt);
			ps.println("<h3>Details for Developers</h3>");
			for(PatternAptitude ap : apt.getPatterns())
				createJsAptitudeDevelopers(ps, ap, developers);
		}

		footer(ps);
		ps.close();
	}

	private void createJsAptitudeDetails(PrintStream ps, Aptitude apt, Map<PatternAptitude, ListTimedScore> score_apt) {
		String aptName = apt.getIdName()+"_details";
		printHeaderChart(ps, aptName, "", true);

	
		Set<Long> timestamps = new HashSet<Long>();
		for(PatternAptitude pa : score_apt.keySet()) 
			if(pa.getAptitude().getId()==apt.getId()) 
				if(!score_apt.get(pa).getList().isEmpty()) 
					for(TimedScore ts : score_apt.get(pa).scoreSortedByTime()) 
						timestamps.add(ts.getTimestamp());

		boolean firstApt = true;
		for(PatternAptitude pa : score_apt.keySet()) {
			if(pa.getAptitude().getId()==apt.getId()) {
				if(!score_apt.get(pa).getList().isEmpty()) {
					if(!firstApt)
						ps.print(",");
					ps.print(" {"
							+ "		name: '"+pa.getIdName()+"',"
							+ "data : [ ");
					long inc = 0;
					boolean start=true;
					for(TimedScore ts : score_apt.get(pa).scoreSortedByTime()) {
						inc += ts.getValue();
						if(!start)
							ps.print(",");
						ps.print(" { x : "+UtilsDate.convertEpoch(ts.getTimestamp())+ ", y : "+inc+" }");
						start=false;
					}

					ps.print(" ], "
							+ "color: palette.color()"
							+ "} ");
					firstApt=false;
				}
			}
		}
		printFooterChart(ps, aptName, true);
	}

	private void createJsAptitudeDevelopers(PrintStream ps, PatternAptitude aptitude, List<Developer> developers) {

		String aptName = aptitude.getIdName()+"_devs";

		//Need to uniformize the dataset. we compute the union of each timestamp found.
		Set<Long> timestamps = new HashSet<Long>();
		for(Developer dev : developers) 
			if(dev.getScore().get(aptitude) != null) 
				if(!dev.getScore().get(aptitude).getList().isEmpty())
					timestamps.addAll(dev.getScore().get(aptitude).getTimestamps());

		if(timestamps.isEmpty()) {
			ps.println("No information to display here");
			return ;
		}

		printHeaderChart(ps, aptName, aptitude.getIdName()+" : "+aptitude.getDescription(), true);

		//On ajoute des valeurs vides aux devs pour les timestamps
		for(Developer dev : developers) 
			if(dev.getScore().get(aptitude) != null) 
				if(!dev.getScore().get(aptitude).getList().isEmpty()) {
					for(long timestamp : timestamps)
						dev.addIfAbsentAptitudePattern(aptitude, timestamp);
				}

		//Now for each developer we have to put the lines
		boolean firstDev = true;
		for(Developer dev : developers) {
			if(dev.getScore().get(aptitude) != null) {
				if(!dev.getScore().get(aptitude).getList().isEmpty()) {
					if(!firstDev)
						ps.println(",");
					ps.println(" {"
							+ "		name: '"+dev.getName()+"',"
							+ "data : [ ");
					long inc = 0;
					boolean start=true;
					for(TimedScore ts : dev.getScore().get(aptitude).scoreSortedByTime()) {
						inc += ts.getValue();
						if(!start)
							ps.println(",");
						ps.println(" { x : "+UtilsDate.convertEpoch(ts.getTimestamp())+ ", y : "+inc+" }");
						start=false;
					}
					ps.println(" ], "
							+ "color: palette.color()"
							+ "} ");
					firstDev=false;
				}
			}
		}
		printFooterChart(ps, aptName, true);

	}

}
