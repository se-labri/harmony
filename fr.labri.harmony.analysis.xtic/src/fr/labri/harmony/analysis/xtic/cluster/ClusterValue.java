package fr.labri.harmony.analysis.xtic.cluster;

import java.util.ArrayList;
import java.util.List;

public class ClusterValue {
		private List<Integer> values = new ArrayList<Integer>();
		private List<String> names = new ArrayList<String>();
		
		public List<String> getNames() {
			return names;
		}

		public ClusterValue() {

		}
		
		public ClusterValue(List<Integer> values) {
			this.values = values;
		}
		
		public List<Integer> getValues() {
			return values;
		}

		public void setValues(List<Integer> values) {
			this.values = values;
		}
	}
	
	