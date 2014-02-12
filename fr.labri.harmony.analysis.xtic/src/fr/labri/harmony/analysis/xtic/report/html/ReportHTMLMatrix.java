package fr.labri.harmony.analysis.xtic.report.html;

import java.util.ArrayList;
import java.util.List;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;

public class ReportHTMLMatrix extends ReportHTML {

	public ReportHTMLMatrix(String path) {
		super(path);
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
		header(ps);

		ps.println("<h1>Summary Matrix</h1>");

		ps.println("<button type='button' class='btn btn-success' id='showdetails'>Show Details</button>");
		ps.println("<button type='button' class='btn btn-danger' id='hidedetails'>Hide Details</button>");

		ps.println("<table id='report' class='table table-striped tablesorter'>");

		ps.println("<thead><tr><th><a  href='#'>Developer</a></th>");

		List<Integer> index_colonnes =  new ArrayList<Integer>();
		int cpt=1;
		for(Aptitude as : aptitudes) {
			cpt++;
			ps.println("<th><a class='descriptor_skill_total' href='#'>"+as.getIdName()+"</a></th>");
			for(PatternAptitude ap : as.getPatterns()) {
				ps.println("<th><a class='descriptor_skill' href='#' data-toggle='tooltip' title=\""+ap.getDescription()+"\">"+ap.getIdName()+"</a></th>");
				cpt++;
				index_colonnes.add(cpt);
			}
		}

		ps.println("</tr></thead>");

		for(Developer dev : developers) {
			ps.println("<tr><td><a class='descriptor' href='#' data-toggle='tooltip' title=\""+dev.getDescription()+"\">"+dev.getName()+"</a></td>");
			for(Aptitude as : aptitudes) {
				double scoreGlobal = 0D;
				StringBuffer sb = new StringBuffer();
				for(PatternAptitude pa : as.getPatterns()) {
					if(dev.getScore().get(pa)==null || dev.getScore().get(pa).getScore() == 0L)
						sb.append("<td class='warning'>-</td>");
					else {
						sb.append("<td class='success'>"+dev.getScore().get(pa).getScore()+"</td>");
						scoreGlobal+=dev.getScore().get(pa).getScore();
					}

				}
				if(scoreGlobal==0D)
					ps.println("<td class='warning'><b>-</b></td>");
				else
					ps.println("<td class='success'><b>"+scoreGlobal+"</b></td>");
				ps.println(sb.toString());
			}
			ps.println("</tr>");
		}

		ps.println("</table>");

		ps.println("<script>");
		ps.println("$('#showdetails').click(function(){ ");
		for(int index : index_colonnes) {
			ps.println("$('th:nth-child("+index+")').show();");
			ps.println("$('td:nth-child("+index+")').show();");
		}
		ps.println("});");
		ps.println("$('#hidedetails').click(function(){ ");
		for(int index : index_colonnes) {
			ps.println("$('th:nth-child("+index+")').hide();");
			ps.println("$('td:nth-child("+index+")').hide();");
		}
		ps.println("});");
		ps.println("$(document).ready(function(){");
		for(int index : index_colonnes) {
			ps.println("$('th:nth-child("+index+")').hide();");
			ps.println("$('td:nth-child("+index+")').hide();");
		}
		ps.println("});");
		ps.println("</script>");

		footer(ps);
		ps.close();

	}

}
