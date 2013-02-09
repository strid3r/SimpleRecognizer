/*
 * Copyright (C) 2012-2013 strider
 * 
 * Util Text Class
 * By © strider 2012-2013.
 */

package ru.strider.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util Text Class.
 * 
 * @author strider
 */
public class Text {
	
	//private static final String LOG_TAG = Text.class.getSimpleName();
	
	public static final String LF = "\n";
	public static final String BR = "<br />";
	
	public static final String SEPARATOR = " :: ";
	public static final String SEPARATOR_BY = " by ";
	
	public static final String QUESTION_MARK = "?";
	
	public static final String L_AQUO = "«";
	public static final String R_AQUO = "»";
	
	//public static final String L_S_AQUO = "‹";
	//public static final String R_S_AQUO = "›";
	
	public static final String NOT_AVAILABLE = "N/A";
	public static final String NOT_AVAILABLE_EXTRA = "[ N/A ]";
	
	public static final Pattern PATTERN_EMAIL_ADDRESS = Pattern.compile(
			"[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
			"\\@" +
			"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
			"(" +
			"\\." +
			"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
			")+"
		);
	
	private Text() {
		//
	}
	
	@TargetApi(Build.VERSION_CODES.FROYO)
	public static boolean isEmailValid(CharSequence email) {
		Matcher matcher = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			matcher = Patterns.EMAIL_ADDRESS.matcher(email);
		} else {
			matcher = PATTERN_EMAIL_ADDRESS.matcher(email);
		}
		
		return matcher.matches();
	}
	
}
