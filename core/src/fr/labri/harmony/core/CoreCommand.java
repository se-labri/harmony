package fr.labri.harmony.core;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import fr.labri.harmony.core.config.GlobalConfigReader;
import fr.labri.harmony.core.config.SourceConfigReader;
import fr.labri.harmony.core.execution.StudyScheduler;

public class CoreCommand implements CommandProvider {

	public CoreCommand() {
	}

	public void _harmony(CommandInterpreter ci) {
		try {
			String globalConfigPath = ci.nextArgument();
			String sourceConfigPath = ci.nextArgument();

			GlobalConfigReader global = new GlobalConfigReader(globalConfigPath);
			SourceConfigReader sources = new SourceConfigReader(sourceConfigPath, global);
			new StudyScheduler(global.getSchedulerConfiguration()).run(global, sources);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public String getHelp() {
		return null;
	}

}
