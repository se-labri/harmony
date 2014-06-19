package fr.labri.harmony.author.merge;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import fr.labri.harmony.core.analysis.SingleSourceAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.output.OutputUtils;
import fr.labri.harmony.core.util.EventWalker;

public class AuthorMerger extends SingleSourceAnalysis {

	public AuthorMerger() {
		super();
	}

	public AuthorMerger(AnalysisConfiguration config, Dao dao) {
		super(config, dao);
	}

	@Override
	public void runOn(Source src) {
		@SuppressWarnings("unchecked")
		ArrayList<String> releasesCommits = (ArrayList<String>) src.getConfig().getOption("releases");
		// releases order : R0, R -1, R -2

		Event r0 = dao.getEvent(src, releasesCommits.get(0));
		Event r1 = dao.getEvent(src, releasesCommits.get(releasesCommits.size() - 1));

		HashSet<Author> authors = new HashSet<>();

		for (Event e : new EventWalker().getEventsBetween(r1, r0)) {
			authors.addAll(e.getAuthors());
		}
		
		SetMultimap<Author, String> authorsTerms = HashMultimap.create();
		for (Author a : authors) {
			authorsTerms.putAll(a, AuthorIdNormalizer.getNameParts(a, 1));
		}

		HashMap<Author, MergeGroup> groupsMap = new HashMap<>();
		
		Author[] authorsArray = authors.toArray(new Author[authors.size()]);
		for (int i = 0; i < authorsArray.length; i++) {
			MergeGroup currentGroup = new MergeGroup();
			currentGroup.terms.addAll(authorsTerms.get(authorsArray[i]));
			currentGroup.authors.add(authorsArray[i]);

			for (int j = i + 1; j < authorsArray.length; j++) {
				Author a2 = authorsArray[j];
				if (!Sets.intersection(currentGroup.terms, authorsTerms.get(a2)).isEmpty()) {
					if (groupsMap.containsKey(a2)) {
						MergeGroup otherGroup = groupsMap.get(a2);
						currentGroup.authors.addAll(otherGroup.authors);
						currentGroup.terms.addAll(otherGroup.terms);
					} else {
						currentGroup.terms.addAll(authorsTerms.get(a2));
						currentGroup.authors.add(a2);
					}
				}
			}
			if (currentGroup.authors.size() > 1) {
				for (Author a : currentGroup.authors) {
					groupsMap.put(a, currentGroup);
				}
			}
		}
		
		HashSet<MergeGroup> mergeGroups = new HashSet<>(groupsMap.values());
		try {
			Writer w = new BufferedWriter(Files.newBufferedWriter(OutputUtils.buildOutputPath(src, this, "merge-groups.txt"), Charset.defaultCharset()));
			for (MergeGroup group : mergeGroups) {
				for (Author a : group.authors)
					w.write(a.getName() + "^^@^^" + a.getEmail() + "\t");
				w.write("\n");
			}
			w.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private class MergeGroup {
		public HashSet<String> terms;
		public HashSet<Author> authors;

		public MergeGroup() {
			terms = new HashSet<>();
			authors = new HashSet<>();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((authors == null) ? 0 : authors.hashCode());
			result = prime * result + ((terms == null) ? 0 : terms.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			MergeGroup other = (MergeGroup) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (authors == null) {
				if (other.authors != null) return false;
			} else if (!authors.equals(other.authors)) return false;
			if (terms == null) {
				if (other.terms != null) return false;
			} else if (!terms.equals(other.terms)) return false;
			return true;
		}

		private AuthorMerger getOuterType() {
			return AuthorMerger.this;
		}
		
		
	}

}