/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * Util PrefsAdapter Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

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
	
	public static int requestedOrientation = Configuration.ORIENTATION_UNDEFINED;
	
	private String mLanguageCode = null;
	
	private boolean mIsKeepScreenOn = false;
	private boolean mIsFullScreen = false;
	private boolean mIsOrientation = true;
	
	private PrefsAdapter() {
		//
	}
	
	public static PrefsAdapter getInstance() {
		return PrefsHolder.INSTANCE;
	}
	
	public PrefsAdapter getValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "getValues() called");
		
		SharedPreferences preferences = getSharedPreferences();
		
		Resources res = SimpleRecognizer.getPackageContext().getResources();
		
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
		
		return this;
	}
	
	public PrefsAdapter setValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "setValues() called");
		
		SharedPreferences preferences = getSharedPreferences();
		
		Editor editor = preferences.edit();
		
		Resources res = SimpleRecognizer.getPackageContext().getResources();
		
		editor.putString(res.getString(R.string.prefs_key_localization_language_code), mLanguageCode);
		
		editor.putBoolean(res.getString(R.string.prefs_key_display_is_keep_screen_on), mIsKeepScreenOn);
		editor.putBoolean(res.getString(R.string.prefs_key_display_is_full_screen), mIsFullScreen);
		editor.putBoolean(res.getString(R.string.prefs_key_display_is_orientation), mIsOrientation);
		
		editor.commit();
		
		return this;
	}
	
	public PrefsAdapter useValues(Activity activity) {
		return useValues(activity, true);
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public PrefsAdapter useValues(Activity activity, boolean isWithOrientation) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useValues(...) called");
		
		if (mIsKeepScreenOn) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		if (mIsFullScreen) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		if (isWithOrientation) {
			if (requestedOrientation == Configuration.ORIENTATION_UNDEFINED) {
				requestedOrientation = ((Context) activity).getResources().getConfiguration().orientation;
			}
			
			if (mIsOrientation) {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			} else {
				switch (requestedOrientation) {
					case (Configuration.ORIENTATION_PORTRAIT): {
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
							activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
						} else {
							activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);	
						}
						
						break;
					}
					case (Configuration.ORIENTATION_LANDSCAPE): {
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
							activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
						} else {
							activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
						}
						
						break;
					}
					default: {
						activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
						
						break;
					}
				}
			}
		}
		
		return this;
	}
	
	public String getLanguageCode() {
		return mLanguageCode;
	}
	
	public PrefsAdapter setLanguageCode(String languageCode) {
		mLanguageCode = languageCode;
		
		return this;
	}
	
	public boolean getIsKeepScreenOn() {
		return mIsKeepScreenOn;
	}
	
	public PrefsAdapter setIsKeepScreenOn(boolean isKeepScreenOn) {
		mIsKeepScreenOn = isKeepScreenOn;
		
		return this;
	}
	
	public boolean getIsFullScreen() {
		return mIsFullScreen;
	}
	
	public PrefsAdapter setIsFullScreen(boolean isFullScreen) {
		mIsFullScreen = isFullScreen;
		
		return this;
	}
	
	public boolean getIsOrientation() {
		return mIsOrientation;
	}
	
	public PrefsAdapter setIsOrientation(boolean isOrientation) {
		mIsOrientation = isOrientation;
		
		return this;
	}
	
	public static SharedPreferences getSharedPreferences() {
		//return PreferenceManager.getDefaultSharedPreferences(context);
		return SimpleRecognizer.getPackageContext().getSharedPreferences(
				SimpleRecognizer.getPackageContext().getString(R.string.preferences_file_name),
				Context.MODE_PRIVATE
			);
	}
	
	public static void clearValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "clearValues() called");
		
		SharedPreferences preferences = getSharedPreferences();
		
		preferences.edit().clear().commit();
	}
	
	public static int getVersionCode() {
		SharedPreferences preferences = getSharedPreferences();
		
		return preferences.getInt(KEY_VERSION_CODE, DEFAULT_VALUE_VERSION_CODE);
	}
	
	public static void setVersionCode(int versionCode) {
		SharedPreferences preferences = getSharedPreferences();
		
		Editor editor = preferences.edit();
		
		editor.putInt(KEY_VERSION_CODE, versionCode);
		
		editor.commit();
	}
	
	/**
	 * PrefsAdapter PrefsHolder Class.
	 * 
	 * @author strider
	 */
	private static class PrefsHolder {
		
		private static final PrefsAdapter INSTANCE = new PrefsAdapter();
		
	}
	
}
