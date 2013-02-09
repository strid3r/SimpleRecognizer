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
	
	private static volatile ConfigAdapter sInstance = null;
	
	private Context mContext = null;
	
	private long mCourseId = 0L;
	private long mItemId = 0L;
	
	private boolean mIsCreator = false; 
	
	private String mDirectory = null;
	
	private ConfigAdapter(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public static ConfigAdapter getInstance(Context context) {
		return getInstance(context, false);
	}
	
	public static ConfigAdapter getInstance(Context context, boolean isInit) {
		ConfigAdapter localInstance = sInstance;
		
		if (localInstance == null) {
			synchronized (ConfigAdapter.class) {
				localInstance = sInstance;
				
				if (localInstance == null) {
					sInstance = localInstance = new ConfigAdapter(context);
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
		
		SharedPreferences config = getSharedPreferences(mContext);
		
		Resources res = mContext.getResources();
		
		mCourseId = config.getLong(res.getString(R.string.config_key_course_id), DEFAULT_VALUE_COURSE_ID);
		mItemId = config.getLong(res.getString(R.string.config_key_item_id), DEFAULT_VALUE_ITEM_ID);
		
		mIsCreator = config.getBoolean(res.getString(R.string.config_key_is_creator), DEFAULT_VALUE_IS_CREATOR);
		
		mDirectory = config.getString(res.getString(R.string.config_key_directory), DEFAULT_VALUE_DIRECTORY);
	}
	
	public void setValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "setValues() called");
		
		SharedPreferences config = getSharedPreferences(mContext);
		
		Editor editor = config.edit();
		
		Resources res = mContext.getResources();
		
		editor.putLong(res.getString(R.string.config_key_course_id), mCourseId);
		editor.putLong(res.getString(R.string.config_key_item_id), mItemId);
		
		editor.putBoolean(res.getString(R.string.config_key_is_creator), mIsCreator);
		
		editor.putString(res.getString(R.string.config_key_directory), mDirectory);
		
		editor.commit();
	}
	
	public long getCourseId() {
		return mCourseId;
	}
	
	public void setCourseId(long courseId) {
		mCourseId = courseId;
	}
	
	public long getItemId() {
		return mItemId;
	}
	
	public void setItemId(long itemId) {
		mItemId = itemId;
	}
	
	public boolean getIsCreator() {
		return mIsCreator;
	}
	
	public void setIsCreator(boolean isCreator) {
		mIsCreator = isCreator;
	}
	
	public String getDirectory() {
		return mDirectory;
	}
	
	public void setDirectory(String directory) {
		mDirectory = directory;
	}
	
	public void setDefaultValues() {
		setDefaultValues(false);
	}
	
	public void setDefaultValues(boolean isWithSwitchMode) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "setDefaultValues() called");
		
		setCourseId(DEFAULT_VALUE_COURSE_ID);
		setItemId(DEFAULT_VALUE_ITEM_ID);
		
		if (isWithSwitchMode) {
			setIsCreator(DEFAULT_VALUE_IS_CREATOR);
		}
		
		setDirectory(DEFAULT_VALUE_DIRECTORY);
		
		setValues();
	}
	
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(
				context.getString(R.string.config_file_name),
				Context.MODE_PRIVATE
			);
	}
	
	public static void clearValues(Context context) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "clearValues(Context context) called");
		
		SharedPreferences config = getSharedPreferences(context);
		
		config.edit().clear().commit();
	}
	
}
