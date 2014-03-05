package fr.labri.harmony.analysis.xtic.report;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UtilsDate {
	
	public static long convertEpoch(long timestamp) {
		Calendar current = Calendar.getInstance();
		Calendar myCal = Calendar.getInstance();
		myCal.setTimeInMillis(timestamp);
		current.setTimeInMillis(System.currentTimeMillis());
		if(myCal.get(Calendar.YEAR) <= 1970)
			return convertEpoch(timestamp*1000);
		if(current.get(Calendar.YEAR) < myCal.get(Calendar.YEAR))
			return convertEpoch(timestamp/1000);
		myCal.setTimeInMillis(timestamp);
		//put it in seconds now
		return timestamp/1000;
	}
	
	public static String format(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM YYYY");
		return sdf.format(new Date(convertEpoch(timestamp)*1000));
	}
	
}
