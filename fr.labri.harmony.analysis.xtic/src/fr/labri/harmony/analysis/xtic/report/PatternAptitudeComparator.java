package fr.labri.harmony.analysis.xtic.report;

import java.util.Comparator;

import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;

public class PatternAptitudeComparator implements Comparator<PatternAptitude> {

	@Override
	public int compare(PatternAptitude arg0, PatternAptitude arg1) {
		return arg0.getIdName().compareTo(arg1.getIdName());
	}

}
