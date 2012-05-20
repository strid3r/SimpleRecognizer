/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * ListActivity Course Export Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdView;

import ru.strider.simplerecognizer.adapter.DirectoryListArrayAdapter;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.simplerecognizer.util.PrefsAdapter;

/**
 * ListActivity Course Export Class.
 * 
 * @author strider
 */
public class CourseExport extends SherlockListActivity {
	
	private static final String LOG_TAG = "CourseExport";
	
	public static final int PICK_DIRECTORY_REQUEST = 0;
	
	public static final String KEY_PICK_DIRECTORY = "PickDirectory";
	
	private static final String KEY_DIRECTORY = "Directory";
	
	private static final String PARENT_DIR = ".." + File.separator;
	
	private PrefsAdapter mPrefsAdapter = null;
	private ConfigAdapter mConfigAdapter = null;
	
	private boolean mIsRoot = false;
	
	private File mDirectory = null;
	
	private List<File> mListFile = null;
	private List<String> mListTitle = null;
	
	private DirectoryListArrayAdapter mAdapter = null;
	private ListView mView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		doInit();
		
		if (savedInstanceState == null) {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is NULL");
		} else {
			String directory = savedInstanceState.getString(KEY_DIRECTORY);
			
			if (directory != null) {
				mDirectory = new File(directory);
				
				//
				reloadView();
				//
			}
			
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is ~NULL");
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		usePreferencesValues();
		
		//useConfigValues();
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onResume() called");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (outState != null) {
			outState.putString(KEY_DIRECTORY, mDirectory.getPath());
		}
		
		SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS called");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mConfigAdapter.setDirectory(mDirectory.getPath());
		mConfigAdapter.setValues();
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onPause() called");
	}
	
	@Override
	protected void onDestroy() {
		AdView adView = (AdView) this.findViewById(R.id.adView);
		
		if (adView != null) {
			adView.destroy();
		}
		
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.course_export_menu, menu);
		
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
			case (R.id.courseExportMenuExport): {
				returnDirectory();
				
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
		
		File file = new File(mConfigAdapter.getDirectory());
		
		if (file.isDirectory() && file.canRead()) {
			mDirectory = file;
		} else {
			mDirectory = File.listRoots()[0];
		}
		
		mListFile = new ArrayList<File>();
		mListTitle = new ArrayList<String>();
		
		initData();
		
		this.setTitle(mDirectory.getAbsolutePath());
		
		this.setContentView(R.layout.course_export);
		
		mAdapter = new DirectoryListArrayAdapter(this, mListTitle);
		mView = this.getListView();
		
		mView.setAdapter(mAdapter);
		//this.setListAdapter(mAdapter);
		//this.registerForContextMenu(mView);//IF CONTEXT MENU
		
		mView.setTextFilterEnabled(true);
		//mView.setItemsCanFocus(false);
		//mView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		//SimpleRecognizer.initLisence(//TODO: TO ENABLE
		//		PickDirectory.this,
		//		(LinearLayout) this.findViewById(R.id.linearLayoutAdView)
		//	);
		
		final ActionBar actionBar = this.getSupportActionBar();
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
		mIsRoot = mDirectory.equals(File.listRoots()[0]);
		
		mListFile.clear();
		mListTitle.clear();
		
		if (!mIsRoot) {
			mListFile.add(mDirectory.getParentFile());
			
			mListTitle.add(PARENT_DIR);
		}
		
		List<File> listDir = new ArrayList<File>();
		List<File> listFile = new ArrayList<File>();
		
		FilenameFilter filter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				File file = new File(dir, filename);
				
				return (!file.isHidden() && (file.isDirectory() || file.isFile()));
			}
			
		};
		
		for (File file : mDirectory.listFiles(filter)) {
			if (file.isDirectory()) {
				listDir.add(file);
			} else {
				listFile.add(file);
			}
		}
		
		Collections.sort(listDir);
		Collections.sort(listFile);
		
		mListFile.addAll(listDir);
		mListFile.addAll(listFile);
		
		for (File file : listDir) {
			mListTitle.add(file.getName() + File.separator);
		}
		
		for (File file : listFile) {
			mListTitle.add(file.getName());
		}
	}
	
	private void reloadView() {
		initData();
		
		mAdapter.notifyDataSetChanged();
		
		this.setTitle(mDirectory.getAbsolutePath());
	}
	
	private void returnDirectory() {
		Intent iData = new Intent();
		iData.putExtra(KEY_PICK_DIRECTORY, mDirectory.getPath());
		
		this.setResult(RESULT_OK, iData);
		
		this.finish();
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "returnDirectory() called");
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
	/*
	private void useConfigValues() {
		mConfigAdapter.getValues();
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
	}
	*/
	private File getFile(String title) {
		for (File file : mListFile) {
			String fileName = file.getName();
			
			if (!mIsRoot && (mDirectory.getParentFile() != null) && (file.equals(mDirectory.getParentFile()))) {
				fileName = PARENT_DIR;
			} else if (file.isDirectory()) {
				fileName += File.separator;
			}
			
			if (fileName.equals(title)) {
				return file;
			}
		}
		
		return null;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = getFile(mAdapter.getItem(position).toString());
		
		if (file.isDirectory()) {
			if (file.canRead()) {
				mDirectory = file;
				
				//
				reloadView();
				//
			} else {
				//TODO: Couldn't read alert
			}
		}
	}
	
	public void onClickButtonCancel(View view) {
		this.finish();
	}
	
	public void onClickButtonExport(View view) {
		returnDirectory();
	}
	
	@Override
	public void onBackPressed() {
		if (mIsRoot) {
			super.onBackPressed();
		} else {
			mDirectory = mDirectory.getParentFile();
			
			//
			reloadView();
			//
		}
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
