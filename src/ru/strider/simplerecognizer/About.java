/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BaseActivity About Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import ru.strider.app.BaseActivity;
import ru.strider.simplerecognizer.util.PrefsAdapter;
import ru.strider.util.BuildConfig;
import ru.strider.util.Text;

/**
 * BaseActivity About Class.
 * 
 * @author strider
 */
public class About extends BaseActivity {
	
	private static final String LOG_TAG = About.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		doInit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		PrefsAdapter.getInstance().getValues().useValues(this, false);
	}
	
	private void doInit() {
		this.setInitPreference(false);
		
		this.setContentView(R.layout.about);
		
		//((TextView) this.findViewById(R.id.textViewAboutName)).setSelected(true);
		
		TextView textViewVersion = (TextView) this.findViewById(R.id.textViewAboutVersion);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getString(R.string.app_version));
		sb.append(": ");
		
		try {
			sb.append(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
			sb.append(Text.SEPARATOR);
			
			if (BuildConfig.DEBUG) {
				sb.append("Debug");
			} else {
				sb.append(BuildConfig.VERSION);
			}
			
			textViewVersion.setText(sb.toString());
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, e.toString());
		}
		
		//textViewVersion.setSelected(true);
		
		//((TextView) this.findViewById(R.id.textViewAboutCopyright)).setSelected(true);
	}
	
}
