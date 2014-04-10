package fr.labri.harmony.core.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HarmonyLogger {

	public static void info(String message, Object... params) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.printf("[%s] %s\n", dateFormat.format(cal.getTime()), String.format(message, (Object[])params));
	}

	public static void error(String message, Object... params) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
	
		System.err.printf("!!![%s] %s\n", dateFormat.format(cal.getTime()), String.format(message, (Object[])params)); // TODO Factorize code
	}		
}
