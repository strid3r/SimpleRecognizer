/*
 * Copyright (C) 2012 strider
 * 
 * Widget
 * TextView MainTextView Class
 * By © strider 2012.
 */

package ru.strider.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import ru.strider.simplerecognizer.SimpleRecognizer;

/**
 * Widget TextView MainTextView Class.
 * 
 * @author strider
 */
public class MainTextView extends TextView {
	
	public MainTextView(Context context) {
		super(context);
		
		doInit(context);
	}
	
	public MainTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		doInit(context);
	}
	
	public MainTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		doInit(context);
	}
	
	private void doInit(Context context) {
		if (!isInEditMode()) {
			this.setTypeface(SimpleRecognizer.getTypefaceMain(context));
		}
	}
	
}
