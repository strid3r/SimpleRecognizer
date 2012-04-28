/*
 * Copyright (C) 2012 strider
 * 
 * Widget
 * CheckedTextView MainCheckedTextView Class
 * By © strider 2012.
 */

package ru.strider.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import ru.strider.simplerecognizer.SimpleRecognizer;

/**
 * Widget CheckedTextView MainCheckedTextView Class.
 * 
 * @author strider
 */
public class MainCheckedTextView extends CheckedTextView {
	
	public MainCheckedTextView(Context context) {
		super(context);
		
		doInit(context);
	}
	
	public MainCheckedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		doInit(context);
	}
	
	public MainCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		doInit(context);
	}
	
	private void doInit(Context context) {
		if (!isInEditMode()) {
			this.setTypeface(SimpleRecognizer.getTypefaceMain(context));
		}
	}
	
}
