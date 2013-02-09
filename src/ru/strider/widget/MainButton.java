/*
 * Copyright (C) 2012-2013 strider
 * 
 * Widget
 * Button MainButton Class
 * By © strider 2012-2013.
 */

package ru.strider.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import ru.strider.widget.util.Font;

/**
 * Button MainButton Class.
 * 
 * @author strider
 */
public class MainButton extends Button {
	
	public MainButton(Context context) {
		super(context);
	}
	
	public MainButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Font.initTypeface(this, attrs);
	}
	
	public MainButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		Font.initTypeface(this, attrs);
	}
	
}
