/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * Util PrefsAdapter Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;

import ru.strider.simplerecognizer.R;
import ru.strider.simplerecognizer.SimpleRecognizer;

/**
 * Util PrefsAdapter Class.
 * 
 * @author strider
 */
public class PrefsAdapter {
	
	private static final String LOG_TAG = PrefsAdapter.class.getSimpleName();
	
	public static final int DEFAULT_VALUE_VERSION_CODE = 0;
	
	private static final String KEY_VERSION_CODE = "versionCode";
	
	private static volatile PrefsAdapter sInstance = null;
	
	private Context mContext = null;
	
	private String mLanguageCode = null;
	
	private boolean mIsKeepScreenOn = false;
	private boolean mIsFullScreen = false;
	private boolean mIsOrientation = true;
	
	private PrefsAdapter(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public static PrefsAdapter getInstance(Context context) {
		return getInstance(context, false);
	}
	
	public static PrefsAdapter getInstance(Context context, boolean isInit) {
		PrefsAdapter localInstance = sInstance;
		
		if (localInstance == null) {
			synchronized (PrefsAdapter.class) {
				localInstance = sInstance;
				
				if (localInstance == null) {
					sInstance = localInstance = new PrefsAdapter(context);
				}
			}
		}
		
		if (isInit) {
			localInstance.getValues();
		}
		
		return localInstance;
	}
	
	public static synchronized void release() {
		if (sInstance != null) {
			sInstance = null;
		}
	}
	
	public void getValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "getValues() called");
		
		SharedPreferences preferences = getSharedPreferences(mContext);
		
		Resources res = mContext.getResources();
		
		mLanguageCode = preferences.getString(
				res.getString(R.string.prefs_key_localization_language_code),
				res.getString(R.string.prefs_default_value_localization_language_code)
			);
		
		mIsKeepScreenOn = preferences.getBoolean(
				res.getString(R.string.prefs_key_display_is_keep_screen_on),
				res.getBoolean(R.bool.prefs_default_value_display_is_keep_screen_on)
			);
		mIsFullScreen = preferences.getBoolean(
				res.getString(R.string.prefs_key_display_is_full_screen),
				res.getBoolean(R.bool.prefs_default_value_display_is_full_screen)
			);
		mIsOrientation = preferences.getBoolean(
				res.getString(R.string.prefs_key_display_is_orientation),
				res.getBoolean(R.bool.prefs_default_value_display_is_orientation)
			);
	}
	
	public void setValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "setValues() called");
		
		SharedPreferences preferences = getSharedPreferences(mContext);
		
		Editor editor = preferences.edit();
		
		Resources res = mContext.getResources();
		
		editor.putString(res.getString(R.string.prefs_key_localization_language_code), mLanguageCode);
		
		editor.putBoolean(res.getString(R.string.prefs_key_display_is_keep_screen_on), mIsKeepScreenOn);
		editor.putBoolean(res.getString(R.string.prefs_key_display_is_full_screen), mIsFullScreen);
		editor.putBoolean(res.getString(R.string.prefs_key_display_is_orientation), mIsOrientation);
		
		editor.commit();
	}
	
	public String getLanguageCode() {
		return mLanguageCode;
	}
	
	public void setLanguageCode(String languageCode) {
		mLanguageCode = languageCode;
	}
	
	public boolean getIsKeepScreenOn() {
		return mIsKeepScreenOn;
	}
	
	public void setIsKeepScreenOn(boolean isKeepScreenOn) {
		mIsKeepScreenOn = isKeepScreenOn;
	}
	
	public boolean getIsFullScreen() {
		return mIsFullScreen;
	}
	
	public void setIsFullScreen(boolean isFullScreen) {
		mIsFullScreen = isFullScreen;
	}
	
	public boolean getIsOrientation() {
		return mIsOrientation;
	}
	
	public void setIsOrientation(boolean isOrientation) {
		mIsOrientation = isOrientation;
	}
	
	public static SharedPreferences getSharedPreferences(Context context) {
		//return PreferenceManager.getDefaultSharedPreferences(context);
		return context.getSharedPreferences(
				context.getString(R.string.preferences_file_name),
				Context.MODE_PRIVATE
			);
	}
	
	public static void clearValues(Context context) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "clearValues(Context context) called");
		
		SharedPreferences preferences = getSharedPreferences(context);
		
		preferences.edit().clear().commit();
	}
	
	public static int getVersionCode(Context context) {
		SharedPreferences preferences = getSharedPreferences(context);
		
		return preferences.getInt(KEY_VERSION_CODE, DEFAULT_VALUE_VERSION_CODE);
	}
	
	public static void setVersionCode(Context context, int versionCode) {
		SharedPreferences preferences = getSharedPreferences(context);
		
		Editor editor = preferences.edit();
		
		editor.putInt(KEY_VERSION_CODE, versionCode);
		
		editor.commit();
	}
	
}
