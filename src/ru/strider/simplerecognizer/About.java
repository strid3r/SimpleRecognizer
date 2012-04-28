/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Activity About Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdView;

import ru.strider.simplerecognizer.util.PrefsAdapter;
import ru.strider.widget.MainTextView;

/**
 * Activity About Class.
 * 
 * @author strider
 */
public class About extends SherlockActivity {
	
	private static final String LOG_TAG = "About";
	
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
	
	/*//TODO:?
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
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
				//startActivity(intent);
				
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	private void doInit() {
		mPrefsAdapter = new PrefsAdapter(this);
		
		this.setContentView(R.layout.about);
		
		MainTextView textView = (MainTextView) this.findViewById(R.id.textViewAboutVersion);
		
		try {
			textView.setText(this.getString(R.string.app_version) + " " + this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
		
		//SimpleRecognizer.initLisence(//TODO: TO ENABLE
		//		About.this,
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
