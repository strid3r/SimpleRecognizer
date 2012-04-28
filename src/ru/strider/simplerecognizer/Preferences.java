/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * PreferenceActivity Preferences Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdView;

import ru.strider.simplerecognizer.util.PrefsAdapter;

/**
 * PreferenceActivity Preferences Class.
 * 
 * @author strider
 */
public class Preferences extends SherlockPreferenceActivity {
	
	private static final String LOG_TAG = "Preferences";
	
	private PrefsAdapter mPrefsAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		doInit();
		
		if (savedInstanceState == null) {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is NULL");
		} else {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is ~NULL");
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		usePreferencesValues();
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onResume() called");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onPause() called");
	}
	
	@Override
	protected void onDestroy() {
		AdView adView = (AdView) this.findViewById(R.id.adView);
		
		if (adView != null) {
			adView.destroy();
		}
		
		super.onDestroy();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.preferences_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (android.R.id.home): {
				this.finish();
				
				//Intent intent = new Intent(this, Main.class);
				//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//startActivity(intent);
				
				return true;
			}
			case (R.id.preferencesMenuSave): {
				this.finish();
				
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	private void doInit() {
		mPrefsAdapter = new PrefsAdapter(this);
		
		this.setContentView(R.layout.preferences);
		
		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setSharedPreferencesName(getString(R.string.preferences_file_name));
		preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		this.addPreferencesFromResource(R.xml.preferences);
		
		//SimpleRecognizer.initLisence(//TODO: TO ENABLE
		//		Preferences.this,
		//		(LinearLayout) this.findViewById(R.id.linearLayoutAdView)
		//	);
		
		final ActionBar actionBar = this.getSupportActionBar();
		if (actionBar != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				actionBar.setHomeButtonEnabled(true);
				actionBar.setDisplayHomeAsUpEnabled(true);
				
				//actionBar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.background_header));
				//actionBar.setDisplayUseLogoEnabled(true);
				//actionBar.setDisplayShowHomeEnabled(true);
				//actionBar.setDisplayShowTitleEnabled(true);
			}
		} else {
			SimpleRecognizer.logIfDebug(Log.ERROR, LOG_TAG, "// TODO: getSupportActionBar() is NULL");
		}
	}
	
	private void usePreferencesValues() {
		mPrefsAdapter.getValues();
		
		if (mPrefsAdapter.getIsKeepScreenOn()) {
			this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		if (mPrefsAdapter.getIsFullScreen()) {
			this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "usePreferencesValues() called");
	}
	
	public void onClickButtonPurchase(View view) {
		//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		
		//new SimpleRecognizer().AsyncLicensePurchase(this).execute();//TODO: TO ENABLE
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
	
}
