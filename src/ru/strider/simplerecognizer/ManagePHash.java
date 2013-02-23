/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BaseListActivity ManagePHash Class
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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;

import ru.strider.adapter.BaseArrayAdapter;
import ru.strider.app.BaseListActivity;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.model.Item;
import ru.strider.simplerecognizer.model.PHash;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.simplerecognizer.util.ImagePHash;
import ru.strider.util.BuildConfig;
import ru.strider.util.Text;
import ru.strider.view.OnLockViewListener;

/**
 * BaseListActivity ManagePHash Class.
 * 
 * @author strider
 */
public class ManagePHash extends BaseListActivity implements OnLockViewListener {
	
	private static final String LOG_TAG = ManagePHash.class.getSimpleName();
	
	private Course mCourse = null;
	private Item mItem = null;
	
	private ConfigAdapter mConfigAdapter = null;
	
	private Menu mMainMenu = null;
	
	private BaseArrayAdapter<PHash> mAdapter = null;
	private ListView mView = null;
	
	private boolean mIsLock = false;
	
	private AsyncPHashImport mAsyncPHashImport = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		doInit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		useConfigValues();
		
		SimpleRecognizer.getMediaReceiver().startWatchingExternalStorage(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		SimpleRecognizer.getMediaReceiver().stopWatchingExternalStorage(this);
	}
	
	@Override
	protected void onDestroy() {
		mCourse = null;
		mItem = null;
		
		mConfigAdapter = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mMainMenu = null;
		}
		
		mAdapter = null;
		mView = null;
		
		mAsyncPHashImport = null;
		
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.manage_phash_menu, menu);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mMainMenu = menu;
		}
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			menu.setGroupEnabled(R.id.managePHashMenuContent, (!mIsLock));
			menu.setGroupEnabled(R.id.managePHashMenuControls, (!mIsLock));
		} else {
			SubMenu action = menu.findItem(R.id.mainActionOverflow).getSubMenu();
			
			action.setGroupEnabled(R.id.managePHashMenuContent, (!mIsLock));
			action.setGroupEnabled(R.id.managePHashMenuControls, (!mIsLock));
		}
		
		boolean isCreator = SimpleRecognizer.checkCourseCreator(mCourse.getCreator());
		
		menu.findItem(R.id.managePHashMenuDeleteItem)
				.setEnabled((!mIsLock) && isCreator)
				.setVisible(isCreator);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.mainActionOverflow): {
				performActionMenu(false);
				
				return true;
			}
			case (R.id.managePHashMenuImport): {
				mAsyncPHashImport = new AsyncPHashImport();
				
				FileManager.requestPickFile(this, FileManager.FILE_TYPE_IMAGE);
				
				return true;
			}
			case (R.id.managePHashMenuAddPHash): {
				buildAddPHash(null).show();//TODO: Dialog
				
				return true;
			}
			case (R.id.managePHashMenuDeleteItem): {
				DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
				
				dbAdapter.write();
				
				dbAdapter.deleteItem(mItem.getId());
				
				dbAdapter.close();
				
				this.finish();
				
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
		
		//menu.setHeaderIcon(iconRes);
		menu.setHeaderTitle(R.string.manage_phash_context_menu_header_title);
		
		android.view.MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.manage_phash_context_menu, menu);
		
		menu.setGroupEnabled(
				R.id.managePHashContextMenuControls,
				((!mIsLock) && mConfigAdapter.getIsCreator())
			);
		
		if (mConfigAdapter.getIsCreator()) {
			boolean isCreator = SimpleRecognizer.checkCourseCreator(mCourse.getCreator());
			
			menu.findItem(R.id.managePHashContextMenuEdit)
					.setEnabled((!mIsLock) && isCreator)
					.setVisible(isCreator);
			menu.findItem(R.id.managePHashContextMenuDelete)
					.setEnabled((!mIsLock) && isCreator)
					.setVisible(isCreator);
		}
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		final PHash pHash = mAdapter.getItem(info.position);
		
		switch (item.getItemId()) {
			case (R.id.managePHashContextMenuShowComment): {//TODO: Dialog
				LayoutInflater inflater = LayoutInflater.from(this);
				View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				View viewContent = inflater.inflate(R.layout.alert_dialog_manage_phash_show_comment, null);
				
				TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(pHash.getHexValue());
				textViewTitle.setSelected(true);
				
				EditText editTextComment = (EditText) viewContent.findViewById(R.id.editTextComment);
				editTextComment.setText(Html.fromHtml(pHash.getComment().replace(Text.LF, Text.BR)));
				editTextComment.setEnabled(false);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(viewContent);
				
				builder.setNeutralButton(R.string.dialog_button_close, null);
				
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
			case (R.id.managePHashContextMenuEdit): {
				DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
				
				dbAdapter.open();
				
				List<Item> listItem = dbAdapter.getListItem(mCourse.getId());
				
				dbAdapter.close();
				
				final BaseArrayAdapter<Item> adapter = new BaseArrayAdapter<Item>(
						this,
						R.layout.spinner_item_activated
					);
				adapter.setDropDownViewResource(R.layout.list_item_single_choice_activated);
				
				int itemPosition = AdapterView.INVALID_POSITION;
				
				if (listItem != null) {
					itemPosition = listItem.indexOf(mItem);
					
					adapter.addData(listItem);
				}
				//TODO: Dialog
				LayoutInflater inflater = LayoutInflater.from(this);
				View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				View viewContent = inflater.inflate(R.layout.alert_dialog_manage_phash_edit, null);
				
				TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.manage_phash_context_menu_edit);
				textViewTitle.setSelected(true);
				
				final Spinner spinnerItem = (Spinner) viewContent.findViewById(R.id.spinnerItem);
				spinnerItem.setAdapter(adapter);
				spinnerItem.setSelection(itemPosition);
				
				final EditText editTextHexValue = (EditText) viewContent.findViewById(R.id.editTextHexValue);
				editTextHexValue.setText(pHash.getHexValue());
				
				final EditText editTextComment = (EditText) viewContent.findViewById(R.id.editTextComment);
				editTextComment.setText(pHash.getComment());
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(viewContent);
				
				builder.setNegativeButton(R.string.dialog_button_cancel, null);
				
				builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							pHash.setHexValue(editTextHexValue.getText().toString().trim());
							pHash.setComment(editTextComment.getText().toString().trim());
							pHash.setItemId(adapter.getItem(spinnerItem.getSelectedItemPosition()).getId());
							
							DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
							
							dbAdapter.write();
							
							dbAdapter.updatePHash(pHash);
							
							dbAdapter.close();
							
							initView();
						}
						
					});
				
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
			case (R.id.managePHashContextMenuDelete): {
				DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
				
				dbAdapter.write();
				
				dbAdapter.deletePHash(pHash.getId());
				
				dbAdapter.close();
				
				initView();
				
				return true;
			}
			default: {
				return super.onContextItemSelected(item);
			}
		}
	}
	
	private void doInit() {
		mConfigAdapter = ConfigAdapter.getInstance();
		
		Intent intent = this.getIntent();
		
		mCourse = intent.getParcelableExtra(Course.KEY);
		mItem = intent.getParcelableExtra(Item.KEY);
		
		this.setTitle(mCourse.getCategory()
				+ Text.SEPARATOR + mCourse.getTitle()
				+ Text.SEPARATOR + mItem.getTitle());
		
		this.setContentView(R.layout.manage_phash);
		
		this.setSupportProgressBarIndeterminateVisibility(false);
		
		mAdapter = new BaseArrayAdapter<PHash>(this, R.layout.list_item_activated);
		mView = this.getListView();
		
		mView.setEmptyView(this.findViewById(R.id.textViewEmpty));
		mView.setAdapter(mAdapter);
		//this.setListAdapter(mAdapter);
		this.registerForContextMenu(mView);
		
		mView.setTextFilterEnabled(true);
		//mView.setItemsCanFocus(false);
		//mView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		initData();
	}
	
	private void initData() {
		mAdapter.clear();
		
		DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
		
		dbAdapter.open();
		
		List<PHash> listPHash = dbAdapter.getListPHash(mItem.getId());
		
		dbAdapter.close();
		
		if (listPHash != null) {
			mAdapter.addData(listPHash);
		}
		
		mAdapter.notifyDataSetChanged();
	}
	
	private void initView() {
		initData();
		
		useConfigValues();
	}
	
	private void useConfigValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
		
		mConfigAdapter.getValues();
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
	
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		//
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
		if (requestCode == FileManager.REQUEST_FILE) {
			if ((resultCode == RESULT_OK) && (data != null)) {
				String file = data.getStringExtra(FileManager.KEY_FILE);
				
				if ((!TextUtils.isEmpty(file)) && (mAsyncPHashImport != null)) {
					mAsyncPHashImport.execute(file);
				}
			}
			
			mAsyncPHashImport = null;
		}
	}
	
	private AlertDialog buildAddPHash(String pHashHex) {//TODO: Dialog
		DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
		
		dbAdapter.open();
		
		List<Item> listItem = dbAdapter.getListItem(mCourse.getId());
		
		dbAdapter.close();
		
		final BaseArrayAdapter<Item> adapter = new BaseArrayAdapter<Item>(
				this,
				R.layout.spinner_item_activated
			);
		adapter.setDropDownViewResource(R.layout.list_item_single_choice_activated);
		
		int itemPosition = AdapterView.INVALID_POSITION;
		
		if (listItem != null) {
			itemPosition = listItem.indexOf(mItem);
			
			adapter.addData(listItem);
		}
		
		LayoutInflater inflater = LayoutInflater.from(this);
		View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
		View viewContent = inflater.inflate(R.layout.alert_dialog_manage_phash_edit, null);
		
		TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
		textViewTitle.setText(R.string.manage_phash_menu_add_phash);
		textViewTitle.setSelected(true);
		
		final Spinner spinnerItem = (Spinner) viewContent.findViewById(R.id.spinnerItem);
		spinnerItem.setAdapter(adapter);
		spinnerItem.setSelection(itemPosition);
		
		final EditText editTextHexValue = (EditText) viewContent.findViewById(R.id.editTextHexValue);
		
		if (!TextUtils.isEmpty(pHashHex)) {
			editTextHexValue.setText(pHashHex);
		}
		
		final EditText editTextComment = (EditText) viewContent.findViewById(R.id.editTextComment);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCustomTitle(viewTitle);
		builder.setView(viewContent);
		
		builder.setNegativeButton(R.string.dialog_button_cancel, null);
		
		builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
					
					dbAdapter.write();
					
					dbAdapter.addPHash(new PHash(
							editTextHexValue.getText().toString().trim(),
							editTextComment.getText().toString().trim(),
							adapter.getItem(spinnerItem.getSelectedItemPosition()).getId()
						));
					
					dbAdapter.close();
					
					initView();
				}
				
			});
		
		AlertDialog alert = builder.create();
		
		return alert;
	}
	
	/**
	 * AsyncTask AsyncPHashImport<String, Void, List<String>> Class.
	 * 
	 * @author strider
	 */
	private class AsyncPHashImport extends AsyncTask<String, Void, List<String>> {
		
		//private static final String LOG_TAG = "AsyncPHashImport";
		
		private long mInitTime = 0L;
		
		@Override
		protected void onPreExecute() {
			mInitTime = SystemClock.elapsedRealtime();
			
			ManagePHash.this.onLockView(true);
		}
		
		@Override
		protected List<String> doInBackground(String... params) {
			List<String> listPHashHex = new ArrayList<String>();
			
			for (String path : params) {
				if (SimpleRecognizer.getMediaReceiver().isExternalStorageAvailable()) {
					String pHashHex = ImagePHash.getPHash(path);
					
					if (pHashHex != null) {
						listPHashHex.add(pHashHex);
					}
				} else {
					// TODO: "Failed, external storage not available."
				}
			}
			
			return listPHashHex;
		}
		
		@Override
		protected void onPostExecute(List<String> result) {
			if (!ManagePHash.this.isDestroy()) {
				StringBuilder sb = new StringBuilder();
				
				if (BuildConfig.DEBUG) {
					sb.append("Work Time: ");
					sb.append(SystemClock.elapsedRealtime() - mInitTime);
					sb.append(" ms.").append(Text.LF);
				}
				
				for (String pHashHex : result) {
					buildAddPHash(pHashHex).show();
				}
				
				ManagePHash.this.onLockView(false);
			}
		}
		
	}
	
}
