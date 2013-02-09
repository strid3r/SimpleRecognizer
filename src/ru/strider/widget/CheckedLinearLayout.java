/*
 * Copyright (C) 2012-2013 strider
 * 
 * Widget
 * LinearLayout CheckedLinearLayout Class
 * By © strider 2012-2013.
 */

package ru.strider.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

/**
 * LinearLayout CheckedLinearLayout Class.
 * 
 * @author strider
 */
public class CheckedLinearLayout extends LinearLayout implements Checkable {
	
	private CheckedTextView mCheckedTextView = null;
	
	public CheckedLinearLayout(Context context) {
		super(context);
	}
	
	public CheckedLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public CheckedLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		for (int i = 0; i < this.getChildCount(); i++) {
			View child = this.getChildAt(i);
			
			if (child instanceof CheckedTextView) {
				mCheckedTextView = (CheckedTextView) child;
			}
		}
	}
	
	@Override
	public void setChecked(boolean isChecked) {
		if (mCheckedTextView != null) {
			mCheckedTextView.setChecked(isChecked);
		}
	}
	
	@Override
	public boolean isChecked() {
		return ((mCheckedTextView != null) ? mCheckedTextView.isChecked() : false);
	}
	
	@Override
	public void toggle() {
		if (mCheckedTextView != null) {
			mCheckedTextView.toggle();
		}
	}
	
}
