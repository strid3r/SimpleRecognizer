/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BaseListActivity ManageItem Class
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
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import ru.strider.adapter.BaseArrayAdapter;
import ru.strider.app.BaseListActivity;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.model.Item;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.util.BuildConfig;
import ru.strider.util.Text;

/**
 * BaseListActivity ManageItem Class.
 * 
 * @author strider
 */
public class ManageItem extends BaseListActivity {
	
	private static final String LOG_TAG = ManageItem.class.getSimpleName();
	
	private Course mCourse = null;
	
	private ConfigAdapter mConfigAdapter = null;
	
	private Menu mMainMenu = null;
	
	private BaseArrayAdapter<Item> mAdapter = null;
	private ListView mView = null;
	
	private AsyncItemImport mAsyncItemImport = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		doInit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		useConfigValues();
		
		SimpleRecognizer.mediaReceiver.startWatchingExternalStorage(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		SimpleRecognizer.mediaReceiver.stopWatchingExternalStorage(this);
	}
	
	@Override
	protected void onDestroy() {
		mCourse = null;
		
		mConfigAdapter = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mMainMenu = null;
		}
		
		mAdapter = null;
		mView = null;
		
		mAsyncItemImport = null;
		
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.manage_item_menu, menu);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mMainMenu = menu;
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.mainActionOverflow): {
				performActionMenu(false);
				
				return true;
			}
			case (R.id.manageItemMenuImport): {
				mAsyncItemImport = new AsyncItemImport();
				
				Intent iPickFile = new Intent(this, FileManager.class);
				iPickFile.putExtra(FileManager.KEY_FILE_TYPE, FileManager.FILE_TYPE_ALL);
				this.startActivityForResult(iPickFile, FileManager.REQUEST_FILE);
				
				return true;
			}
			case (R.id.manageItemMenuAddItem): {
				buildAddItem(null).show();
				
				return true;
			}
			case (R.id.manageItemMenuDeleteCourse): {
				DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance(this);
				
				dbAdapter.write();
				
				dbAdapter.deleteCourse(mCourse.getId());
				
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
		menu.setHeaderTitle(R.string.manage_item_context_menu_header_title);
		
		android.view.MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.manage_item_context_menu, menu);
		
		if (mConfigAdapter.getIsCreator()) {
			boolean isCreator = SimpleRecognizer.checkCourseCreator(this, mCourse.getCreator());
			
			menu.findItem(R.id.manageItemContextMenuEdit).setEnabled(isCreator).setVisible(isCreator);
			menu.findItem(R.id.manageItemContextMenuDelete).setEnabled(isCreator).setVisible(isCreator);
		}
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem menuItem) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuItem.getMenuInfo();
		
		final Item item = mAdapter.getItem(info.position);
		
		switch (menuItem.getItemId()) {
			case (R.id.manageItemContextMenuApplySelection): {
				mConfigAdapter.setItemId(item.getId());
				
				mConfigAdapter.setValues();
				
				Intent iMainCamera = new Intent(this, MainCamera.class);
				iMainCamera.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(iMainCamera);
				
				return true;
			}
			case (R.id.manageItemContextMenuShowContent): {
				LayoutInflater inflater = LayoutInflater.from(this);
				View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				View viewContent = inflater.inflate(R.layout.alert_dialog_manage_item_show_content, null);
				
				TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(item.getTitle());
				textViewTitle.setSelected(true);
				
				EditText editTextContent = (EditText) viewContent.findViewById(R.id.editTextContent);
				editTextContent.setText(Html.fromHtml(item.getContent()));
				editTextContent.setEnabled(false);
				
				EditText editTextVideoUri = (EditText) viewContent.findViewById(R.id.editTextVideoUri);
				editTextVideoUri.setText(item.getVideoUri());
				editTextVideoUri.setEnabled(false);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(viewContent);
				
				builder.setNeutralButton(R.string.dialog_button_close, null);
				
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
			case (R.id.manageItemContextMenuEdit): {
				LayoutInflater inflater = LayoutInflater.from(this);
				View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
				View viewContent = inflater.inflate(R.layout.alert_dialog_manage_item_edit, null);
				
				TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
				textViewTitle.setText(R.string.manage_item_context_menu_edit);
				textViewTitle.setSelected(true);
				
				final EditText editTextTitle = (EditText) viewContent.findViewById(R.id.editTextTitle);
				editTextTitle.setText(item.getTitle());
				
				final EditText editTextContent = (EditText) viewContent.findViewById(R.id.editTextContent);
				editTextContent.setText(item.getContent());
				
				final EditText editTextVideoUri = (EditText) viewContent.findViewById(R.id.editTextVideoUri);
				editTextVideoUri.setText(item.getVideoUri());
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCustomTitle(viewTitle);
				builder.setView(viewContent);
				
				builder.setNegativeButton(R.string.dialog_button_cancel, null);
				
				builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							item.setTitle(editTextTitle.getText().toString().trim());
							item.setContent(editTextContent.getText().toString().trim());
							item.setVideoUri(editTextVideoUri.getText().toString().trim());
							
							DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance(ManageItem.this);
							
							dbAdapter.write();
							
							dbAdapter.updateItem(item);
							
							dbAdapter.close();
							
							initView();
						}
						
					});
				
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
			case (R.id.manageItemContextMenuDelete): {
				DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance(this);
				
				dbAdapter.write();
				
				dbAdapter.deleteItem(item.getId());
				
				dbAdapter.close();
				
				initView();
				
				return true;
			}
			default: {
				return super.onContextItemSelected(menuItem);
			}
		}
	}
	
	private void doInit() {
		mConfigAdapter = ConfigAdapter.getInstance(this);
		
		mCourse = this.getIntent().getParcelableExtra(Course.KEY);
		
		this.setTitle(mCourse.getCategory() + Text.SEPARATOR + mCourse.getTitle());
		
		this.setContentView(R.layout.manage_item);
		
		this.setSupportProgressBarIndeterminateVisibility(false);
		
		mAdapter = new BaseArrayAdapter<Item>(this, R.layout.list_item_single_choice_activated);
		mView = this.getListView();
		
		mView.setEmptyView(this.findViewById(R.id.textViewEmpty));
		mView.setAdapter(mAdapter);
		//this.setListAdapter(mAdapter);
		this.registerForContextMenu(mView);
		
		mView.setTextFilterEnabled(true);
		//mView.setItemsCanFocus(false);
		mView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		initData();
	}
	
	private void initData() {
		mAdapter.clear();
		
		DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance(this);
		
		dbAdapter.open();
		
		List<Item> listItem = dbAdapter.getListItem(mCourse.getId());
		
		dbAdapter.close();
		
		if (listItem != null) {
			mAdapter.addData(listItem);
		}
		
		mAdapter.notifyDataSetChanged();
	}
	
	private void initView() {
		initData();
		
		useConfigValues();
	}
	
	private AlertDialog buildAddItem(String content) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View viewTitle = inflater.inflate(R.layout.alert_dialog_title, null);
		View viewContent = inflater.inflate(R.layout.alert_dialog_manage_item_edit, null);
		
		TextView textViewTitle = (TextView) viewTitle.findViewById(R.id.textViewAlertDialogTitle);
		textViewTitle.setText(R.string.manage_item_menu_add_item);
		textViewTitle.setSelected(true);
		
		final EditText editTextTitle = (EditText) viewContent.findViewById(R.id.editTextTitle);
		
		final EditText editTextContent = (EditText) viewContent.findViewById(R.id.editTextContent);
		
		if (!TextUtils.isEmpty(content)) {
			editTextContent.setText(content);
		}
		
		final EditText editTextVideoUri = (EditText) viewContent.findViewById(R.id.editTextVideoUri);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCustomTitle(viewTitle);
		builder.setView(viewContent);
		
		builder.setNegativeButton(R.string.dialog_button_cancel, null);
		
		builder.setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance(ManageItem.this);
					
					dbAdapter.write();
					
					dbAdapter.addItem(new Item(
							editTextTitle.getText().toString().trim(),
							editTextContent.getText().toString().trim(),
							editTextVideoUri.getText().toString().trim(),
							mCourse.getId()
						));
					
					dbAdapter.close();
					
					initView();
				}
				
			});
		
		AlertDialog alert = builder.create();
		
		return alert;
	}
	
	private void useConfigValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
		
		mConfigAdapter.getValues();
		
		mView.setItemChecked(getItemPosition(), true);
	}
	
	private int getItemPosition() {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			if (mAdapter.getItem(i).getId() == mConfigAdapter.getItemId()) {
				return i;
			}
		}
		
		return AdapterView.INVALID_POSITION;
	}
	
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		listView.setItemChecked(position, true);
		
		Item item = mAdapter.getItem(position);
		
		mConfigAdapter.setItemId(item.getId());
		
		mConfigAdapter.setValues();
		
		Intent iManagePHash = new Intent(this, ManagePHash.class);
		iManagePHash.putExtra(Course.KEY, mCourse);
		iManagePHash.putExtra(Item.KEY, item);
		this.startActivity(iManagePHash);
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
				
				if ((!TextUtils.isEmpty(file)) && (mAsyncItemImport != null)) {
					mAsyncItemImport.execute(file);
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
		
		//private static final String LOG_TAG = "AsyncItemImport";
		
		private long mInitTime = 0L;
		
		//private Context mContext = null;
		
		public AsyncItemImport() {
			//mContext = ManageItem.this.getApplicationContext();
		}
		
		@Override
		protected void onPreExecute() {
			mInitTime = SystemClock.elapsedRealtime();
			
			ManageItem.this.setSupportProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected List<String> doInBackground(String... params) {
			List<String> listContent = new ArrayList<String>();
			
			for (String path : params) {
				if (SimpleRecognizer.mediaReceiver.isExternalStorageAvailable()) {
					String content = null;
					
					// 1st Implementation // TODO: FIND A WAY TO READ RUSSIAN CHARS
					BufferedReader buffer = null;
					
					StringBuilder sb = new StringBuilder();
					
					try {
						buffer = new BufferedReader(new FileReader(path));
						
						String line = null;
						
						while ((line = buffer.readLine()) != null) {
							sb.append(line).append(Text.LF);
						}
						
						content = sb.toString();
					} catch (FileNotFoundException e) {
						// TODO: SOME FEEDBACK TO USER
					} catch (IOException e) {
						// TODO: SOME FEEDBACK TO USER
					} finally {
						if (buffer != null) {
							try {
								buffer.close();
							} catch (IOException e) {
								//
							}
							
							buffer = null;
						}
					}
					/*// 2nd Implementation
					FileInputStream fis = null;
					FileChannel fc = null;
					
					MappedByteBuffer buffer = null;
					
					try {
						fis = new FileInputStream(path);
						fc = fis.getChannel();
						
						buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
						
						content = Charset.defaultCharset().decode(buffer).toString();
					} catch (FileNotFoundException e) {
						// TODO: SOME FEEDBACK TO USER
					} catch (IOException e) {
						// TODO: SOME FEEDBACK TO USER
					} finally {
						if (fis != null) {
							try {
								fis.close();
							} catch (IOException e) {
								//
							}
							
							fis = null;
						}
						
						if (fc != null) {
							try {
								fc.close();
							} catch (IOException e) {
								//
							}
							
							fc = null;
						}
					}
					*/
					listContent.add(content);
				} else {
					// TODO: "Failed, external storage not available."
				}
			}
			
			return listContent;
		}
		
		@Override
		protected void onPostExecute(List<String> result) {
			if (ManageItem.this.isAlive()) {
				ManageItem.this.setSupportProgressBarIndeterminateVisibility(false);
				
				StringBuilder sb = new StringBuilder();
				
				if (BuildConfig.DEBUG) {
					sb.append("Work Time: ");
					sb.append(SystemClock.elapsedRealtime() - mInitTime);
					sb.append(" ms.").append(Text.LF);
				}
				
				for (String content : result) {
					buildAddItem(content).show();
				}
			}
		}
		
	}
	
}
