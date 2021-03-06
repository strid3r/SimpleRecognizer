/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * Util ConfigAdapter Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;

import java.io.File;

import ru.strider.simplerecognizer.R;
import ru.strider.simplerecognizer.SimpleRecognizer;

/**
 * Util ConfigAdapter Class.
 * 
 * @author strider
 */
public class ConfigAdapter {
	
	private static final String LOG_TAG = ConfigAdapter.class.getSimpleName();
	
	public static final long DEFAULT_VALUE_COURSE_ID = 1L;
	public static final long DEFAULT_VALUE_ITEM_ID = 1L;
	
	public static final boolean DEFAULT_VALUE_IS_CREATOR = false;
	
	public static final String DEFAULT_VALUE_DIRECTORY = File.listRoots()[0].getPath();
	
	private long mCourseId = 0L;
	private long mItemId = 0L;
	
	private boolean mIsCreator = false; 
	
	private String mDirectory = null;
	
	private ConfigAdapter() {
		//
	}
	
	public static ConfigAdapter getInstance() {
		return ConfigHolder.INSTANCE;
	}
	
	public ConfigAdapter getValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "getValues() called");
		
		SharedPreferences config = getSharedPreferences();
		
		Resources res = SimpleRecognizer.getPackageContext().getResources();
		
		mCourseId = config.getLong(res.getString(R.string.config_key_course_id), DEFAULT_VALUE_COURSE_ID);
		mItemId = config.getLong(res.getString(R.string.config_key_item_id), DEFAULT_VALUE_ITEM_ID);
		
		mIsCreator = config.getBoolean(res.getString(R.string.config_key_is_creator), DEFAULT_VALUE_IS_CREATOR);
		
		mDirectory = config.getString(res.getString(R.string.config_key_directory), DEFAULT_VALUE_DIRECTORY);
		
		return this;
	}
	
	public ConfigAdapter setValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "setValues() called");
		
		SharedPreferences config = getSharedPreferences();
		
		Editor editor = config.edit();
		
		Resources res = SimpleRecognizer.getPackageContext().getResources();
		
		editor.putLong(res.getString(R.string.config_key_course_id), mCourseId);
		editor.putLong(res.getString(R.string.config_key_item_id), mItemId);
		
		editor.putBoolean(res.getString(R.string.config_key_is_creator), mIsCreator);
		
		editor.putString(res.getString(R.string.config_key_directory), mDirectory);
		
		editor.commit();
		
		return this;
	}
	
	public long getCourseId() {
		return mCourseId;
	}
	
	public ConfigAdapter setCourseId(long courseId) {
		mCourseId = courseId;
		
		return this;
	}
	
	public long getItemId() {
		return mItemId;
	}
	
	public ConfigAdapter setItemId(long itemId) {
		mItemId = itemId;
		
		return this;
	}
	
	public boolean getIsCreator() {
		return mIsCreator;
	}
	
	public ConfigAdapter setIsCreator(boolean isCreator) {
		mIsCreator = isCreator;
		
		return this;
	}
	
	public String getDirectory() {
		return mDirectory;
	}
	
	public ConfigAdapter setDirectory(String directory) {
		mDirectory = directory;
		
		return this;
	}
	
	public void setDefaultValues() {
		setDefaultValues(false);
	}
	
	public ConfigAdapter setDefaultValues(boolean isWithSwitchMode) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "setDefaultValues() called");
		
		setCourseId(DEFAULT_VALUE_COURSE_ID);
		setItemId(DEFAULT_VALUE_ITEM_ID);
		
		if (isWithSwitchMode) {
			setIsCreator(DEFAULT_VALUE_IS_CREATOR);
		}
		
		setDirectory(DEFAULT_VALUE_DIRECTORY);
		
		setValues();
		
		return this;
	}
	
	public static SharedPreferences getSharedPreferences() {
		return SimpleRecognizer.getPackageContext().getSharedPreferences(
				SimpleRecognizer.getPackageContext().getString(R.string.config_file_name),
				Context.MODE_PRIVATE
			);
	}
	
	public static void clearValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "clearValues() called");
		
		SharedPreferences config = getSharedPreferences();
		
		config.edit().clear().commit();
	}
	
	/**
	 * ConfigAdapter ConfigHolder Class.
	 * 
	 * @author strider
	 */
	private static class ConfigHolder {
		
		private static final ConfigAdapter INSTANCE = new ConfigAdapter();
		
	}
	
}
