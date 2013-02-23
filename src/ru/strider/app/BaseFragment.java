/*
 * Copyright (C) 2013 strider
 * 
 * Application
 * Fragment BaseFragment Class
 * By © strider 2013.
 */

package ru.strider.app;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import ru.strider.simplerecognizer.SimpleRecognizer;

/**
 * Fragment BaseFragment Class.
 * 
 * @author strider
 */
public class BaseFragment extends SherlockFragment {
	
	private static final String LOG_TAG = BaseFragment.class.getSimpleName();
	
	private volatile boolean mIsDestroy = false;
	
	public static BaseFragment newInstance() {
		BaseFragment fragment = new BaseFragment();
		
		Bundle args = new Bundle();
		
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is NULL");
		} else {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is ~NULL");
		}
		
		super.onCreate(savedInstanceState);
		
		mIsDestroy = false;
	}
	
	@Override
	public void onResume() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onResume() called");
		
		super.onResume();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onSIS() called");
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onPause() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onPause() called");
		
		super.onPause();
	}
	
	@Override
	public void onDestroyView() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onDestroyView() called");
		
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onDestroy() called");
		
		mIsDestroy = true;
		
		super.onDestroy();
	}
	
	public boolean isDestroy() {
		return mIsDestroy;
	}
	
}
