/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BaseFragmentActivity ManageItem Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;

import ru.strider.adapter.BaseArrayAdapter;
import ru.strider.app.BaseDialogFragment;
import ru.strider.app.BaseFragmentActivity;
import ru.strider.simplerecognizer.database.DataBaseAdapter;
import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.model.Item;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.simplerecognizer.view.OnItemListener;
import ru.strider.util.Text;
import ru.strider.view.OnLockViewListener;

/**
 * BaseFragmentActivity ManageItem Class.
 * 
 * @author strider
 */
public class ManageItem extends BaseFragmentActivity implements OnLockViewListener, OnItemListener,
			AdapterView.OnItemClickListener {
	
	private static final String LOG_TAG = ManageItem.class.getSimpleName();
	
	private Course mCourse = null;
	
	private ConfigAdapter mConfigAdapter = null;
	
	private Menu mMainMenu = null;
	
	private BaseArrayAdapter<Item> mAdapter = null;
	private ListView mView = null;
	
	private boolean mIsLock = false;
	
	private AsyncItemImport mAsyncItemImport = null;
	
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			menu.setGroupEnabled(R.id.manageItemMenuContent, (!mIsLock));
			menu.setGroupEnabled(R.id.manageItemMenuControls, (!mIsLock));
		} else {
			SubMenu action = menu.findItem(R.id.mainActionOverflow).getSubMenu();
			
			action.setGroupEnabled(R.id.manageItemMenuContent, (!mIsLock));
			action.setGroupEnabled(R.id.manageItemMenuControls, (!mIsLock));
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
				mAsyncItemImport = new AsyncItemImport(mCourse);
				
				FileManager.requestPickFile(this, FileManager.FILE_TYPE_ALL);
				
				return true;
			}
			case (R.id.manageItemMenuAddItem): {
				AddItemDialog.newInstance(mCourse, null)
						.show(this.getSupportFragmentManager(), AddItemDialog.KEY);
				
				return true;
			}
			case (R.id.manageItemMenuDeleteCourse): {//TODO: Remove or dialog...
				DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
				
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
		
		menu.setGroupEnabled(
				R.id.manageItemContextMenuControls,
				((!mIsLock) && mConfigAdapter.getIsCreator())
			);
		
		if (mConfigAdapter.getIsCreator()) {
			boolean isCreator = SimpleRecognizer.checkCourseCreator(mCourse.getCreator());
			
			menu.findItem(R.id.manageItemContextMenuEdit)
					.setEnabled((!mIsLock) && isCreator)
					.setVisible(isCreator);
			menu.findItem(R.id.manageItemContextMenuDelete)
					.setEnabled((!mIsLock) && isCreator)
					.setVisible(isCreator);
		}
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem menuItem) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuItem.getMenuInfo();
		
		Item item = mAdapter.getItem(info.position);
		
		switch (menuItem.getItemId()) {
			case (R.id.manageItemContextMenuApplySelection): {
				mConfigAdapter.setItemId(item.getId()).setValues();
				
				Intent iMainCamera = new Intent(this, MainCamera.class);
				iMainCamera.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(iMainCamera);
				
				return true;
			}
			case (R.id.manageItemContextMenuShowContent): {
				ShowItemDialog.newInstance(item)
						.show(this.getSupportFragmentManager(), ShowItemDialog.KEY);
				
				return true;
			}
			case (R.id.manageItemContextMenuEdit): {
				AddItemDialog.newInstance(mCourse, item)
						.show(this.getSupportFragmentManager(), AddItemDialog.KEY);
				
				return true;
			}
			case (R.id.manageItemContextMenuDelete): {
				DeleteItemDialog.newInstance(item)
						.show(this.getSupportFragmentManager(), DeleteItemDialog.KEY);
				
				return true;
			}
			default: {
				return super.onContextItemSelected(menuItem);
			}
		}
	}
	
	private void doInit() {
		mConfigAdapter = ConfigAdapter.getInstance();
		
		mCourse = this.getIntent().getParcelableExtra(Course.KEY);
		
		this.setTitle(mCourse.getCategory() + Text.SEPARATOR + mCourse.getTitle());
		
		this.setContentView(R.layout.manage_item);
		
		this.setSupportProgressBarIndeterminateVisibility(false);
		
		mAdapter = new BaseArrayAdapter<Item>(this, R.layout.list_item_single_choice_activated);
		mView = (ListView) this.findViewById(R.id.listViewItem);
		//mView = this.getListView();
		
		mView.setEmptyView(this.findViewById(R.id.textViewEmpty));
		mView.setAdapter(mAdapter);
		//this.setListAdapter(mAdapter);
		this.registerForContextMenu(mView);
		
		mView.setTextFilterEnabled(true);
		//mView.setItemsCanFocus(false);
		mView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mView.setOnItemClickListener(this);
		
		initData();
	}
	
	private void initData() {
		mAdapter.clear();
		
		DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
		
		dbAdapter.open();
		
		List<Item> listItem = dbAdapter.getListItem(mCourse.getId());
		
		dbAdapter.close();
		
		if (listItem != null) {
			mAdapter.addData(listItem);
		}
		
		mAdapter.notifyDataSetChanged();
	}
	
	private void useConfigValues() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "useConfigValues() called");
		
		mConfigAdapter.getValues();
		
		mView.setItemChecked(getItemPosition(), true);
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
	public void onItemChanged() {
		onLockView(true);
		
		if (!this.isDestroy()) {
			initData();
			
			useConfigValues();
		}
		
		onLockView(false);
	}
	
	@Override
	public void onDeleteItem(Object item) {
		onLockView(true);
		
		if (!this.isDestroy()) {
			mAdapter.remove((Item) item);
			
			mAdapter.notifyDataSetChanged();
		}
		
		onLockView(false);
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		((ListView) parent).setItemChecked(position, true);
		
		Item item = mAdapter.getItem(position);
		
		mConfigAdapter.setItemId(item.getId()).setValues();
		
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
	 * BaseDialogFragment AddItemDialog Class.
	 * 
	 * @author strider
	 */
	public static class AddItemDialog extends BaseDialogFragment implements OnLockViewListener {
		
		//private static final String LOG_TAG = AddItemDialog.class.getSimpleName();
		
		public static final String KEY = AddItemDialog.class.getSimpleName();
		
		private static final String KEY_MODE = "Mode";
		
		private static final int VIEW_FORM_POSITION = 0;
		private static final int VIEW_PROGRESS_POSITION = 1;
		
		private OnItemListener mItemListener = null;
		
		private Course mCourse = null;
		private Item mItem = null;
		
		private View mTitle = null;
		private View mView = null;
		
		private ViewSwitcher mViewSwitcher = null;
		
		private EditText mEditTextTitle = null;
		private EditText mEditTextContent = null;
		private EditText mEditTextVideoUri = null;
		
		private boolean mIsLock = false;
		
		private boolean mIsEditMode = false;
		
		public static AddItemDialog newInstance(Course course, Item item) {
			AddItemDialog fragment = new AddItemDialog();
			
			Bundle args = new Bundle();
			args.putParcelable(Course.KEY, course);
			args.putParcelable(Item.KEY, item);
			
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public Course getCourse() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.<Course> getParcelable(Course.KEY) : null);
		}
		
		public Item getItem() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.<Item> getParcelable(Item.KEY) : null);
		}
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
			try {
				mItemListener = (OnItemListener) activity;
			} catch (ClassCastException e) {
				throw (new ClassCastException(
						activity.toString() + " must implement OnItemListener."
					));
			}
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mCourse = getCourse();
			
			if (mCourse == null) {
				throw (new IllegalArgumentException(
						"The Fragment requires valid Course as an argument."
					));
			}
			
			if (savedInstanceState != null) {
				mItem = savedInstanceState.getParcelable(Item.KEY);
				
				mIsEditMode = savedInstanceState.getBoolean(KEY_MODE, false);
			} else {
				mItem = getItem();
				
				if (mItem == null) {
					mItem = new Item();
					
					mItem.setCourseId(mCourse.getId());
				} else {
					if ((mItem.getId() != 0L) && (mItem.getCourseId() == mCourse.getId())) {
						mIsEditMode = true;
					}
				}
			}
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = LayoutInflater.from((Context) this.getSherlockActivity());
			
			mTitle = inflater.inflate(R.layout.alert_dialog_title, null);
			mView = inflater.inflate(R.layout.alert_dialog_add_item, null);
			
			mViewSwitcher = (ViewSwitcher) mView.findViewById(R.id.viewSwitcherAlertDialogAddItem);
			
			this.registerNegativeButton(mView, R.id.buttonAlertDialogCancel);
			this.registerPositiveButton(mView, R.id.buttonAlertDialogOk);
			
			return (new AlertDialog.Builder(inflater.getContext()))
					.setCustomTitle(mTitle)
					.setView(mView)
					.create();
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			TextView textViewTitle = (TextView) mTitle.findViewById(R.id.textViewAlertDialogTitle);
			textViewTitle.setText(mIsEditMode
					? R.string.manage_item_context_menu_edit
					: R.string.manage_item_menu_add_item
				);
			textViewTitle.setSelected(true);
			
			mEditTextTitle = (EditText) mView.findViewById(R.id.editTextItemTitle);
			mEditTextTitle.setText(mItem.getTitle());
			
			mEditTextContent = (EditText) mView.findViewById(R.id.editTextItemContent);
			mEditTextContent.setText(mItem.getContent());
			
			mEditTextVideoUri = (EditText) mView.findViewById(R.id.editTextItemVideoUri);
			mEditTextVideoUri.setText(mItem.getVideoUri());
			
			TextView textViewAddItem = (TextView) mView.findViewById(R.id.textViewAddItemHint);
			textViewAddItem.setSelected(true);
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			
			mItem.setTitle(mEditTextTitle.getText().toString().trim());
			mItem.setContent(mEditTextContent.getText().toString().trim());
			mItem.setVideoUri(mEditTextVideoUri.getText().toString().trim());
			
			outState.putParcelable(Item.KEY, mItem);
			
			outState.putBoolean(KEY_MODE, mIsEditMode);
		}
		
		@Override
		public void onDestroyView() {
			super.onDestroyView();
			
			mTitle = null;
			mView = null;
			
			mViewSwitcher = null;
			
			mEditTextTitle = null;
			mEditTextContent = null;
			mEditTextVideoUri = null;
		}
		
		@Override
		public void onDestroy() {
			mCourse = null;
			mItem = null;
			
			super.onDestroy();
		}
		
		@Override
		public void onDetach() {
			mItemListener = null;
			
			super.onDetach();
		}
		
		@Override
		public void onLockView(boolean isLock) {
			mIsLock = isLock;
			
			if (!this.isDestroy()) {
				mViewSwitcher.setDisplayedChild(isLock
						? VIEW_PROGRESS_POSITION
						: VIEW_FORM_POSITION
					);
			}
			
			this.setCancelable(!isLock);
		}
		
		@Override
		public boolean isLock() {
			return mIsLock;
		}
		
		private void itemChanged() {
			if (mItemListener != null) {
				mItemListener.onItemChanged();
			}
		}
		
		@Override
		public void onPositiveClick(View view) {
			onLockView(true);
			
			mItem.setTitle(mEditTextTitle.getText().toString().trim());
			mItem.setContent(mEditTextContent.getText().toString().trim());
			mItem.setVideoUri(mEditTextVideoUri.getText().toString().trim());
			
			DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
			
			dbAdapter.write();
			
			if (mIsEditMode) {
				dbAdapter.updateItem(mItem);
			} else {
				dbAdapter.addItem(mItem);
			}
			
			dbAdapter.close();
			
			itemChanged();
			
			super.onPositiveClick(view);
			
			onLockView(false);
		}
		
	}
	
	/**
	 * BaseDialogFragment DeleteItemDialog Class.
	 * 
	 * @author strider
	 */
	public static class DeleteItemDialog extends BaseDialogFragment implements OnLockViewListener {
		
		//private static final String LOG_TAG = DeleteItemDialog.class.getSimpleName();
		
		public static final String KEY = DeleteItemDialog.class.getSimpleName();
		
		private static final int VIEW_FORM_POSITION = 0;
		private static final int VIEW_PROGRESS_POSITION = 1;
		
		private OnItemListener mItemListener = null;
		
		private Item mItem = null;
		
		private View mTitle = null;
		private View mView = null;
		
		private ViewSwitcher mViewSwitcher = null;
		
		private boolean mIsLock = false;
		
		public static DeleteItemDialog newInstance(Item item) {
			DeleteItemDialog fragment = new DeleteItemDialog();
			
			Bundle args = new Bundle();
			args.putParcelable(Item.KEY, item);
			
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public Item getItem() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.<Item> getParcelable(Item.KEY) : null);
		}
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
			try {
				mItemListener = (OnItemListener) activity;
			} catch (ClassCastException e) {
				throw (new ClassCastException(
						activity.toString() + " must implement OnItemListener."
					));
			}
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mItem = getItem();
			
			if (mItem == null) {
				throw (new IllegalArgumentException(
						"The Fragment requires valid Item as an argument."
					));
			}
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = LayoutInflater.from((Context) this.getSherlockActivity());
			
			mTitle = inflater.inflate(R.layout.alert_dialog_title, null);
			mView = inflater.inflate(R.layout.alert_dialog_delete, null);
			
			mViewSwitcher = (ViewSwitcher) mView.findViewById(R.id.viewSwitcherAlertDialogDelete);
			
			this.registerNegativeButton(mView, R.id.buttonAlertDialogCancel);
			this.registerPositiveButton(mView, R.id.buttonAlertDialogDelete);
			
			return (new AlertDialog.Builder(inflater.getContext()))
					.setCustomTitle(mTitle)
					.setView(mView)
					.create();
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			TextView textViewTitle = (TextView) mTitle.findViewById(R.id.textViewAlertDialogTitle);
			
			StringBuilder sb = new StringBuilder();
			
			int color = this.getResources().getColor(R.color.main);
			
			sb.append("<font color=\"").append(color).append("\">");
			sb.append(mItem.getTitle());
			sb.append("</font>");
			
			textViewTitle.setText(Html.fromHtml(sb.toString()));
			textViewTitle.setSelected(true);
			
			TextView textViewDeletePHash = (TextView) mView.findViewById(R.id.textViewDeleteConfirm);
			textViewDeletePHash.setSelected(true);
			
			TextView textViewDeleteHint = (TextView) mView.findViewById(R.id.textViewDeleteHint);
			textViewDeleteHint.setText(R.string.manage_item_dialog_delete_hint);
			textViewDeleteHint.setSelected(true);
		}
		
		@Override
		public void onDestroyView() {
			super.onDestroyView();
			
			mTitle = null;
			mView = null;
			
			mViewSwitcher = null;
		}
		
		@Override
		public void onDestroy() {
			mItem = null;
			
			super.onDestroy();
		}
		
		@Override
		public void onDetach() {
			mItemListener = null;
			
			super.onDetach();
		}
		
		@Override
		public void onLockView(boolean isLock) {
			mIsLock = isLock;
			
			if (!this.isDestroy()) {
				mViewSwitcher.setDisplayedChild(isLock
						? VIEW_PROGRESS_POSITION
						: VIEW_FORM_POSITION
					);
			}
			
			this.setCancelable(!isLock);
		}
		
		@Override
		public boolean isLock() {
			return mIsLock;
		}
		
		private void deleteItem() {
			if (mItemListener != null) {
				mItemListener.onDeleteItem(mItem);
			}
		}
		
		@Override
		public void onPositiveClick(View view) {
			onLockView(true);
			
			DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
			
			dbAdapter.write();
			
			dbAdapter.deleteItem(mItem.getId());
			
			dbAdapter.close();
			
			deleteItem();
			
			super.onPositiveClick(view);
			
			onLockView(false);
		}
		
	}
	
	/**
	 * BaseDialogFragment ShowItemDialog Class.
	 * 
	 * @author strider
	 */
	public static class ShowItemDialog extends BaseDialogFragment {
		
		//private static final String LOG_TAG = ShowItemDialog.class.getSimpleName();
		
		public static final String KEY = ShowItemDialog.class.getSimpleName();
		
		private Item mItem = null;
		
		private View mTitle = null;
		private View mView = null;
		
		public static ShowItemDialog newInstance(Item item) {
			ShowItemDialog fragment = new ShowItemDialog();
			
			Bundle args = new Bundle();
			args.putParcelable(Item.KEY, item);
			
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public Item getItem() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.<Item> getParcelable(Item.KEY) : null);
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mItem = getItem();
			
			if (mItem == null) {
				throw (new IllegalArgumentException(
						"The Fragment requires valid Item as an argument."
					));
			}
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = LayoutInflater.from((Context) this.getSherlockActivity());
			
			mTitle = inflater.inflate(R.layout.alert_dialog_title, null);
			mView = inflater.inflate(R.layout.alert_dialog_show_item, null);
			
			this.registerNeutralButton(mView, R.id.buttonAlertDialogClose);
			
			return (new AlertDialog.Builder(inflater.getContext()))
					.setCustomTitle(mTitle)
					.setView(mView)
					.create();
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			TextView textViewTitle = (TextView) mTitle.findViewById(R.id.textViewAlertDialogTitle);
			textViewTitle.setText(mItem.getTitle());
			textViewTitle.setSelected(true);
			
			TextView textViewContent = (TextView) mView.findViewById(R.id.textViewItemContent);
			textViewContent.setText(Html.fromHtml((!TextUtils.isEmpty(mItem.getContent()))
					? mItem.getContent().replace(Text.LF, Text.BR)
					: Text.NOT_AVAILABLE
				));
			
			if (TextUtils.isEmpty(mItem.getContent())) {
				textViewContent.setGravity(Gravity.CENTER);
			}
			
			TextView textViewVideoUri = (TextView) mView.findViewById(R.id.textViewItemVideoUri);
			textViewVideoUri.setText(Html.fromHtml((!TextUtils.isEmpty(mItem.getVideoUri()))
					? mItem.getVideoUri()
					: Text.NOT_AVAILABLE
				));
			
			if (TextUtils.isEmpty(mItem.getVideoUri())) {
				textViewVideoUri.setGravity(Gravity.CENTER);
			}
		}
		
		@Override
		public void onDestroyView() {
			super.onDestroyView();
			
			mTitle = null;
			mView = null;
		}
		
		@Override
		public void onDestroy() {
			mItem = null;
			
			super.onDestroy();
		}
		
	}
	
	/**
	 * AsyncTask AsyncItemImport<String, Void, List<Item>> Class.
	 * 
	 * @author strider
	 */
	private class AsyncItemImport extends AsyncTask<String, Void, List<Item>> {
		
		//private static final String LOG_TAG = "AsyncItemImport";
		
		private Course mCourse = null;
		
		public AsyncItemImport(Course course) {
			mCourse = course;
		}
		
		@Override
		protected void onPreExecute() {
			ManageItem.this.onLockView(true);
		}
		
		@Override
		protected List<Item> doInBackground(String... params) {
			List<Item> listItem = new ArrayList<Item>();
			
			for (String path : params) {
				if (SimpleRecognizer.getMediaReceiver().isExternalStorageAvailable()) {
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
					listItem.add(new Item(null, content, null, mCourse.getId()));
				} else {
					SimpleRecognizer.makeToast(
							R.string.error_media_storage_not_available,
							Toast.LENGTH_LONG
						).show();
				}
			}
			
			return listItem;
		}
		
		@Override
		protected void onPostExecute(List<Item> result) {
			if (!ManageItem.this.isDestroy()) {
				for (Item item : result) {
					AddItemDialog.newInstance(mCourse, item)
							.show(ManageItem.this.getSupportFragmentManager(), AddItemDialog.KEY);
				}
			}
			
			ManageItem.this.onLockView(false);
		}
		
	}
	
}
