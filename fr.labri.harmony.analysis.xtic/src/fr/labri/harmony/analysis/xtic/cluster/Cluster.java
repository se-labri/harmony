package fr.labri.harmony.analysis.xtic.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.CSVLoader;

import java.io.File;

public class Cluster {

	public static void main(String[] args) throws Exception {
		getClusters("results/res.csv");
	}

	public static Map<String,List<ClusterValue>> getClusters(String csvFile) throws Exception {
		

		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(csvFile));
		Instances data = loader.getDataSet();
		
		Map<Integer,String> authors = new HashMap<Integer, String>();
		Map<String,List<ClusterValue>> clusters = new HashMap<String, List<ClusterValue>>();
		
		@SuppressWarnings("unchecked")
		Enumeration<Attribute> att = data.enumerateAttributes();
		int k = 0;
		while(att.hasMoreElements()) {
			Attribute a = att.nextElement();
			String attname = a.name();
			int index = a.index();
			
			@SuppressWarnings("unchecked")
			Enumeration<Instance> inst = data.enumerateInstances();
			if(k==0) {
				int j = 0;
				while(inst.hasMoreElements()) {
					String name = inst.nextElement().stringValue(index);
					
					authors.put(j,name);
					j++;
				}
			}
			else {
				
				List<Double> values = new ArrayList<Double>();
				while(inst.hasMoreElements()) {
					values.add(inst.nextElement().value(index));
				}
				SimpleKMeans kmeans = new SimpleKMeans();

				kmeans.setPreserveInstancesOrder(true);
				kmeans.setMaxIterations(10);
				kmeans.setSeed(10);
				kmeans.setNumClusters(3);
				FastVector attributes = new FastVector();
				attributes.addElement(new Attribute("score"));
				Instances instances = new Instances(attname, attributes, 0);
				
				for(double value : values){
					Instance wekaInstance = new SparseInstance(1);
					wekaInstance.setValue(0, value);
					instances.add(wekaInstance);
				}
				//System.out.println(instances.toString());
				kmeans.buildClusterer(instances);
				List<ClusterValue> cls = new ArrayList<ClusterValue>();
				for (int i = 0; i < 3; i++) { 
					cls.add(new ClusterValue());
				}
				for (int i = 0; i < instances.numInstances(); i++) { 
					//System.out.println((int)instances.instance(i).value(0));
					cls.get(kmeans.clusterInstance(instances.instance(i))).getValues().add((int)instances.instance(i).value(0));
					cls.get(kmeans.clusterInstance(instances.instance(i))).getNames().add(authors.get(i));
					//cls.get(kmeans.clusterInstance(instances.instance(i))).getNames().add(authors.get(i) + " ("+(int)instances.instance(i).value(0)+")");
					//System.out.println("\t"+instances.instance(i)+"\t"+(kmeans.clusterInstance(instances.instance(i)) + 1));
				}
				Collections.sort(cls,new ClusterComparator());
				clusters.put(attname, cls);
			
			}
			k++;
		}

		return clusters;
	}
	
	
}
