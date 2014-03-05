package fr.labri.harmony.analysis.xtic.report;

import java.util.Comparator;

import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;

public class AptitudeComparator implements Comparator<Aptitude> {

	@Override
	public int compare(Aptitude arg0, Aptitude arg1) {
		return arg0.getIdName().compareTo(arg1.getIdName());
	}

}
