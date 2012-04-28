/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * ExpandableListActivity Select Course Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (android.R.id.home): {
				this.finish();
				
				//Intent intent = new Intent(this, Main.class);
				//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//startActivity(intent);
				
				return true;
			}
			case (R.id.selectCourseMenuAddCourse): {
				LayoutInflater inflater = LayoutInflater.from(this);
				final View view = inflater.inflate(R.layout.alert_dialog_select_course_edit, null);
				
				final EditText editTextTitle = (EditText) view.findViewById(R.id.editTextTitle);
				
				final EditText editTextCategory = (EditText) view.findViewById(R.id.editTextCategory);
				
				final EditText editTextCreator = (EditText) view.findViewById(R.id.editTextCreator);
				editTextCreator.setText(Utils.GetEmail(this));
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.select_course_menu_add_course);
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
			case (R.id.selectCourseContextMenuShowCreator): {
				ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
				
				int type = ExpandableListView.getPackedPositionType(info.packedPosition);
				
				if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
					int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
					
					String category = mAdapter.getGroup(groupPos).toString();
					String title = mAdapter.getChild(groupPos, childPos).toString();
					
					Course course = getCourse(category, title);
					
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(R.string.select_course_context_menu_show_creator);
					builder.setMessage(course.getCreator());
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
						
						final View view = inflater.inflate(R.layout.alert_dialog_select_course_edit, null);
						
						final EditText editTextTitle = (EditText) view.findViewById(R.id.editTextTitle);
						editTextTitle.setText(course.getTitle());
						
						final EditText editTextCategory = (EditText) view.findViewById(R.id.editTextCategory);
						editTextCategory.setText(course.getCategory());
						
						final EditText editTextCreator = (EditText) view.findViewById(R.id.editTextCreator);
						editTextCreator.setText(course.getCreator());
						
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle(R.string.select_course_context_menu_edit);
						builder.setView(view);
						
						builder.setNegativeButton(R.string.dialog_button_cancel, null);
						
						builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								course.setTitle(editTextTitle.getText().toString());
								course.setCategory(editTextCategory.getText().toString());
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
						builder.setTitle(R.string.select_course_dialog_not_creator_title);
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
					
					final Course course = getCourse(category, title);
					
					if (/*TODO: TEMP*/BuildConfig.DEBUG || SimpleRecognizer.checkCourseCreator(this, course.getCreator())) {
						DataBaseAdapter dbAdapter = new DataBaseAdapter(SelectCourse.this);
						dbAdapter.createDataBase(SelectCourse.this);
						dbAdapter.write();
						
						dbAdapter.deleteCourse(course.getId());
						
						dbAdapter.close();
						
						//
						mConfigAdapter.setDefaultValues();
						
						reloadView();
						//
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle(R.string.select_course_dialog_not_creator_title);
						builder.setMessage(R.string.select_course_dialog_not_creator_message);
						builder.setNeutralButton(R.string.dialog_button_close, null);
						
						AlertDialog alert = builder.create();
						alert.show();
					}
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
		
		setGroupChecked(true);
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
	}
	
	private Course getCourse(String category, String title) {
		DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
		dbAdapter.createDataBase(this);
		dbAdapter.open();
		
		Course course = dbAdapter.getCourse(category, title);
		
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
		
		setChildChecked(groupPosition);
	}
	
	@Override
	public void onGroupCollapse(int groupPosition) {
		mView.setItemChecked(groupPosition, false);
		
		if (mAdapter.getGroup(groupPosition).toString().equals(mCourse.getCategory())) {
			mView.setItemChecked(groupPosition, true);
		} else {
			setGroupChecked(false);
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
		mConfigAdapter.setValues();
		
		if (mConfigAdapter.getIsCreator()) {
			if (/*TODO: TEMP*/BuildConfig.DEBUG || SimpleRecognizer.checkCourseCreator(this, course.getCreator())) {
				Intent iManageItem = new Intent(SelectCourse.this, ManageItem.class);
				this.startActivity(iManageItem);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.select_course_dialog_not_creator_title);
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
	
}
