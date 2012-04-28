/*
 * Copyright (C) 2012 strider
 * 
 * Widget
 * ScrollView ObservableScrollView Class
 * By © strider 2012.
 */

package ru.strider.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Widget ScrollView ObservableScrollView Class.
 * 
 * @author strider
 */
public class ObservableScrollView extends ScrollView {
	
	private OnScrollListener mOnScrollListener = null;
	
	public ObservableScrollView(Context context) {
		super(context);
	}
	
	public ObservableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		mOnScrollListener = onScrollListener;
	}
	
	public boolean isScrollPossible() {
		return (computeVerticalScrollRange() > getHeight());
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollChanged(this, l, t, oldl, oldt);
		}
	}
	
}
