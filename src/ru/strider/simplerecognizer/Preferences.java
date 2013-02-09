/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * PreferenceActivity Preferences Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import ru.strider.simplerecognizer.util.AdMob;
import ru.strider.simplerecognizer.util.PrefsAdapter;

/**
 * PreferenceActivity Preferences Class.
 * 
 * @author strider
 */
public class Preferences extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private static final String LOG_TAG = Preferences.class.getSimpleName();
	
	private static int sRequestedOrientation = Configuration.ORIENTATION_UNDEFINED;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is NULL");
		} else {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is ~NULL");
		}
		
		super.onCreate(savedInstanceState);
		
		doInit();
	}
	
	@Override
	protected void onResume() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onResume() called");
		
		super.onResume();
		
		//AdMob.initLisenceAsync(this); // FIXME: ENABLE FOR DEPLOYMENT
		
		usePreferencesValues(this);
		
		this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onPause() called");
		
		super.onPause();
		
		AdMob.removeAdView(this);
		
		this.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onDestroy() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onDestroy() called");
		
		AdMob.destroyAdView(this);
		
		super.onDestroy();
	}
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.preferences_menu, menu);
		
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
	
	private void doInit() {
		SimpleRecognizer.initActionBar(this, this.getSupportActionBar());
		
		this.setContentView(R.layout.preferences);
		
		PreferenceManager preferenceManager = this.getPreferenceManager();
		preferenceManager.setSharedPreferencesName(this.getString(R.string.preferences_file_name));
		preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		this.addPreferencesFromResource(R.xml.preferences);
	}
	
	public void onClickButtonPurchase(View view) {
		//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		
		//if (AppEngine.isNetworkAvailable(this)) {
			AdMob.purchaseLisenceAsync(this);
		//} else {
		//	SimpleRecognizer.makeToast(this, R.string.error_no_network, Toast.LENGTH_SHORT).show();
		//}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		sRequestedOrientation = this.getResources().getConfiguration().orientation;
		
		usePreferencesValues(this);
	}
	/*
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
	*/
	public static PrefsAdapter usePreferencesValues(Activity activity) {
		return usePreferencesValues(activity, true);
	}
	
	public static PrefsAdapter usePreferencesValues(Activity activity, boolean isWithOrientation) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "usePreferencesValues(...) called");
		
		PrefsAdapter prefsAdapter = PrefsAdapter.getInstance((Context) activity, true);
		
		if (prefsAdapter.getIsKeepScreenOn()) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		if (prefsAdapter.getIsFullScreen()) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		if (isWithOrientation) {
			if (sRequestedOrientation == Configuration.ORIENTATION_UNDEFINED) {
				sRequestedOrientation = ((Context) activity).getResources().getConfiguration().orientation;
			}
			
			if (prefsAdapter.getIsOrientation()) {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			} else {
				switch (sRequestedOrientation) {
					case (Configuration.ORIENTATION_PORTRAIT): {
						activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
						
						break;
					}
					case (Configuration.ORIENTATION_LANDSCAPE): {
						activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
						
						break;
					}
					default: {
						activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
						
						break;
					}
				}
			}
		}
		
		return prefsAdapter;
	}
	
}
