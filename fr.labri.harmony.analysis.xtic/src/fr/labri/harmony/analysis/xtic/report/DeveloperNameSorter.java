package fr.labri.harmony.analysis.xtic.report;
import java.util.Comparator;

import fr.labri.harmony.analysis.xtic.Developer;

public class DeveloperNameSorter implements Comparator<Developer> {
	
	@Override
	public int compare(Developer d1, Developer d2) {
		return d1.getName().compareTo(d2.getName());
	}

}