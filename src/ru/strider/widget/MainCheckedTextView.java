/*
 * Copyright (C) 2012-2013 strider
 * 
 * Widget
 * CheckedTextView MainCheckedTextView Class
 * By © strider 2012-2013.
 */

package ru.strider.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import ru.strider.widget.util.Font;

/**
 * CheckedTextView MainCheckedTextView Class.
 * 
 * @author strider
 */
public class MainCheckedTextView extends CheckedTextView {
	
	public MainCheckedTextView(Context context) {
		super(context);
	}
	
	public MainCheckedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Font.initTypeface(this, attrs);
	}
	
	public MainCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		Font.initTypeface(this, attrs);
	}
	
}
