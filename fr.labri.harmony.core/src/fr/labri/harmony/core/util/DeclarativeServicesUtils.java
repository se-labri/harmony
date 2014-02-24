package fr.labri.harmony.core.util;

public class DeclarativeServicesUtils {
	
	private static final String PROPERTY_NAME = "component.name";

	public static String getFilter(String name) {
		return "(" + PROPERTY_NAME + "=" + name + ")";
	}
}
