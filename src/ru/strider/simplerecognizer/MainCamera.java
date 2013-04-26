/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BaseActivity MainCamera Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import ru.strider.app.BaseFragmentActivity;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.model.Item;
import ru.strider.simplerecognizer.model.PHash;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.simplerecognizer.util.ImagePHash;
import ru.strider.simplerecognizer.util.PrefsAdapter;
import ru.strider.simplerecognizer.view.OnItemListener;
import ru.strider.util.BuildConfig;
import ru.strider.util.Text;
import ru.strider.view.OnLockViewListener;
import ru.strider.widget.CameraPreview;

/**
 * BaseActivity MainCamera Class.
 * 
 * @author strider
 */
public class MainCamera extends BaseFragmentActivity implements OnLockViewListener, OnItemListener,
		Camera.ShutterCallback, Camera.PictureCallback {
	
	private static final String LOG_TAG = MainCamera.class.getSimpleName();
	
	private static final long BACK_PRESS_INTERVAL = 3500L;
	
	private static final int VIEW_SHUTTER_BUTTON_POSITION = 0;
	private static final int VIEW_SHUTTER_PROGRESS_POSITION = 1;
	
	private static final int CAMERA_DEFAULT_ID = -1;
	
	private long mLastBackPress = 0L;
	private Toast mToastBackPress = null;
	
	private ConfigAdapter mConfigAdapter = null;
	
	private Menu mMainMenu = null;
	
	private ActionBar mActionBar = null;
	
	private CameraPreview mPreview = null;
	private ViewSwitcher mViewSwitcher = null;
	
	private boolean mIsLock = false;
	
	private Camera mCamera = null;
	
	private int mCameraCount = 0;
	private int mCameraId = CAMERA_DEFAULT_ID;
	private int mCameraLockedId = CAMERA_DEFAULT_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		doInit();
		
		findCameraFacingBack();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		PrefsAdapter.getInstance().getValues().useValues(this, false);
		
		useConfigValues();
		
		obtainCamera();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mConfigAdapter.setValues();
		
		releaseCamera();
		
		mLastBackPress = 0L;
		mToastBackPress.cancel();
	}
	
	@Override
	protected void onDestroy() {
		mToastBackPress = null;
		
		mConfigAdapter = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mMainMenu = null;
		}
		
		mActionBar = null;
		
		mPreview = null;
		mViewSwitcher = null;
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mMainMenu = menu;
		}
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mViewSwitcher.getDisplayedChild() == VIEW_SHUTTER_PROGRESS_POSITION) {
			return false;
		}
		
		menu.findItem(R.id.mainMenuSwitchMode).setTitle(mConfigAdapter.getIsCreator()
				? R.string.main_menu_switch_mode_viewer
				: R.string.main_menu_switch_mode_creator
			);
		
		menu.findItem(R.id.mainMenuSwitchCamera).setEnabled(mCameraCount > 1);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (android.R.id.home): {
				return true;
			}
			case (R.id.mainActionOverflow): {
				performActionMenu(false);
				
				return true;
			}
			case (R.id.mainMenuSwitchMode): {
				mConfigAdapter.setIsCreator(!mConfigAdapter.getIsCreator());
				
				return true;
			}
			case (R.id.mainMenuCourse): {
				Intent iSelectCourse = new Intent(this, ManageCourse.class);
				this.startActivity(iSelectCourse);
				
				return true;
			}
			case (R.id.mainMenuSwitchCamera): {
				switchCamera();
				
				return true;
			}
			case (R.id.mainMenuPreferences): {
				Intent iPreferences = new Intent(this, Preferences.class);
				this.startActivity(iPreferences);
				
				return true;
			}
			case (R.id.mainMenuAbout): {
				Intent iAbout = new Intent(this, About.class);
				this.startActivity(iAbout);
				
				return true;
			}
			case (R.id.mainMenuExit): {
				SimpleRecognizer.exit(this);
				
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	private void performActionMenu(boolean isOnMenuPressed) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			this.openOptionsMenu();
		} else {
			if (isOnMenuPressed) {
				this.supportInvalidateOptionsMenu();
				
				mMainMenu.performIdentifierAction(R.id.mainActionOverflow, 0);
			} else {
				onPrepareOptionsMenu(mMainMenu);
			}
		}
	}
	
	private void doInit() {
		this.setInitPreference(false);
		
		mConfigAdapter = ConfigAdapter.getInstance();
		
		mActionBar = this.getSupportActionBar();
		mActionBar.setHomeButtonEnabled(false);
		mActionBar.setDisplayHomeAsUpEnabled(false);
		
		this.setContentView(R.layout.main_camera);
		
		mPreview = (CameraPreview) this.findViewById(R.id.cameraPreview);
		mViewSwitcher = (ViewSwitcher) this.findViewById(R.id.viewSwitcherShutter);
		mViewSwitcher.setDisplayedChild(VIEW_SHUTTER_BUTTON_POSITION);
		
		((ImageButton) mViewSwitcher.findViewById(R.id.imageButtonShutter))
				.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View view) {
					onLockView(true);
					
					mCamera.takePicture(MainCamera.this, null, null, MainCamera.this);
				}
				
			});
		
		mToastBackPress = SimpleRecognizer.makeToast(
				R.string.key_button_back_double_press,
				Toast.LENGTH_LONG
			);
		
		if (!SimpleRecognizer.isDataBase()) {
			DataBaseAdapter.DataBaseDialog.newInstance().show(
					this.getSupportFragmentManager(),
					DataBaseAdapter.DataBaseDialog.KEY
				);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void findCameraFacingBack() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mCameraCount = Camera.getNumberOfCameras();
			
			if (mCameraCount > 0) {
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				
				for (int i = 0; i < mCameraCount; i++) {
					Camera.getCameraInfo(i, cameraInfo);
					
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
						mCameraId = i;
						
						break;
					}
				}
				
				if (mCameraId == CAMERA_DEFAULT_ID) {
					mCameraId = 0;
				}
			}
		}
	}
	
	private void useConfigValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
		
		mConfigAdapter.getValues();
	}
	
	@Override
	public void onLockView(boolean isLock) {
		mIsLock = isLock;
		
		if (!this.isDestroy()) {
			if (isLock) {
				mActionBar.hide();
			} else {
				if (mCamera != null) {
					try {
						mCamera.startPreview();
					} catch (Exception e) {
						//
					}
				}
				
				mActionBar.show();
			}
			
			this.supportInvalidateOptionsMenu();
			
			mViewSwitcher.setDisplayedChild(isLock
					? VIEW_SHUTTER_PROGRESS_POSITION
					: VIEW_SHUTTER_BUTTON_POSITION
				);
		}
	}
	
	@Override
	public boolean isLock() {
		return mIsLock;
	}
	
	@Override
	public void onItemChanged() {
		//
	}
	
	@Override
	public void onDeleteItem(Object item) {
		//
	}
	
	private void obtainCamera() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "obtainCamera() called");
		
		mCamera = getCameraInstance(mCameraId);
		
		if (mCamera != null) {
			mCameraLockedId = mCameraId;
			
			Camera.Parameters parameters = mCamera.getParameters();
			
			String focusMode = getOptimalFocusMode(parameters.getSupportedFocusModes());
			
			if (focusMode != null) {
				parameters.setFocusMode(focusMode);
			}
			
			mCamera.setParameters(parameters);
			
			mPreview.setCamera(mCamera);
		} else {
			SimpleRecognizer.MessageDialog dialog = (SimpleRecognizer.MessageDialog) this
					.getSupportFragmentManager().findFragmentByTag(SimpleRecognizer.MessageDialog.KEY);
			
			if (dialog != null) {
				dialog.dismiss();
			}
			
			boolean isCameraMultiple = ((mCameraCount > 1) && (mCameraId != CAMERA_DEFAULT_ID));
			
			dialog = SimpleRecognizer.MessageDialog.newInstance(
					this.getString(R.string.main_dialog_camera_error_title),
					"Couldn't obtain Camera instance.",
					("( Camera ID" + Text.SEPARATOR + Integer.toString(mCameraId) + " )"),
					(!isCameraMultiple)
				);
			dialog.setCancelable(false);
			
			DialogInterface.OnClickListener onClickExit = new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SimpleRecognizer.exit(MainCamera.this);
					}
					
				};
			
			if (isCameraMultiple) {
				dialog.setNegativeButton(R.string.dialog_button_try_next, (new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switchCamera();
						}
						
					}));
				
				dialog.setPositiveButton(R.string.dialog_button_exit, onClickExit);
			} else {
				dialog.setNeutralButton(R.string.dialog_button_exit, onClickExit);
			}
			
			dialog.show(this.getSupportFragmentManager(), SimpleRecognizer.MessageDialog.KEY);
		}
	}
	
	private void releaseCamera() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "releaseCamera() called");
		
		if (mCamera != null) {
			mCamera = null;
		}
		
		mPreview.releaseCamera();
	}
	
	private void switchCamera() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "switchCamera() called");
		
		if (mCameraCount > 1) {
			releaseCamera();
			
			mCameraLockedId = mCameraId;
			
			mCameraId = (mCameraLockedId + 1) % mCameraCount;
			
			obtainCamera();
			
			mPreview.switchCamera();
		} else {
			SimpleRecognizer.MessageDialog.newInstance(
					this.getString(R.string.main_dialog_camera_alert_title),
					this.getString(R.string.main_dialog_camera_alert_message),
					null,
					true
				).show(this.getSupportFragmentManager(), SimpleRecognizer.MessageDialog.KEY);
		}
	}
	
	@Override
	public void onShutter() {
		SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "onShutter() called");
		
		//
		
		//
	}
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "onPictureTaken() called");
		
		DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
		
		dbAdapter.open();
		
		final Course course = dbAdapter.getCourse(mConfigAdapter.getCourseId());
		
		Item item = null;
		
		if (mConfigAdapter.getIsCreator()) {
			item = dbAdapter.getItem(mConfigAdapter.getItemId());
		}
		
		dbAdapter.close();
		
		if ((course != null) && ((!mConfigAdapter.getIsCreator())
				|| ((item != null) && (item.getCourseId() == course.getId())))) {
			(new AsyncGetImagePHash(data, course, item)).execute();
		} else {
			final boolean isRequestItem = ((course != null)
					&& SimpleRecognizer.checkCourseCreator(course.getCreator()));
			
			SimpleRecognizer.MessageDialog dialog = SimpleRecognizer.MessageDialog.newInstance(
					this.getString(R.string.dialog_title_caution),
					(isRequestItem
							? this.getString(R.string.main_dialog_no_item_message)
							: this.getString(R.string.main_dialog_no_course_message)
						),
					null
				);
			
			dialog.setNegativeButton(R.string.dialog_button_close, null);
			
			dialog.setPositiveButton(
					(isRequestItem ? R.string.manage_item_name : R.string.manage_course_name_viewer),
					(new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (isRequestItem) {
									Intent iManageItem = new Intent(MainCamera.this, ManageItem.class);
									iManageItem.putExtra(Course.KEY, course);
									MainCamera.this.startActivity(iManageItem);
								} else {
									Intent iManageCourse = new Intent(MainCamera.this, ManageCourse.class);
									MainCamera.this.startActivity(iManageCourse);
								}
							}
							
						})
				);
			
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						MainCamera.this.onLockView(false);
					}
					
				}).show(this.getSupportFragmentManager(), SimpleRecognizer.MessageDialog.KEY);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mIsLock) {
			return;
		} else {
			long pressBackTime = SystemClock.elapsedRealtime();
			
			if ((pressBackTime - mLastBackPress) > BACK_PRESS_INTERVAL) {
				mToastBackPress.show();
			} else {
				mToastBackPress.cancel();
				
				super.onBackPressed();
			}
			
			mLastBackPress = pressBackTime;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case (KeyEvent.KEYCODE_MENU): {
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
			case (KeyEvent.KEYCODE_MENU): {
				if (!mIsLock) {
					performActionMenu(true);
				}
				
				return true;
			}
			default: {
				return super.onKeyUp(keyCode, event);
			}
		}
	}
	
	public static Camera getCameraInstance() {
		return getCameraInstance(CAMERA_DEFAULT_ID);
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static Camera getCameraInstance(int cameraId) {
		Camera camera = null;
		
		try {
			if (cameraId == CAMERA_DEFAULT_ID) {
				camera = Camera.open();
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				camera = Camera.open(cameraId);
			}
		} catch (Exception e) {
			//
		}
		
		return camera;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static String getOptimalFocusMode(List<String> listFocusMode) {
		String optimalFocusMode = null;
		
		if (listFocusMode != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if (listFocusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
					optimalFocusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
				}
			}
			
			if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					|| (optimalFocusMode == null)) {
				if (listFocusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
					optimalFocusMode = Camera.Parameters.FOCUS_MODE_AUTO;
				}
			}
		}
		
		return optimalFocusMode;
	}
	
	/**
	 * AsyncTask AsyncGetPHash<Void, Void, PHash> Class.
	 * 
	 * @author strider
	 */
	private class AsyncGetImagePHash extends AsyncTask<Void, Void, PHash> {
		
		//private static final String LOG_TAG = "AsyncGetImagePHash";
		
		private long mInitTime = 0L;
		
		private byte[] mData = null;
		
		private Course mCourse = null;
		private Item mItem = null;
		
		public AsyncGetImagePHash(byte[] data, Course course, Item item) {
			mData = data;
			
			mCourse = course;
			mItem = item;
		}
		
		@Override
		protected void onPreExecute() {
			mInitTime = SystemClock.elapsedRealtime();
		}
		
		@Override
		protected PHash doInBackground(Void... params) {
			return (new PHash(
					ImagePHash.getPHash(mData),
					null,
					((mItem != null) ? mItem.getId() : 0L)
				));
		}
		
		@Override
		protected void onPostExecute(PHash result) {
			if (!MainCamera.this.isDestroy()) {
				if (mConfigAdapter.getIsCreator()) {
					if (SimpleRecognizer.checkCourseCreator(mCourse.getCreator())) {
						ManagePHash.AddPHashDialog.newInstance(mItem, result)
								.setOnDismissListener(new DialogInterface.OnDismissListener() {
								
								@Override
								public void onDismiss(DialogInterface dialog) {
									MainCamera.this.onLockView(false);
								}
								
							}).show(
									MainCamera.this.getSupportFragmentManager(),
									ManagePHash.AddPHashDialog.KEY
								);
					} else {
						SimpleRecognizer.MessageDialog.newInstance(
								MainCamera.this.getString(R.string.dialog_title_forbidden),
								MainCamera.this.getString(R.string.manage_course_dialog_not_creator_message),
								null,
								true
							).setOnDismissListener(new DialogInterface.OnDismissListener() {
									
									@Override
									public void onDismiss(DialogInterface dialog) {
										MainCamera.this.onLockView(false);
									}
									
								}).show(
										MainCamera.this.getSupportFragmentManager(),
										SimpleRecognizer.MessageDialog.KEY
									);
					}
				} else { // FIXME: [ DEBUG ] PREPARE FOR DEPLOYMENT
					StringBuilder sb = new StringBuilder();
					
					sb.append("Work Time: ");
					sb.append(SystemClock.elapsedRealtime() - mInitTime);
					sb.append(" ms.").append(Text.LF);
					
					sb.append("pHashHex: ").append(result.getHexValue());
					
					Item itemResult = null;
					
					DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
					
					dbAdapter.open();
					
					List<Item> listItem = dbAdapter.getListItem(mCourse.getId(), true);
					
					dbAdapter.close();
					
					List<PHash> listPHash = new ArrayList<PHash>();
					
					for (Item item : listItem) {
						item.initAllHammingDistance(result.getHexValue());
						
						PHash pHashItemMin = Item.findPHashMin(item.getListPHash());
						
						if (pHashItemMin != null) {
							listPHash.add(pHashItemMin);
						}
					}
					
					PHash pHashMin = Item.findPHashMin(listPHash);
					
					sb.append(Text.LF).append("Item Hamming Distance List:");
					
					if ((pHashMin != null)
							&& (pHashMin.getHammingDistance() < ImagePHash.HAMMING_DISTANCE_THRESHOLD)) {
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
					
					if (BuildConfig.DEBUG) {
						if (itemResult != null) {
							sb.append(Text.LF).append(Text.LF);
							sb.append(itemResult.getContent());
							
							itemResult.setContent(sb.toString());
						} else {
							itemResult = new Item(
									"[ DEBUG ] Item not Found",
									sb.toString(),
									null,
									mCourse.getId()
								);
						}
					}
					
					ManageItem.ShowItemDialog.newInstance(itemResult, true)
							.setOnDismissListener(new DialogInterface.OnDismissListener() {
									
									@Override
									public void onDismiss(DialogInterface dialog) {
										MainCamera.this.onLockView(false);
									}
									
								})
							.show(
									MainCamera.this.getSupportFragmentManager(),
									ManageItem.ShowItemDialog.KEY
								);
				}
			}
		}
		
	}
	
}
