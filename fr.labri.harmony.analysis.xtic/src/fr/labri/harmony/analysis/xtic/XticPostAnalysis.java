package fr.labri.harmony.analysis.xtic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.GenerateReport;
import fr.labri.harmony.analysis.xtic.report.UtilsDeveloper;
import fr.labri.harmony.analysis.xtic.report.json.ReportJSON;
import fr.labri.harmony.analysis.xtic.report.json.ReportJSONStudent;
import fr.labri.harmony.analysis.xtic.report.json.ReportJSONMongo;
import fr.labri.harmony.core.analysis.MultipleSourceAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.output.OutputUtils;

public  class XticPostAnalysis extends MultipleSourceAnalysis{

	public XticPostAnalysis() {
		super();
	}

	public XticPostAnalysis(AnalysisConfiguration config, Dao dao,
			Properties properties) {
		super(config, dao, properties);
		if(config.getOptions()!=null) {
			if(config.getOptions().containsKey("MIN_COMMITS_DEV")) {
				UtilsDeveloper.MIN_COMMITS = Integer.valueOf(config.getOptions().get("MIN_COMMITS_DEV").toString());
			}
		}
	}

	@Override
	public void runOn(Collection<Source> sources) {
//		new ReportJSONMongo("report.json").printReport(new ArrayList<>(sources), dao);
		for(Source src : sources) {
			List<Developer> devs = dao.getData("xtic", Developer.class, src);
			List<Aptitude> apts = new ArrayList<Aptitude>();
			for(Developer dev : devs) {
				for(PatternAptitude ap : dev.getScore().keySet()) {
					if(!apts.contains(ap.getAptitude()))
						apts.add(ap.getAptitude());
				}
			}		
			File directory;
			try {
				directory = OutputUtils.buildOutputPath(src, this, "report").toFile();
				FileUtils.deleteDirectory(directory);
				directory.mkdir();
				GenerateReport.createReport(directory, src, apts, devs, dao);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	

}