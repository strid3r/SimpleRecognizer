/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * BaseFragmentActivity ManagePHash Class
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

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
import ru.strider.simplerecognizer.model.PHash;
import ru.strider.simplerecognizer.util.ConfigAdapter;
import ru.strider.simplerecognizer.util.ImagePHash;
import ru.strider.simplerecognizer.view.OnItemListener;
import ru.strider.util.Text;
import ru.strider.view.OnLockViewListener;

/**
 * BaseFragmentActivity ManagePHash Class.
 * 
 * @author strider
 */
public class ManagePHash extends BaseFragmentActivity implements OnLockViewListener, OnItemListener,
		AdapterView.OnItemClickListener {
	
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
				mAsyncPHashImport = new AsyncPHashImport(mItem);
				
				FileManager.requestPickFile(this, FileManager.FILE_TYPE_IMAGE);
				
				return true;
			}
			case (R.id.managePHashMenuAddPHash): {
				AddPHashDialog.newInstance(mItem, null)
						.show(this.getSupportFragmentManager(), AddPHashDialog.KEY);
				
				return true;
			}
			case (R.id.managePHashMenuDeleteItem): {//TODO: Remove or dialog...
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
		
		PHash pHash = mAdapter.getItem(info.position);
		
		switch (item.getItemId()) {
			case (R.id.managePHashContextMenuShowComment): {
				ShowPHashDialog.newInstance(pHash)
						.show(this.getSupportFragmentManager(), ShowPHashDialog.KEY);
				
				return true;
			}
			case (R.id.managePHashContextMenuEdit): {
				AddPHashDialog.newInstance(mItem, pHash)
						.show(this.getSupportFragmentManager(), AddPHashDialog.KEY);
				
				return true;
			}
			case (R.id.managePHashContextMenuDelete): {
				DeletePHashDialog.newInstance(pHash)
						.show(this.getSupportFragmentManager(), DeletePHashDialog.KEY);
				
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
		mView = (ListView) this.findViewById(R.id.listViewPHash);
		//mView = this.getListView();
		
		mView.setEmptyView(this.findViewById(R.id.textViewEmpty));
		mView.setAdapter(mAdapter);
		//this.setListAdapter(mAdapter);
		this.registerForContextMenu(mView);
		
		mView.setTextFilterEnabled(true);
		//mView.setItemsCanFocus(false);
		//mView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mView.setOnItemClickListener(this);
		
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
			mAdapter.remove((PHash) item);
			
			mAdapter.notifyDataSetChanged();
		}
		
		onLockView(false);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//((ListView) parent).setItemChecked(position, true);
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
	
	/**
	 * BaseDialogFragment AddPHashDialog Class.
	 * 
	 * @author strider
	 */
	public static class AddPHashDialog extends BaseDialogFragment implements OnLockViewListener {
		
		//private static final String LOG_TAG = AddPHashDialog.class.getSimpleName();
		
		public static final String KEY = AddPHashDialog.class.getSimpleName();
		
		private static final String KEY_MODE = "Mode";
		
		private static final int VIEW_FORM_POSITION = 0;
		private static final int VIEW_PROGRESS_POSITION = 1;
		
		private OnItemListener mPHashListener = null;
		
		private Item mItem = null;
		private PHash mPHash = null;
		
		private View mTitle = null;
		private View mView = null;
		
		private ViewSwitcher mViewSwitcher = null;
		
		private BaseArrayAdapter<Item> mAdapter = null;
		private Spinner mSpinnerItem = null;
		private EditText mEditTextHexValue = null;
		private EditText mEditTextComment = null;
		
		private boolean mIsLock = false;
		
		private boolean mIsEditMode = false;
		
		public static AddPHashDialog newInstance(Item item, PHash pHash) {
			AddPHashDialog fragment = new AddPHashDialog();
			
			Bundle args = new Bundle();
			args.putParcelable(Item.KEY, item);
			args.putParcelable(PHash.KEY, pHash);
			
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public Item getItem() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.<Item> getParcelable(Item.KEY) : null);
		}
		
		public PHash getPHash() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.<PHash> getParcelable(PHash.KEY) : null);
		}
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
			try {
				mPHashListener = (OnItemListener) activity;
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
			
			if (savedInstanceState != null) {
				mPHash = savedInstanceState.getParcelable(PHash.KEY);
				
				mIsEditMode = savedInstanceState.getBoolean(KEY_MODE, false);
			} else {
				mPHash = getPHash();
				
				if (mPHash == null) {
					mPHash = new PHash();
					
					mPHash.setItemId(mItem.getId());
				} else {
					if ((mPHash.getId() != 0L) && (mPHash.getItemId() == mItem.getId())) {
						mIsEditMode = true;
					}
				}
			}
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = LayoutInflater.from((Context) this.getSherlockActivity());
			
			mTitle = inflater.inflate(R.layout.alert_dialog_title, null);
			mView = inflater.inflate(R.layout.alert_dialog_add_phash, null);
			
			mViewSwitcher = (ViewSwitcher) mView.findViewById(R.id.viewSwitcherAlertDialogAddPHash);
			
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
					? R.string.manage_phash_context_menu_edit
					: R.string.manage_phash_menu_add_phash
				);
			textViewTitle.setSelected(true);
			
			DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
			
			dbAdapter.open();
			
			List<Item> listItem = dbAdapter.getListItem(mItem.getCourseId());
			
			dbAdapter.close();
			
			mAdapter = new BaseArrayAdapter<Item>(
					(Context) this.getSherlockActivity(),
					R.layout.spinner_item_activated
				);
			mAdapter.setDropDownViewResource(R.layout.list_item_single_choice_activated);
			
			if (listItem != null) {
				mAdapter.addData(listItem);
			}
			
			mSpinnerItem = (Spinner) mView.findViewById(R.id.spinnerItem);
			mSpinnerItem.setAdapter(mAdapter);
			mSpinnerItem.setSelection(mAdapter.getPosition(mItem));
			
			mEditTextHexValue = (EditText) mView.findViewById(R.id.editTextPHashHexValue);
			mEditTextHexValue.setText(mPHash.getHexValue());
			
			mEditTextComment = (EditText) mView.findViewById(R.id.editTextPHashComment);
			mEditTextComment.setText(mPHash.getComment());
			
			TextView textViewAddPHash = (TextView) mView.findViewById(R.id.textViewAddPHashHint);
			textViewAddPHash.setSelected(true);
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			
			mPHash.setHexValue(mEditTextHexValue.getText().toString().trim());
			mPHash.setComment(mEditTextComment.getText().toString().trim());
			mPHash.setItemId(mAdapter.getItem(mSpinnerItem.getSelectedItemPosition()).getId());
			
			outState.putParcelable(PHash.KEY, mPHash);
			
			outState.putBoolean(KEY_MODE, mIsEditMode);
		}
		
		@Override
		public void onDestroyView() {
			super.onDestroyView();
			
			mTitle = null;
			mView = null;
			
			mViewSwitcher = null;
			
			mAdapter = null;
			mSpinnerItem = null;
			mEditTextHexValue = null;
			mEditTextComment = null;
		}
		
		@Override
		public void onDestroy() {
			mItem = null;
			mPHash = null;
			
			super.onDestroy();
		}
		
		@Override
		public void onDetach() {
			mPHashListener = null;
			
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
		
		private void pHashChanged() {
			if (mPHashListener != null) {
				mPHashListener.onItemChanged();
			}
		}
		
		@Override
		public void onPositiveClick(View view) {
			onLockView(true);
			
			if (mSpinnerItem.getSelectedItemPosition() != AdapterView.INVALID_POSITION) {
				mPHash.setHexValue(mEditTextHexValue.getText().toString().trim());
				mPHash.setComment(mEditTextComment.getText().toString().trim());
				mPHash.setItemId(mAdapter.getItem(mSpinnerItem.getSelectedItemPosition()).getId());
				
				DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
				
				dbAdapter.write();
				
				if (mIsEditMode) {
					dbAdapter.updatePHash(mPHash);
				} else {
					dbAdapter.addPHash(mPHash);
				}
				
				dbAdapter.close();
				
				pHashChanged();
				
				super.onPositiveClick(view);
			} else {
				SimpleRecognizer.makeToast(
						R.string.main_dialog_no_item_message,
						Toast.LENGTH_LONG
					).show();
			}
			
			onLockView(false);
		}
		
	}
	
	/**
	 * BaseDialogFragment DeletePHashDialog Class.
	 * 
	 * @author strider
	 */
	public static class DeletePHashDialog extends BaseDialogFragment implements OnLockViewListener {
		
		//private static final String LOG_TAG = DeletePHashDialog.class.getSimpleName();
		
		public static final String KEY = DeletePHashDialog.class.getSimpleName();
		
		private static final int VIEW_FORM_POSITION = 0;
		private static final int VIEW_PROGRESS_POSITION = 1;
		
		private OnItemListener mPHashListener = null;
		
		private PHash mPHash = null;
		
		private View mTitle = null;
		private View mView = null;
		
		private ViewSwitcher mViewSwitcher = null;
		
		private boolean mIsLock = false;
		
		public static DeletePHashDialog newInstance(PHash pHash) {
			DeletePHashDialog fragment = new DeletePHashDialog();
			
			Bundle args = new Bundle();
			args.putParcelable(PHash.KEY, pHash);
			
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public PHash getPHash() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.<PHash> getParcelable(PHash.KEY) : null);
		}
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
			try {
				mPHashListener = (OnItemListener) activity;
			} catch (ClassCastException e) {
				throw (new ClassCastException(
						activity.toString() + " must implement OnItemListener."
					));
			}
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mPHash = getPHash();
			
			if (mPHash == null) {
				throw (new IllegalArgumentException(
						"The Fragment requires valid PHash as an argument."
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
			sb.append(mPHash.getHexValue());
			sb.append("</font>");
			
			textViewTitle.setText(Html.fromHtml(sb.toString()));
			textViewTitle.setSelected(true);
			
			TextView textViewDeletePHash = (TextView) mView.findViewById(R.id.textViewDeleteConfirm);
			textViewDeletePHash.setSelected(true);
			
			TextView textViewDeleteHint = (TextView) mView.findViewById(R.id.textViewDeleteHint);
			textViewDeleteHint.setText(R.string.manage_phash_dialog_delete_hint);
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
			mPHash = null;
			
			super.onDestroy();
		}
		
		@Override
		public void onDetach() {
			mPHashListener = null;
			
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
		
		private void deletePHash() {
			if (mPHashListener != null) {
				mPHashListener.onDeleteItem(mPHash);
			}
		}
		
		@Override
		public void onPositiveClick(View view) {
			onLockView(true);
			
			DataBaseAdapter dbAdapter = DataBaseAdapter.getInstance();
			
			dbAdapter.write();
			
			dbAdapter.deletePHash(mPHash.getId());
			
			dbAdapter.close();
			
			deletePHash();
			
			super.onPositiveClick(view);
			
			onLockView(false);
		}
		
	}
	
	/**
	 * BaseDialogFragment ShowPHashDialog Class.
	 * 
	 * @author strider
	 */
	public static class ShowPHashDialog extends BaseDialogFragment {
		
		//private static final String LOG_TAG = ShowPHashDialog.class.getSimpleName();
		
		public static final String KEY = ShowPHashDialog.class.getSimpleName();
		
		private PHash mPHash = null;
		
		private View mTitle = null;
		private View mView = null;
		
		public static ShowPHashDialog newInstance(PHash pHash) {
			ShowPHashDialog fragment = new ShowPHashDialog();
			
			Bundle args = new Bundle();
			args.putParcelable(PHash.KEY, pHash);
			
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public PHash getPhash() {
			Bundle args = this.getArguments();
			
			return ((args != null) ? args.<PHash> getParcelable(PHash.KEY) : null);
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mPHash = getPhash();
			
			if (mPHash == null) {
				throw (new IllegalArgumentException(
						"The Fragment requires valid PHash as an argument."
					));
			}
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = LayoutInflater.from((Context) this.getSherlockActivity());
			
			mTitle = inflater.inflate(R.layout.alert_dialog_title, null);
			mView = inflater.inflate(R.layout.alert_dialog_show_phash, null);
			
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
			textViewTitle.setText(mPHash.getHexValue());
			textViewTitle.setSelected(true);
			
			TextView textViewComment = (TextView) mView.findViewById(R.id.textViewPHashComment);
			textViewComment.setText(Html.fromHtml((!TextUtils.isEmpty(mPHash.getComment()))
					? mPHash.getComment().replace(Text.LF, Text.BR)
					: Text.NOT_AVAILABLE
				));
			
			if (TextUtils.isEmpty(mPHash.getComment())) {
				textViewComment.setGravity(Gravity.CENTER);
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
			mPHash = null;
			
			super.onDestroy();
		}
		
	}
	
	/**
	 * AsyncTask AsyncPHashImport<String, Void, List<PHash>> Class.
	 * 
	 * @author strider
	 */
	private class AsyncPHashImport extends AsyncTask<String, Void, List<PHash>> {
		
		//private static final String LOG_TAG = "AsyncPHashImport";
		
		private Item mItem = null;
		
		public AsyncPHashImport(Item item) {
			mItem = item;
		}
		
		@Override
		protected void onPreExecute() {
			ManagePHash.this.onLockView(true);
		}
		
		@Override
		protected List<PHash> doInBackground(String... params) {
			List<PHash> listPHash = new ArrayList<PHash>();
			
			for (String path : params) {
				if (SimpleRecognizer.getMediaReceiver().isExternalStorageAvailable()) {
					String pHashHex = ImagePHash.getPHash(path);
					
					if (pHashHex != null) {
						listPHash.add(new PHash(pHashHex, null, mItem.getId()));
					}
				} else {
					SimpleRecognizer.makeToast(
							R.string.error_media_storage_not_available,
							Toast.LENGTH_LONG
						).show();
				}
			}
			
			return listPHash;
		}
		
		@Override
		protected void onPostExecute(List<PHash> result) {
			if (!ManagePHash.this.isDestroy()) {
				for (PHash pHash : result) {
					AddPHashDialog.newInstance(mItem, pHash)
							.show(ManagePHash.this.getSupportFragmentManager(), AddPHashDialog.KEY);
				}
			}
			
			ManagePHash.this.onLockView(false);
		}
		
	}
	
}
