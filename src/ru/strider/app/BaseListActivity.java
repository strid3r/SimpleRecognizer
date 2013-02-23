/*
 * Copyright (C) 2013 strider
 * 
 * Application
 * ListActivity BaseListActivity Class
 * By © strider 2013.
 */

package ru.strider.app;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import ru.strider.simplerecognizer.SimpleRecognizer;
import ru.strider.simplerecognizer.util.AdMob;
import ru.strider.simplerecognizer.util.PrefsAdapter;

/**
 * ListActivity BaseListActivity Class.
 * 
 * @author strider
 */
public class BaseListActivity extends SherlockListActivity {
	
	private static final String LOG_TAG = BaseListActivity.class.getSimpleName();
	
	private volatile boolean mIsDestroy = false;
	
	private boolean mIsLicense = false;
	private boolean mIsPreference = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is NULL");
		} else {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is ~NULL");
		}
		
		super.onCreate(savedInstanceState);
		
		mIsDestroy = false;
		
		mIsLicense = true;
		mIsPreference = true;
		
		SimpleRecognizer.initActionBar(this, this.getSupportActionBar());
	}
	
	@Override
	protected void onResume() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onResume() called");
		
		super.onResume();
		
		if (mIsLicense) {
			//AdMob.initLicense(this); // FIXME: ENABLE FOR DEPLOYMENT
		}
		
		if (mIsPreference) {
			PrefsAdapter.getInstance().getValues().useValues(this);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onSIS() called");
		
		super.onSaveInstanceState(outState);
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
		
		mIsDestroy = true;
		
		AdMob.destroyAdView(this);
		
		super.onDestroy();
	}
	
	public boolean isDestroy() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return mIsDestroy;
		} else {
			return super.isDestroyed();
		}
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
	
	public void setInitLicense(boolean isLicense) {
		mIsLicense = isLicense;
	}
	
	public void setInitPreference(boolean isPreference) {
		mIsPreference = isPreference;
	}
	
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
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
