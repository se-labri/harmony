package fr.labri.harmony.analysis.ownership;

import java.io.IOException;
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
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;

/**
 * This analysis compute for each item of a source the number of actions that an author performed.
 * 
 * In the article 'Don�t Touch My Code! Examining the Effects of Ownership on Software Quality' from Bird et al. they state that an author is a major
 * contributor of an item if he performed at least 5% of the actions on the files. Otherwise he is a minor contributor. In their case study they found that high
 * levels of ownership, specifically operationalized as high values of Ownership and Major, and low values of Minor, are associated with less defects.
 * 
 * By default ownership is computed on the whole history of the project. However, you choose to compute ownership between two commits of the repository.
 * Add the option 'snapshots-commits : ["commitA", "commitB"]' to the configuration of each source to do so.
 * See <a href=http://code.google.com/p/harmony/wiki/SourceConfigOption>this page</a> for an example of how to add options to a source repository
 * 
 * @author SE@LaBRI * 
 */
public class ItemOwnershipAnalysis extends SingleSourceAnalysis {

	protected final static String OPT_SNAPSHOTS_COMMITS = "snapshots-commits";

	public ItemOwnershipAnalysis() {
		super();
	}

	public ItemOwnershipAnalysis(AnalysisConfiguration config, Dao dao) {
		super(config, dao);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void runOn(Source src) throws IOException {

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

		extractContributions(src, fromDate, toDate);
		computeMetrics(src, fromDate, toDate);
	}

	protected void computeMetrics(Source src, Date fromDate, Date toDate) {
		List<ModuleContributions> allContributions = dao.getData(getPersistenceUnitName(), ModuleContributions.class, src);

		for (ModuleContributions contributions : allContributions) {
			OwnershipMetrics ownershipMetrics = new OwnershipMetrics(contributions.getContributions());
			List<Metric> metrics = ownershipMetrics.getMetrics();
			MetricSet metricSet = new MetricSet(contributions.getModuleName(), toDate, fromDate, metrics);
			dao.saveData(getPersistenceUnitName(), metricSet, src);
		}
	}

	protected void extractContributions(Source src, Date fromDate, Date toDate) {
		List<Item> items = null;
		if (fromDate == null) items = dao.getItems(src, Calendar.getInstance().getTime());
		else items = dao.getItems(src, fromDate);
		
		for (Item item : items) {
			ModuleContributions contributions = getItemContributions(fromDate, toDate, item);
			if (contributions != null) dao.saveData(getPersistenceUnitName(), contributions, src);
		}
	}
	
	protected ModuleContributions getItemContributions(Date fromDate, Date toDate, Item i) {
		// the contribs made to this item. Author's nativeId is the key
		ModuleContributions itemContributions = new ModuleContributions(i.getNativeId(), fromDate, toDate);
		List<Action> actions = dao.getActions(i, fromDate, toDate);
		if (actions.isEmpty()) return new ModuleContributions(i.getNativeId(), fromDate, toDate);
		for (Action action : actions) {
			String author = action.getEvent().getAuthors().get(0).getNativeId();
			Contribution contrib = itemContributions.getContribution(author);
			if (contrib == null) contrib = new Contribution(author, 0, 0);
			contrib.setTouches(contrib.getTouches() + 1);

			String churnString = action.getMetadata().get("churn");
			if (churnString != null) {
				contrib.setChurn(contrib.getChurn() + Integer.parseInt(churnString));
			}

			itemContributions.put(author, contrib);
		}
		return itemContributions;
	}

}