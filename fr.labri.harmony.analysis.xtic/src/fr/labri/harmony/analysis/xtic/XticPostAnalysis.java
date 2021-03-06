package fr.labri.harmony.analysis.xtic;


import java.util.ArrayList;
import java.util.Collection;

import fr.labri.harmony.analysis.xtic.report.UtilsDeveloper;
import fr.labri.harmony.analysis.xtic.report.json.ReportJSONMongo;
import fr.labri.harmony.core.analysis.MultipleSourcesAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;

public  class XticPostAnalysis extends MultipleSourcesAnalysis {
	
	String database = "default_post_database";
	
	public XticPostAnalysis() {
		super();

	}

	public XticPostAnalysis(AnalysisConfiguration config, Dao dao) {
		super(config, dao);
		if(config.getOptions()!=null) {
			if(config.getOptions().containsKey("MIN_COMMITS_DEV")) {
				UtilsDeveloper.MIN_COMMITS = Integer.valueOf(config.getOptions().get("MIN_COMMITS_DEV").toString());
			}
			if(config.getOptions().containsKey("mongo-db")) {
				database = config.getOptions().get("mongo-db").toString();
			}
		}
	}

	@Override
	public void runOn(Collection<Source> sources) {
		new ReportJSONMongo("",this.database).printReport(new ArrayList<>(sources), dao);
//		for(Source src : sources) {
//			List<Developer> devs = dao.getData("xtic", Developer.class, src);
//			List<Aptitude> apts = new ArrayList<Aptitude>();
//			for(Developer dev : devs) {
//				for(PatternAptitude ap : dev.getScore().keySet()) {
//					if(!apts.contains(ap.getAptitude()))
//						apts.add(ap.getAptitude());
//				}
//			}		
//			File directory;
//			try {
//				directory = OutputUtils.buildOutputPath(src, this, "report").toFile();
//				FileUtils.deleteDirectory(directory);
//				directory.mkdir();
//				GenerateReport.createReport(directory, src, apts, devs, dao);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
	}

	

}