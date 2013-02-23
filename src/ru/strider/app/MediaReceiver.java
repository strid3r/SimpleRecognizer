/*
 * Copyright (C) 2013 strider
 * 
 * Application
 * BroadcastReceiver MediaReceiver Interface
 * By © strider 2013.
 */

package ru.strider.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;

import ru.strider.simplerecognizer.SimpleRecognizer;
import ru.strider.util.Text;

/**
 * BroadcastReceiver MediaReceiver Interface.
 * 
 * @author strider
 */
public class MediaReceiver extends BroadcastReceiver {
	
	private static final String LOG_TAG = MediaReceiver.class.getSimpleName();
	
	private boolean mIsExternalStorageAvailable = false;
	private boolean mIsExternalStorageWritable = false;
	
	public MediaReceiver() {
		//
	}
	
	public boolean isExternalStorageAvailable() {
		return mIsExternalStorageAvailable;
	}
	
	public boolean isExternalStorageWritable() {
		return mIsExternalStorageWritable;
	}
	
	public void startWatchingExternalStorage(Context context) {
		updateExternalStorageState();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		
		context.registerReceiver(this, intentFilter);
	}
	
	public void stopWatchingExternalStorage(Context context) {
		context.unregisterReceiver(this);
	}
	
	private void updateExternalStorageState() {
		String state = Environment.getExternalStorageState();
		
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			SimpleRecognizer.logIfDebug(
					Log.DEBUG,
					LOG_TAG,
					("Media" + Text.SEPARATOR + "Writable")
				);
			
			mIsExternalStorageAvailable = mIsExternalStorageWritable = true;
		} else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			SimpleRecognizer.logIfDebug(
					Log.DEBUG,
					LOG_TAG,
					("Media" + Text.SEPARATOR + "Available")
				);
			
			mIsExternalStorageAvailable = true;
			mIsExternalStorageWritable = false;
		} else {
			SimpleRecognizer.logIfDebug(
					Log.DEBUG,
					LOG_TAG,
					("Media" + Text.SEPARATOR + Text.NOT_AVAILABLE)
				);
			
			mIsExternalStorageAvailable = mIsExternalStorageWritable = false;
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_TAG, ("Storage: " + intent.getData()));
		
		updateExternalStorageState();
	}
	
}
