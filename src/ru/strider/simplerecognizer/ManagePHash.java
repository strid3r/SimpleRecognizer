/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * ListActivity Manage PHash Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdView;

import ru.strider.simplerecognizer.adapter.ListArrayAdapter;
import ru.strider.simplerecognizer.adapter.SpinnerArrayAdapter;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.model.Item;
import ru.strider.simplerecognizer.model.PHash;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.simplerecognizer.util.PrefsAdapter;

/**
 * ListActivity Manage PHash Class.
 * 
 * @author strider
 */
public class ManagePHash extends SherlockListActivity {
	
	private static final String LOG_TAG = "ManagePHash";
	
	private PrefsAdapter mPrefsAdapter = null;
	private ConfigAdapter mConfigAdapter = null;
	
	private Course mCourse = null;
	
	private Item mItem = null;
	
	private List<PHash> mListPHash = null;
	private List<String> mListTitle = null;
	
	private ListArrayAdapter mAdapter = null;
	private ListView mView = null;
	
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
		inflater.inflate(R.menu.manage_phash_menu, menu);
		
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
			case (R.id.managePHashMenuAddPHash): {
				DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
				dbAdapter.createDataBase(this);
				dbAdapter.open();
				
				final List<Item> listItem = dbAdapter.getListItem(mConfigAdapter.getCourseId());
				
				dbAdapter.close();
				
				List<String> listTitle = new ArrayList<String>();
				for (Item courseItem : listItem) {
					listTitle.add(courseItem.getTitle());
				}
				
				LayoutInflater inflater = LayoutInflater.from(this);
				final View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				final View view = inflater.inflate(R.layout.alert_dialog_manage_phash_edit, null);
				
				final TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.manage_phash_menu_add_phash);
				
				final Spinner spinnerItem = (Spinner) view.findViewById(R.id.spinnerItem);
				
				SpinnerArrayAdapter adapter = new SpinnerArrayAdapter(this, listTitle);
				
				spinnerItem.setAdapter(adapter);
				
				spinnerItem.setSelection(listTitle.indexOf(mItem.getTitle()));
				
				final EditText editTextHexValue = (EditText) view.findViewById(R.id.editTextHexValue);
				
				final EditText editTextComment = (EditText) view.findViewById(R.id.editTextComment);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(view);
				
				builder.setNegativeButton(R.string.dialog_button_cancel, null);
				
				builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DataBaseAdapter dbAdapter = new DataBaseAdapter(ManagePHash.this);
						dbAdapter.createDataBase(ManagePHash.this);
						dbAdapter.write();
						
						dbAdapter.addPHash(new PHash(
								editTextHexValue.getText().toString(),
								editTextComment.getText().toString(),
								listItem.get(spinnerItem.getSelectedItemPosition()).getId()
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
			case (R.id.managePHashMenuDeleteItem): {
				DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
				dbAdapter.createDataBase(this);
				dbAdapter.write();
				
				dbAdapter.deleteItem(mConfigAdapter.getItemId());
				
				dbAdapter.close();
				
				//
				Intent iMainCamera = new Intent(ManagePHash.this, MainCamera.class);
				iMainCamera.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(iMainCamera);
				//
				
				return true;
			}
			case (R.id.managePHashMenuSave): {
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
		menu.setHeaderTitle(R.string.manage_phash_context_menu_header_title);
		
		android.view.MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manage_phash_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.managePHashContextMenuShowComment): {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				
				LayoutInflater inflater = LayoutInflater.from(this);
				final View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				final View view = inflater.inflate(R.layout.alert_dialog_manage_phash_edit, null);
				
				final TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.manage_phash_context_menu_show_comment);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setMessage(Html.fromHtml(getPHash(mAdapter.getItem(info.position).toString()).getComment()));
				builder.setNeutralButton(R.string.dialog_button_close, null);
				
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
			case (R.id.managePHashContextMenuEdit): {
				DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
				dbAdapter.createDataBase(this);
				dbAdapter.open();
				
				final List<Item> listItem = dbAdapter.getListItem(mConfigAdapter.getCourseId());
				
				dbAdapter.close();
				
				List<String> listTitle = new ArrayList<String>();
				for (Item courseItem : listItem) {
					listTitle.add(courseItem.getTitle());
				}
				
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				
				final PHash pHash = getPHash(mAdapter.getItem(info.position).toString());
				
				LayoutInflater inflater = LayoutInflater.from(this);
				final View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				final View view = inflater.inflate(R.layout.alert_dialog_manage_phash_edit, null);
				
				final TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.manage_phash_context_menu_edit);
				
				final Spinner spinnerItem = (Spinner) view.findViewById(R.id.spinnerItem);
				
				SpinnerArrayAdapter adapter = new SpinnerArrayAdapter(this, listTitle);
				
				spinnerItem.setAdapter(adapter);
				
				spinnerItem.setSelection(listTitle.indexOf(mItem.getTitle()));
				
				final EditText editTextHexValue = (EditText) view.findViewById(R.id.editTextHexValue);
				editTextHexValue.setText(pHash.getHexValue());
				
				final EditText editTextComment = (EditText) view.findViewById(R.id.editTextComment);
				editTextComment.setText(pHash.getComment());
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(view);
				
				builder.setNegativeButton(R.string.dialog_button_cancel, null);
				
				builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						pHash.setHexValue(editTextHexValue.getText().toString());
						pHash.setComment(editTextComment.getText().toString());
						pHash.setItemId(listItem.get(spinnerItem.getSelectedItemPosition()).getId());
						
						DataBaseAdapter dbAdapter = new DataBaseAdapter(ManagePHash.this);
						dbAdapter.createDataBase(ManagePHash.this);
						dbAdapter.write();
						
						dbAdapter.updatePHash(pHash);
						
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
			case (R.id.managePHashContextMenuDelete): {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				
				DataBaseAdapter dbAdapter = new DataBaseAdapter(this);
				dbAdapter.createDataBase(this);
				dbAdapter.write();
				
				dbAdapter.deletePHash(getPHash(mAdapter.getItem(info.position).toString()).getId());
				
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
		
		initData();
		
		this.setTitle(mCourse.getCategory() + SimpleRecognizer.SEPARATOR + mCourse.getTitle() + SimpleRecognizer.SEPARATOR + mItem.getTitle());
		
		this.setContentView(R.layout.manage_phash);
		
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
		
		mItem = dbAdapter.getItem(mConfigAdapter.getItemId());
		
		mListPHash = dbAdapter.getListPHash(mConfigAdapter.getItemId());
		
		dbAdapter.close();
		
		if (mListPHash == null) {
			mListPHash = new ArrayList<PHash>();
		}
		
		mListTitle = new ArrayList<String>();
		for (PHash pHash : mListPHash) {
			mListTitle.add(pHash.getHexValue());
		}
	}
	
	private void reloadView() {
		initData();
		
		mAdapter = new ListArrayAdapter(ManagePHash.this, mListTitle);
		
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
		//mConfigAdapter.getValues();
		
		//
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
	}
	
	private PHash getPHash(String hexValue) {
		for (PHash pHash : mListPHash) {
			if (pHash.getHexValue().equals(hexValue)) {
				return pHash;
			}
		}
		
		return null;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		l.setItemChecked(position, true);
		
		//
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
