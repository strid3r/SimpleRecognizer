/*
 * Copyright (C) 2012-2013 strider
 * 
 * Widget
 * ScrollView OnScrollListener Interface
 * By © strider 2012-2013.
 */

package ru.strider.widget;

/**
 * ScrollView OnScrollListener Interface.
 * 
 * @author strider
 */
public interface OnScrollListener {
	
	void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
	
	void onScrollChanged(ObservableHorizontalScrollView horizontalScrollView, int x, int y, int oldx, int oldy);
	
}
