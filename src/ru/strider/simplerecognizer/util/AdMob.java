/*
 * Copyright (C) 2013 strider
 * 
 * Simple Recognizer
 * Util AdMob Class
 * By © strider 2013.
 */

package ru.strider.simplerecognizer.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;
import android.widget.LinearLayout;

import java.io.File;
import java.lang.ref.WeakReference;

import org.json.JSONException;

import cloud4apps.Utils;
import cloud4apps.Licensing.LicenseInfo;
import cloud4apps.Licensing.LicenseServices;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import ru.strider.simplerecognizer.R;
import ru.strider.simplerecognizer.SimpleRecognizer;
import ru.strider.util.BuildConfig;
import ru.strider.util.Text;

/**
 * Util AdMob Class.
 * 
 * @author strider
 */
public class AdMob {
	
	private static final String LOG_TAG = AdMob.class.getSimpleName();
	
	private static final String VERSION_KEY_NO_ADS = "8-4-4-4-12";//FIXME: ADD KEY FOR DEPLOYMENT
	
	private static final String AD_FREE_PACKAGE = "com.bigtincan.android.adfree";
	
	private static final String HOSTS_PATH = "/etc/hosts";
	
	private static final String PUBLISHER_ID = "a14f71c5654af39";
	
	private static final String EMULATOR_INTEL_ATOM_X86_4_0_3 = "6501C509CD383EC804D00651B5FB19DB";
	private static final String EMULATOR_INTEL_ATOM_X86_4_1 = "C261CBC76429066573F07F9A8F91B5B6";
	private static final String EMULATOR_INTEL_ATOM_X86_4_2 = "B1072D981E85BAA5510BF710213E0DFC";
	private static final String DEVICE_SAMSUNG_GALAXY_S = "16DA42D4FB93A1B2C4682497878B3C12";
	private static final String DEVICE_HTC_DESIRE = "BBA022EC80EAA9F63042D98D032734E1";
	
	private AdMob() {
		//
	}
	
	public static void addAdView(Activity activity) {
		if (!activity.isFinishing()) {
			LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.linearLayoutAdView);
			
			if (linearLayout != null) {
				AdView adView = (AdView) linearLayout.findViewById(R.id.adView);
				
				if (adView == null) {
					adView = new AdView(activity, AdSize.SMART_BANNER, PUBLISHER_ID);
					
					adView.setId(R.id.adView);
					
					linearLayout.addView(adView, new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT
						));
				}
				
				adView.loadAd(getAdRequest());
			}
		}
	}
	
	public static void removeAdView(Activity activity) {
		LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.linearLayoutAdView);
		
		if (linearLayout != null) {
			AdView adView = (AdView) linearLayout.findViewById(R.id.adView);
			
			if (adView != null) {
				adView.stopLoading();
			}
		}
	}
	
	public static void destroyAdView(Activity activity) {
		LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.linearLayoutAdView);
		
		if (linearLayout != null) {
			AdView adView = (AdView) linearLayout.findViewById(R.id.adView);
			
			if (adView != null) {
				linearLayout.removeView(adView);
				
				adView.destroy();
			}
		}
	}
	
	public static AdRequest getAdRequest() {
		AdRequest adRequest = new AdRequest();
		
		if (BuildConfig.DEBUG) {
			adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
			adRequest.addTestDevice(EMULATOR_INTEL_ATOM_X86_4_0_3);
			adRequest.addTestDevice(EMULATOR_INTEL_ATOM_X86_4_1);
			adRequest.addTestDevice(EMULATOR_INTEL_ATOM_X86_4_2);
			adRequest.addTestDevice(DEVICE_SAMSUNG_GALAXY_S);
			adRequest.addTestDevice(DEVICE_HTC_DESIRE);
		}
		
		return adRequest;
	}
	
	public static void purchaseLisenceAsync(Context context) {
		(new AsyncLicensePurchase(context)).execute();
	}
	
	public static void initLisenceAsync(Activity activity) {
		initLisenceAsync(activity, false);
	}
	
	public static void initLisenceAsync(Activity activity, boolean isCheckAdFreePackage) {
		(new AsyncInitLicense(activity, isCheckAdFreePackage)).execute();
	}
	
	private static void checkAdFreePackage(final Activity activity) {
		final Context context = (Context) activity;
		
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(AD_FREE_PACKAGE, 0);
			
			File hostsFile = new File(HOSTS_PATH);
			
			if (hostsFile.exists() && (hostsFile.length() > (1024L * 1024L / 2L))) {
				StringBuilder sb = new StringBuilder();
				sb.append(context.getString(R.string.dialog_ad_free_found)).append(Text.LF);
				sb.append(info.packageName).append(Text.LF);
				sb.append("( ").append(HOSTS_PATH).append(Text.SEPARATOR)
						.append(hostsFile.length() / 1024L).append(" KB )");
				
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
							Log.i(LOG_TAG, "Exiting Application...");
							
							activity.finish();
							
							Process.killProcess(Process.myPid());
						}
						
					});
				
				AlertDialog alert = builder.create();
				alert.setCanceledOnTouchOutside(false);
				alert.show();
			} else {
				SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "AdFree Package :: Found >> "
						+ HOSTS_PATH + " :: (Size < ThresholdValue) || NULL");
			}
		} catch (NameNotFoundException e) {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "AdFree Package :: Not Found");
		}
	}
	
	/**
	 * AsyncTask AsyncLicensePurchase<Void, Void, Void> Class.
	 * 
	 * @author strider
	 */
	private static class AsyncLicensePurchase extends AsyncTask<Void, Void, Void> {
		
		private static final String LOG_TAG = AsyncLicensePurchase.class.getSimpleName();
		
		private Context mContext = null;
		
		public AsyncLicensePurchase(Context context) {
			mContext = context.getApplicationContext();
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
				Log.e(LOG_TAG, "doInBackground() >> " + e.toString());
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
		
		private static final String LOG_TAG = AsyncInitLicense.class.getSimpleName();
		
		private WeakReference<Activity> mWeakActivity = null;
		
		private boolean mIsCheckAdFreePackage = false;
		
		public AsyncInitLicense(Activity activity, boolean isCheckAdFreePackage) {
			mWeakActivity = new WeakReference<Activity>(activity);
			
			mIsCheckAdFreePackage = isCheckAdFreePackage;
		}
		
		@Override
		protected LicenseInfo doInBackground(Void... params) {
			LicenseInfo lic = null;
			
			Activity activity = mWeakActivity.get();
			
			if (activity != null) {
				Context context = ((Context) activity).getApplicationContext();
				
				activity = null;
				
				try {
					lic = LicenseInfo.GetActiveLicense(
							context,
							SimpleRecognizer.APP_KEY,
							Utils.GetClientId(context)
						);
				} catch (JSONException e) {
					Log.e(LOG_TAG, "Error getting active License");
					Log.w(LOG_TAG, e.toString());
				} catch (Exception e) {
					Log.e(LOG_TAG, "doInBackground() >> " + e.toString());
				}
			}
			
			return lic;
		}
		
		@Override
		protected void onPostExecute(LicenseInfo lic) {
			Activity activity = mWeakActivity.get();
			
			if (activity != null) {
				if ((lic != null)
						&& lic.VersionKey.contentEquals(VERSION_KEY_NO_ADS)
						&& lic.IsActive) {
					SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "License :: " + lic.VersionName);
					
					removeAdView(activity);
					
					destroyAdView(activity);
				} else {
					SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "License :: Free");
					
					addAdView(activity);
					
					if (mIsCheckAdFreePackage) {
						checkAdFreePackage(activity);
					}
				}
			}
		}
		
	}
	
}
