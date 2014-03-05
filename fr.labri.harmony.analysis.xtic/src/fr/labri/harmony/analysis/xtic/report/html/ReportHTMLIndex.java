package fr.labri.harmony.analysis.xtic.report.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.EventComparator;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.UtilsDate;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class ReportHTMLIndex extends ReportHTML {

	Source source;

	public ReportHTMLIndex(Source src, String path) {
		this(path);
		this.source = src;
	}

	public ReportHTMLIndex(String path) {
		super(path);
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
		header(ps);
		ps.println("<h1>XTic Report</h1><hr>");
		
		ps.println("<h2>Repository</h2>");
		
		ps.println("<ul><li>URL : "+source.getUrl());
		ps.println("</li><li>Commits : "+ source.getEvents().size());
		ps.println("</li><li>Developers : "+ developers.size());
		List<Event> events = new ArrayList<>(source.getEvents());
		Collections.sort(events, new EventComparator());
		ps.println("</li><li>Start : "+ UtilsDate.format(events.get(0).getTimestamp()));
		ps.println("</li><li>End : "+ UtilsDate.format(events.get(events.size()-1).getTimestamp()));
		ps.println("</li></ul>");
		
		ps.println("<h2>Aptitudes</h2>");
		ps.println("<ul>");
		for(Aptitude aptitude : aptitudes) {
			ps.println("<li><b>"+aptitude.getIdName()+"</b> : "+aptitude.getDescription());
			ps.println("<ul>");
			for(PatternAptitude pa : aptitude.getPatterns()) {
				ps.println("<li>"+pa.getIdName()+" : "+pa.getDescription()+"</li>");
			}
			ps.println("</ul>");
			ps.println("</li>");
		}
		ps.println("</ul>");
		footer(ps);
		ps.close();
	}

}
