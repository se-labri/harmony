package fr.labri.harmony.analysis.xtic.report.html;

import java.util.ArrayList;
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

public class ReportHTMLAptCoEvolution extends ReportHTML {

	public ReportHTMLAptCoEvolution(String path) {
		super(path);
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
		header(ps);

		String aptName = "correlations_aptitudes_general";
		ps.println("<br><hr><h1>Co-Evolution of Aptitudes</h1>");
		printHeaderChart(ps, aptName, "", false);

		Map<PatternAptitude, ListTimedScore> score_apt = new HashMap<>(); 
		//Need to uniformize the dataset. we compute the union of each timestamp found.
		List<PatternAptitude> list = new ArrayList<PatternAptitude>();

		for(Aptitude apt : aptitudes)
			for(PatternAptitude ap : apt.getPatterns()) {
				//Increment the list
				list.add(ap);
			}

		//For each couple of aptitudes
		boolean firstApt = false;
		for(int i = 0; i < list.size()-1 ; i++) {
			for(int j = i+1; j < list.size() ; j++) {
				Set<Long> timestamps = new HashSet<Long>();
				List<PatternAptitude> couple = new ArrayList<PatternAptitude>(2);
				score_apt = new HashMap<PatternAptitude, ListTimedScore>();
				couple.add(list.get(i));
				couple.add(list.get(j));
				for(PatternAptitude ap : couple) {
					score_apt.put(ap, new ListTimedScore());
					for(Developer dev : developers) {
						if(dev.getScore().get(ap) != null) 
							if(!dev.getScore().get(ap).getList().isEmpty()) {
								timestamps.addAll(dev.getScore().get(ap).getTimestamps());
								for(TimedScore ts : dev.getScore().get(ap).getList()) {
									score_apt.get(ap).addValues(ts.getTimestamp(), ts.getValue());
								}
							}
					}
				}
				for(PatternAptitude ap : couple) {
					for(long ts : timestamps) {
						score_apt.get(ap).addIfAbsent(ts);
					}
				}
				String entryName = "("+couple.get(0).getIdName()+","+couple.get(1).getIdName()+")";

				if(score_apt.get(couple.get(0)).scoreSortedByTime().isEmpty() || score_apt.get(couple.get(1)).scoreSortedByTime().isEmpty())
					continue;
				if(score_apt.get(couple.get(0)).scoreSortedByTime().size() < 10 || score_apt.get(couple.get(1)).scoreSortedByTime().size() < 10)
					continue;


				long inc_src = 0;
				long inc_tgt = 0;
				boolean start=true;
				List<TimedScore> src = score_apt.get(couple.get(0)).scoreSortedByTime();
				List<TimedScore> tgt = score_apt.get(couple.get(1)).scoreSortedByTime();
				StringBuffer data = new StringBuffer();
				for(int k = 0 ; k < src.size() ; k++) {
					inc_src += src.get(k).getValue();
					inc_tgt += tgt.get(k).getValue();
					if(!start)
						data.append(",");
					data.append(" { x : "+inc_src+ ", y : "+inc_tgt+" }");
					start=false;
				}
				if(inc_src > 0 && inc_tgt > 0) {
					if(!firstApt)
						ps.println(",");
					ps.println(" {"
							+ "		name: '"+entryName+"',"
							+ "data : [ ");
					ps.println(data.toString());
					ps.println(" ], "
							+ "color: palette.color()"
							+ "} ");
					firstApt=false;
				}
			}

		}


		printFooterChart(ps, aptName, false);
		ps.println("</script>");
		footer(ps);
		ps.close();

	}

}
