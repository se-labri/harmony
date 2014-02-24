package fr.labri.harmony.source.tfs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ChangeType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ItemType;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.ModelPersister;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.source.AbstractSourceExtractor;

public class TFSSourceExtractor extends AbstractSourceExtractor<TFSWorkspace> {

	private static final String COMMIT_LOG = "commit_log";

	public TFSSourceExtractor() {
		super();
	}

	public TFSSourceExtractor(SourceConfiguration config, ModelPersister modelPersister) {
		super(config, modelPersister);
	}

	@Override
	public void initializeWorkspace() {
		workspace = new TFSWorkspace(this);
		workspace.init();

	}

	@Override
	public void extractEvents() {
		Changeset[] changesets = workspace.getChangeset();

		Event last = null;

		for (int i = 0; i < changesets.length; ++i) {

			Changeset changeset = changesets[i];

			Change[] changes = changeset.getChanges();
			if (changes.length != 0 && !changes[0].getChangeType().contains(ChangeType.BRANCH)) {

				int eventId = changeset.getChangesetID();

				long eventTime = changeset.getDate().getTimeInMillis() / 10;

				// Author Identification
				String userName = changeset.getOwner();
				String displayName = changeset.getOwnerDisplayName();
				Author author = modelPersister.getAuthor(source, userName);
				if (author == null) {
					author = new Author(source, userName, displayName);
					modelPersister.saveAuthor(author);
				}
				List<Author> authors = new ArrayList<>(Arrays.asList(new Author[] { author }));

				// Parent identification
				// TODO check this definition of parent
				HashSet<Event> parents = new HashSet<>();
				if (last != null) parents.add(last);

				Event e = new Event(source, String.valueOf(eventId), eventTime, parents, authors);
				modelPersister.saveEvent(e);

				last = e;

				// TODO Add management metadata
				/*
				 * Metadata metadata = new Metadata(); metadata.getMetadata().put(COMMIT_LOG,
				 * changeset.getComment()); metadata.getMetadata().put("committer",
				 * changeset.getCommitter()); metadata.getMetadata().put("committer-display-name",
				 * changeset.getCommitterDisplayName()); e.getData().add(metadata);
				 * metadata.setHarmonyElement(e);
				 */

				// TODO Requirements
				// WorkItem wi[] = changeset.getWorkItems();

			}

		}

	}

	@Override
	public void extractActions(Event e) {

		Changeset changeset = workspace.getTFSClient().getChangeset(Integer.parseInt(e.getNativeId()));
		Change[] changes = changeset.getChanges();
		for (Change change : changes) {

			// We do not track folders
			if (change.getItem().getItemType().equals(ItemType.FILE)) {
				// String itemId = Integer.toString(change.getItem().getItemID());
				String itemId = change.getItem().getServerItem();
				if (extractItemWithPath(itemId)) {
					Item i = modelPersister.getItem(source, itemId);
					if (i == null) {
						i = new Item(source, itemId);
						modelPersister.saveItem(i);
					}

					ActionKind kind = null;
					ChangeType changeType = change.getChangeType();
					if (changeType.contains(ChangeType.ADD)) kind = ActionKind.Create;
					else if (changeType.contains(ChangeType.EDIT)) kind = ActionKind.Edit;
					else if (changeType.contains(ChangeType.DELETE)) kind = ActionKind.Delete;

					// We check if the related event has parents, if it the case we select
					// arbitrarily the first one as parent of the action.
					Event parentOfA = null;
					if (e.getParents().isEmpty()) {
						parentOfA = null;
					} else {
						parentOfA = e.getParents().get(0);
					}
					Action a = new Action(i, kind, e, parentOfA, source);
					modelPersister.saveAction(a);
				}
				// TODO Add metadata management
				/*
				 * String serverPath = change.getItem().getServerItem(); Metadata metadata = new
				 * Metadata(); metadata.getMetadata().put("server-path", serverPath);
				 * 
				 * i.getData().add(metadata); metadata.setHarmonyElement(i);
				 */
			}
		}
	}

}
