/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BasePreferenceActivity Preferences Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import ru.strider.app.BasePreferenceActivity;
import ru.strider.simplerecognizer.util.AdMob;

/**
 * BasePreferenceActivity Preferences Class.
 * 
 * @author strider
 */
public class Preferences extends BasePreferenceActivity {
	
	//private static final String LOG_TAG = Preferences.class.getSimpleName();
	
	private static final String KEY_IS_PURCHASE = "isPurchase";
	
	private boolean mIsPurchase = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		doInit();
		
		if (savedInstanceState != null) {
			mIsPurchase = savedInstanceState.getBoolean(KEY_IS_PURCHASE, false);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (mIsPurchase) {
			AdMob.checkLisenceAsync();
			
			mIsPurchase = false;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean(KEY_IS_PURCHASE, mIsPurchase);
	}
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.preferences_menu, menu);
		
		return true;
	}
	*/
	@SuppressWarnings("deprecation")
	private void doInit() {
		this.setContentView(R.layout.preferences);
		
		PreferenceManager preferenceManager = this.getPreferenceManager();
		preferenceManager.setSharedPreferencesName(this.getString(R.string.preferences_file_name));
		preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		this.addPreferencesFromResource(R.xml.preferences);
	}
	
	public void onClickButtonPurchase(View view) {
		//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		
		if (SimpleRecognizer.isNetworkAvailable()) {
			AdMob.purchaseLisenceAsync();
			
			mIsPurchase = true;
		} else {
			SimpleRecognizer.makeToast(R.string.error_no_network, Toast.LENGTH_SHORT).show();
		}
	}
	
}
