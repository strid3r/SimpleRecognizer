/*
 * Copyright (C) 2012-2013 strider
 * 
 * Util BuildConfig Class
 * By © strider 2012-2013.
 */

package ru.strider.util;

/**
 * Util BuildConfig Class.
 * 
 * @author strider
 */
public class BuildConfig {
	
	//private static final String LOG_TAG = BuildConfig.class.getSimpleName();
	
	public static final String VERSION_ALPHA = "Alpha";
	public static final String VERSION_BETA = "Beta";
	public static final String VERSION_RC = "RC";
	public static final String VERSION_RELEASE = "Release";
	
	/**
	 * Defines current project Debugging state.
	 * <p>
	 * <br>Debug 	: true
	 * <br>Release 	: false
	 */
	public static final boolean DEBUG = true;//FIXME: TURN RELEASE ON
	
	/**
	 * Defines current project Version state.
	 * <p>
	 * <br>Alpha 	: VERSION_ALPHA
	 * <br>Beta 	: VERSION_BETA
	 * <br>RC 		: VERSION_RC
	 * <br>Release 	: VERSION_RELEASE
	 */
	public static final String VERSION = VERSION_BETA;
	
	private BuildConfig() {
		throw new AssertionError();
	}
	
}
