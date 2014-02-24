package fr.labri.harmony.analysis.ownership;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.labri.harmony.analysis.ownership.contributions.Contribution;
import fr.labri.harmony.analysis.ownership.contributions.ModuleContributions;
import fr.labri.harmony.analysis.ownership.metric.Metric;
import fr.labri.harmony.analysis.ownership.metric.MetricSet;
import fr.labri.harmony.analysis.ownership.metric.OwnershipMetrics;
import fr.labri.harmony.core.analysis.SingleSourceAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;

public class ProjectOwnershipAnalysis extends SingleSourceAnalysis {
	
	protected final static String OPT_SNAPSHOTS_COMMITS = "snapshots-commits";
	
	public ProjectOwnershipAnalysis() {
		super();
	}

	public ProjectOwnershipAnalysis(AnalysisConfiguration config, Dao dao) {
		super(config, dao);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void runOn(Source src) throws Exception {
		Date fromDate = null;
		Date toDate = null;

		ArrayList<String> snapshotsNativeIds = (ArrayList<String>) src.getConfig().getOptions().get(OPT_SNAPSHOTS_COMMITS);
		if (snapshotsNativeIds != null && !snapshotsNativeIds.isEmpty()) {
			String currentVersion = snapshotsNativeIds.get(0);

			Event currentVersionEvent = dao.getEvent(src, currentVersion);
			if (currentVersionEvent == null) {
				HarmonyLogger.error("Could not find snapshot " + currentVersion + " in source " + src.getUrl());
				return;
			}
			toDate = new Date(currentVersionEvent.getTimestamp());

			if (snapshotsNativeIds.size() > 1) {
				String previousVersion = snapshotsNativeIds.get(1);
				Event previousVersionEvent = dao.getEvent(src, previousVersion);
				if (previousVersionEvent == null) {
					HarmonyLogger.error("Could not find snapshot " + previousVersion + " in source " + src.getUrl());
					return;
				}
				fromDate = new Date(previousVersionEvent.getTimestamp());
			}
		}
		
		ModuleContributions contributions = extractContributions(src, fromDate, toDate);
		computeMetrics(src, contributions);
	}
	
	protected ModuleContributions extractContributions(Source src, Date fromDate, Date toDate) {
		List<Item> items = null;
		if (fromDate == null) items = dao.getItems(src, Calendar.getInstance().getTime());
		else items = dao.getItems(src, fromDate);
		
		ModuleContributions contributions = getModuleContributions(src.getUrl(), fromDate, toDate, items);
		if (contributions != null) dao.saveData(getPersistenceUnitName(), contributions, src);
		return contributions;
	}
	
	protected void computeMetrics(Source src, ModuleContributions contributions) {	
		OwnershipMetrics ownershipMetrics = new OwnershipMetrics(contributions.getContributions());
		List<Metric> metrics = ownershipMetrics.getMetrics();
		MetricSet metricSet = new MetricSet(contributions.getModuleName(), contributions.getToDate(), contributions.getFromDate(), metrics);
		dao.saveData(getPersistenceUnitName(), metricSet, src);
	}

	public ModuleContributions getModuleContributions(String moduleName, Date fromDate, Date toDate, List<Item> itemsInModule) {
		ModuleContributions contribs = new ModuleContributions(moduleName, fromDate, toDate);
		List<Action> actions = new ArrayList<>();
		for (Item i : itemsInModule) {
			actions.addAll(dao.getActions(i, fromDate, toDate));
		}
		for (Action action : actions) {
			Author author = action.getEvent().getAuthors().get(0);
			if (!author.getName().equals("cvs2svn")) {
				Contribution contrib = contribs.getContribution(author.getNativeId());
				if (contrib == null) contrib = new Contribution(author.getNativeId(), 0, 0);
				contrib.setTouches(contrib.getTouches() + 1);

				String churnString = action.getMetadata().get("churn");
				if (churnString != null) {
					contrib.setChurn(contrib.getChurn() + Integer.parseInt(churnString));
				}
				contribs.put(author.getNativeId(), contrib);
			}
		}
		return contribs;
	}

}
