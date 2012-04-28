/*
 * Copyright (C) 2012 strider
 * 
 * Widget
 * Button MainButton Class
 * By © strider 2012.
 */

package ru.strider.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import ru.strider.simplerecognizer.SimpleRecognizer;

/**
 * Widget Button MainButton Class.
 * 
 * @author strider
 */
public class MainButton extends Button {
	
	public MainButton(Context context) {
		super(context);
		
		doInit(context);
	}
	
	public MainButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		doInit(context);
	}
	
	public MainButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		doInit(context);
	}
	
	private void doInit(Context context) {
		if (!isInEditMode()) {
			this.setTypeface(SimpleRecognizer.getTypefaceMain(context));
		}
	}
	
}
