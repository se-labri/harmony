package fr.labri.harmony.analysis.xtic.report.html;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.report.Report;
import fr.labri.harmony.core.output.FileUtils;

public class ReportHTML extends Report {

	public ReportHTML(String path) {
		super(path);
	}

	protected static final int WIDTH_GRAPH = 740;
	protected static final int HEIGHT_GRAPH = 455;

	public static void initHTML(File htmlFolder) throws IOException{
		//CSS
		new File( htmlFolder+"/css").delete();
		new File( htmlFolder+"/css").mkdir();
		FileUtils.copyDirectory("fr.labri.harmony.analysis.xtic", "report/css", htmlFolder+"/css");
		//JS
		new File( htmlFolder+"/js").delete();
		new File( htmlFolder+"/js").mkdir();
		FileUtils.copyDirectory("fr.labri.harmony.analysis.xtic", "report/js", htmlFolder+"/js");
	}

	protected void header(PrintStream ps) {
		String content = FileUtils.getFileContent("fr.labri.harmony.analysis.xtic", "report/html/bootstrap_header.html", null);
		ps.println(content);
	}

	protected void footer(PrintStream ps) {
		String content = FileUtils.getFileContent("fr.labri.harmony.analysis.xtic", "report/html/bootstrap_footer.html", null);
		ps.println(content);
	}

	protected void printHeaderChart(PrintStream ps, String aptName, String desc, boolean offset) {

		ps.println("<hr>");
		ps.println("<h4>"+desc+"</h4>");

		ps.println("<div id=\"chart_container\">");
		ps.println(" <div class=\"y_axis\" id=\"y_axis_"+aptName+"\"></div>");
		ps.println(" <div class=\"chart\" id=\"chart_"+aptName+"\"></div>");
		ps.println(" <div class=\"legend\" id=\"legend_"+aptName+"\"></div>");
		if(offset) {
			ps.println(" <form id=\"offset_form_"+aptName+"\" class=\"toggler\">");
			ps.println("       <input type=\"radio\" name=\"offset\" id=\"lines_"+aptName+"\" value=\"lines\" checked>");
			ps.println("         <label class=\"lines\" for=\"lines\">lines</label><br>");
			ps.println("        <input type=\"radio\" name=\"offset\" id=\"stack_"+aptName+"\" value=\"zero\">");
			ps.println("        <label class=\"stack\" for=\"stack\">stack</label>");
			ps.println("   </form>");
		}
		ps.println("</div>");

		ps.println("<script>");
		ps.println("var palette = new Rickshaw.Color.Palette();");
		ps.println("var graph_"+aptName+" = new Rickshaw.Graph( {");
		ps.println("element: document.querySelector(\"#chart_"+aptName+"\"),");
		ps.println(" width: "+WIDTH_GRAPH+",");
		ps.println(" height: "+HEIGHT_GRAPH+",");
		ps.println(" renderer: 'line',");
		ps.println(" interpolation: 'linear',");
		ps.println("series: [");
	}

	protected void printFooterChart(PrintStream ps, String aptName, boolean offsetForm) {
		ps.println("]"
				+"} );");

		if(offsetForm) 
			ps.println("var x_axis = new Rickshaw.Graph.Axis.Time( { graph: graph_"+aptName+" } );");
		else 
			ps.println("var x_axis = new Rickshaw.Graph.Axis.X( { graph: graph_"+aptName+" } );");


		ps.println("var y_axis = new Rickshaw.Graph.Axis.Y( {"
				+"      graph: graph_"+aptName+","
				+"     orientation: 'left',"
				+"     tickFormat: Rickshaw.Fixtures.Number.formatKMBT,"
				+"    element: document.getElementById('y_axis_"+aptName+"'),"
				+"} );");


		ps.println("		var legend = new Rickshaw.Graph.Legend( {"
				+"			element: document.querySelector('#legend_"+aptName+"'),"
				+"			graph: graph_"+aptName
				+"		} );");


		ps.println("var shelving = new Rickshaw.Graph.Behavior.Series.Toggle({"
				+   "graph: graph_"+aptName+","
				+  " legend: legend"
				+"});");

		ps.println("var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight({"
				+ "   graph: graph_"+aptName+","
				+ "  legend: legend"
				+ "});");

		if(offsetForm) {
			ps.println("var offsetForm = document.getElementById('offset_form_"+aptName+"')");

			ps.println("offsetForm.addEventListener('change', function(e) {"
					+"var offsetMode = e.target.value;"

					+"if (offsetMode == 'lines') {"
					+"	graph_"+aptName+".setRenderer('line');"
					+"	graph_"+aptName+".offset = 'zero';"
					+"} else {"
					+"graph_"+aptName+".setRenderer('stack');"
					+"graph_"+aptName+".offset = offsetMode;"
					+"}"   
					+"graph_"+aptName+".render();"

				+"}, false);");
		}

		ps.println("var hoverDetail = new Rickshaw.Graph.HoverDetail( {"
				+"graph: graph_"+aptName+","
				+"xFormatter: function(x) {"
				+	"return new Date(x * 1000).toString();"
				+"}"
				+"} );");

		ps.println("graph_"+aptName+".render();");
		ps.println("</script>");
	}


	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
	}

}
