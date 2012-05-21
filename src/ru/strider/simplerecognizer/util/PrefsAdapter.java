/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Util PrefsAdapter Class
 * By © strider 2012.
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
	
	private static final String LOG_TAG = "PrefsAdapter";
	
	private final Context mContext;
	
	private String mLanguageCode = null;
	
	private boolean mIsKeepScreenOn = false;
	private boolean mIsFullScreen = false;
	
	public PrefsAdapter(Context context) {
		mContext = context;
		
		getValues();
	}
	
	public void getValues() {
		Resources res = mContext.getResources();
		
		//SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = mContext.getSharedPreferences(
				res.getString(R.string.preferences_file_name),
				Context.MODE_PRIVATE
			);
		
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
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "getValues() called");
	}
	
	public void setValues() {
		Resources res = mContext.getResources();
		
		SharedPreferences preferences = mContext.getSharedPreferences(
				res.getString(R.string.preferences_file_name),
				Context.MODE_PRIVATE
			);
		Editor editor = preferences.edit();
		
		editor.putString(res.getString(R.string.prefs_key_localization_language_code), mLanguageCode);
		
		editor.putBoolean(res.getString(R.string.prefs_key_display_is_keep_screen_on), mIsKeepScreenOn);
		editor.putBoolean(res.getString(R.string.prefs_key_display_is_full_screen), mIsFullScreen);
		
		editor.commit();
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "setValues() called");
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
	
}
