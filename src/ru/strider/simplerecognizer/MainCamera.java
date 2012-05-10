/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Activity Main Camera Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import ru.strider.simplerecognizer.adapter.SpinnerArrayAdapter;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.model.Item;
import ru.strider.simplerecognizer.model.PHash;
import ru.strider.simplerecognizer.util.BuildConfig;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.simplerecognizer.util.ImagePHash;
import ru.strider.simplerecognizer.util.PrefsAdapter;

/**
 * Activity Main Class.
 * 
 * @author strider
 */
public class MainCamera extends SherlockActivity implements ShutterCallback, PictureCallback {
	
	private static final String LOG_TAG = "MainCamera";
	
	private static final long BACK_PRESS_INTERVAL = 3500L;
	
	private static final int VIEW_BUTTON_SHUTTER_POSITION = 0;
	private static final int VIEW_PROGRESS_SHUTTER_POSITION = 1;
	
	private long mLastBackPress = 0L;
	private Toast mToastBackPress = null;
	
	private PrefsAdapter mPrefsAdapter = null;
	private ConfigAdapter mConfigAdapter = null;
	
	private Camera mCamera = null;
	
	private CameraPreview mPreview = null;
	
	private ViewSwitcher mViewSwitcher = null;
	
	private int mNumberOfCameras = -1;
	private int mCurrentCamera = -1;
	private int mCameraCurrentlyLocked = -1;
	
	private int mDefaultCameraId = -1;
	
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
		
		useConfigValues();
		
		obtainCamera();
		
		//new AsyncLoadView().execute();
		
		//SimpleRecognizer.initLisence(//TODO: TO ENABLE
		//		MainCamera.this,
		//		(LinearLayout) this.findViewById(R.id.linearLayoutAdView),
		//		true,
		//		true
		//	);
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onResume() called");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onSIS() called");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mConfigAdapter.setValues();
		
		releaseCamera();
		
		mToastBackPress.cancel();
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onPause() called");
	}
	
	@Override
	protected void onDestroy() {
		//AdView adView = (AdView) this.findViewById(R.id.adView);
		
		//if (adView != null) {
		//	adView.destroy();
		//}
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mViewSwitcher.getDisplayedChild() == VIEW_PROGRESS_SHUTTER_POSITION) {
			return false;
		}
		
		MenuItem itemSwitchMode = menu.findItem(R.id.mainMenuSwitchMode);
		
		if (mConfigAdapter.getIsCreator()) {
			itemSwitchMode.setTitle(R.string.main_menu_switch_mode_viewer);
		} else {
			itemSwitchMode.setTitle(R.string.main_menu_switch_mode_creator);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.mainMenuSwitchMode): {
				mConfigAdapter.setIsCreator(!mConfigAdapter.getIsCreator());
				
				return true;
			}
			case (R.id.mainMenuCourse): {
				Intent iSelectCourse = new Intent(MainCamera.this, SelectCourse.class);
				this.startActivity(iSelectCourse);
				
				return true;
			}
			case (R.id.mainMenuSwitchCamera): {
				switchCamera();
				
				return true;
			}
			case (R.id.mainMenuPreferences): {
				Intent iPreferences = new Intent(MainCamera.this, Preferences.class);
				this.startActivity(iPreferences);
				
				return true;
			}
			case (R.id.mainMenuAbout): {
				Intent iAbout = new Intent(MainCamera.this, About.class);
				this.startActivity(iAbout);
				
				return true;
			}
			case (R.id.mainMenuExit): {
				Log.i(LOG_TAG, "Application Exit called");
				
				super.onDestroy();
				
				Process.killProcess(Process.myPid());
				//System.runFinalizersOnExit(true);
				//System.exit(0);
				
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	private void doInit() {
		mPrefsAdapter = new PrefsAdapter(this);
		mConfigAdapter = new ConfigAdapter(this);
		
		mNumberOfCameras = Camera.getNumberOfCameras();//TODO: SINCE API 9
		
		CameraInfo cameraInfo = new CameraInfo();
		
		for (int i = 0; i < mNumberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				mCurrentCamera = mDefaultCameraId = i;
			}
		}
		
		this.setContentView(R.layout.main_camera);
		
		mPreview = (CameraPreview) this.findViewById(R.id.cameraPreview);
		
		mViewSwitcher = (ViewSwitcher) this.findViewById(R.id.viewSwitcherShutter);
		
		((ImageButton) mViewSwitcher.findViewById(R.id.imageButtonShutter)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mViewSwitcher.setDisplayedChild(VIEW_PROGRESS_SHUTTER_POSITION);
				
				mCamera.takePicture(MainCamera.this, null, null, MainCamera.this);
			}
			
		});
		
		mToastBackPress = Toast.makeText(this, this.getString(R.string.key_button_back_double_press), Toast.LENGTH_LONG);
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
	
	private void useConfigValues() {
		mConfigAdapter.getValues();
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
	}
	
	private void obtainCamera() {
		mCamera = getCameraInstance(mCurrentCamera);
		
		mCameraCurrentlyLocked = mCurrentCamera;
		
		if (mCamera != null) {
			mPreview.setCamera(mCamera);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.main_dialog_camera_error_title);
			builder.setMessage("Couldn't obtain Camera with id: " + Integer.toString(mCurrentCamera));
			builder.setCancelable(false);
			
			builder.setNegativeButton(R.string.dialog_button_try_next, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switchCamera();
				}
				
			});
			
			builder.setPositiveButton(R.string.dialog_button_exit, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Process.killProcess(Process.myPid());
				}
				
			});
			
			AlertDialog alert = builder.create();
			alert.show();
		}
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "obtainCamera() called");
	}
	
	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "releaseCamera() called");
	}
	
	private void switchCamera() {
		if (mNumberOfCameras > 1) {
			releaseCamera();
			
			mCurrentCamera = (mCameraCurrentlyLocked + 1) % mNumberOfCameras;
			
			obtainCamera();
			
			mPreview.switchCamera();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.main_dialog_camera_alert_title);
			builder.setMessage(R.string.main_dialog_camera_alert_message);
			builder.setNeutralButton(R.string.dialog_button_close, null);
			
			AlertDialog alert = builder.create();
			alert.show();
		}
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "switchCamera() called");
	}
	
	@Override
	public void onShutter() {
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "ShutterCallback() called");
	}
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		new AsyncGetImagePHash(this, data, camera).execute();
		
		//
		
		SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "PictureCallback() called");
	}
	
	@Override
	public void onBackPressed() {
		long pressBackTime = SystemClock.elapsedRealtime();
		
		if ((pressBackTime - mLastBackPress) > BACK_PRESS_INTERVAL) {
			mToastBackPress.show();
		} else {
			mToastBackPress.cancel();
			
			super.onBackPressed();
		}
		
		mLastBackPress = pressBackTime;
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
	
	public static Camera getCameraInstance(int cameraId) {
		Camera camera = null;
		
		try {
			camera = Camera.open(cameraId);
		} catch (Exception e) {
			//
		}
		
		return camera;
	}
	
	/**
	 * AsyncTask AsyncGetPHash<Void, Void, String> Class.
	 * 
	 * @author strider
	 */
	private class AsyncGetImagePHash extends AsyncTask<Void, Void, String> {
		
		private static final String LOG_TAG = "AsyncGetImagePHash";
		
		private long mInitTime = 0L;
		private long mWorkTime = 0L;
		
		private Context mContext = null;
		
		private Activity mActivity = null;
		
		private byte[] mData = null;
		
		private Camera mCamera = null;
		
		private ImagePHash mImagePHash = null;
		
		public AsyncGetImagePHash(Activity activity, byte[] data, Camera camera) {
			mContext = (Context) activity;
			
			mActivity = activity;
			
			mData = data;
			
			mCamera = camera;
		}
		
		@Override
		protected void onPreExecute() {
			DataBaseAdapter dbAdapter = new DataBaseAdapter(mContext);
			dbAdapter.createDataBase(mActivity);
			dbAdapter.open();
			
			Course course = dbAdapter.getCourse(mConfigAdapter.getCourseId());
			
			dbAdapter.close();
			
			if (course == null) {
				this.cancel(true);
			}
		}
		
		@Override
		protected String doInBackground(Void... params) {
			mInitTime = SystemClock.elapsedRealtime();
			
			//
			
			if (this.isCancelled()) {
				return null;
			}
			
			//
			
			mImagePHash = new ImagePHash(
					//ImagePHash.DCT_SIZE_FAST,
					//ImagePHash.DCT_LOW_SIZE
				);
			
			String pHashHex = null;
			
			try {
				pHashHex = mImagePHash.getPHash(mData);
			} catch (OutOfMemoryError e) {
				//FIXME: Temp Handler For Not Intended Behavior
				mActivity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(mContext, "[ DEBUG ] OutOfMemoryError", Toast.LENGTH_SHORT).show();
					}
					
				});
			}
			
			//
			
			mWorkTime = SystemClock.elapsedRealtime() - mInitTime;
			
			return pHashHex;
		}
		
		@Override
		protected void onCancelled() {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.dialog_title_caution);
			builder.setMessage(R.string.main_dialog_no_course_message);
			
			builder.setNegativeButton(R.string.dialog_button_close, null);
			
			builder.setPositiveButton(R.string.main_menu_course, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent iSelectCourse = new Intent(MainCamera.this, SelectCourse.class);
					mActivity.startActivity(iSelectCourse);
				}
				
			});
			
			AlertDialog alert = builder.create();
			alert.show();
			
			//
			restoreCamera();
			//
		}
		
		@Override
		protected void onPostExecute(String result) {
			StringBuilder sb = new StringBuilder();
			
			sb.append("Work Time: ").append(mWorkTime).append(" ms.").append(SimpleRecognizer.BR_LINE);
			
			final DataBaseAdapter dbAdapter = new DataBaseAdapter(mContext);
			dbAdapter.createDataBase(mActivity);
			
			if (mConfigAdapter.getIsCreator()) {
				dbAdapter.open();
				
				Course course = dbAdapter.getCourse(mConfigAdapter.getCourseId());
				
				final List<Item> listItem = dbAdapter.getListItem(mConfigAdapter.getCourseId());
				
				dbAdapter.close();
				
				if (/*TODO: TEMP*/BuildConfig.DEBUG || SimpleRecognizer.checkCourseCreator(mContext, course.getCreator())) {
					int itemPosition = AdapterView.INVALID_POSITION;
					
					List<String> listTitle = new ArrayList<String>();
					for (Item item : listItem) {
						listTitle.add(item.getTitle());
						
						if (item.getId() == mConfigAdapter.getItemId()) {
							itemPosition = listTitle.indexOf(item.getTitle());
						}
					}
					
					LayoutInflater inflater = LayoutInflater.from(mContext);
					final View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
					final View view = inflater.inflate(R.layout.alert_dialog_manage_phash_edit, null);
					
					final TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
					textViewTitle.setText(course.getCategory() + SimpleRecognizer.SEPARATOR + course.getTitle());
					
					final Spinner spinnerItem = (Spinner) view.findViewById(R.id.spinnerItem);
					
					SpinnerArrayAdapter adapter = new SpinnerArrayAdapter(mContext, listTitle);
					
					spinnerItem.setAdapter(adapter);
					
					spinnerItem.setSelection(itemPosition);
					
					final EditText editTextHexValue = (EditText) view.findViewById(R.id.editTextHexValue);
					editTextHexValue.setText(result);
					
					final EditText editTextComment = (EditText) view.findViewById(R.id.editTextComment);
					
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setCustomTitle(viewTitle);
					builder.setView(view);
					
					builder.setNegativeButton(R.string.dialog_button_cancel, null);
					
					builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int itemPosition = spinnerItem.getSelectedItemPosition();
							
							if (itemPosition != AdapterView.INVALID_POSITION) {
								dbAdapter.write();
								
								dbAdapter.addPHash(new PHash(
										editTextHexValue.getText().toString(),
										editTextComment.getText().toString(),
										listItem.get(spinnerItem.getSelectedItemPosition()).getId()
									));
								
								dbAdapter.close();
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
								builder.setTitle(R.string.dialog_title_caution);
								builder.setMessage(R.string.main_dialog_no_item_message);
								builder.setNeutralButton(R.string.dialog_button_close, null);
								
								AlertDialog alert = builder.create();
								alert.show();
							}
						}
						
					});
					
					AlertDialog alert = builder.create();
					alert.show();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(R.string.dialog_title_forbidden);
					builder.setMessage(R.string.select_course_dialog_not_creator_message);
					builder.setNeutralButton(R.string.dialog_button_close, null);
					
					AlertDialog alert = builder.create();
					alert.show();
				}
			} else {
				//TODO: TO SEARCH FOR ITEM AND SHOW CONTENT
				sb.append("pHashHex: ").append((result != null) ? result : "null");
				
				Item itemResult = null;
				
				dbAdapter.open();
				
				List<Item> listItem = dbAdapter.getListItem(mConfigAdapter.getCourseId(), true);
				
				dbAdapter.close();
				
				List<PHash> listPHash = new ArrayList<PHash>();
				
				for (Item item : listItem) {
					item.initAllHammingDistance(result);
					
					PHash pHashItemMin = Item.findPHashMin(item.getListPHash());
					
					if (pHashItemMin != null) {
						listPHash.add(pHashItemMin);
					}
				}
				
				PHash pHashMin = Item.findPHashMin(listPHash);
				
				sb.append(SimpleRecognizer.BR_LINE).append("Hamming Distance List:");
				
				if ((pHashMin != null) && (pHashMin.getHammingDistance() < ImagePHash.HAMMING_DISTANCE_THRESHOLD)) {
					//PROFIT!!!
					for (Item item : listItem) {
						if (item.getId() == pHashMin.getItemId()) {
							itemResult = item;
						}
					}
					
					for (PHash pHash : itemResult.getListPHash()) {
						sb.append(" " + pHash.getHammingDistance());
					}
				} else {
					sb.append(" NO MATCH FOUND");
				}
				
				//
				
				LayoutInflater inflater = LayoutInflater.from(mContext);
				final View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				
				final TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText((itemResult != null) ? itemResult.getTitle() : "Item not found");
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setCustomTitle(viewTitle);
				builder.setMessage(sb.toString());
				
				builder.setNeutralButton(R.string.dialog_button_close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
					}
					
				});
				
				AlertDialog alert = builder.create();
				alert.show();
			}
			
			Log.d(LOG_TAG, sb.toString());
			
			//
			restoreCamera();
			//
		}
		
		private void restoreCamera() {
			try {
				mCamera.startPreview();
			} catch (Exception e) {
				//
			}
			
			mViewSwitcher.setDisplayedChild(VIEW_BUTTON_SHUTTER_POSITION);
		}
		
	}
	
	/*
	//implements OnSharedPreferenceChangeListener //@class
	//getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this); //@onResume
	//getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this); //@onPause
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PREFS_KEY_IS_TEMP)) {
			isTemp = sharedPreferences.getBoolean(key, false);
			
			if (isTemp) {
				//
			}
			
			return;
		}
		
		if (key.equals(PREFS_KEY_IS_KEEP_SCREEN_ON)) {
			isKeepScreenOn = sharedPreferences.getBoolean(key, false);
			
			if (isKeepScreenOn) {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			} else {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			}
			
			return;
		}
		
		if (key.equals(PREFS_KEY_IS_FULL_SCREEN)) {
			isFullScreen = sharedPreferences.getBoolean(key, true);
			
			if (isFullScreen) {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			
			return;
		}
	}
	*/
	
}
