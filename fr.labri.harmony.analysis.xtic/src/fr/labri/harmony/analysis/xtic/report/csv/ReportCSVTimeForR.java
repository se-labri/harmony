package fr.labri.harmony.analysis.xtic.report.csv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.TimedScore;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.Report;
import fr.labri.harmony.core.model.Source;

public class ReportCSVTimeForR extends Report {

	public ReportCSVTimeForR(String path) {
		super(path);
	}
	
	class DayComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			SimpleDateFormat dateF = new SimpleDateFormat("dd-MM-yyyy");
			try {
				return dateF.parse(o1).compareTo(dateF.parse(o2));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
		
	}
	
	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {

		List<String> devs = new ArrayList<String>();
		String line = "";
		for(Developer dev : developers) {
			line += ";"+dev.getName();
			devs.add(dev.getName());
		}
		ps.println("Date"+line);

		SimpleDateFormat dateF = new SimpleDateFormat("dd-MM-yyyy");
		Set<String> timestamps = new HashSet<String>();
		for(Aptitude apt : aptitudes) 
			for(PatternAptitude ap : apt.getPatterns()) {
				if(ap.getIdName().equalsIgnoreCase("ruby")==false)
					continue;
				for(Developer dev : developers) 
					if(dev.getScore().get(ap) != null) 
						if(!dev.getScore().get(ap).getList().isEmpty()) 
							for(long ts : dev.getScore().get(ap).getTimestamps() )
								timestamps.add(dateF.format(new Date(ts)));
			}
		
		List<String> days = new ArrayList<String>(timestamps);
		Collections.sort(days, new DayComparator());

		//for each timestamp
		for(String day : days) {
			Map<String, Long> scores = new HashMap<String, Long>();
			for(Aptitude apt : aptitudes) {
				for(PatternAptitude ap : apt.getPatterns()) {
					if(ap.getIdName().equalsIgnoreCase("ruby")==false)
						continue;
					for(Developer dev : developers)  {
						long score_dev = 0L;
						if(dev.getScore().get(ap) != null) 
							if(!dev.getScore().get(ap).getList().isEmpty()) {
								for(TimedScore ts : dev.getScore().get(ap).getList()) {
									try {
										if(ts.getTimestamp() <= dateF.parse(day).getTime() ||
												dateF.format(new Date(ts.getTimestamp())).equals(day)) {
											score_dev+=ts.getValue();
										}
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						scores.put(dev.getName(), score_dev);
					}
				}
			}
			String line_dev = "";
			for(String dev : devs)
				line_dev+=";"+(scores.containsKey(dev) ?  scores.get(dev) : 0);
			ps.println(day+line_dev);
		}

		ps.close();
	}

	@Override
	public void printReport(List<Source> sources, List<Aptitude> aptitudes,
			List<Developer> developers) {
		// TODO Auto-generated method stub

	}
}

