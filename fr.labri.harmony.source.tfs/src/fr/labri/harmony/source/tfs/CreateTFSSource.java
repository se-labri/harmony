package fr.labri.harmony.source.tfs;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ChangeType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ItemType;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Metadata;
import fr.labri.harmony.core.source.AbstractSourceExtractor;
import fr.labri.harmony.core.source.WorkspaceException;
import fr.labri.harmony.source.svnkit.SvnKitWorkspace;



public class TFSSourceExtractor extends AbstractSourceExtractor<TFSWorkspace> {

	private static final String COMMIT_LOG = "commit_log";
	
	public TFSSourceExtractor() {
		super();
	}

	public TFSSourceExtractor(SourceConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}
	
	@Override
	public void initializeWorkspace() {
		workspace = new TFSWorkspace(config);
		workspace.init();

	}

	@Override
	public void extractEvents() {
		Changeset[] changesets = workspace.getChangeset();

		Event[] events = new Event[changesets.length];

		for (int i = 0; i < changesets.length; ++i) {
			Changeset changeset = changesets[i];
			int eventId = changeset.getChangesetID();
			long eventTime = changeset.getDate().getTimeInMillis() / 10;

			Event e = sourceFactories.events.create(Integer.toString(eventId), source, eventTime);
			events[i] = e;

			Metadata metadata = new Metadata();
			metadata.getMetadata().put(COMMIT_LOG, changeset.getComment());
			metadata.getMetadata().put("committer", changeset.getCommitter());
			metadata.getMetadata().put("committer-display-name", changeset.getCommitterDisplayName());
			e.getData().add(metadata);
			metadata.setHarmonyElement(e);

			Set<Author> authors = new HashSet<Author>();
			Author author = null;
			String userName = changeset.getOwner();
			String displayName = changeset.getOwnerDisplayName();
			try {
				author = sourceFactories.authors.tryGet(userName);
			} catch (NativeElementNotFound ne) {
				author = sourceFactories.authors.create(userName, source, displayName);
			}
			authors.add(author);
			e.setAuthors(authors);
			author.getEvents().add(e);
			Set<Event> parents = new HashSet<>();
			if (i != 0) parents.add(events[i - 1]);
			e.setParents(parents);
		}

		return events;
	}

	@Override
	public void extractActions(Event e) {

			Changeset changeset = workspace.getTFSClient().getChangeset(Integer.parseInt(e.getNativeId()));
			Change[] changes = changeset.getChanges();
			for (Change change : changes) {
				Action a = new Action();
				Item i = null;
				// We do not track folders
				if (change.getItem().getItemType().equals(ItemType.FILE)) {
					String itemId = Integer.toString(change.getItem().getItemID());
					try {
						i = sourceFactories.items.tryGet(itemId);
					} catch (NativeElementNotFound ne) {
						i = sourceFactories.items.create(itemId, source);
					}
					a.setEvent(e);
					e.getActions().add(a);
					a.setItem(i);
					i.getActions().add(a);
					ActionKind kind = null;

					ChangeType changeType = change.getChangeType();
					if (changeType.contains(ChangeType.ADD)) kind = ActionKind.Create;
					else if (changeType.contains(ChangeType.EDIT)) kind = ActionKind.Edit;
					else if (changeType.contains(ChangeType.DELETE)) kind = ActionKind.Delete;
					a.setKind(kind);

					String serverPath = change.getItem().getServerItem();
					Metadata metadata = new Metadata();
					metadata.getMetadata().put("server-path", serverPath);

					i.getData().add(metadata);
					metadata.setHarmonyElement(i);
				}
			}
	}

}
