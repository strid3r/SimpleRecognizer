/*
 * Copyright (C) 2013 strider
 * 
 * Application
 * DialogFragment BaseDialogFragment Class
 * By © strider 2013.
 */

package ru.strider.app;

import android.content.DialogInterface;
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
	
	private int mNegativeButtonId = View.NO_ID;
	private int mNeutralButtonId = View.NO_ID;
	private int mPositiveButtonId = View.NO_ID;
	
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
	
	private Button initControlButton(Button button, int which) {
		if (button.getId() == View.NO_ID) {
			throw (new IllegalArgumentException("The Button must have a valid ID."));
		}
		
		switch (which) {
			case (DialogInterface.BUTTON_NEGATIVE): {
				mNegativeButtonId = button.getId();
				
				break;
			}
			case (DialogInterface.BUTTON_NEUTRAL): {
				mNeutralButtonId = button.getId();
				
				break;
			}
			case (DialogInterface.BUTTON_POSITIVE): {
				mPositiveButtonId = button.getId();
				
				break;
			}
			default: {
				throw (new IllegalArgumentException());
			}
		}
		
		button.setOnClickListener(this);
		
		return button;
	}
	
	private int findButtonIdByView(View view) {
		if (view.getId() != View.NO_ID) {
			if (view.getId() == mNegativeButtonId) {
				return DialogInterface.BUTTON_NEGATIVE;
			}
			
			if (view.getId() == mNeutralButtonId) {
				return DialogInterface.BUTTON_NEUTRAL;
			}
			
			if (view.getId() == mPositiveButtonId) {
				return DialogInterface.BUTTON_POSITIVE;
			}
		}
		
		return view.getId();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		//
		
		super.onCancel(dialog);
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		//
		
		super.onDismiss(dialog);
	}
	
	public Button setNegativeButton(Button button) {
		return initControlButton(button, DialogInterface.BUTTON_NEGATIVE);
	}
	
	public Button setNeutralButton(Button button) {
		return initControlButton(button, DialogInterface.BUTTON_NEUTRAL);
	}
	
	public Button setPositiveButton(Button button) {
		return initControlButton(button, DialogInterface.BUTTON_POSITIVE);
	}
	
	@Override
	public void onClick(View view) {
		switch (findButtonIdByView(view)) {
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
		this.dismiss();
	}
	
	public void onNeutralClick(View view) {
		this.dismiss();
	}
	
	public void onPositiveClick(View view) {
		this.dismiss();
	}
	
}
