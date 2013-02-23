/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BaseFragmentActivity ManageCourse Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cloud4apps.Utils;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;

import ru.strider.app.BaseFragmentActivity;
import ru.strider.simplerecognizer.adapter.CourseAdapter;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.util.BuildConfig;
import ru.strider.util.Text;
import ru.strider.view.OnLockViewListener;

/**
 * BaseFragmentActivity ManageCourse Class.
 * 
 * @author strider
 */
public class ManageCourse extends BaseFragmentActivity implements OnLockViewListener,
		ExpandableListView.OnChildClickListener,
		ExpandableListView.OnGroupExpandListener, ExpandableListView.OnGroupCollapseListener {
	
	private static final String LOG_TAG = ManageCourse.class.getSimpleName();
	
	private ConfigAdapter mConfigAdapter = null;
	
	private Menu mMainMenu = null;
	
	private CourseAdapter mAdapter = null;
	private ExpandableListView mView = null;
	
	private Course mCourse = null;
	
	private boolean mIsLock = false;
	
	private AsyncCourseExport mAsyncCourseExport = null;
	private AsyncCourseImport mAsyncCourseImport = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		doInit();
		
		if (savedInstanceState != null) {
			mCourse = savedInstanceState.getParcelable(Course.KEY);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		useConfigValues();
		
		SimpleRecognizer.getMediaReceiver().startWatchingExternalStorage(this);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putParcelable(Course.KEY, mCourse);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		SimpleRecognizer.getMediaReceiver().stopWatchingExternalStorage(this);
	}
	
	@Override
	protected void onDestroy() {
		mConfigAdapter = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mMainMenu = null;
		}
		
		mAdapter = null;
		mView = null;
		
		mCourse = null;
		
		mAsyncCourseExport = null;
		mAsyncCourseImport = null;
		
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.manage_course_menu, menu);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mMainMenu = menu;
		}
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupEnabled(R.id.manageCourseMenuContent, (!mIsLock));
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			menu.setGroupEnabled(R.id.manageCourseMenuControls, (!mIsLock));
		} else {
			SubMenu action = menu.findItem(R.id.mainActionOverflow).getSubMenu();
			
			action.setGroupEnabled(R.id.manageCourseMenuControls, (!mIsLock));
		}
		
		menu.findItem(R.id.manageCourseMenuSwitchMode).setTitle(mConfigAdapter.getIsCreator()
				? R.string.manage_course_menu_switch_mode_viewer
				: R.string.manage_course_menu_switch_mode_creator
			);
		
		menu.findItem(R.id.manageCourseMenuAddCourse)
				.setEnabled((!mIsLock) && mConfigAdapter.getIsCreator());
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.mainActionOverflow): {
				performActionMenu(false);
				
				return true;
			}
			case (R.id.manageCourseMenuSwitchMode): {
				mConfigAdapter.setIsCreator(!mConfigAdapter.getIsCreator());
				
				this.setTitle(mConfigAdapter.getIsCreator()
						? R.string.manage_course_name_creator
						: R.string.manage_course_name_viewer
					);
				
				this.supportInvalidateOptionsMenu();
				
				return true;
			}
			case (R.id.manageCourseMenuImport): {
				mAsyncCourseImport = new AsyncCourseImport();
				
				FileManager.requestPickFile(this, FileManager.FILE_TYPE_COURSE);
				
				return true;
			}
			case (R.id.manageCourseMenuAddCourse): {//TODO: Dialog
				LayoutInflater inflater = LayoutInflater.from(this);
				View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				View viewContent = inflater.inflate(R.layout.alert_dialog_manage_course_edit, null);
				
				TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.manage_course_menu_add_course);
				textViewTitle.setSelected(true);
				
				final EditText editTextTitle = (EditText) viewContent.findViewById(R.id.editTextTitle);
				
				final EditText editTextCategory = (EditText) viewContent.findViewById(R.id.editTextCategory);
				
				final EditText editTextVersion = (EditText) viewContent.findViewById(R.id.editTextVersion);
				editTextVersion.setText(Integer.toString(Course.INIT_VERSION));
				editTextVersion.setEnabled(false);
				
				final EditText editTextCreator = (EditText) viewContent.findViewById(R.id.editTextCreator);
				editTextCreator.setText(Utils.GetEmail(this));
				editTextCreator.setEnabled(false);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(viewContent);
				
				builder.setNegativeButton(R.string.dialog_button_cancel, null);
				
				builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
							
							dbAdapter.write();
							
							dbAdapter.addCourse(new Course(
									editTextTitle.getText().toString().trim(),
									editTextCategory.getText().toString().trim(),
									Integer.parseInt(editTextVersion.getText().toString().trim()),
									editTextCreator.getText().toString().trim()
								));
							
							dbAdapter.close();
							
							initView();
						}
						
					});
				
				AlertDialog alert = builder.create();
				alert.show();
				
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			//menu.setHeaderIcon(iconRes);
			menu.setHeaderTitle(R.string.manage_course_context_menu_header_title);
			
			android.view.MenuInflater inflater = this.getMenuInflater();
			inflater.inflate(R.menu.manage_course_context_menu, menu);
			
			menu.setGroupEnabled(R.id.manageCourseContextMenuContent, (!mIsLock));
			menu.setGroupEnabled(
					R.id.manageCourseContextMenuControls,
					((!mIsLock) && mConfigAdapter.getIsCreator())
				);
			
			if (mConfigAdapter.getIsCreator()) {
				int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
				int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
				
				boolean isCreator = SimpleRecognizer.checkCourseCreator(
						((Course) mAdapter.getChild(groupPosition, childPosition)).getCreator()
					);
				
				menu.findItem(R.id.manageCourseContextMenuEdit)
						.setEnabled((!mIsLock) && isCreator)
						.setVisible(isCreator);
			}
			
			menu.findItem(R.id.manageCourseContextMenuDelete).setEnabled(!mIsLock);
		}
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
			
			final Course course = (Course) mAdapter.getChild(groupPosition, childPosition);
			
			switch (item.getItemId()) {
				case (R.id.manageCourseContextMenuExport): {
					mAsyncCourseExport = new AsyncCourseExport(initCourseData(course, true));
					
					FileManager.requestPickDirectory(this);
					
					return true;
				}
				case (R.id.manageCourseContextMenuShowInfo): {//TODO: Dialog
					LayoutInflater inflater = LayoutInflater.from(this);
					View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
					View viewContent = inflater.inflate(R.layout.alert_dialog_manage_course_show_info, null);
					
					TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
					
					StringBuilder sb = new StringBuilder();
					
					int color = this.getResources().getColor(R.color.main);
					
					sb.append("<font color=\"").append(color).append("\">");
					sb.append(course.getCategory());
					sb.append("</font>");
					sb.append(Text.SEPARATOR);
					sb.append("<font color=\"").append(color).append("\">");
					sb.append(course.getTitle());
					sb.append("</font>");
					
					textViewTitle.setText(Html.fromHtml(sb.toString()));
					textViewTitle.setSelected(true);
					
					EditText editTextVersion = (EditText) viewContent.findViewById(R.id.editTextVersion);
					editTextVersion.setText(Integer.toString(course.getVersion()));
					editTextVersion.setEnabled(false);
					
					EditText editTextCreator = (EditText) viewContent.findViewById(R.id.editTextCreator);
					editTextCreator.setText(course.getCreator());
					editTextCreator.setEnabled(false);
					
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setCustomTitle(viewTitle);
					builder.setView(viewContent);
					
					builder.setNeutralButton(R.string.dialog_button_close, null);
					
					AlertDialog alert = builder.create();
					alert.show();
					
					return true;
				}
				case (R.id.manageCourseContextMenuEdit): {
					if (SimpleRecognizer.checkCourseCreator(course.getCreator())) {//TODO: Dialog
						LayoutInflater inflater = LayoutInflater.from(this);
						View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
						View viewContent = inflater.inflate(R.layout.alert_dialog_manage_course_edit, null);
						
						TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
						textViewTitle.setText(R.string.manage_course_context_menu_edit);
						textViewTitle.setSelected(true);
						
						final EditText editTextTitle = (EditText) viewContent.findViewById(R.id.editTextTitle);
						editTextTitle.setText(course.getTitle());
						
						final EditText editTextCategory = (EditText) viewContent.findViewById(R.id.editTextCategory);
						editTextCategory.setText(course.getCategory());
						
						final EditText editTextVersion = (EditText) viewContent.findViewById(R.id.editTextVersion);
						editTextVersion.setText(Integer.toString(course.getVersion()));
						
						final EditText editTextCreator = (EditText) viewContent.findViewById(R.id.editTextCreator);
						editTextCreator.setText(course.getCreator());
						editTextCreator.setEnabled(false);
						
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setCustomTitle(viewTitle);
						builder.setView(viewContent);
						
						builder.setNegativeButton(R.string.dialog_button_cancel, null);
						
						builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									course.setTitle(editTextTitle.getText().toString().trim());
									course.setCategory(editTextCategory.getText().toString().trim());
									course.setVersion(Integer.parseInt(editTextVersion.getText().toString().trim()));
									//course.setCreator(editTextCreator.getText().toString().trim());
									
									DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
									
									dbAdapter.write();
									
									dbAdapter.updateCourse(course);
									
									dbAdapter.close();
									
									initView();
								}
								
							});
						
						AlertDialog alert = builder.create();
						alert.show();
					} else {//TODO: Dialog
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle(R.string.dialog_title_forbidden);
						builder.setMessage(R.string.manage_course_dialog_not_creator_message);
						
						builder.setNeutralButton(R.string.dialog_button_close, null);
						
						AlertDialog alert = builder.create();
						alert.show();
					}
					
					return true;
				}
				case (R.id.manageCourseContextMenuDelete): {
					DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
					
					dbAdapter.write();
					
					dbAdapter.deleteCourse(course.getId());
					
					dbAdapter.close();
					
					initView();
					
					return true;
				}
				default: {
					return super.onContextItemSelected(item);
				}
			}
		} else {
			return super.onContextItemSelected(item);
		}
	}
	
	private void doInit() {
		mConfigAdapter = ConfigAdapter.getInstance();
		
		this.setTitle(mConfigAdapter.getIsCreator()
				? R.string.manage_course_name_creator
				: R.string.manage_course_name_viewer
			);
		
		this.setContentView(R.layout.manage_course);
		
		this.setSupportProgressBarIndeterminateVisibility(false);
		
		mAdapter = new CourseAdapter(this);
		mView = (ExpandableListView) this.findViewById(android.R.id.list);
		//mView = this.getExpandableListView();
		
		mView.setEmptyView(this.findViewById(R.id.textViewEmpty));
		mView.setAdapter(mAdapter);
		//this.setListAdapter(mAdapter);
		this.registerForContextMenu(mView);
		
		mView.setTextFilterEnabled(true);
		//mView.setItemsCanFocus(false);
		mView.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
		
		mView.setOnChildClickListener(this);
		mView.setOnGroupExpandListener(this);
		mView.setOnGroupCollapseListener(this);
		
		initData();
	}
	
	private void initData() {
		mAdapter.clear();
		
		DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
		
		dbAdapter.open();
		
		List<String> listCategory = dbAdapter.getListCategory();
		
		if (listCategory != null) {
			List<List<Course>> listCourse = new ArrayList<List<Course>>();
			List<List<Integer>> listItem = new ArrayList<List<Integer>>();
			
			for (int i = 0; i < listCategory.size(); i++) {
				String category = listCategory.get(i);
				
				listCourse.add(dbAdapter.getListCourse(category));
				
				listItem.add(new ArrayList<Integer>());
				
				for (int j = 0; j < listCourse.get(i).size(); j++) {
					listItem.get(i).add(dbAdapter.getItemCount(listCourse.get(i).get(j).getId()));
				}
			}
			
			mAdapter.addData(listCategory, listCourse, listItem);
		}
		
		dbAdapter.close();
		
		mAdapter.notifyDataSetChanged();
	}
	
	private void initView() {
		initData();
		
		useConfigValues();
	}
	
	private void useConfigValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
		
		mConfigAdapter.getValues();
		
		if ((mCourse == null) || (mCourse.getId() != mConfigAdapter.getCourseId())) {
			DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
			
			dbAdapter.open();
			
			mCourse = dbAdapter.getCourse(mConfigAdapter.getCourseId());
			
			dbAdapter.close();
		}
		
		if (mCourse != null) {
			setGroupChecked(true);
		}
	}
	
	@Override
	public void onLockView(boolean isLock) {
		mIsLock = isLock;
		
		if (!this.isDestroy()) {
			this.supportInvalidateOptionsMenu();
			
			this.setSupportProgressBarIndeterminateVisibility(isLock);
		}
	}
	
	@Override
	public boolean isLock() {
		return mIsLock;
	}
	
	private Course initCourseData(Course course, boolean isWithPHash) {
		DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
		
		dbAdapter.open();
		
		course = dbAdapter.initCourseData(course, isWithPHash);
		
		dbAdapter.close();
		
		return course;
	}
	
	public void setGroupChecked(boolean shouldExpandGroup) {
		for (int i = 0; i < mAdapter.getGroupCount(); i++) {
			if (mAdapter.getGroup(i).equals(mCourse.getCategory())) {
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
		if (mAdapter.getGroup(groupPosition).equals(mCourse.getCategory())) {
			for (int i = 0; i < mAdapter.getChildrenCount(groupPosition); i++) {
				if (mAdapter.getChild(groupPosition, i).equals(mCourse)) {
					mView.setItemChecked(
							Long.valueOf(mAdapter.getChildId(groupPosition, i)).intValue(),
							true
						);
					
					break;
				}
			}
		}
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
			int childPosition, long id) {
		parent.setItemChecked(
				Long.valueOf(mAdapter.getChildId(groupPosition, childPosition)).intValue(),
				true
			);
		
		Course course = (Course) mAdapter.getChild(groupPosition, childPosition);
		
		mConfigAdapter.setCourseId(course.getId()).setValues();
		
		if (mConfigAdapter.getIsCreator()) {
			if (SimpleRecognizer.checkCourseCreator(course.getCreator())) {
				Intent iManageItem = new Intent(this, ManageItem.class);
				iManageItem.putExtra(Course.KEY, course);
				this.startActivity(iManageItem);
			} else {//TODO: Dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.dialog_title_forbidden);
				builder.setMessage(R.string.manage_course_dialog_not_creator_message);
				
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
			if (mAdapter.getGroup(groupPosition).equals(mCourse.getCategory())) {
				mView.setItemChecked(groupPosition, true);
			} else {
				setGroupChecked(false);
			}
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
				performActionMenu(true);
				
				return true;
			}
			default: {
				return super.onKeyUp(keyCode, event);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FileManager.REQUEST_DIRECTORY) {
			if ((resultCode == RESULT_OK) && (data != null)) {
				String directory = data.getStringExtra(FileManager.KEY_DIRECTORY);
				
				if ((!TextUtils.isEmpty(directory)) && (mAsyncCourseExport != null)) {
					mAsyncCourseExport.execute(directory);
				}
			}
			
			mAsyncCourseExport = null;
		} else if (requestCode == FileManager.REQUEST_FILE) {
			if ((resultCode == RESULT_OK) && (data != null)) {
				String file = data.getStringExtra(FileManager.KEY_FILE);
				
				if ((!TextUtils.isEmpty(file)) && (mAsyncCourseImport != null)) {
					mAsyncCourseImport.execute(file);
				}
			}
			
			mAsyncCourseImport = null;
		}
	}
	
	/**
	 * AsyncTask AsyncCourseExport<String, Void, String> Class.
	 * 
	 * @author strider
	 */
	private class AsyncCourseExport extends AsyncTask<String, Void, String> {
		
		private static final String LOG_TAG = "AsyncCourseExport";
		
		private long mInitTime = 0L;
		
		private Course mCourse = null;
		
		public AsyncCourseExport(Course course) {
			mCourse = course;
		}
		
		@Override
		protected void onPreExecute() {
			mInitTime = SystemClock.elapsedRealtime();
			
			ManageCourse.this.onLockView(true);
		}
		
		@Override
		protected String doInBackground(String... params) {
			StringBuilder sb = new StringBuilder("Course:");
			
			String dbName = mCourse.getCategory()
					+ "_" + mCourse.getTitle()
					+ "_v" + Integer.toString(mCourse.getVersion())
					+ ".sr";
			
			sb.append(Text.LF).append(dbName);
			
			if (SimpleRecognizer.getMediaReceiver().isExternalStorageAvailable()) {
				if (SimpleRecognizer.getMediaReceiver().isExternalStorageWritable()) {
					DataBaseAdapter dbAdapter = new DataBaseAdapter(dbName);
					
					dbAdapter.write();
					
					dbAdapter.addCourse(mCourse, true);
					
					for (String path : params) {
						File dir = new File(path);
						
						if (dir.exists() && dir.isFile()) {
							dir.delete();
						}
						
						if (!dir.exists()) {
							dir.mkdirs();
						}
						
						sb.append(Text.LF).append(dir.getPath()).append(Text.SEPARATOR);
						
						File file = new File(dir, dbName);
						
						try {
							dbAdapter.copyTo(file.getPath());
							
							sb.append("Exported.");
						} catch (IOException e) {
							Log.e(LOG_TAG, ("Course file not copied >> " + file.getPath()));
							Log.w(LOG_TAG, e.toString());
							
							sb.append("Failed, IOException.");
						}
					}
					
					dbAdapter.close();
					
					dbAdapter.delete();
				} else {
					sb.append("Failed, external storage not writable.");
				}
			} else {
				sb.append("Failed, external storage not available.");
			}
			
			return sb.toString();
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (!ManageCourse.this.isDestroy()) {
				StringBuilder sb = new StringBuilder();
				
				if (BuildConfig.DEBUG) {
					sb.append("Work Time: ");
					sb.append(SystemClock.elapsedRealtime() - mInitTime);
					sb.append(" ms.").append(Text.LF);
				}
				
				sb.append(result);
				//TODO: Dialog
				LayoutInflater inflater = LayoutInflater.from(SimpleRecognizer.getPackageContext());
				View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				
				TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.manage_course_context_menu_export);
				textViewTitle.setSelected(true);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ManageCourse.this);
				builder.setCustomTitle(viewTitle);
				builder.setMessage(sb.toString());
				
				builder.setNeutralButton(R.string.dialog_button_close, null);
				
				AlertDialog alert = builder.create();
				alert.show();
				
				ManageCourse.this.onLockView(false);
			}
		}
		
	}
	
	/**
	 * AsyncTask AsyncCourseImport<String, Void, String> Class.
	 * 
	 * @author strider
	 */
	private class AsyncCourseImport extends AsyncTask<String, Void, String> {
		
		private static final String LOG_TAG = "AsyncCourseImport";
		
		private long mInitTime = 0L;
		
		@Override
		protected void onPreExecute() {
			mInitTime = SystemClock.elapsedRealtime();
			
			ManageCourse.this.onLockView(true);
		}
		
		@Override
		protected String doInBackground(String... params) {
			StringBuilder sb = new StringBuilder("File:");
			
			for (String path : params) {
				String dbName = (new File(path)).getName();
				
				sb.append(Text.LF).append(dbName).append(Text.SEPARATOR);
				
				if (SimpleRecognizer.getMediaReceiver().isExternalStorageAvailable()) {
					DataBaseAdapter dbAdapter = new DataBaseAdapter(dbName);
					
					dbAdapter.write();
					
					try {
						dbAdapter.copyFrom(path);
					} catch (IOException e) {
						Log.e(LOG_TAG, ("File not found >> " + path));
						Log.w(LOG_TAG, e.toString());
						
						sb.append("Failed, IOException.");
						
						continue;
					} finally {
						dbAdapter.close();
					}
					
					dbAdapter.open();
					
					List<Course> listCourse = dbAdapter.initListCourseData(
							dbAdapter.getListCourse(),
							true
						);
					
					dbAdapter.close();
					
					dbAdapter.delete();
					
					if (listCourse != null) {
						dbAdapter = DataBaseAdapter.getInstance();
						
						dbAdapter.write();
						
						for (Course course : listCourse) {
							Course oldCourse = dbAdapter.getCourse(course.getCategory(), course.getTitle());
							
							if ((oldCourse != null)	&& oldCourse.getCreator().equals(course.getCreator())
									&& (oldCourse.getVersion() < course.getVersion())) {
								// FIXME: ASK USER IF OVERWRITE
								dbAdapter.deleteCourse(oldCourse.getId());
							}
							
							dbAdapter.addCourse(course, true);
						}
						
						dbAdapter.close();
					}
					
					sb.append("Imported.");
				} else {
					sb.append("Failed, external storage not available.");
				}
			}
			
			return sb.toString();
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (!ManageCourse.this.isDestroy()) {
				StringBuilder sb = new StringBuilder();
				
				if (BuildConfig.DEBUG) {
					sb.append("Work Time: ");
					sb.append(SystemClock.elapsedRealtime() - mInitTime);
					sb.append(" ms.").append(Text.LF);
				}
				
				sb.append(result);
				//TODO: Dialog
				LayoutInflater inflater = LayoutInflater.from(SimpleRecognizer.getPackageContext());
				View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				
				TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.manage_course_menu_import);
				textViewTitle.setSelected(true);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ManageCourse.this);
				builder.setCustomTitle(viewTitle);
				builder.setMessage(sb.toString());
				
				builder.setNeutralButton(R.string.dialog_button_close, null);
				
				AlertDialog alert = builder.create();
				alert.show();
				
				initView();
				
				ManageCourse.this.onLockView(false);
			}
		}
		
	}
	
}
