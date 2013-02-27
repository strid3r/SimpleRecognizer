/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Application Simple Recognizer Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
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

import ru.strider.app.BaseDialogFragment;
import ru.strider.app.MediaReceiver;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.util.AdMob;
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
	
	public static final String APP_KEY = "N/A"; // FIXME: ADD KEY FOR DEPLOYMENT
	
	private static final String LOG_DEBUG = "[ DEBUG ] ";
	
	private static Context sPackageContext = null;
	
	private static boolean sIsDataBase = false;
	
	private static boolean sIsAdMobHazard = false;
	
	private static MediaReceiver sMediaReceiver = null;
	
	private Locale mLocale = null;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: Configuration Changed");
		
		super.onConfigurationChanged(newConfig);
		
		// FIXME: VERY BAD APPROACH. Required by TOR...
		/*if (mLocale != null) {
			Locale.setDefault(mLocale);
			
			newConfig.locale = mLocale;
			
			Resources res = this.getResources();
			
			res.updateConfiguration(newConfig, res.getDisplayMetrics()); // Infinite loop...
		}*/
		//
	}
	
	@Override
	public void onCreate() {
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: Launched");
		
		super.onCreate();
		
		sPackageContext = this.getApplicationContext();
		
		doInit();
	}
	
	@Override
	public void onLowMemory() {
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: On Low Memory");
		
		super.onLowMemory();
	}
	
	@Override
	public void onTerminate() { // Only for Emulator
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: Terminated");
		
		super.onTerminate();
		
		if (sPackageContext != null) {
			sPackageContext = null;
		}
	}
	
	private void doInit() {
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		//AdMob.checkLisenceAsync(); // ENABLE FOR DEPLOYMENT
		
		sIsDataBase = DataBaseAdapter.getInstance().createDataBase();
		
		sIsAdMobHazard = AdMob.checkAdFreePackage();
		
		int versionCode = PrefsAdapter.getVersionCode();
		
		try {
			PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			
			if (versionCode != info.versionCode) {
				//if (versionCode < 7) {
				//	ConfigAdapter.clearValues(this);
				//} else if (versionCode < 8) {
				//	//
				//}
				
				PrefsAdapter.setVersionCode(info.versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "onCreate() >> " + e.toString());
		}
		
		sMediaReceiver = new MediaReceiver();
		
		// FIXME: VERY BAD APPROACH. Required by TOR...
		Resources res = this.getResources();
		
		Configuration config = res.getConfiguration();
		
		String languageCode = PrefsAdapter.getInstance().getValues().getLanguageCode();
		
		if ((!languageCode.equals(res.getString(R.string.locale_language_code_default)))
				&& (!config.locale.getLanguage().equals(languageCode))) {
			mLocale = new Locale(languageCode);
			
			Locale.setDefault(mLocale);
			
			config.locale = mLocale;
			
			res.updateConfiguration(config, res.getDisplayMetrics());
		}
		//
	}
	
	public static Context getPackageContext() {
		return sPackageContext;
	}
	
	public static boolean isDataBase() {
		return sIsDataBase;
	}
	
	public static boolean isAdMobHazard() {
		return sIsAdMobHazard;
	}
	
	public static MediaReceiver getMediaReceiver() {
		return sMediaReceiver;
	}
	
	public static ActionBar initActionBar(Activity activity, ActionBar actionBar) {
		if (actionBar == null) {
			return null;
		}
		
		Resources res = sPackageContext.getResources();
		
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
			title.setTypeface(getTypefaceMain());
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
	
	public static Typeface getTypefaceMain() {
		String font = null;
		
		Resources res = sPackageContext.getResources();
		
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
	
	public static Toast makeToast(int resId, int duration) throws Resources.NotFoundException {
		return makeToast(sPackageContext.getResources().getText(resId), duration);
	}
	
	public static Toast makeToast(CharSequence text, int duration) {
		LayoutInflater inflater = LayoutInflater.from(sPackageContext);
		View view = inflater.inflate(R.layout.toast, null);
		
		TextView textView = (TextView) view.findViewById(R.id.textViewToast);
		textView.setText(text);
		
		Toast toast = new Toast(sPackageContext); 
		toast.setView(view);
		toast.setDuration(duration);
		
		return toast;
	}
	
	public static boolean checkCourseCreator(String creator) {
		return TextUtils.equals(Utils.GetEmail(sPackageContext), creator);
	}
	
	public static boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) sPackageContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		
		return ((networkInfo != null) && networkInfo.isConnectedOrConnecting());
	}
	
	public static void exit(Activity activity) {
		Log.i(LOG_TAG, "Exiting Application...");
		
		if (activity != null) {
			activity.finish();
		}
		
		Process.killProcess(Process.myPid());
	}
	
	/**
	 * BaseDialogFragment MessageDialog Class.
	 * 
	 * @author strider
	 */
	public static class MessageDialog extends BaseDialogFragment {
		
		//private static final String LOG_TAG = MessageDialog.class.getSimpleName();
		
		public static final String KEY = MessageDialog.class.getSimpleName();
		
		private static final String KEY_MESSAGE_TITLE = "messageTitle";
		private static final String KEY_MESSAGE_INFO = "messageInfo";
		private static final String KEY_MESSAGE_HINT = "messageHint";
		
		private String mMessageTitle = null;
		private String mMessageInfo = null;
		private String mMessageHint = null;
		
		private View mTitle = null;
		private View mView = null;
		
		public static MessageDialog newInstance(String title, String info, String hint) {
			MessageDialog fragment = new MessageDialog();
			
			Bundle args = new Bundle();
			args.putString(KEY_MESSAGE_TITLE, title);
			args.putString(KEY_MESSAGE_INFO, info);
			args.putString(KEY_MESSAGE_HINT, hint);
			
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public String getMessageTitle() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.getString(KEY_MESSAGE_TITLE) : null);
		}
		
		public String getMessageInfo() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.getString(KEY_MESSAGE_INFO) : null);
		}
		
		public String getMessageHint() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.getString(KEY_MESSAGE_HINT) : null);
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mMessageTitle = getMessageTitle();
			mMessageInfo = getMessageInfo();
			mMessageHint = getMessageHint();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = LayoutInflater.from((Context) this.getSherlockActivity());
			
			mTitle = inflater.inflate(R.layout.alert_dialog_title, null);
			mView = inflater.inflate(R.layout.alert_dialog_message, null);
			
			this.registerNegativeButton(mView, R.id.buttonAlertDialogNegative);
			this.registerPositiveButton(mView, R.id.buttonAlertDialogPositive);
			
			return (new AlertDialog.Builder(inflater.getContext()))
					.setCustomTitle(mTitle)
					.setView(mView)
					.create();
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			TextView textViewTitle = (TextView) mTitle.findViewById(R.id.textViewAlertDialogTitle);
			textViewTitle.setText(mMessageTitle);
			textViewTitle.setSelected(true);
			
			TextView textViewInfo = (TextView) mView.findViewById(R.id.textViewMessageInfo);
			textViewInfo.setText(mMessageInfo);
			
			if (!TextUtils.isEmpty(mMessageHint)) {
				TextView textViewHint = (TextView) mView.findViewById(R.id.textViewMessageHint);
				textViewHint.setVisibility(View.VISIBLE);
				textViewHint.setText(mMessageHint);
			}
		}
		
		@Override
		public void onDestroyView() {
			super.onDestroyView();
			
			mTitle = null;
			mView = null;
		}
		
		@Override
		public void onDestroy() {
			mMessageTitle = null;
			mMessageInfo = null;
			mMessageHint = null;
			
			super.onDestroy();
		}
		
	}
	
	/**
	 * MessageDialog MessageNeutralDialog Class.
	 * 
	 * @author strider
	 */
	public static class MessageNeutralDialog extends MessageDialog {
		
		//private static final String LOG_TAG = MessageNeutralDialog.class.getSimpleName();
		
		public static final String KEY = MessageNeutralDialog.class.getSimpleName();
		
		private View mTitle = null;
		private View mView = null;
		
		public static MessageNeutralDialog newInstance(String title, String info, String hint) {
			MessageNeutralDialog fragment = new MessageNeutralDialog();
			
			Bundle args = new Bundle();
			args.putString(MessageDialog.KEY_MESSAGE_TITLE, title);
			args.putString(MessageDialog.KEY_MESSAGE_INFO, info);
			args.putString(MessageDialog.KEY_MESSAGE_HINT, hint);
			
			fragment.setArguments(args);
			
			return fragment;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = LayoutInflater.from((Context) this.getSherlockActivity());
			
			mTitle = inflater.inflate(R.layout.alert_dialog_title, null);
			mView = inflater.inflate(R.layout.alert_dialog_message, null);
			
			this.registerNeutralButton(mView, R.id.buttonAlertDialogNeutral);
			
			return (new AlertDialog.Builder(inflater.getContext()))
					.setCustomTitle(mTitle)
					.setView(mView)
					.create();
		}
		
		@Override
		public void onDestroyView() {
			super.onDestroyView();
			
			mTitle = null;
			mView = null;
		}
		
	}
	
}
