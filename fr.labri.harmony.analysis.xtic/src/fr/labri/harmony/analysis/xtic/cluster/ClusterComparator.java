package fr.labri.harmony.analysis.xtic.cluster;

import java.util.Comparator;

public class ClusterComparator implements Comparator<ClusterValue> {

		@Override
		public int compare(ClusterValue e1, ClusterValue e2) {
			int max1 = 0;
			for(int v : e1.getValues())
				max1 = Math.max(max1, v);
			int max2 = 0;
			for(int v : e2.getValues())
				max2 = Math.max(max2, v);
			return Integer.compare(max1,max2);
		}
		

	}
