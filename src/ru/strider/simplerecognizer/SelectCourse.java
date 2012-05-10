/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * ExpandableListActivity Select Course Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cloud4apps.Utils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockExpandableListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import ru.strider.simplerecognizer.adapter.TwoLevelExpandableListAdapter;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.util.BuildConfig;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.simplerecognizer.util.PrefsAdapter;

/**
 * ExpandableListActivity Select Course Class.
 * 
 * @author strider
 */
public class SelectCourse extends SherlockExpandableListActivity {
	
	private static final String LOG_TAG = "SelectCourse";
	
	private PrefsAdapter mPrefsAdapter = null;
	private ConfigAdapter mConfigAdapter = null;
	
	private Course mCourse = null;
	
	private List<String> mListCategory = null;
	private List<List<String>> mListTitle = null;
	
	private TwoLevelExpandableListAdapter mAdapter = null;
	private ExpandableListView mView = null;
	
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
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onResume() called");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mConfigAdapter.setValues();
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onPause() called");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.select_course_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem itemSwitchMode = menu.findItem(R.id.selectCourseMenuSwitchMode);
		
		if (mConfigAdapter.getIsCreator()) {
			itemSwitchMode.setTitle(R.string.select_course_menu_switch_mode_viewer);
		} else {
			itemSwitchMode.setTitle(R.string.select_course_menu_switch_mode_creator);
		}
		
		menu.findItem(R.id.selectCourseMenuAddCourse).setEnabled(mConfigAdapter.getIsCreator());
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (android.R.id.home): {
				this.finish();
				
				//Intent intent = new Intent(this, Main.class);
				//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//startActivity(intent);
				
				return true;
			}
			case (R.id.selectCourseMenuSwitchMode): {
				mConfigAdapter.setIsCreator(!mConfigAdapter.getIsCreator());
				
				this.invalidateOptionsMenu();
				
				return true;
			}
			case (R.id.selectCourseMenuImport): {
				//TODO: To get from pick file dialog
				String dbName = "Main_Temp_v1.sr";
				String dbPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "Temp" + File.separator;
				//
				
				File dbFile = new File(dbPath, dbName);
				
				new AsyncCourseImport(this).execute(dbFile);
				
				return true;
			}
			case (R.id.selectCourseMenuAddCourse): {
				LayoutInflater inflater = LayoutInflater.from(this);
				final View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				final View view = inflater.inflate(R.layout.alert_dialog_select_course_edit, null);
				
				final TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.select_course_menu_add_course);
				
				final EditText editTextTitle = (EditText) view.findViewById(R.id.editTextTitle);
				
				final EditText editTextCategory = (EditText) view.findViewById(R.id.editTextCategory);
				
				final EditText editTextVersion = (EditText) view.findViewById(R.id.editTextVersion);
				editTextVersion.setText(Integer.toString(Course.INIT_VERSION));
				
				final EditText editTextCreator = (EditText) view.findViewById(R.id.editTextCreator);
				editTextCreator.setText(Utils.GetEmail(this));
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(view);
				
				builder.setNegativeButton(R.string.dialog_button_cancel, null);
				
				builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DataBaseAdapter dbAdapter = new DataBaseAdapter(SelectCourse.this);
						dbAdapter.createDataBase(SelectCourse.this);
						dbAdapter.write();
						
						dbAdapter.addCourse(new Course(
								editTextTitle.getText().toString(),
								editTextCategory.getText().toString(),
								Integer.parseInt(editTextVersion.getText().toString()),
								editTextCreator.getText().toString()
							));
						
						dbAdapter.close();
						
						//
						reloadView();
						//
					}
					
				});
				
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
			case (R.id.selectCourseMenuSave): {
				this.finish();
				
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			//menu.setHeaderIcon(iconRes);
			menu.setHeaderTitle(R.string.select_course_context_menu_header_title);
			
			android.view.MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.select_course_context_menu, menu);
			
			if (!mConfigAdapter.getIsCreator()) {
				menu.removeItem(R.id.selectCourseContextMenuEdit);
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
			/*case (R.id.selectCourseContextMenuApplySelection): {
				ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
				
				String title = ((TextView) info.targetView).getText().toString();
				
				int type = ExpandableListView.getPackedPositionType(info.packedPosition);
				
				if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					//String title = ((TwoLineListItem) info.targetView).getText1().toString();
					int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
					int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
					Toast.makeText(this, title + ":" + "\nChild " + childPos + " clicked in group " + groupPos, Toast.LENGTH_SHORT).show();
					
					return true;
				} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
					//String title = ((TextView) info.targetView).getText().toString();
					int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
					Toast.makeText(this, title + ":" + "\nGroup " + groupPos + " clicked", Toast.LENGTH_SHORT).show();
					
					return true;
				}
			}*/
			case (R.id.selectCourseContextMenuExport): {
				ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
				
				int type = ExpandableListView.getPackedPositionType(info.packedPosition);
				
				if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
					int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
					
					String category = mAdapter.getGroup(groupPos).toString();
					String title = mAdapter.getChild(groupPos, childPos).toString();
					
					Course course = getCourse(category, title, true);
					
					//
					/*Intent intent = new Intent(Intent.ACTION_PICK);
					//intent.setData(Uri.parse("file:///sdcard"));
					//intent.putExtra(Intent.EXTRA_TITLE, "Choose a Folder");
					intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					
					try {
						this.startActivityForResult(intent, 10);
					} catch (ActivityNotFoundException e) {
						//
					}*/
					//
					
					//TODO: To get from pick directory dialog
					String dbPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "Temp" + File.separator;
					//
					
					new AsyncCourseExport(this, dbPath).execute(course);
				}
				
				return true;
			}
			case (R.id.selectCourseContextMenuShowInfo): {
				ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
				
				int type = ExpandableListView.getPackedPositionType(info.packedPosition);
				
				if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
					int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
					
					String category = mAdapter.getGroup(groupPos).toString();
					String title = mAdapter.getChild(groupPos, childPos).toString();
					
					Course course = getCourse(category, title);
					
					LayoutInflater inflater = LayoutInflater.from(this);
					final View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
					final View view = inflater.inflate(R.layout.alert_dialog_select_course_show_info, null);
					
					final TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
					textViewTitle.setText(course.getCategory() + SimpleRecognizer.SEPARATOR + course.getTitle());
					
					final EditText editTextVersion = (EditText) view.findViewById(R.id.editTextVersion);
					editTextVersion.setText(Integer.toString(course.getVersion()));
					editTextVersion.setEnabled(false);
					
					final EditText editTextCreator = (EditText) view.findViewById(R.id.editTextCreator);
					editTextCreator.setText(course.getCreator());
					editTextCreator.setEnabled(false);
					
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setCustomTitle(viewTitle);
					builder.setView(view);
					builder.setNeutralButton("Close", null);
					
					AlertDialog alert = builder.create();
					alert.show();
				}
				
				return true;
			}
			case (R.id.selectCourseContextMenuEdit): {
				ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
				
				int type = ExpandableListView.getPackedPositionType(info.packedPosition);
				
				if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
					int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
					
					String category = mAdapter.getGroup(groupPos).toString();
					String title = mAdapter.getChild(groupPos, childPos).toString();
					
					final Course course = getCourse(category, title);
					
					if (/*TODO: TEMP*/BuildConfig.DEBUG || SimpleRecognizer.checkCourseCreator(this, course.getCreator())) {
						LayoutInflater inflater = LayoutInflater.from(this);
						final View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
						final View view = inflater.inflate(R.layout.alert_dialog_select_course_edit, null);
						
						final TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
						textViewTitle.setText(R.string.select_course_context_menu_edit);
						
						final EditText editTextTitle = (EditText) view.findViewById(R.id.editTextTitle);
						editTextTitle.setText(course.getTitle());
						
						final EditText editTextCategory = (EditText) view.findViewById(R.id.editTextCategory);
						editTextCategory.setText(course.getCategory());
						
						final EditText editTextVersion = (EditText) view.findViewById(R.id.editTextVersion);
						editTextVersion.setText(Integer.toString(course.getVersion()));
						
						final EditText editTextCreator = (EditText) view.findViewById(R.id.editTextCreator);
						editTextCreator.setText(course.getCreator());
						
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setCustomTitle(viewTitle);
						builder.setView(view);
						
						builder.setNegativeButton(R.string.dialog_button_cancel, null);
						
						builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								course.setTitle(editTextTitle.getText().toString());
								course.setCategory(editTextCategory.getText().toString());
								course.setVersion(Integer.parseInt(editTextVersion.getText().toString()));
								course.setCreator(editTextCreator.getText().toString());
								
								DataBaseAdapter dbAdapter = new DataBaseAdapter(SelectCourse.this);
								dbAdapter.createDataBase(SelectCourse.this);
								dbAdapter.write();
								
								dbAdapter.updateCourse(course);
								
								dbAdapter.close();
								
								//
								reloadView();
								//
							}
							
						});
						
						AlertDialog alert = builder.create();
						alert.show();
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle(R.string.dialog_title_forbidden);
						builder.setMessage(R.string.select_course_dialog_not_creator_message);
						builder.setNeutralButton(R.string.dialog_button_close, null);
						
						AlertDialog alert = builder.create();
						alert.show();
					}
				}
				
				return true;
			}
			case (R.id.selectCourseContextMenuDelete): {
				ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
				
				int type = ExpandableListView.getPackedPositionType(info.packedPosition);
				
				if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
					int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
					
					String category = mAdapter.getGroup(groupPos).toString();
					String title = mAdapter.getChild(groupPos, childPos).toString();
					
					Course course = getCourse(category, title);
					
					DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
					dbAdapter.createDataBase(this);
					dbAdapter.write();
					
					dbAdapter.deleteCourse(course.getId());
					
					dbAdapter.close();
					
					//
					reloadView();
					//
				}
				
				return true;
			}
			default: {
				return super.onContextItemSelected(item);
			}
		}
	}
	
	private void doInit() {
		mPrefsAdapter = new PrefsAdapter(this);
		mConfigAdapter = new ConfigAdapter(this);
		
		initData();
		
		if (mConfigAdapter.getIsCreator()) {
			this.setTitle(this.getString(R.string.select_course_name_creator));
		}
		
		this.setContentView(R.layout.select_course);
		
		mAdapter = new TwoLevelExpandableListAdapter(this, mListCategory, mListTitle);
		//mView = (ExpandableListView) findViewById(R.id.expandable_list_view);
		mView = this.getExpandableListView();
		
		mView.setAdapter(mAdapter);
		//this.setListAdapter(mAdapter);
		this.registerForContextMenu(mView);
		
		mView.setTextFilterEnabled(true);
		//mView.setItemsCanFocus(false);
		mView.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
		
		//SimpleRecognizer.initLisence(//TODO: TO ENABLE
		//		SelectCourse.this,
		//		(LinearLayout) this.findViewById(R.id.linearLayoutAdView)
		//	);
		
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				actionBar.setHomeButtonEnabled(true);
			}
			
			actionBar.setDisplayHomeAsUpEnabled(true);
			//actionBar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.background_header));
			//actionBar.setDisplayUseLogoEnabled(true);
			//actionBar.setDisplayShowHomeEnabled(true);
			//actionBar.setDisplayShowTitleEnabled(true);
		} else {
			SimpleRecognizer.logIfDebug(Log.ERROR, LOG_TAG, "// TODO: getSupportActionBar() is NULL");
		}
	}
	
	private void initData() {
		DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
		dbAdapter.createDataBase(this);
		dbAdapter.open();
		
		mListCategory = dbAdapter.getListCategory();
		if (mListCategory == null) {
			mListCategory = new ArrayList<String>();
		}
		
		mListTitle = new ArrayList<List<String>>();
		for (int i = 0; i < mListCategory.size(); i++) {
			mListTitle.add(dbAdapter.getListCourse(mListCategory.get(i)));
		}
		
		dbAdapter.close();
	}
	
	private void reloadView() {
		initData();
		
		mAdapter = new TwoLevelExpandableListAdapter(this, mListCategory, mListTitle);
		
		mView.setAdapter(mAdapter);
		
		useConfigValues();
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
		
		DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
		dbAdapter.createDataBase(this);
		dbAdapter.open();
		
		mCourse = dbAdapter.getCourse(mConfigAdapter.getCourseId());
		
		dbAdapter.close();
		
		if (mCourse != null) {
			setGroupChecked(true);
		}
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
	}
	
	private Course getCourse(String category, String title) {
		return getCourse(category, title, false);
	}
	
	private Course getCourse(String category, String title, boolean isWithData) {
		DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
		dbAdapter.createDataBase(this);
		dbAdapter.open();
		
		Course course = dbAdapter.getCourse(category, title, isWithData);
		
		dbAdapter.close();
		
		return course;
	}
	
	public void setGroupChecked(boolean shouldExpandGroup) {
		for (int i = 0; i < mAdapter.getGroupCount(); i++) {
			if (mAdapter.getGroup(i).toString().equals(mCourse.getCategory())) {
				if (shouldExpandGroup) {
					mView.expandGroup(i);
				} else {
					mView.setItemChecked(i, true);
				}
				
				break;
			}
		}
	}
	
	public void setChildChecked(int groupPosition) {
		if (mAdapter.getGroup(groupPosition).toString().equals(mCourse.getCategory())) {
			for (int i = 0; i < mAdapter.getChildrenCount(groupPosition); i++) {
				if (mAdapter.getChild(groupPosition, i).toString().equals(mCourse.getTitle())) {
					mView.setItemChecked((int) mAdapter.getChildId(groupPosition, i), true);
					
					break;
				}
			}
		}
	}
	
	@Override
	public void onGroupExpand(int groupPosition) {
		for (int i = 0; i < mAdapter.getGroupCount(); i++) {
			if (i != groupPosition) {
				mView.collapseGroup(i);
			}
		}
		
		mView.setItemChecked(groupPosition, true);
		
		if (mCourse != null) {
			setChildChecked(groupPosition);
		}
	}
	
	@Override
	public void onGroupCollapse(int groupPosition) {
		mView.setItemChecked(groupPosition, false);
		
		if (mCourse != null) {
			if (mAdapter.getGroup(groupPosition).toString().equals(mCourse.getCategory())) {
				mView.setItemChecked(groupPosition, true);
			} else {
				setGroupChecked(false);
			}
		}
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		//CheckedTextView textView = (CheckedTextView) v.findViewById(android.R.id.text1);
		
		mView.setItemChecked((int) mAdapter.getChildId(groupPosition, childPosition), true);
		
		//TextView textViewModel = (TextView) v.findViewById(android.R.id.text1);
		//String course = textViewCourse.getText().toString();
		String category = mAdapter.getGroup(groupPosition).toString();
		String title = mAdapter.getChild(groupPosition, childPosition).toString();
		
		Course course = getCourse(category, title);
		
		mConfigAdapter.setCourseId(course.getId());
		
		if (mConfigAdapter.getIsCreator()) {
			if (/*TODO: TEMP*/BuildConfig.DEBUG || SimpleRecognizer.checkCourseCreator(this, course.getCreator())) {
				Intent iManageItem = new Intent(SelectCourse.this, ManageItem.class);
				this.startActivity(iManageItem);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.dialog_title_forbidden);
				builder.setMessage(R.string.select_course_dialog_not_creator_message);
				builder.setNeutralButton(R.string.dialog_button_close, null);
				
				AlertDialog alert = builder.create();
				alert.show();
			}			
		} else {
			this.finish();
		}
		
		return true;
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
	
	/**
	 * AsyncTask AsyncCourseExport<Course, Void, String> Class.
	 * 
	 * @author strider
	 */
	private class AsyncCourseExport extends AsyncTask<Course, Void, String> {
		
		private static final String LOG_TAG = "AsyncCourseExport";
		
		private long mInitTime = 0L;
		private long mWorkTime = 0L;
		
		private Context mContext = null;
		
		private Activity mActivity = null;
		
		private String mDbPath = null;
		
		public AsyncCourseExport(Activity activity, String dbPath) {
			mContext = (Context) activity;
			
			mActivity = activity;
			
			mDbPath = dbPath;
		}
		
		@Override
		protected void onPreExecute() {
			//
		}
		
		@Override
		protected String doInBackground(Course... params) {
			mInitTime = SystemClock.elapsedRealtime();
			
			//
			
			StringBuilder sb = new StringBuilder("Course:");
			
			for (Course course : params) {
				String dbName = course.getCategory() + "_" + course.getTitle() + "_v" + Integer.toString(course.getVersion()) + ".sr";
				
				sb.append(SimpleRecognizer.BR_LINE).append(dbName).append(SimpleRecognizer.SEPARATOR);
				
				boolean isExternalStorageAvailable = false;
				boolean isExternalStorageWritable = false;
				
				String state = Environment.getExternalStorageState();
				
				if (state.equals(Environment.MEDIA_MOUNTED)) {
					isExternalStorageAvailable = true;
					isExternalStorageWritable = true;
					
					SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "MEDIA_MOUNTED");
				} else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
					isExternalStorageAvailable = true;
					isExternalStorageWritable = false;
					
					SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "MEDIA_MOUNTED_READ_ONLY");
				} else {
					isExternalStorageAvailable = false;
					isExternalStorageWritable = false;
					
					SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "MEDIA_NONE");
				}
				
				if (isExternalStorageAvailable) {
					if (isExternalStorageWritable) {
						DataBaseAdapter dbAdapter = new DataBaseAdapter(mContext, dbName);
						dbAdapter.createDataBase(mActivity);
						dbAdapter.write();
						
						dbAdapter.addCourse(course, true);
						
						dbAdapter.close();
						
						File dbDir = new File(mDbPath);
						if (!dbDir.exists()) {
							dbDir.mkdirs();
						}
						
						// Trash //TODO
						//File dbFile = new File(mDbPath + mDbName);
						//if (!dbFile.exists() && dbFile.canWrite()) {
						//	//
						//}
						//
						
						try {
							dbAdapter.copyTo(mDbPath + dbName);
							
							sb.append("Exported.");
						} catch (IOException e) {
							Log.e(LOG_TAG, "Course File Not Copied >> " + dbName);
							Log.w(LOG_TAG, e.getMessage());
							
							sb.append("Failed, IOException.");
						} finally {
							dbAdapter.delete();
						}
					} else {
						sb.append("Failed, External Storage Not Writable.");
					}
				} else {
					sb.append("Failed, External Storage Not Available.");
				}
			}
			
			//
			
			mWorkTime = SystemClock.elapsedRealtime() - mInitTime;
			
			return sb.toString();
		}
		
		@Override
		protected void onPostExecute(String result) {
			StringBuilder sb = new StringBuilder();
			
			if (BuildConfig.DEBUG) {
				sb.append("Work Time: ").append(mWorkTime).append(" ms.").append(SimpleRecognizer.BR_LINE);
			}
			
			sb.append(result);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(mContext.getString(R.string.select_course_context_menu_export));
			builder.setMessage(sb.toString());
			builder.setNeutralButton(R.string.dialog_button_close, null);
			
			AlertDialog alert = builder.create();
			alert.show();
			
			Log.d(LOG_TAG, sb.toString());
		}
		
	}
	
	/**
	 * AsyncTask AsyncCourseImport<Course, Void, String> Class.
	 * 
	 * @author strider
	 */
	private class AsyncCourseImport extends AsyncTask<File, Void, String> {
		
		private static final String LOG_TAG = "AsyncCourseImport";
		
		private long mInitTime = 0L;
		private long mWorkTime = 0L;
		
		private Context mContext = null;
		
		private Activity mActivity = null;
		
		public AsyncCourseImport(Activity activity) {
			mContext = (Context) activity;
			
			mActivity = activity;
		}
		
		@Override
		protected void onPreExecute() {
			//
		}
		
		@Override
		protected String doInBackground(File... params) {
			mInitTime = SystemClock.elapsedRealtime();
			
			//
			
			StringBuilder sb = new StringBuilder("File:");
			
			for (File dbFile : params) {
				String dbName = dbFile.getName();
				
				sb.append(SimpleRecognizer.BR_LINE).append(dbName).append(SimpleRecognizer.SEPARATOR);
				
				boolean isExternalStorageAvailable = false;
				
				String state = Environment.getExternalStorageState();
				
				if (state.equals(Environment.MEDIA_MOUNTED)) {
					isExternalStorageAvailable = true;
					
					SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "MEDIA_MOUNTED");
				} else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
					isExternalStorageAvailable = true;
					
					SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "MEDIA_MOUNTED_READ_ONLY");
				} else {
					isExternalStorageAvailable = false;
					
					SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "MEDIA_NONE");
				}
				
				if (isExternalStorageAvailable) {
					DataBaseAdapter dbAdapter = new DataBaseAdapter(mContext, dbName);
					dbAdapter.createDataBase(mActivity);
					
					try {
						dbAdapter.write();
						
						dbAdapter.copyFrom(dbFile.getPath());
					} catch (IOException e) {
						Log.e(LOG_TAG, "File Not Found >> " + dbName);
						Log.w(LOG_TAG, e.getMessage());
						
						sb.append("Failed, IOException.");
						
						continue;
					} finally {
						dbAdapter.close();
					}
					
					dbAdapter.open();
					
					List<Course> listCourse = dbAdapter.getListCourse(true);
					
					dbAdapter.close();
					
					dbAdapter.delete();
					
					//
					if (listCourse != null) {
						dbAdapter = new DataBaseAdapter(mContext);
						dbAdapter.createDataBase(mActivity);
						dbAdapter.write();
						
						for (Course course : listCourse) {
							Course oldCourse = dbAdapter.getCourse(course.getCategory(), course.getTitle());
							
							if ((oldCourse != null)	&& oldCourse.getCreator().equals(course.getCreator())
									&& (oldCourse.getVersion() < course.getVersion())) {
								dbAdapter.deleteCourse(oldCourse.getId());//FIXME: ASK USER IF OVERWRITE
							}
							
							dbAdapter.addCourse(course, true);
						}
						
						dbAdapter.close();
					}
					//
					
					sb.append("Imported.");
				} else {
					sb.append("Failed, External Storage Not Available.");
				}
			}
			
			//
			
			mWorkTime = SystemClock.elapsedRealtime() - mInitTime;
			
			return sb.toString();
		}
		
		@Override
		protected void onPostExecute(String result) {
			StringBuilder sb = new StringBuilder();
			
			if (BuildConfig.DEBUG) {
				sb.append("Work Time: ").append(mWorkTime).append(" ms.").append(SimpleRecognizer.BR_LINE);
			}
			
			sb.append(result);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(mContext.getString(R.string.select_course_menu_import));
			builder.setMessage(sb.toString());
			builder.setNeutralButton(R.string.dialog_button_close, null);
			
			AlertDialog alert = builder.create();
			alert.show();
			
			Log.d(LOG_TAG, sb.toString());
			
			//
			reloadView();
			//
		}
		
	}
	
}
