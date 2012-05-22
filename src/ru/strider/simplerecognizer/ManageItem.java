/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * ListActivity Manage Item Class
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
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.ads.AdView;

import ru.strider.simplerecognizer.adapter.ListArrayAdapter;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.model.Item;
import ru.strider.simplerecognizer.util.BuildConfig;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.simplerecognizer.util.PrefsAdapter;

/**
 * ListActivity Manage Item Class.
 * 
 * @author strider
 */
public class ManageItem extends SherlockListActivity {
	
	private static final String LOG_TAG = "ManageItem";
	
	private PrefsAdapter mPrefsAdapter = null;
	private ConfigAdapter mConfigAdapter = null;
	
	private Course mCourse = null;
	
	private List<Item> mListItem = null;
	private List<String> mListTitle = null;
	
	private ListArrayAdapter mAdapter = null;
	private ListView mView = null;
	
	private AsyncItemImport mAsyncItemImport = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
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
		inflater.inflate(R.menu.manage_item_menu, menu);
		
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
			case (R.id.manageItemMenuImport): {
				mAsyncItemImport = new AsyncItemImport(this);
				
				Intent iPickFile = new Intent(ManageItem.this, ItemImport.class);
				this.startActivityForResult(iPickFile, ItemImport.PICK_FILE_REQUEST);
				
				return true;
			}
			case (R.id.manageItemMenuAddItem): {
				showAddItem();
				
				return true;
			}
			case (R.id.manageItemMenuDeleteCourse): {
				DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
				dbAdapter.createDataBase(this);
				dbAdapter.write();
				
				dbAdapter.deleteCourse(mConfigAdapter.getCourseId());
				
				dbAdapter.close();
				
				//
				Intent iMainCamera = new Intent(ManageItem.this, MainCamera.class);
				iMainCamera.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(iMainCamera);
				//
				
				return true;
			}
			case (R.id.manageItemMenuSave): {
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
		
		//menu.setHeaderIcon(iconRes);
		menu.setHeaderTitle(R.string.manage_item_context_menu_header_title);
		
		android.view.MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manage_item_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.manageItemContextMenuApplySelection): {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				
				mConfigAdapter.setItemId(getItem(mAdapter.getItem(info.position).toString()).getId());
				mConfigAdapter.setValues();
				
				Intent iMainCamera = new Intent(ManageItem.this, MainCamera.class);
				iMainCamera.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(iMainCamera);
				
				return true;
			}
			case (R.id.manageItemContextMenuShowContent): {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				
				Item courseItem = getItem(mAdapter.getItem(info.position).toString());
				
				LayoutInflater inflater = LayoutInflater.from(this);
				View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				View view = inflater.inflate(R.layout.alert_dialog_manage_item_show_content, null);
				
				TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(courseItem.getTitle());
				
				EditText editTextContent = (EditText) view.findViewById(R.id.editTextContent);
				editTextContent.setText(Html.fromHtml(courseItem.getContent()));
				editTextContent.setEnabled(false);
				
				EditText editTextVideoUri = (EditText) view.findViewById(R.id.editTextVideoUri);
				editTextVideoUri.setText(courseItem.getVideoUri());
				editTextVideoUri.setEnabled(false);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(view);
				builder.setNeutralButton(R.string.dialog_button_close, null);
				
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
			case (R.id.manageItemContextMenuEdit): {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				
				final Item courseItem = getItem(mAdapter.getItem(info.position).toString());
				
				LayoutInflater inflater = LayoutInflater.from(this);
				View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				View view = inflater.inflate(R.layout.alert_dialog_manage_item_edit, null);
				
				TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.manage_item_context_menu_edit);
				
				final EditText editTextTitle = (EditText) view.findViewById(R.id.editTextTitle);
				editTextTitle.setText(courseItem.getTitle());
				
				final EditText editTextContent = (EditText) view.findViewById(R.id.editTextContent);
				editTextContent.setText(courseItem.getContent());
				
				final EditText editTextVideoUri = (EditText) view.findViewById(R.id.editTextVideoUri);
				editTextVideoUri.setText(courseItem.getVideoUri());
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(view);
				
				builder.setNegativeButton(R.string.dialog_button_cancel, null);
				
				builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						courseItem.setTitle(editTextTitle.getText().toString());
						courseItem.setContent(editTextContent.getText().toString());
						courseItem.setVideoUri(editTextVideoUri.getText().toString());
						
						DataBaseAdapter dbAdapter = new DataBaseAdapter(ManageItem.this);
						dbAdapter.createDataBase(ManageItem.this);
						dbAdapter.write();
						
						dbAdapter.updateItem(courseItem);
						
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
			case (R.id.manageItemContextMenuDelete): {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				
				DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
				dbAdapter.createDataBase(this);
				dbAdapter.write();
				
				dbAdapter.deleteItem(getItem(mAdapter.getItem(info.position).toString()).getId());
				
				dbAdapter.close();
				
				//
				reloadView();
				//
				
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
		
		mListTitle = new ArrayList<String>();
		
		initData();
		
		this.setTitle(mCourse.getCategory() + SimpleRecognizer.SEPARATOR + mCourse.getTitle());
		
		this.setContentView(R.layout.manage_item);
		
		this.setSupportProgressBarIndeterminateVisibility(false);
		
		mAdapter = new ListArrayAdapter(this, mListTitle);
		mView = this.getListView();
		
		mView.setAdapter(mAdapter);
		//this.setListAdapter(mAdapter);
		this.registerForContextMenu(mView);
		
		mView.setTextFilterEnabled(true);
		//mView.setItemsCanFocus(false);
		mView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		//SimpleRecognizer.initLisence(//TODO: TO ENABLE
		//		SelectCourse.this,
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
		DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
		dbAdapter.createDataBase(this);
		dbAdapter.open();
		
		mCourse = dbAdapter.getCourse(mConfigAdapter.getCourseId());
		
		mListItem = dbAdapter.getListItem(mConfigAdapter.getCourseId());
		
		dbAdapter.close();
		
		if (mListItem == null) {
			mListItem = new ArrayList<Item>();
		}
		
		mListTitle.clear();
		for (Item item : mListItem) {
			mListTitle.add(item.getTitle());
		}
	}
	
	private void reloadView() {
		initData();
		
		mAdapter.notifyDataSetChanged();
		
		useConfigValues();
	}
	
	private void showAddItem() {
		showAddItem(null);
	}
	
	private void showAddItem(String content) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
		View view = inflater.inflate(R.layout.alert_dialog_manage_item_edit, null);
		
		TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
		textViewTitle.setText(R.string.manage_item_menu_add_item);
		
		final EditText editTextTitle = (EditText) view.findViewById(R.id.editTextTitle);
		
		final EditText editTextContent = (EditText) view.findViewById(R.id.editTextContent);
		if (content != null) {
			editTextContent.setText(content);
		}
		
		final EditText editTextVideoUri = (EditText) view.findViewById(R.id.editTextVideoUri);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCustomTitle(viewTitle);
		builder.setView(view);
		
		builder.setNegativeButton(R.string.dialog_button_cancel, null);
		
		builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DataBaseAdapter dbAdapter = new DataBaseAdapter(ManageItem.this);
				dbAdapter.createDataBase(ManageItem.this);
				dbAdapter.write();
				
				dbAdapter.addItem(new Item(
						editTextTitle.getText().toString(),
						editTextContent.getText().toString(),
						editTextVideoUri.getText().toString(),
						mCourse.getId()
					));
				
				dbAdapter.close();
				
				//
				reloadView();
				//
			}
			
		});
		
		AlertDialog alert = builder.create();
		alert.show();
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
		
		mView.setItemChecked(getItemPosition(), true);
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
	}
	
	private int getItemPosition() {
		for (Item item : mListItem) {
			if (item.getId() == mConfigAdapter.getItemId()) {
				for (int i = 0; i < mAdapter.getCount(); i++) {
					if (mAdapter.getItem(i).toString().equals(item.getTitle())) {
						return i;
					}
				}
			}
		}
		
		return AdapterView.INVALID_POSITION;
	}
	
	private Item getItem(String title) {
		for (Item item : mListItem) {
			if (item.getTitle().equals(title)) {
				return item;
			}
		}
		
		return null;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		l.setItemChecked(position, true);
		
		mConfigAdapter.setItemId(getItem(mAdapter.getItem(position).toString()).getId());
		mConfigAdapter.setValues();
		
		Intent iManagePHash = new Intent(ManageItem.this, ManagePHash.class);
		this.startActivity(iManagePHash);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ItemImport.PICK_FILE_REQUEST) {
			if ((resultCode == RESULT_OK) && (data != null)) {
				String contentPath = data.getStringExtra(ItemImport.KEY_PICK_FILE);
				
				if ((contentPath != null) && (mAsyncItemImport != null)) {
					mAsyncItemImport.execute(contentPath);
				}
			}
			
			mAsyncItemImport = null;
		}
	}
	
	/**
	 * AsyncTask AsyncItemImport<String, Void, List<String>> Class.
	 * 
	 * @author strider
	 */
	private class AsyncItemImport extends AsyncTask<String, Void, List<String>> {
		
		private static final String LOG_TAG = "AsyncItemImport";
		
		private long mInitTime = 0L;
		private long mWorkTime = 0L;
		
		private Context mContext = null;
		
		private Activity mActivity = null;
		
		public AsyncItemImport(Activity activity) {
			mContext = (Context) activity;
			
			mActivity = activity;
		}
		
		@Override
		protected void onPreExecute() {
			ManageItem.this.setSupportProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected List<String> doInBackground(String... params) {
			mInitTime = SystemClock.elapsedRealtime();
			
			//
			
			List<String> listContent = new ArrayList<String>();
			
			for (String contentPath : params) {
				boolean isExternalStorageAvailable = false;
				
				String state = Environment.getExternalStorageState();
				
				if (!Environment.isExternalStorageRemovable() || state.equals(Environment.MEDIA_MOUNTED)
						|| state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
					isExternalStorageAvailable = true;
					
					SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "MEDIA_AVAILABLE");
				} else {
					isExternalStorageAvailable = false;
					
					SimpleRecognizer.logIfDebug(Log.DEBUG, LOG_TAG, "MEDIA_N/A");
				}
				
				if (isExternalStorageAvailable) {
					String content = null;
					
					// 1st Implementation //TODO FIND WAY TO READ RUSSIAN CHARS
					BufferedReader buffer = null;
					
					StringBuilder sb = new StringBuilder();
					
					try {
						buffer = new BufferedReader(new FileReader(contentPath));
						
						String line = null;
						
						while ((line = buffer.readLine()) != null) {
							sb.append(line).append(SimpleRecognizer.BR_LINE);
						}
						
						content = sb.toString();
					} catch (FileNotFoundException e) {
						//TODO: SOME FEEDBACK
					} catch (IOException e) {
						//TODO: SOME FEEDBACK
					} finally {
						if (buffer != null) {
							try {
								buffer.close();
							} catch (IOException e) {
								//
							} finally {
								buffer = null;
							}
						}
					}
					/*// 2nd Implementation
					FileInputStream fis = null;
					FileChannel fc = null;
					
					MappedByteBuffer buffer = null;
					
					try {
						fis = new FileInputStream(contentPath);
						fc = fis.getChannel();
						
						buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
						
						content = Charset.defaultCharset().decode(buffer).toString();
					} catch (FileNotFoundException e) {
						//TODO: SOME FEEDBACK
					} catch (IOException e) {
						//TODO: SOME FEEDBACK
					} finally {
						if (fis != null) {
							try {
								fis.close();
							} catch (IOException e) {
								//
							} finally {
								fis = null;
							}
						}
						
						if (fc != null) {
							try {
								fc.close();
							} catch (IOException e) {
								//
							} finally {
								fc = null;
							}
						}
					}
					*/
					listContent.add(content);
				} else {
					//TODO: "Failed, External Storage Not Available."
				}
			}
			
			//
			
			mWorkTime = SystemClock.elapsedRealtime() - mInitTime;
			
			return listContent;
		}
		
		@Override
		protected void onPostExecute(List<String> result) {
			ManageItem.this.setSupportProgressBarIndeterminateVisibility(false);
			
			//
			StringBuilder sb = new StringBuilder();
			
			if (BuildConfig.DEBUG) {
				sb.append("Work Time: ").append(mWorkTime).append(" ms.").append(SimpleRecognizer.BR_LINE);
			}
			//
			
			for (String content : result) {
				showAddItem(content);
			}
		}
		
	}
	
}
