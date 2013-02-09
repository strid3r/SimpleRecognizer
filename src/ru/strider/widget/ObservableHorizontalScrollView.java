/*
 * Copyright (C) 2012-2013 strider
 * 
 * Widget
 * HorizontalScrollView ObservableHorizontalScrollView Class
 * By © strider 2012-2013.
 */

package ru.strider.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * HorizontalScrollView ObservableHorizontalScrollView Class.
 * 
 * @author strider
 */
public class ObservableHorizontalScrollView extends HorizontalScrollView {
	
	private OnScrollListener mOnScrollListener = null;
	
	public ObservableHorizontalScrollView(Context context) {
		super(context);
	}
	
	public ObservableHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ObservableHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setOnScrollistener(OnScrollListener onScrollListener) {
		mOnScrollListener = onScrollListener;
	}
	
	public boolean isScrollPossible() {
		return (computeHorizontalScrollRange() > getWidth());
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollChanged(this, l, t, oldl, oldt);
		}
	}
	
}
