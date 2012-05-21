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
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;

import java.io.File;
import java.util.Locale;

import org.json.JSONException;

import cloud4apps.Utils;
import cloud4apps.Licensing.LicenseInfo;
import cloud4apps.Licensing.LicenseServices;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import ru.strider.simplerecognizer.util.BuildConfig;
import ru.strider.simplerecognizer.util.PrefsAdapter;

/**
 * Application Simple Recognizer Class.
 * 
 * @author strider
 */
public class SimpleRecognizer extends Application {
	
	private static final String LOG_TAG = "SimpleRecognizer";
	
	public static final String SEPARATOR = " :: ";
	public static final String BR_LINE = "\n";
	
	public static final String APP_KEY = "N/A";//TODO
	public static final String VERSION_KEY_NO_ADS = "N/A";//TODO
	
	private static final String FONT_AGENCYB = "Fonts/AGENCYB.TTF";
	
	private static final String LOG_DEBUG = "[ DEBUG ] ";
	
	private static final String AD_FREE_PACKAGE = "com.bigtincan.android.adfree";
	
	private static final String HOSTS_PATH = "/etc/hosts";
	
	private static final long KB = 1024L;
	private static final long MB = KB * KB;	
	
	private static final String ADMOB_PUBLISHER_ID = "a14f8f618f8b291";
	
	private static final LinearLayout.LayoutParams LAYOUT_PARAMS = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		);
	
	private PrefsAdapter mPrefsAdapter = null;
	
	private Locale mLocale = null;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (mLocale != null) {
			Resources res = this.getBaseContext().getResources();
			
			Locale.setDefault(mLocale);
			
			newConfig.locale = mLocale;
			
			res.updateConfiguration(newConfig, res.getDisplayMetrics());
		}
		
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: Configuration Changed");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		doInit();
		
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: Launched");
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: On Low Memory");
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		Log.i(LOG_TAG, this.getString(R.string.app_name) + " :: Terminated");
	}
	
	private void doInit() {
		mPrefsAdapter = new PrefsAdapter(this);
		
		usePreferencesValues();
	}
	
	private void usePreferencesValues() {
		mPrefsAdapter.getValues();
		
		Resources res = this.getBaseContext().getResources();
		
		Configuration config = res.getConfiguration();
		
		String languageCode = mPrefsAdapter.getLanguageCode();
		
		if (!languageCode.equals(res.getString(R.string.locale_language_code_default))
				&& !config.locale.getLanguage().equals(languageCode)) {
			Locale locale = new Locale(languageCode);
			
			Locale.setDefault(locale);
			
			config.locale = locale;
			
			res.updateConfiguration(config, res.getDisplayMetrics());
		}
		
		logIfDebug(Log.INFO, LOG_TAG, "usePreferencesValues() called");
	}
	
	public static final Typeface getTypefaceMain(Context context) {
		return Typeface.createFromAsset(context.getAssets(), FONT_AGENCYB);
	}
	
	public static final void logIfDebug(int priority, String tag, String msg) {
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
					//
					break;
				}
			}
		}
	}
	
	public static final void initLisence(Activity activity, LinearLayout linearLayout) {
		initLisence(activity, linearLayout, false, false);
	}
	
	public static final void initLisence(Activity activity, LinearLayout linearLayout, boolean isLog) {
		initLisence(activity, linearLayout, isLog, false);
	}
	
	public static final void initLisence(Activity activity, LinearLayout linearLayout, boolean isLog, boolean isCheckAdFreePackage) {
		new AsyncInitLicense(activity, linearLayout, isLog, isCheckAdFreePackage).execute();
	}
	
	private static final void checkAdFreePackage(final Context context) {
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(AD_FREE_PACKAGE, 0);
			
			File hostsFile = new File(HOSTS_PATH);
			
			if (hostsFile.exists() && (hostsFile.length() > (MB / 2))) {
				StringBuilder sb = new StringBuilder();
				sb.append(context.getString(R.string.dialog_ad_free_found)).append(BR_LINE);
				sb.append(info.packageName).append(BR_LINE);
				sb.append("( ").append(HOSTS_PATH).append(SEPARATOR).append(hostsFile.length() / KB).append(" KB )");
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.app_name);
				builder.setMessage(sb.toString());
				builder.setCancelable(false);
				
				builder.setNegativeButton(R.string.dialog_button_buy_license, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new AsyncLicensePurchase(context).execute();
					}
					
				});
				
				builder.setPositiveButton(R.string.dialog_button_exit, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Process.killProcess(Process.myPid());
					}
					
				});
				
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				logIfDebug(Log.WARN, LOG_TAG, "AdFree Package :: Found >> " + HOSTS_PATH + " :: (Size < ThresholdValue) || NULL");
			}
		} catch (PackageManager.NameNotFoundException e) {
			logIfDebug(Log.WARN, LOG_TAG, "AdFree Package :: Not Found");
		}
	}
	
	public static final boolean checkCourseCreator(Context context, String creator) {
		return TextUtils.equals(Utils.GetEmail(context), creator);
	}
	
	/**
	 * AsyncTask AsyncLicensePurchase<Void, Void, Void> Class.
	 * 
	 * @author strider
	 */
	public static class AsyncLicensePurchase extends AsyncTask<Void, Void, Void> {
		
		private static final String LOG_TAG = "AsyncLicensePurchase";
		
		private Context mContext = null;
		
		public AsyncLicensePurchase(Context context) {
			mContext = context;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				LicenseServices.Purchase(
						mContext,
						SimpleRecognizer.APP_KEY,
						Utils.GetClientId(mContext),
						Utils.GetEmail(mContext)
					);
			} catch (Exception e) {
				Log.e(LOG_TAG, "doInBackground() >> " + e.getMessage());
			}
			
			return null;
		}
		
	}
	
	/**
	 * AsyncTask AsyncInitLicense<Void, Void, LicenseInfo> Class.
	 * 
	 * @author strider
	 */
	private static class AsyncInitLicense extends AsyncTask<Void, Void, LicenseInfo> {
		
		private static final String LOG_TAG = "AsyncInitLicense";
		
		private Context mContext = null;
		
		private Activity mActivity = null; 
		
		private LinearLayout mLinearLayout = null;
		
		private boolean mIsLog = false;
		
		private boolean mIsCheckAdFreePackage = false;
		
		public AsyncInitLicense(Activity activity, LinearLayout linearLayout, boolean isLog, boolean isCheckAdFreePackage) {
			mContext = (Context) activity;
			
			mActivity = activity;
			
			mLinearLayout = linearLayout;
			
			mIsLog = isLog;
			
			mIsCheckAdFreePackage = isCheckAdFreePackage;
		}
		
		@Override
		protected LicenseInfo doInBackground(Void... params) {
			LicenseInfo lic = null;
			
			try {
				lic = LicenseInfo.GetActiveLicense(
						mContext,
						APP_KEY,
						Utils.GetClientId(mContext)
					);
			} catch (JSONException e) {
				Log.e(LOG_TAG, "ErrorGettingActiveLicense");
				Log.w(LOG_TAG, e.getMessage());
			} catch (Exception e) {
				Log.e(LOG_TAG, "doInBackground() >> " + e.getMessage());
			}
			
			return lic;
		}
		
		@Override
		protected void onPostExecute(LicenseInfo lic) {
			AdView adView = (AdView) mLinearLayout.findViewById(R.id.adView);
			
			if (TextUtils.equals(lic.VersionKey, VERSION_KEY_NO_ADS) && lic.IsActive) {
				if (mIsLog) {
					SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "License :: No Ads");
				}
				
				if (adView != null) {
					mLinearLayout.removeView(adView);
				}
			} else {
				if (mIsLog) {
					SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "License :: Free");
				}
				
				if (mIsCheckAdFreePackage) {
					checkAdFreePackage(mContext);
				}
				
				if (adView == null) {
					adView = new AdView(mActivity, AdSize.BANNER, ADMOB_PUBLISHER_ID);
					
					adView.setId(R.id.adView);
					
					mLinearLayout.addView(adView, LAYOUT_PARAMS);
				}
				
				AdRequest adRequest = new AdRequest();
				
				adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
				adRequest.addTestDevice("CC37D88448286ACC82EC41CB988E5E97");//SAMSUNG_GALAXY_S
				adRequest.addTestDevice("BBA022EC80EAA9F63042D98D032734E1");//HTC_DESIRE
				
				adView.loadAd(adRequest);
			}
		}
		
	}
	
}
