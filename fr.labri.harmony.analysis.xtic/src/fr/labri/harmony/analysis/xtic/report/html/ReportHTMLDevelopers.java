package fr.labri.harmony.analysis.xtic.report.html;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import fr.labri.Counters;
import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.TimedScore;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.UtilsDate;
import fr.labri.utils.collections.Pair;

public class ReportHTMLDevelopers extends ReportHTML {

	public ReportHTMLDevelopers(String path) {
		super(path);
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
		header(ps);
		ps.println("<br><hr><h1>Developers Evolution of Aptitudes</h1>");
		for(Developer dev : developers) {
			ps.println("<h2>"+dev.getName()+"</h2>");
			ps.println("<h4>"+dev.getDescription()+"</h4>");
			ps.println("<h3>Evolution per Aptitudes</h3>");
			createJsGraphDeveloperAptitudes(ps, dev, aptitudes);
			ps.println("<h3>Evolution per Pattern Aptitudes</h3>");
			createJsGraphDeveloper(ps, dev, aptitudes);
		}
		footer(ps);
		ps.close();
	}

	private void createJsGraphDeveloperAptitudes(PrintStream ps, Developer dev, List<Aptitude> aptitudes) {

		String devName = dev.getName().replaceAll(" ", "\\_").replaceAll("\\.", "\\_").replaceAll("\\'", "\\_").replaceAll("\\-", "\\_");
		devName+="_aptitudes";
		
		//Need to uniformize the dataset. we compute the union of each timestamp found.
		Set<Long> timestamps = new HashSet<Long>();
		for(Aptitude apt : aptitudes) {
			for(PatternAptitude ap : apt.getPatterns()) {
				if(dev.getScore().get(ap) != null) {
					if(!dev.getScore().get(ap).getList().isEmpty()) 
						timestamps.addAll(dev.getScore().get(ap).getTimestamps());
				}
			}
		}

		if(timestamps.isEmpty()) {
			ps.println("<hr>");
			ps.println("<p>No information to display</p>");
			return ;
		}

		printHeaderChart(ps, devName, "", true);

		//On ajoute des valeurs vides aux devs pour les timestamps
		for(Aptitude apt : aptitudes)
			for(PatternAptitude ap : apt.getPatterns())
				if(dev.getScore().get(ap)!=null) {
					if(!dev.getScore().get(ap).getList().isEmpty()) {
						for(long timestamp : timestamps)
							dev.addIfAbsentAptitudePattern(ap, timestamp);
					}
				}

		//Now for each aptitude we have to put the lines
		boolean firstApt = true;
		for(Aptitude apt : aptitudes) {
			if(!firstApt)
				ps.println(",");
			
			StringBuffer data = new StringBuffer();
			Counters<Long> values = new Counters<>();
			for(PatternAptitude ap : apt.getPatterns()) {
				if(dev.getScore().get(ap)!=null) {
					if(!dev.getScore().get(ap).getList().isEmpty()) {
						System.out.println(dev.getScore().get(ap).scoreSortedByTime().size());
						for(TimedScore ts : dev.getScore().get(ap).scoreSortedByTime()) {
							values.add(ts.getTimestamp(),ts.getValue());
						}
					}
				}
			}
			List<Pair<Long,Long>> values_list = new ArrayList<>();
			Iterator<Entry<Long, AtomicLong>> it = values.iterator();
			while(it.hasNext()) {
				Entry<Long, AtomicLong> tmp = it.next();
				values_list.add(new Pair<Long,Long>(tmp.getKey(),tmp.getValue().get()));
			}
			System.out.println(dev.getName()+"\t"+timestamps.size()+"\t"+values_list.size());
			Collections.sort(values_list, new PairComparator());
			boolean start=true;
			long inc = 0;
			for(Pair<Long,Long> value : values_list) {
				inc += value.getSecond();
				if(!start){
					data.append(",");
				}
				data.append(" { x : "+UtilsDate.convertEpoch(value.getFirst())+ ", y : "+inc+" }");
				start=false;
			}
			
			if(!start) {
				ps.println(" {"
						+ "		name: '"+apt.getIdName()+"',"
						+ "data : [ ");
				ps.println(data.toString());
				ps.println(" ], "
						+ "color: palette.color()"
						+ "} ");
			}
			firstApt=false;
		}
		printFooterChart(ps, devName, true);

	}

	private void createJsGraphDeveloper(PrintStream ps, Developer dev, List<Aptitude> aptitudes) {

		String devName = dev.getName().replaceAll(" ", "\\_").replaceAll("\\.", "\\_").replaceAll("\\'", "\\_").replaceAll("\\-", "\\_");

		//Need to uniformize the dataset. we compute the union of each timestamp found.
		Set<Long> timestamps = new HashSet<Long>();
		for(Aptitude apt : aptitudes)
			for(PatternAptitude ap : apt.getPatterns()) {
				if(dev.getScore().get(ap) != null) {
					if(!dev.getScore().get(ap).getList().isEmpty())
						timestamps.addAll(dev.getScore().get(ap).getTimestamps());
				}
			}

		if(timestamps.isEmpty()) {
			ps.println("<hr>");
			ps.println("<p>No information to display</p>");
			return ;
		}

		printHeaderChart(ps, devName, "", true);

		//On ajoute des valeurs vides aux devs pour les timestamps
		for(Aptitude apt : aptitudes)
			for(PatternAptitude ap : apt.getPatterns())
				if(dev.getScore().get(ap)!=null) {
					if(!dev.getScore().get(ap).getList().isEmpty()) {
						for(long timestamp : timestamps)
							dev.addIfAbsentAptitudePattern(ap, timestamp);
					}
				}

		//Now for each developer we have to put the lines
		boolean firstApt = true;
		for(Aptitude apt : aptitudes) {
			for(PatternAptitude ap : apt.getPatterns()) {
				if(dev.getScore().get(ap)!=null) {
					if(!dev.getScore().get(ap).getList().isEmpty()) {
						if(!firstApt)
							ps.println(",");
						ps.println(" {"
								+ "		name: '"+ap.getIdName()+"',"
								+ "data : [ ");
						long inc = 0;
						boolean start=true;
						for(TimedScore ts : dev.getScore().get(ap).scoreSortedByTime()) {
							inc += ts.getValue();
							if(!start)
								ps.println(",");
							ps.println(" { x : "+UtilsDate.convertEpoch(ts.getTimestamp())+ ", y : "+inc+" }");
							start=false;
						}
						ps.println(" ], "
								+ "color: palette.color()"
								+ "} ");
						firstApt=false;
					}
				}
			}
		}
		printFooterChart(ps, devName, true);
		ps.println("</script>");
	}

}

class PairComparator implements Comparator<Pair<Long,Long>> {

	@Override
	public int compare(Pair<Long, Long> arg0, Pair<Long, Long> arg1) {
		return Long.compare(arg0.getFirst(), arg1.getFirst());
	}
	
}
