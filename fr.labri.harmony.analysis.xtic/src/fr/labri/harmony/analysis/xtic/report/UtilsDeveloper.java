package fr.labri.harmony.analysis.xtic.report;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.core.log.HarmonyLogger;

public class UtilsDeveloper {
	
	public static int MIN_COMMITS = 1;

	public static void cleanDevelopers(List<Developer> devs) {
		//detectMerge(devs);
		removeSmall(devs);
	}
	
	static void removeSmall(List<Developer> devs) {
		List<Developer> toRemove = new ArrayList<Developer>();
		for(Developer dev : devs)
			if(dev.getNbCommit() <= MIN_COMMITS)
				toRemove.add(dev);
		devs.removeAll(toRemove);	
	}

	static void detectMerge(List<Developer> devs) {

		List<Developer> copy = new ArrayList<Developer>();

		for(Developer dev : devs) {
			String name = clean(dev.getName());
			dev.setName(name);
		}
		int newSize = devs.size();
		int oldSize = 0;
		while(oldSize != newSize) {
			for(int i = 0 ; i < devs.size() ; i++) {
				Developer dev = devs.get(i);
				for(int j = i ; j < devs.size() ; j++) {
					Developer dev2 = devs.get(j);
					if(dev.getId()!=dev2.getId()) {
						if(dev.getName().equals(dev2.getName())) {
							copy.add(dev);
							copy.add(dev2);
							break;
						}
						else {
							String s1 = dev.getEmail().toLowerCase().replaceAll("\\.", "").replaceAll("\\-", "");
							String s2 = dev2.getEmail().toLowerCase().replaceAll("\\.", "").replaceAll("\\-", "");
							
							if(s1.contains("@") && s2.contains("@")) {
								s1 = s1.substring(0,s1.indexOf("@"));
								s2 = s2.substring(0,s2.indexOf("@"));
								if(s1.equals(s2)){
									copy.add(dev);
									copy.add(dev2);
									break;
								}
							}

						}
					}
				}
			}
			oldSize = devs.size();
			for(int j = 0 ; j < copy.size() ; j=j+2 ){
				if(copy.get(j).getName().length() > copy.get(j+1).getName().length()) {
					copy.get(j).mergeDeveloper(copy.get(j+1));
					devs.remove(copy.get(j+1));
					HarmonyLogger.info("Merge "+	copy.get(j).getName()+" with "+	copy.get(j+1).getName());
				}
				else {
					copy.get(j+1).mergeDeveloper(copy.get(j));
					devs.remove(copy.get(j));
					HarmonyLogger.info("Merge "+	copy.get(j+1).getName()+" with "+	copy.get(j).getName());
				}
			}
			newSize = devs.size();
		}
	}

	static String clean(String name) {
		
		String n = "";
		name = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		name = name.replaceAll("\\_", "\\.").replaceAll("\\?", "");
		for(int i = 0 ; i < name.length() ; i++)
			if(i > 0 && name.charAt(i-1)==' ')
				n+= Character.toUpperCase(name.charAt(i));
			else
				n+= name.charAt(i);
		//System.out.println(base+"\t->\t"+n);
		return n;
	}
	
}
