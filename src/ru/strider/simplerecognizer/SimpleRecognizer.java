/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Application Simple Recognizer Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import cloud4apps.Utils;

import com.actionbarsherlock.app.ActionBar;

import ru.strider.app.MediaReceiver;
import ru.strider.simplerecognizer.util.PrefsAdapter;
import ru.strider.util.BuildConfig;
import ru.strider.widget.util.Font;

/**
 * Application Simple Recognizer Class.
 * 
 * @author strider
 */
public class SimpleRecognizer extends Application {
	
	private static final String LOG_TAG = SimpleRecognizer.class.getSimpleName();
	
	public static final String APP_KEY = "N/A";//FIXME: ADD KEY FOR DEPLOYMENT
	
	private static final String LOG_DEBUG = "[ DEBUG ] ";
	
	public static MediaReceiver mediaReceiver = null;
	
	private Locale mLocale = null;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: Configuration Changed");
		
		super.onConfigurationChanged(newConfig);
		
		if (mLocale != null) { // FIXME: BAD SOLUTION
			Locale.setDefault(mLocale);
			
			newConfig.locale = mLocale;
			
			Resources res = this.getResources();
			
			res.updateConfiguration(newConfig, res.getDisplayMetrics());
		}
	}
	
	@Override
	public void onCreate() {
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: Launched");
		
		super.onCreate();
		
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		int versionCode = PrefsAdapter.getVersionCode(this);
		
		try {
			PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			
			if (versionCode != info.versionCode) {
				//if (versionCode < 7) {
				//	ConfigAdapter.clearValues(this);
				//} else if (versionCode < 8) {
				//	//
				//}
				
				PrefsAdapter.setVersionCode(this, info.versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "onCreate() >> " + e.toString());
		}
		
		mediaReceiver = new MediaReceiver();
		
		Resources res = this.getResources(); // FIXME: BAD SOLUTION
		
		Configuration config = res.getConfiguration();
		
		String languageCode = PrefsAdapter.getInstance(this, true).getLanguageCode();
		
		if ((!languageCode.equals(res.getString(R.string.locale_language_code_default)))
				&& (!config.locale.getLanguage().equals(languageCode))) {
			mLocale = new Locale(languageCode);
			
			Locale.setDefault(mLocale);
			
			config.locale = mLocale;
			
			res.updateConfiguration(config, res.getDisplayMetrics());
		}
	}
	
	@Override
	public void onLowMemory() {
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: On Low Memory");
		
		super.onLowMemory();
	}
	
	@Override
	public void onTerminate() {
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: Terminated");
		
		super.onTerminate();
	}
	
	public static ActionBar initActionBar(Activity activity, ActionBar actionBar) {
		if (actionBar == null) {
			return null;
		}
		
		Resources res = ((Context) activity).getResources();
		
		TextView title = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			actionBar.setHomeButtonEnabled(true);
			
			title = (TextView) activity.findViewById(
					Resources.getSystem().getIdentifier("action_bar_title", "id", "android")
				);
		} else {
			actionBar.setHomeButtonEnabled(true); // FIXME: Only for custom Title Font
			
			title = (TextView) activity.findViewById(
					com.actionbarsherlock.R.id.abs__action_bar_title
				);
			
			BitmapDrawable header = (BitmapDrawable) res.getDrawable(R.drawable.bkgd_tile_black);
			header.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
			
			actionBar.setBackgroundDrawable(header);
		}
		
		if (title != null) {
			title.setTypeface(getTypefaceMain(activity.getResources()));
			title.setTextSize(
					TypedValue.COMPLEX_UNIT_SP,
					Math.round(title.getTextSize() / res.getDisplayMetrics().scaledDensity + 4.0f)
				);
		} else {
			Log.w(LOG_TAG, "Failed to obtain ActionBar Title reference.");
		}
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		return actionBar;
	}
	
	public static Typeface getTypefaceMain(Resources res) {
		String font = null;
		
		if (res.getConfiguration().locale.getLanguage()
				.equals(res.getString(R.string.locale_language_code_ru))) {
			font = Font.AGENCY_FB_REGULAR_CYRILLIC;
		} else {
			font = Font.AGENCY_FB_BOLD;
		}
		
		return Font.getTypeface(res, font);
	}
	
	public static void logIfDebug(int priority, String tag, String msg) {
		if (BuildConfig.DEBUG) {
			msg = LOG_DEBUG + msg;
			
			switch (priority) {
				case (Log.VERBOSE): {
					Log.v(tag, msg);
					
					break;
				}
				case (Log.DEBUG): {
					Log.d(tag, msg);
					
					break;
				}
				case (Log.INFO): {
					Log.i(tag, msg);
					
					break;
				}
				case (Log.WARN): {
					Log.w(tag, msg);
					
					break;
				}
				case (Log.ERROR): {
					Log.e(tag, msg);
					
					break;
				}
				default: {
					Log.d(tag, msg);
					
					break;
				}
			}
		}
	}
	
	public static Toast makeToast(Context context, int resId, int duration) throws Resources.NotFoundException {
		return makeToast(context, context.getResources().getText(resId), duration);
	}
	
	public static Toast makeToast(Context context, CharSequence text, int duration) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.toast, null);
		
		TextView textView = (TextView) view.findViewById(R.id.textViewToast);
		textView.setText(text);
		
		Toast toast = new Toast(context.getApplicationContext()); 
		toast.setView(view);
		toast.setDuration(duration);
		
		return toast;
	}
	
	public static boolean checkCourseCreator(Context context, String creator) {
		return TextUtils.equals(Utils.GetEmail(context), creator);
	}
	
}
