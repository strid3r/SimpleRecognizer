/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Util ConfigAdapter Class
 * By © strider 2012.
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
	
	private static final String LOG_TAG = "ConfigAdapter";
	
	public static final int DEFAULT_VALUE_COURSE_ID = 1;
	public static final int DEFAULT_VALUE_ITEM_ID = 1;
	
	public static final boolean DEFAULT_VALUE_IS_CREATOR = false;
	
	public static final String DEFAULT_VALUE_DIRECTORY = File.listRoots()[0].getPath();
	
	private final Context mContext;
	
	private int mCourseId = 0;
	private int mItemId = 0;
	
	private boolean mIsCreator = false; 
	
	private String mDirectory = null;
	
	public ConfigAdapter(Context context) {
		mContext = context;
		
		getValues();
	}
	
	public void getValues() {
		Resources res = mContext.getResources();
		
		//SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences config = mContext.getSharedPreferences(
				res.getString(R.string.config_file_name),
				Context.MODE_PRIVATE
			);
		
		mCourseId = config.getInt(res.getString(R.string.config_key_course_id), DEFAULT_VALUE_COURSE_ID);
		mItemId = config.getInt(res.getString(R.string.config_key_item_id), DEFAULT_VALUE_ITEM_ID);
		
		mIsCreator = config.getBoolean(res.getString(R.string.config_key_is_creator), DEFAULT_VALUE_IS_CREATOR);
		
		mDirectory = config.getString(res.getString(R.string.config_key_directory), DEFAULT_VALUE_DIRECTORY);
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "getValues() called");
	}
	
	public void setValues() {
		Resources res = mContext.getResources();
		
		SharedPreferences config = mContext.getSharedPreferences(
				res.getString(R.string.config_file_name),
				Context.MODE_PRIVATE
			);
		Editor editor = config.edit();
		
		editor.putInt(res.getString(R.string.config_key_course_id), mCourseId);
		editor.putInt(res.getString(R.string.config_key_item_id), mItemId);
		
		editor.putBoolean(res.getString(R.string.config_key_is_creator), mIsCreator);
		
		editor.putString(res.getString(R.string.config_key_directory), mDirectory);
		
		editor.commit();
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "setValues() called");
	}
	
	public void setDefaultValues() {
		setDefaultValues(false);
	}
	
	public void setDefaultValues(boolean isWithSwitchMode) {
		setCourseId(DEFAULT_VALUE_COURSE_ID);
		setItemId(DEFAULT_VALUE_ITEM_ID);
		
		if (isWithSwitchMode) {
			setIsCreator(DEFAULT_VALUE_IS_CREATOR);
		}
		
		setDirectory(DEFAULT_VALUE_DIRECTORY);
		
		setValues();
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "setDefaultValues() called");
	}
	
	public int getCourseId() {
		return mCourseId;
	}
	
	public void setCourseId(int courseId) {
		mCourseId = courseId;
	}
	
	public int getItemId() {
		return mItemId;
	}
	
	public void setItemId(int itemId) {
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
	
}
