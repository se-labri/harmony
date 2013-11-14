package fr.labri.harmony.analysis.xtic.report.html;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.ListTimedScore;
import fr.labri.harmony.analysis.xtic.TimedScore;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.utils.collections.Pair;

public class ReportHTMLCoHappening extends ReportHTML{

	public ReportHTMLCoHappening(String path) {
		super(path);
	}

	public <T> void computeTable(PrintStream ps, List<T> list_elements, Map<T,ListTimedScore> scores_apt, Map<Pair<T, T>, List<Double>> index, boolean fillIndex, double nbEvents, String id_table) {

		StringBuffer header = new StringBuffer();

		header.append("<table id='happening_"+id_table+"' class='table table-stripped table-sorter'>");

		header.append("<thead><tr><th>Aptitude couple (A,B)</th>");
		header.append("<th><a class='descriptor_skill' href='#' data-toggle='tooltip' title=\"Number of times A happen\">Hap. A</a></th>");
		header.append("<th><a class='descriptor_skill' href='#' data-toggle='tooltip' title=\"Number of times B happen\">Hap. B</a></th>");
		header.append("<th><a class='descriptor_skill' href='#' data-toggle='tooltip' title=\"Number of times A and B happen together\">Happening</a></th>");
		header.append("<th><a class='descriptor_skill' href='#' data-toggle='tooltip' title=\"When A or B happens, how many times they happen together ?\">Co-Hap. (%)</a></th>");
		header.append("<th><a class='descriptor_skill' href='#' data-toggle='tooltip' title=\"When A happens, how many times B happens?\">Co-Hap. (A => B) (%)</a></th>");
		header.append("<th><a class='descriptor_skill' href='#' data-toggle='tooltip' title=\"When B happens, how many times A happens?\">Co-Hap. (B => A) (%)</a></th>");
		header.append("<th><a class='descriptor_skill' href='#' data-toggle='tooltip' title=\"Number of times A and happen B together compared to the total number of events ?\">Hap. General (%)</a></th>");
		header.append("</tr></thead>");

		int cpt=0;
		for(int i = 0 ; i < list_elements.size()-1 ; i++) {
			for(int j = i+1 ; j < list_elements.size() ; j++) {
				Set<Long> times_src = new HashSet<Long>();
				for(TimedScore ts : scores_apt.get(list_elements.get(i)).getList())
					times_src.add(ts.getTimestamp());
				Set<Long> times_tgt = new HashSet<Long>();
				for(TimedScore ts : scores_apt.get(list_elements.get(j)).getList())
					times_tgt.add(ts.getTimestamp());

				Set<Long> union = new HashSet<Long>(times_src);
				Set<Long> common = new HashSet<Long>(times_src);
				Set<Long> saveSrc = new HashSet<Long>(times_src);
				Set<Long> saveTgt = new HashSet<Long>(times_tgt);
				int src_size = saveSrc.size();
				int tgt_size = saveTgt.size();
				union.addAll(times_tgt);
				common.retainAll(times_tgt);
				saveTgt.retainAll(common);
				saveSrc.retainAll(common);

				if(times_src.size()>0) {
					cpt++;
					Pair<T,T> p = new Pair<T, T>(list_elements.get(i), list_elements.get(j));
					
					String entryName = "";
					if(list_elements.get(i) instanceof Aptitude)
						entryName="("+((Aptitude)list_elements.get(i)).getIdName()+", "+((Aptitude)list_elements.get(j)).getIdName()+")";
					if(list_elements.get(i) instanceof PatternAptitude)
						entryName="("+((PatternAptitude)list_elements.get(i)).getIdName()+", "+((PatternAptitude)list_elements.get(j)).getIdName()+")";
					header.append("<tr><td>"+entryName+"</td>");
					//Hap.A 
					header.append("<td class='gray'>"+src_size+"</td>");
					//Hap.B 
					header.append("<td class='gray'>"+tgt_size+"</td>");
					//Happening
					header.append("<td>"+common.size()+"</td>");
					if(fillIndex) {
						//Co-happening -> one at least one appears, how many times they appear together
						double percCo = ((double)(common.size())/(double)(union.size()))*100;
						header.append("<td>"+formatDouble(percCo)+" %</td>");
						//Co-happening A=> B
						double percCoT = ((double)(saveSrc.size())/(double)(src_size))*100;
						header.append("<td>"+formatDouble(percCoT)+" %</td>");
						//Co-happening B=> A
						double percCoS = ((double)(saveTgt.size())/(double)(tgt_size))*100;
						header.append("<td>"+formatDouble(percCoS)+" %</td>");
						//Co-happening General  -> how many times they appear together compared to the total number of events
						double perc = ((common.size())/(nbEvents))*100;
						header.append("<td>"+formatDouble(perc)+" %</td></tr>");
						index.put(new Pair<T, T>(list_elements.get(i), list_elements.get(j)), new ArrayList<Double>(Arrays.asList(percCo,percCoT,percCoS,perc)));
					}
					else {
						List<Double> l = index.get(p);
						//Co-happening. When at least one appears, how many times they appear together ?
						double percCo = ((double)(common.size())/(double)(union.size()))*100;
						double diff = percCo - l.get(0);
						String text = "";
						if(diff!=0)
							text = (diff < 0 ) ? " (<span class='less'>"+formatDouble(diff)+" %</span>)" :  " (<span class='more'>+ "+formatDouble(diff)+" %</span>)";
						header.append("<td>"+formatDouble(percCo)+" %"+ text+"</td>");
						//Co-happening. When the first appears, how many times the second appear too ?
						double percCoT = ((double)(saveSrc.size())/(double)(src_size))*100;
						diff = percCoT - l.get(1);
						text = "";
						if(diff!=0)
							text = (diff < 0 ) ? " (<span class='less'>"+formatDouble(diff)+" %</span>)" :  " (<span class='more'>+ "+formatDouble(diff)+" %</span>)";
						header.append("<td>"+formatDouble(percCoT)+" %"+ text+"</td>");
						//Co-happening. When the second appears, how many times the first appear too ?
						double percCoS = ((double)(saveTgt.size())/(double)(tgt_size))*100;
						diff = percCoS - l.get(2);
						text = "";
						if(diff!=0)
							text = (diff < 0 ) ? " (<span class='less'>"+formatDouble(diff)+" %</span>)" :  " (<span class='more'>+ "+formatDouble(diff)+" %</span>)";
						header.append("<td>"+formatDouble(percCoS)+" %"+ text+"</td>");
						//happening. How many time both appear together in general ?
						double perc = ((common.size())/(nbEvents))*100;
						diff = perc - l.get(3);
						text = "";
						if(diff!=0)
							text = (diff < 0 ) ? " (<span class='less'>"+formatDouble(diff)+" %</span>)" :  " (<span class='more'>+ "+formatDouble(diff)+" %</span>)";
						header.append("<td>"+formatDouble(perc)+" %"+ text+"</td></tr>");
					}
				}
			}

		}
		header.append("</table>");
		if(cpt==0) 
			ps.println("No co-happening of aptitudes to display");
		else
			ps.println(header.toString());
	}

	public <T> void printVennDiagram(PrintStream ps, List<T> list_elements, Map<T,ListTimedScore> scores_apt, String idName) {
	
		StringBuffer sb = new StringBuffer();
		sb.append("<div class='venn_aptitudes_"+idName+"'></div>");
		sb.append("<script>");
		sb.append("var sets = [");
		boolean firstApt =true;
		int cpt=0;
		for(int i = 0 ; i < list_elements.size() ; i++) {
			Set<Long> times = new HashSet<Long>();
			for(TimedScore ts : scores_apt.get(list_elements.get(i)).getList())
				times.add(ts.getTimestamp());
			if(!firstApt)
				sb.append(",");
			if(list_elements.get(i) instanceof Aptitude)
				sb.append("{label: \""+((Aptitude)list_elements.get(i)).getIdName()+"\", size: "+times.size()+"}");
			if(list_elements.get(i) instanceof PatternAptitude)
				sb.append("{label: \""+((PatternAptitude)list_elements.get(i)).getIdName()+"\", size: "+times.size()+"}");
			cpt++;
			firstApt=false;
		}
		firstApt = true;
		sb.append("] ,  overlaps = [");
		for(int i = 0 ; i < list_elements.size()-1 ; i++) {
			for(int j = i+1 ; j < list_elements.size() ; j++) {
				Set<Long> times_src = new HashSet<Long>();
				for(TimedScore ts : scores_apt.get(list_elements.get(i)).getList())
					times_src.add(ts.getTimestamp());
				Set<Long> times_tgt = new HashSet<Long>();
				for(TimedScore ts : scores_apt.get(list_elements.get(j)).getList())
					times_tgt.add(ts.getTimestamp());
				times_src.retainAll(times_tgt);
				if(!firstApt)
					sb.append(",");
				if(times_src.size()>0) 
					sb.append("{sets : ["+i+","+j+"] , size: "+times_src.size()+"}");
				else
					sb.append("{sets : ["+i+","+j+"] , size: 0}");
				firstApt=false;
			}
		}
		sb.append("];");
		// get positions for each set
		sb.append("sets = venn.venn(sets, overlaps);");
		// draw the diagram in the 'simple_example' div
		sb.append("venn.drawD3Diagram(d3.select(\".venn_aptitudes_"+idName+"\"), sets, 250, 250);");
		sb.append("</script>");
		//On affiche rien si il y a moins de deux éléments
		if(cpt>=2)
			ps.println(sb.toString());
		else
			ps.println("No visualization to display");
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {

	}

}
