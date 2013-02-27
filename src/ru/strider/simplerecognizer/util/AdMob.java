/*
 * Copyright (C) 2013 strider
 * 
 * Simple Recognizer
 * Util AdMob Class
 * By © strider 2013.
 */

package ru.strider.simplerecognizer.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import org.json.JSONException;

import cloud4apps.Utils;
import cloud4apps.Licensing.LicenseInfo;
import cloud4apps.Licensing.LicenseServices;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import ru.strider.app.BaseDialogFragment;
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
	
	private static final String VERSION_KEY_NO_ADS = "8-4-4-4-12"; // FIXME: ADD KEY FOR DEPLOYMENT
	
	private static final String AD_FREE_PACKAGE_OLD = "com.bigtincan.android.adfree";
	private static final String AD_FREE_PACKAGE = "com.bigtincan.adfree";
	
	private static final String HOSTS_PATH = "/etc/hosts";
	
	private static final String PUBLISHER_ID = "a14f71c5654af39";
	
	private static final String EMULATOR_INTEL_ATOM_X86_4_0_3 = "6501C509CD383EC804D00651B5FB19DB";
	private static final String EMULATOR_INTEL_ATOM_X86_4_1 = "C261CBC76429066573F07F9A8F91B5B6";
	private static final String EMULATOR_INTEL_ATOM_X86_4_2 = "B1072D981E85BAA5510BF710213E0DFC";
	private static final String DEVICE_SAMSUNG_GALAXY_S = "4BB4DFC11C5713B996D16683EA0A0831";
	private static final String DEVICE_HTC_DESIRE = "C41B592FE5EE6EEBD69918FDDF75BDA2";
	
	public static volatile int pendingLic = 0;
	
	private static volatile boolean sIsAsyncLic = true;
	
	private static volatile boolean sIsFree = true;
	
	private AdMob() {
		throw (new AssertionError());
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	public static void addAdView(Activity activity) {
		if (!activity.isFinishing()) {
			LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.linearLayoutAdView);
			
			if (linearLayout != null) {
				AdView adView = (AdView) linearLayout.findViewById(R.id.adView);
				
				if (adView == null) {
					adView = new AdView(activity, AdSize.SMART_BANNER, PUBLISHER_ID);
					
					adView.setId(R.id.adView);
					
					linearLayout.addView(adView, (new LinearLayout.LayoutParams(
							((Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO)
									? LinearLayout.LayoutParams.FILL_PARENT
									: LinearLayout.LayoutParams.MATCH_PARENT
								),
							LinearLayout.LayoutParams.WRAP_CONTENT
						)));
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
	
	public static boolean isAsyncLic() {
		return sIsAsyncLic;
	}
	
	public static boolean isFree() {
		return sIsFree;
	}
	
	public static void initLicense(Activity activity) {
		if (sIsFree) {
			addAdView(activity);
		} else {
			removeAdView(activity);
			
			destroyAdView(activity);
		}
	}
	
	public static void purchaseLisenceAsync() {
		(new AsyncLicensePurchase()).execute();
	}
	
	public static void checkLisenceAsync() {
		(new AsyncCheckLicense()).execute();
	}
	
	public static boolean checkAdFreePackage() {
		boolean isHazard = false;
		
		ApplicationInfo info = null;
		
		try {
			info = SimpleRecognizer.getPackageContext().getPackageManager()
					.getApplicationInfo(AD_FREE_PACKAGE_OLD, 0);
		} catch (NameNotFoundException e) {
			try {
				info = SimpleRecognizer.getPackageContext().getPackageManager()
						.getApplicationInfo(AD_FREE_PACKAGE, 0);
			} catch (NameNotFoundException ex) {
				SimpleRecognizer.logIfDebug(
						Log.WARN,
						LOG_TAG,
						("AdFree Package" + Text.SEPARATOR + "Not Found")
					);
			}
		}
		
		if (info != null) {
			File hostsFile = new File(HOSTS_PATH);
			
			if (hostsFile.exists() && (hostsFile.length() > (1024L * 1024L / 2L))) {
				isHazard = true;
			}
			
			SimpleRecognizer.logIfDebug(
					Log.WARN,
					LOG_TAG,
					("AdFree Package" + Text.SEPARATOR + (isHazard ? "Found" : "Disabled"))
				);
		}
		
		return isHazard;
	}
	
	/**
	 * BaseDialogFragment AdMobDialog Class.
	 * 
	 * @author strider
	 */
	public static class AdMobDialog extends BaseDialogFragment {
		
		//private static final String LOG_TAG = AdMobDialog.class.getSimpleName();
		
		public static final String KEY = AdMobDialog.class.getSimpleName();
		
		private View mTitle = null;
		private View mView = null;
		
		public static AdMobDialog newInstance() {
			AdMobDialog fragment = new AdMobDialog();
			
			Bundle args = new Bundle();
			
			fragment.setArguments(args);
			fragment.setCancelable(false);
			
			return fragment;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = LayoutInflater.from((Context) this.getSherlockActivity());
			
			mTitle = inflater.inflate(R.layout.alert_dialog_title, null);
			mView = inflater.inflate(R.layout.alert_dialog_ad_free, null);
			
			this.registerNegativeButton(mView, R.id.buttonAlertDialogBuyLicense);
			this.registerPositiveButton(mView, R.id.buttonAlertDialogExit);
			
			return (new AlertDialog.Builder(inflater.getContext()))
					.setCustomTitle(mTitle)
					.setView(mView)
					.create();
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			TextView textViewTitle = (TextView) mTitle.findViewById(R.id.textViewAlertDialogTitle);
			textViewTitle.setText(R.string.app_name);
			textViewTitle.setSelected(true);
		}
		
		@Override
		public void onDestroyView() {
			super.onDestroyView();
			
			mTitle = null;
			mView = null;
		}
		
		@Override
		public void onNegativeClick(View view) {
			if (SimpleRecognizer.isNetworkAvailable()) {
				purchaseLisenceAsync();
				
				super.onNegativeClick(view);
			} else {
				SimpleRecognizer.makeToast(R.string.error_no_network, Toast.LENGTH_SHORT).show();
			}
		}
		/*//TODO: TEST NEUTRAL BUTTON -> Manage Apps
		@Override
		public void onNeutralClick(View view) {
			super.onNeutralClick(view);
			
			Intent intent = new Intent();
			intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", "com.test.test", null);
			intent.setData(uri);
			startActivity(intent);
		}
		*/
		@Override
		public void onPositiveClick(View view) {
			super.onPositiveClick(view);
			
			SimpleRecognizer.exit(this.getSherlockActivity());
		}
		
	}
	
	/**
	 * AsyncTask AsyncLicensePurchase<Void, Void, Void> Class.
	 * 
	 * @author strider
	 */
	private static class AsyncLicensePurchase extends AsyncTask<Void, Void, Void> {
		
		private static final String LOG_TAG = AsyncLicensePurchase.class.getSimpleName();
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				LicenseServices.Purchase(
						SimpleRecognizer.getPackageContext(),
						SimpleRecognizer.APP_KEY,
						Utils.GetClientId(SimpleRecognizer.getPackageContext()),
						Utils.GetEmail(SimpleRecognizer.getPackageContext())
					);
			} catch (Exception e) {
				Log.e(LOG_TAG, "doInBackground() >> " + e.toString());
			}
			
			return null;
		}
		
	}
	
	/**
	 * AsyncTask AsyncCheckLicense<Void, Void, Void> Class.
	 * 
	 * @author strider
	 */
	private static class AsyncCheckLicense extends AsyncTask<Void, Void, Void> {
		
		private static final String LOG_TAG = AsyncCheckLicense.class.getSimpleName();
		
		@Override
		protected Void doInBackground(Void... params) {
			pendingLic++;
			
			synchronized (AsyncCheckLicense.class) {
				sIsAsyncLic = true;
				
				LicenseInfo lic = null;
				
				try {
					lic = LicenseInfo.GetActiveLicense(
							SimpleRecognizer.getPackageContext(),
							SimpleRecognizer.APP_KEY,
							Utils.GetClientId(SimpleRecognizer.getPackageContext())
						);
				} catch (JSONException e) {
					Log.e(LOG_TAG, "Error getting active License");
					Log.w(LOG_TAG, e.toString());
				} catch (Exception e) {
					Log.e(LOG_TAG, "doInBackground() >> " + e.toString());
				}
				
				if ((lic != null)
						&& lic.VersionKey.contentEquals(VERSION_KEY_NO_ADS)
						&& lic.IsActive) {
					sIsFree = false;
					
					SimpleRecognizer.logIfDebug(
							Log.WARN,
							LOG_TAG,
							("License" + Text.SEPARATOR + lic.VersionName)
						);
				} else {
					sIsFree = true;
					
					SimpleRecognizer.logIfDebug(
							Log.WARN,
							LOG_TAG,
							("License" + Text.SEPARATOR + "Free")
						);
				}
				
				sIsAsyncLic = false;
			}
			
			return null;
		}
		
	}
	
}
