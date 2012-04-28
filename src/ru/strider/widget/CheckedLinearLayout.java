/*
 * Copyright (C) 2012 strider
 * 
 * Widget
 * LinearLayout CheckedLinearLayout Class
 * By © strider 2012.
 */

package ru.strider.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * Widget LinearLayout CheckedLinearLayout Class.
 * 
 * @author strider
 */
public class CheckedLinearLayout extends LinearLayout implements Checkable {
	
	private MainCheckedTextView checkedTextView = null;
	
	public CheckedLinearLayout(Context context) {
		super(context);
	}
	
	public CheckedLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public CheckedLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		for (int i = 0; i < getChildCount(); i++) {
			View child = this.getChildAt(i);
			
			if (child instanceof MainCheckedTextView) {
				checkedTextView = (MainCheckedTextView) child;
			}
		}
	}
	
	@Override
	public void setChecked(boolean checked) {
		if (checkedTextView != null) {
			checkedTextView.setChecked(checked);
		}
	}
	
	@Override
	public boolean isChecked() {
		return (checkedTextView != null) ? checkedTextView.isChecked() : false;
	}
	
	@Override
	public void toggle() {
		if (checkedTextView != null) {
			checkedTextView.toggle();
		}
	}
	
}
