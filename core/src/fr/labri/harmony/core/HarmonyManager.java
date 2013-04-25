package fr.labri.harmony.core;


public class HarmonyManager {


	public static String getFilter(String name) {
		return "(" + HarmonyService.PROPERTY_NAME + "=" + name + ")";
	}

}
