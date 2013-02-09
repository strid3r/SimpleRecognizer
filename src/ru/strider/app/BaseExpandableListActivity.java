/*
 * Copyright (C) 2013 strider
 * 
 * Application
 * ExpandableListActivity BaseExpandableListActivity Class
 * By © strider 2013.
 */

package ru.strider.app;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ExpandableListView;

import com.actionbarsherlock.app.SherlockExpandableListActivity;
import com.actionbarsherlock.view.MenuItem;

import ru.strider.simplerecognizer.Preferences;
import ru.strider.simplerecognizer.SimpleRecognizer;
import ru.strider.simplerecognizer.util.AdMob;

/**
 * ExpandableListActivity BaseExpandableListActivity Class.
 * 
 * @author strider
 */
public class BaseExpandableListActivity extends SherlockExpandableListActivity
		implements ActivityLifecycle {
	
	private static final String LOG_TAG = BaseExpandableListActivity.class.getSimpleName();
	
	private volatile boolean mIsAlive = false;
	private boolean mIsSaveInstanceState = false;
	private boolean mIsRestoreInstanceState = false;
	
	private boolean mIsCheckAdFree = false;
	private boolean mIsPreferences = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is NULL");
		} else {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is ~NULL");
		}
		
		super.onCreate(savedInstanceState);
		
		mIsAlive = true;
		mIsRestoreInstanceState = (savedInstanceState != null);
		
		mIsCheckAdFree = false;
		mIsPreferences = true;
		
		SimpleRecognizer.initActionBar(this, this.getSupportActionBar());
	}
	
	@Override
	protected void onResume() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onResume() called");
		
		super.onResume();
		
		//AdMob.initLisenceAsync(this, mIsCheckAdFree); // FIXME: ENABLE FOR DEPLOYMENT
		
		if (mIsPreferences) {
			Preferences.usePreferencesValues(this);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onSIS() called");
		
		super.onSaveInstanceState(outState);
		
		mIsSaveInstanceState = true;
	}
	
	@Override
	protected void onPause() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onPause() called");
		
		super.onPause();
		
		AdMob.removeAdView(this);
	}
	
	@Override
	protected void onDestroy() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onDestroy() called");
		
		mIsAlive = false;
		
		AdMob.destroyAdView(this);
		
		super.onDestroy();
	}
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (android.R.id.home): {
				this.finish();
				
				//Intent intent = new Intent(this, Main.class);
				//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//this.startActivity(intent);
				
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	public void setCheckAdFree(boolean isCheckAdFree) {
		mIsCheckAdFree = isCheckAdFree;
	}
	
	public void setInitPreferences(boolean isPreferences) {
		mIsPreferences = isPreferences;
	}
	
	@Override
	public BaseExpandableListActivity getActivity() {
		return this;
	}
	
	@Override
	public boolean isAlive() {
		return mIsAlive;
	}
	
	@Override
	public boolean isSaveInstanceState() {
		return mIsSaveInstanceState;
	}
	
	@Override
	public boolean isRestoreInstanceState() {
		return mIsRestoreInstanceState;
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
			int childPosition, long id) {
		return false;
	}
	
	@Override
	public void onGroupExpand(int groupPosition) {
		//
	}
	
	@Override
	public void onGroupCollapse(int groupPosition) {
		//
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case (KeyEvent.KEYCODE_VOLUME_UP): {
				//
				return true;
			}
			case (KeyEvent.KEYCODE_VOLUME_DOWN): {
				//
				return true;
			}
			default: {
				return super.onKeyDown(keyCode, event);
			}
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case (KeyEvent.KEYCODE_VOLUME_UP): {
				//
				return true;
			}
			case (KeyEvent.KEYCODE_VOLUME_DOWN): {
				//
				return true;
			}
			default: {
				return super.onKeyUp(keyCode, event);
			}
		}
	}
	
}
