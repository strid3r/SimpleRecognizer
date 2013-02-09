/*
 * Copyright (C) 2012-2013 strider
 * 
 * Widget
 * TextView MainTextView Class
 * By © strider 2012-2013.
 */

package ru.strider.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import ru.strider.widget.util.Font;

/**
 * TextView MainTextView Class.
 * 
 * @author strider
 */
public class MainTextView extends TextView {
	
	public MainTextView(Context context) {
		super(context);
	}
	
	public MainTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Font.initTypeface(this, attrs);
	}
	
	public MainTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		Font.initTypeface(this, attrs);
	}
	
}
