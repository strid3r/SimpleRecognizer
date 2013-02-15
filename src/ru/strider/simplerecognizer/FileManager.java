/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BaseListActivity FileManager Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.strider.adapter.BaseArrayAdapter;
import ru.strider.app.BaseListActivity;
import ru.strider.simplerecognizer.util.ConfigAdapter;

/**
 * BaseListActivity FileManager Class.
 * 
 * @author strider
 */
public class FileManager extends BaseListActivity {
	
	private static final String LOG_TAG = FileManager.class.getSimpleName();
	
	public static final String KEY_DIRECTORY = "Directory";
	public static final String KEY_FILE = "File";
	
	public static final String KEY_MODE = "Mode";
	public static final String KEY_REQUEST_CODE = "requestCode";
	public static final String KEY_FILE_TYPE = "fileType";
	
	public static final int MODE_INVALID = -1;
	public static final int MODE_DIRECTORY = 0;
	public static final int MODE_FILE = 1;
	
	public static final int REQUEST_INVALID = -1;
	public static final int REQUEST_DIRECTORY = 10;
	public static final int REQUEST_FILE = 100;
	
	public static final int FILE_TYPE_INVALID = -1;
	public static final int FILE_TYPE_ALL = 0;
	public static final int FILE_TYPE_COURSE = 10;
	public static final int FILE_TYPE_IMAGE = 100;
	
	private static final String KEY_SUB_DIR = "subDir";
	
	private static final String PARENT_DIR = ".." + File.separator;
	
	private int mMode = MODE_INVALID;
	private int mRequestCode = REQUEST_INVALID;
	private int mFileType = FILE_TYPE_INVALID;
	
	private ConfigAdapter mConfigAdapter = null;
	
	private File mDirectory = null;
	private File mFile = null;
	
	private BaseArrayAdapter<File> mAdapter = null;
	private ListView mView = null;
	
	private FilenameFilter mFilenameFilter = null;
	
	private int mSubDir = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = this.getIntent();
		
		mRequestCode = intent.getIntExtra(KEY_REQUEST_CODE, REQUEST_INVALID);
		
		switch (mRequestCode) {
			case (REQUEST_DIRECTORY): {
				mMode = MODE_DIRECTORY;
				
				break;
			}
			case (REQUEST_FILE): {
				mMode = MODE_FILE;
				
				break;
			}
			default: {
				final int defaultMode = MODE_FILE;
				
				mMode = ((intent.getIntExtra(KEY_MODE, defaultMode) == MODE_DIRECTORY)
						? MODE_DIRECTORY : defaultMode);
				
				break;
			}
		}
		
		mFileType = intent.getIntExtra(KEY_FILE_TYPE, FILE_TYPE_INVALID);
		
		if (savedInstanceState != null) {
			mDirectory = new File(savedInstanceState.getString(KEY_DIRECTORY));
			
			if (mMode == MODE_FILE) {
				String file = savedInstanceState.getString(KEY_FILE);
				
				if (file != null) {
					mFile = new File(file);
				}
			}
			
			mSubDir = savedInstanceState.getInt(KEY_SUB_DIR, 0);
		}
		
		doInit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		useConfigValues();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString(KEY_DIRECTORY, mDirectory.getPath());
		
		if (mMode == MODE_FILE) {
			if (mFile != null) {
				outState.putString(KEY_FILE, mFile.getPath());
			}
		}
		
		outState.putInt(KEY_SUB_DIR, mSubDir);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mConfigAdapter.setDirectory(mDirectory.getPath());
		
		mConfigAdapter.setValues();
	}
	
	@Override
	protected void onDestroy() {
		mConfigAdapter = null;
		
		mDirectory = null;
		mFile = null;
		
		mAdapter = null;
		mView = null;
		
		mFilenameFilter = null;
		
		super.onDestroy();
	}
	
	private void doInit() {
		mConfigAdapter = ConfigAdapter.getInstance(this, true);
		
		if (mDirectory == null) {
			File file = new File(mConfigAdapter.getDirectory());
			
			if (file.isDirectory() && file.canRead()) {
				mDirectory = file;
			} else {
				mDirectory = File.listRoots()[0];
			}
		}
		
		this.setContentView(R.layout.file_manager);
		
		mAdapter = new BaseArrayAdapter<File>(this, ((mMode == MODE_DIRECTORY)
				? R.layout.list_directory_activated
				: R.layout.list_file_single_choice_activated
			)) {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View view = super.getView(position, convertView, parent);
					
					File file = this.getItem(position);
					
					String fileName = null;
					
					if (file.equals(mDirectory.getParentFile())) {
						fileName = PARENT_DIR;
					} else {
						fileName = this.getItem(position).getName();
						
						if (file.isDirectory()) {
							fileName += File.separator; 
						}
					}
					
					TextView textView = null;
					
					if (mMode == MODE_DIRECTORY) {
						textView = (TextView) view.findViewById(R.id.textViewItem);
					} else {
						textView = (TextView) view.findViewById(R.id.checkedTextViewItem);
					}
					
					textView.setText(fileName);
					
					return view;
				}
				
			};
		mView = this.getListView();
		
		mView.setEmptyView(this.findViewById(R.id.textViewEmpty));
		mView.setAdapter(mAdapter);
		//this.setListAdapter(mAdapter);
		//this.registerForContextMenu(mView);
		
		mView.setTextFilterEnabled(true);
		//mView.setItemsCanFocus(false);
		
		if (mMode == MODE_FILE) {
			mView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
		
		mFilenameFilter = new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String fileName) {
					File file = new File(dir, fileName);
					
					switch (mFileType) {
						case (FILE_TYPE_INVALID): {
							return (file.exists() && (!file.isHidden())
									&& (file.isDirectory()
											|| file.isFile()));
						}
						case (FILE_TYPE_ALL): {
							return (file.exists());
						}
						case (FILE_TYPE_COURSE): {
							return (file.exists() && (!file.isHidden())
									&& (file.isDirectory()
											|| (file.isFile() && isFileTypeCourse(fileName))));
						}
						case (FILE_TYPE_IMAGE): {
							return (file.exists() && (!file.isHidden())
									&& (file.isDirectory()
											|| (file.isFile() && isFileTypeImage(fileName))));
						}
						default: {
							return false;
						}
					}
				}
				
			};
		
		initView();
	}
	
	public boolean isFileTypeCourse(String fileName) {
		int extensionIndex = fileName.lastIndexOf(".");
		
		if (extensionIndex != -1) {
			String extension = fileName.substring(extensionIndex);
			
			if (extension.equalsIgnoreCase(".sr")) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isFileTypeImage(String fileName) {
		int extensionIndex = fileName.lastIndexOf(".");
		
		if (extensionIndex != -1) {
			String extension = fileName.substring(extensionIndex);
			
			if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".gif")
					|| extension.equalsIgnoreCase(".png") || extension.equalsIgnoreCase(".bmp")) {
				return true;
			}
		}
		
		return false;
	}
	
	private void initData() {
		mAdapter.clear();
		
		File parentDir = mDirectory.getParentFile();
		
		if (parentDir != null) {
			mAdapter.add(parentDir);
		}
		
		List<File> listDir = new ArrayList<File>();
		List<File> listFile = new ArrayList<File>();
		
		for (File file : mDirectory.listFiles(mFilenameFilter)) {
			if (file.isDirectory()) {
				listDir.add(file);
			} else {
				listFile.add(file);
			}
		}
		
		Collections.sort(listDir);
		Collections.sort(listFile);
		
		mAdapter.addData(listDir);
		mAdapter.addData(listFile);
		
		mAdapter.notifyDataSetChanged();
	}
	
	private void initView() {
		this.setTitle(mDirectory.getAbsolutePath());
		
		initData();
		
		useConfigValues();
	}
	
	private void initView(File dir) {
		mDirectory = dir;
		
		initView();
	}
	
	private void useConfigValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
		
		//mConfigAdapter.getValues();
		
		if (mMode == MODE_FILE) {
			int position = mView.getCheckedItemPosition();
			
			if (position != AdapterView.INVALID_POSITION) {
				mView.setItemChecked(position, false);
			}
			
			if (mFile != null) {
				mView.setItemChecked(getFilePosition(), true);
			}
		}
	}
	
	private int getFilePosition() {
		// Parent Directory
		int parentPosition = AdapterView.INVALID_POSITION;
		
		if (mDirectory.getParentFile() != null) {
			parentPosition = mAdapter.indexOf(mDirectory.getParentFile());
			
			if (!mFile.getPath().startsWith(mDirectory.getPath())) {
				return parentPosition;
			}
		}
		
		// Current Directory
		if (mFile.getParentFile().equals(mDirectory)) {
			for (int i = 0; i < mAdapter.getCount(); i++) {
				File file = mAdapter.getItem(i);
				
				if (file.isFile() && file.equals(mFile)) {
					return i;
				}
			}
		}
		
		// Sub Directory
		for (int i = ((parentPosition == AdapterView.INVALID_POSITION)
				? 0 : 1); i < mAdapter.getCount(); i++) {
			File file = mAdapter.getItem(i);
			
			if (file.isDirectory() && mFile.getPath().startsWith(file.getPath())) {
				return i;
			}
		}
		
		return AdapterView.INVALID_POSITION;
	}
	
	private void returnFile() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "returnFile() called");
		
		Intent iFile = new Intent();
		
		switch (mRequestCode) {
			case (REQUEST_DIRECTORY): {
				iFile.putExtra(KEY_DIRECTORY, mDirectory.getPath());
				
				this.setResult(RESULT_OK, iFile);
				
				break;
			}
			case (REQUEST_FILE): {
				boolean isExists = ((mFile != null) && mFile.exists());
				
				iFile.putExtra(KEY_FILE, (isExists ? mFile.getPath() : null));
				
				this.setResult((isExists ? RESULT_OK : RESULT_CANCELED), iFile);
				
				break;
			}
			default: {
				this.setResult(RESULT_CANCELED);
				
				break;
			}
		}
	}
	
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		File file = mAdapter.getItem(position);
		
		if (file.exists() && file.canRead()) {
			if (file.isDirectory()) {
				if (!file.equals(mDirectory.getParentFile())) {
					mSubDir++;
				} else {
					mSubDir--;
				}
				
				initView(file);
			} else if (file.isFile()) {
				if (mMode == MODE_FILE) {
					listView.setItemChecked(position, true);
					
					mFile = file;
				}
			}
		} else {
			//TODO: Couldn't read alert
		}
	}
	
	public void onClickButtonCancel(View view) {
		if (mRequestCode != REQUEST_INVALID) {
			this.setResult(RESULT_CANCELED);
		}
		
		this.finish();
	}
	
	public void onClickButtonAccept(View view) {
		if (mRequestCode != REQUEST_INVALID) {
			returnFile();
		}
		
		this.finish();
	}
	
	@Override
	public void onBackPressed() {
		File parentDir = mDirectory.getParentFile();
		
		if ((mSubDir > 0) && (parentDir != null)) {
			mSubDir--;
			
			initView(parentDir);
		} else {
			super.onBackPressed();
		}
	}
	
}
