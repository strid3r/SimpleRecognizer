/*
 * Copyright (C) 2012-2013 strider
 * 
 * Util DateTime Class
 * By © strider 2012-2013.
 */

package ru.strider.util;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Util DateTime Class.
 * 
 * @author strider
 */
public class DateTime {
	
	private static final String LOG_TAG = DateTime.class.getSimpleName();
	
	public static final String PATTERN_SHORT = "EEE, dd MMM yyyy HH:mm:ss";
	public static final String PATTERN_MEDIUM = "EEE, dd MMM yyyy HH:mm:ss Z";
	public static final String PATTERN_LONG = "EEE, dd MMM yyyy HH:mm:ss Z '@' z";
	public static final String PATTERN_FULL = "EEE, dd MMM yyyy HH:mm:ss Z '@' z '@' zzzz";
	
	private static final String TIME_ZONE_GMT = "GMT";
	
	private static DateFormat sDateFormat = null;
	
	static {
		setDateFormat(PATTERN_MEDIUM);
	}
	
	private static DateFormat sDateFormatLocal = new SimpleDateFormat(PATTERN_SHORT, Locale.ENGLISH);
	
	private DateTime() {
		throw (new AssertionError());
	}
	
	/**
	 * Get GMT DateFormat instance.
	 * 
	 * @return GMT DateFormat instance.
	 */
	public static DateFormat getDateFormat() {
		return sDateFormat;
	}
	
	/**
	 * Set GMT DateFormat pattern String.
	 * 
	 * @param pattern : date format pattern String.
	 */
	private static void setDateFormat(String pattern) {
		sDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
		sDateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
	}
	
	/**
	 * Get localized DateFormat instance.
	 * 
	 * @return localized DateFormat instance.
	 */
	public static DateFormat getDateFormatLocal() {
		return sDateFormatLocal;
	}
	
	/**
	 * Set localized DateFormat pattern String.
	 * 
	 * @param pattern : localized date format pattern String.
	 * @param locale : date format Locale.
	 */
	public static synchronized void setDateFormatLocal(String pattern, Locale locale) {
		sDateFormatLocal = new SimpleDateFormat(pattern, locale);
	}
	
	/**
	 * Get DateTime as GMT String.
	 * 
	 * @param date : date to format.
	 * 
	 * @return The String with Date in GMT.
	 */
	public static String format(Date date) {
		return sDateFormat.format(date);
	}
	
	/**
	 * Get localized DateTime as String.
	 * 
	 * @param date : date to format.
	 * 
	 * @return The String with Date in Local Time.
	 */
	public static String formatLocal(Date date) {
		return sDateFormatLocal.format(date);
	}
	
	/**
	 * Parse DateTime String as Date.
	 * 
	 * @param dateTime : a String with Date and Time to parse.
	 * 
	 * @return Date resulting from the parsing or null.
	 */
	public static Date parse(String dateTime) {
		Date date = null;
		
		try {
			date = sDateFormat.parse(dateTime);
		} catch (ParseException e) {
			Log.e(LOG_TAG, "Passed DateTime String cannot be parsed >> " + e.toString());
		}
		
		return date;
	}
	
}
