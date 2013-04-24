package fr.labri.harmony.core;

import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import fr.labri.harmony.core.config.GlobalConfigReader;
import fr.labri.harmony.core.config.SourceConfigReader;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.dao.DaoImpl;
import fr.labri.harmony.core.execution.StudyScheduler;
import fr.labri.harmony.core.source.SourceExtractor;

public class CoreCommand implements CommandProvider {

	public CoreCommand() {
	}

	public void _harmony(CommandInterpreter ci) {
		try {
			String globalConfigPath = ci.nextArgument();
			String sourceConfigPath = ci.nextArgument();

			GlobalConfigReader global = new GlobalConfigReader(globalConfigPath);
			SourceConfigReader sources = new SourceConfigReader(sourceConfigPath, global);

			Dao dao = new DaoImpl(global.getDatabaseConfig());

			//HarmonyManager.createAnalyses(global, dao);
			
			//new StudyScheduler().run(global, sources);
			
			List<SourceExtractor<?>> extractors = HarmonyManager.createSourceExtractors(sources, dao);
			for (SourceExtractor<?> e: extractors) {
				e.setAnalyses(HarmonyManager.createAnalyses(global, dao));
				e.run();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public String getHelp() {
		return null;
	}

}
