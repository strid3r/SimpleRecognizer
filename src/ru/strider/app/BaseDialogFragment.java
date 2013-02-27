/*
 * Copyright (C) 2013 strider
 * 
 * Application
 * DialogFragment BaseDialogFragment Class
 * By © strider 2013.
 */

package ru.strider.app;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockDialogFragment;

import ru.strider.simplerecognizer.SimpleRecognizer;

/**
 * DialogFragment BaseDialogFragment Class.
 * 
 * @author strider
 */
public class BaseDialogFragment extends SherlockDialogFragment implements View.OnClickListener {
	
	private static final String LOG_TAG = BaseDialogFragment.class.getSimpleName();
	
	private volatile boolean mIsDestroy = false;
	
	private OnCancelListener mOnCancelListener = null;
	private OnDismissListener mOnDismissListener = null;
	
	private Button mNegativeButton = null;
	private Button mNeutralButton = null;
	private Button mPositiveButton = null;
	
	private CharSequence mNegativeText = null;
	private CharSequence mNeutralText = null;
	private CharSequence mPositiveText = null;
	
	private int mNegativeTextId = View.NO_ID;
	private int mNeutralTextId = View.NO_ID;
	private int mPositiveTextId = View.NO_ID;
	
	private OnClickListener mOnNegativeListener = null;
	private OnClickListener mOnNeutralListener = null;
	private OnClickListener mOnPositiveListener = null;
	
	public static BaseDialogFragment newInstance() {
		BaseDialogFragment fragment = new BaseDialogFragment();
		
		Bundle args = new Bundle();
		
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is NULL");
		} else {
			SimpleRecognizer.logIfDebug(Log.WARN, LOG_TAG, "SIS is ~NULL");
		}
		
		super.onCreate(savedInstanceState);
		
		mIsDestroy = false;
	}
	
	@Override
	public void onResume() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onResume() called");
		
		super.onResume();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onSIS() called");
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onPause() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onPause() called");
		
		super.onPause();
	}
	
	@Override
	public void onDestroyView() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onDestroyView() called");
		
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onDestroy() called");
		
		mIsDestroy = true;
		
		super.onDestroy();
	}
	
	public boolean isDestroy() {
		return mIsDestroy;
	}
	
	private Button registerControlButton(Button button, int which) {
		if (button.getId() == View.NO_ID) {
			throw (new IllegalArgumentException("The Button must have a valid ID."));
		}
		
		switch (which) {
			case (DialogInterface.BUTTON_NEGATIVE): {
				mNegativeButton = button;
				
				if (mNegativeText != null) {
					mNegativeButton.setText(mNegativeText);
				} else if (mNegativeTextId != View.NO_ID) {
					mNegativeButton.setText(mNegativeTextId);
				}
				
				break;
			}
			case (DialogInterface.BUTTON_NEUTRAL): {
				mNeutralButton = button;
				
				if (mNeutralText != null) {
					mNeutralButton.setText(mNeutralText);
				} else if (mNeutralTextId != View.NO_ID) {
					mNeutralButton.setText(mNeutralTextId);
				}
				
				break;
			}
			case (DialogInterface.BUTTON_POSITIVE): {
				mPositiveButton = button;
				
				if (mPositiveText != null) {
					mPositiveButton.setText(mPositiveText);
				} else if (mPositiveTextId != View.NO_ID) {
					mPositiveButton.setText(mPositiveTextId);
				}
				
				break;
			}
			default: {
				throw (new IllegalArgumentException());
			}
		}
		
		button.setOnClickListener(this);
		
		return button;
	}
	
	private Button registerControlButton(View parent, int buttonId, int which) {
		return registerControlButton((Button) parent.findViewById(buttonId), which);
	}
	
	private int findWhichByView(View view) {
		int id = view.getId();
		
		if (id != View.NO_ID) {
			if ((mNegativeButton != null) && (id == mNegativeButton.getId())) {
				return DialogInterface.BUTTON_NEGATIVE;
			}
			
			if ((mNeutralButton != null) && (id == mNeutralButton.getId())) {
				return DialogInterface.BUTTON_NEUTRAL;
			}
			
			if ((mPositiveButton != null) && (id == mPositiveButton.getId())) {
				return DialogInterface.BUTTON_POSITIVE;
			}
		}
		
		return id;
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		if (mOnCancelListener != null) {
			mOnCancelListener.onCancel(this.getDialog());
		}
		
		super.onCancel(dialog);
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mOnDismissListener != null) {
			mOnDismissListener.onDismiss(this.getDialog());
		}
		
		super.onDismiss(dialog);
	}
	
	public BaseDialogFragment setOnCancelListener(OnCancelListener listener) {
		mOnCancelListener = listener;
		
		return this;
	}
	
	public BaseDialogFragment setOnDismissListener(OnDismissListener listener) {
		mOnDismissListener = listener;
		
		return this;
	}
	
	//--------------------------------------------------------------------
	// Negative Button
	//--------------------------------------------------------------------
	
	public Button getNegativeButton() {
		return mNegativeButton;
	}
	
	protected Button registerNegativeButton(Button button) {
		return registerControlButton(button, DialogInterface.BUTTON_NEGATIVE);
	}
	
	protected Button registerNegativeButton(View parent, int buttonId) {
		return registerControlButton(parent, buttonId, DialogInterface.BUTTON_NEGATIVE);
	}
	
	public void setNegativeButton(CharSequence text, OnClickListener listener) {
		if (mNegativeButton != null) {
			mNegativeButton.setText(text);
		} else {
			mNegativeText = text;
		}
		
		mOnNegativeListener = listener;
	}
	
	public void setNegativeButton(int resId, OnClickListener listener) {
		if (mNegativeButton != null) {
			mNegativeButton.setText(resId);
		} else {
			mNegativeTextId = resId;
		}
		
		mOnNegativeListener = listener;
	}
	
	//--------------------------------------------------------------------
	// Neutral Button
	//--------------------------------------------------------------------
	
	public Button getNeutralButton() {
		return mNeutralButton;
	}
	
	protected Button registerNeutralButton(Button button) {
		return registerControlButton(button, DialogInterface.BUTTON_NEUTRAL);
	}
	
	protected Button registerNeutralButton(View parent, int buttonId) {
		return registerControlButton(parent, buttonId, DialogInterface.BUTTON_NEUTRAL);
	}
	
	public void setNeutralButton(CharSequence text, OnClickListener listener) {
		if (mNeutralButton != null) {
			mNeutralButton.setText(text);
		} else {
			mNeutralText = text;
		}
		
		mOnNeutralListener = listener;
	}
	
	public void setNeutralButton(int resId, OnClickListener listener) {
		if (mNeutralButton != null) {
			mNeutralButton.setText(resId);
		} else {
			mNeutralTextId = resId;
		}
		
		mOnNeutralListener = listener;
	}
	
	//--------------------------------------------------------------------
	// Positive Button
	//--------------------------------------------------------------------
	
	public Button getPositiveButton() {
		return mPositiveButton;
	}
	
	protected Button registerPositiveButton(Button button) {
		return registerControlButton(button, DialogInterface.BUTTON_POSITIVE);
	}
	
	protected Button registerPositiveButton(View parent, int buttonId) {
		return registerControlButton(parent, buttonId, DialogInterface.BUTTON_POSITIVE);
	}
	
	public void setPositiveButton(CharSequence text, OnClickListener listener) {
		if (mPositiveButton != null) {
			mPositiveButton.setText(text);
		} else {
			mPositiveText = text;
		}
		
		mOnPositiveListener = listener;
	}
	
	public void setPositiveButton(int resId, OnClickListener listener) {
		if (mPositiveButton != null) {
			mPositiveButton.setText(resId);
		} else {
			mPositiveTextId = resId;
		}
		
		mOnPositiveListener = listener;
	}
	
	@Override
	public void onClick(View view) {
		switch (findWhichByView(view)) {
			case (DialogInterface.BUTTON_NEGATIVE): {
				onNegativeClick(view);
				
				break;
			}
			case (DialogInterface.BUTTON_NEUTRAL): {
				onNeutralClick(view);
				
				break;
			}
			case (DialogInterface.BUTTON_POSITIVE): {
				onPositiveClick(view);
				
				break;
			}
			default: {
				break;
			}
		}
	}
	
	public void onNegativeClick(View view) {
		if (mOnNegativeListener != null) {
			mOnNegativeListener.onClick(this.getDialog(), DialogInterface.BUTTON_NEGATIVE);
		}
		
		this.dismiss();
	}
	
	public void onNeutralClick(View view) {
		if (mOnNeutralListener != null) {
			mOnNeutralListener.onClick(this.getDialog(), DialogInterface.BUTTON_NEUTRAL);
		}
		
		this.dismiss();
	}
	
	public void onPositiveClick(View view) {
		if (mOnPositiveListener != null) {
			mOnPositiveListener.onClick(this.getDialog(), DialogInterface.BUTTON_POSITIVE);
		}
		
		this.dismiss();
	}
	
}
