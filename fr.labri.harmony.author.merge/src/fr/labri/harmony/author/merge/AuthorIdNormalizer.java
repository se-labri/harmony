package fr.labri.harmony.author.merge;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashSet;

import fr.labri.harmony.core.model.Author;

public class AuthorIdNormalizer {

	public static Collection<String> getNameParts(Author author, int minLen) {
		Author nAuthor = getNormalizedAuthor(author);
		HashSet<String> terms = new HashSet<>();
		for (String term : nAuthor.getName().split(" ")) {
			if (term.length() >= minLen) terms.add(term);
		}
		
		String mail = nAuthor.getEmail();
		if (mail.contains("@")) {
			for (String term : mail.split("@")[0].split(" ")) {
				if (term.length() >= minLen) terms.add(term);
			}
		}
		return terms;
	}
	
	public static Author getNormalizedAuthor(Author author) {
		// remove email address suffix if included in the name.
		String name = author.getName();
		if (name.contains("@")) {
			String[] terms = name.split(" +");
			name = "";
			for (String term : terms) {
				if (term.contains("@")) name += term.split("@")[0];
				else name += term;
			}
		}
		Author normalizedAuthor = new Author(author.getSource(), author.getNativeId(), normalize(name));
		normalizedAuthor.setEmail(normalize(author.getEmail()));
		return normalizedAuthor;
	}

	private static String normalize(String s) {
		String normalized = s.toLowerCase().replace(".", " ").replace("_", " ").trim().replaceAll(" +", " ");
		normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		return normalized;
	}
}
