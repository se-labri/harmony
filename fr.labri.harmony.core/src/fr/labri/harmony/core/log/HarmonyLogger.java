package fr.labri.harmony.core.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HarmonyLogger {

	public static void info(String message) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println("[" + dateFormat.format(cal.getTime()) + "] " + message);
	}

	public static void error(String message) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.err.println("!!![" + dateFormat.format(cal.getTime()) + "] " + message);	}
	
	
}
