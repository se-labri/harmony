package fr.labri.harmony.analysis.report;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jfree.chart.JFreeChart;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import fr.labri.harmony.analysis.report.charts.ActionAuthorChart;
import fr.labri.harmony.analysis.report.charts.AuthorActionChart;
import fr.labri.harmony.analysis.report.charts.AuthorDeletedRatioChart;
import fr.labri.harmony.analysis.report.charts.AuthorEventChart;
import fr.labri.harmony.analysis.report.charts.AuthorItemCreateChart;
import fr.labri.harmony.analysis.report.charts.AuthorItemDeleteChart;
import fr.labri.harmony.analysis.report.charts.AuthorItemNonSelfDeleteChart;
import fr.labri.harmony.analysis.report.charts.DevelopersActionsChart;
import fr.labri.harmony.analysis.report.charts.EventAuthorChart;
import fr.labri.harmony.analysis.report.charts.ItemAuthorChart;
import fr.labri.harmony.analysis.report.charts.ItemEditionChart;
import fr.labri.harmony.analysis.report.charts.ItemKindChart;
import fr.labri.harmony.analysis.report.charts.ItemNumberChart;
import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Source;

public class ReportAnalysis extends AbstractAnalysis {

	public ReportAnalysis() {
		super();
	}

	public ReportAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	@Override
	public void runOn(Source src) {
		HarmonyLogger.info("Starting reporting analysis on " + src.getUrl() + ".");
		String baseFolder = config.getFoldersConfiguration().getOutFolder();
		String urlFolder = convertToFolderName(src.getUrl());
		Path outputPath = Paths.get(baseFolder, urlFolder);
		File outputFolder = outputPath.toFile();
		if (!outputFolder.exists()) outputFolder.mkdir();
		for (ChartDrawer drawer : getChartDrawers()) {
			try {
				saveChartToPDF(drawer.createChart(src), outputFolder.getAbsolutePath() + File.separator + drawer.getChartName() + ".pdf", 1680, 1050);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public List<ChartDrawer> getChartDrawers() {
		List<ChartDrawer> produces = new ArrayList<>();
		produces.add(new DevelopersActionsChart(getDao()));
		produces.add(new ItemNumberChart(dao));
		//produces.add(new ClocChart(dao));
		 produces.add(new EventAuthorChart(dao));
		 produces.add(new ActionAuthorChart(dao));
		 produces.add(new AuthorEventChart(dao));
		 produces.add(new ItemEditionChart(dao));
		 produces.add(new ItemAuthorChart(dao));
		 produces.add(new ItemKindChart(dao));
		 produces.add(new AuthorItemCreateChart(dao));
		 produces.add(new AuthorItemDeleteChart(dao));
		 produces.add(new AuthorItemNonSelfDeleteChart(dao));
		 produces.add(new AuthorDeletedRatioChart(dao));
		 produces.add(new AuthorActionChart(dao));
		return produces;
	}

	private static String convertToFolderName(String src) {
		return src.replaceAll("http://", "").replaceAll("https://", "").replaceAll("/", "-").replaceAll(":", "");
	}
	
	public void saveChartToPDF(JFreeChart chart, String fileName, int width, int height) throws Exception {
	    if (chart != null) {
	        BufferedOutputStream out = null;
	        try {
	            out = new BufferedOutputStream(new FileOutputStream(fileName)); 
	                
	            //convert chart to PDF with iText:
	            Rectangle pagesize = new Rectangle(width, height); 
	            Document document = new Document(pagesize); 
	            try { 
	                PdfWriter writer = PdfWriter.getInstance(document, out); 
	                document.addAuthor("JFreeChart"); 
	                document.open(); 
	        
	                PdfContentByte cb = writer.getDirectContent(); 
	                PdfTemplate tp = cb.createTemplate(width, height); 
	                Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper()); 
	        
	                Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height); 
	                chart.draw(g2, r2D, null); 
	                g2.dispose(); 
	                cb.addTemplate(tp, 0, 0); 
	            } finally {
	                document.close(); 
	            }
	        } finally {
	            if (out != null) {
	                out.close();
	            }
	        }
	    }//else: input values not availabel
	}//saveChartToPDF()
}

