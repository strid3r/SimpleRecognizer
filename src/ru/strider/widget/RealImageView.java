/*
 * Copyright (C) 2012 strider
 * 
 * Widget
 * ImageView RealImageView Class
 * By © strider 2012.
 */

package ru.strider.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Widget ImageView RealImageView Class.
 * 
 * @author strider
 */
public class RealImageView extends ImageView {
	
	private static final int LAYOUT_WIDTH_ID = 0;
	private static final int LAYOUT_HEIGHT_ID = 1;
	private static final int ADJUST_VIEW_BOUNDS_ID = 2;
	
	private static final int[] LAYOUT_ATTRS = new int[] {
		android.R.attr.layout_width,
		android.R.attr.layout_height,
		android.R.attr.adjustViewBounds
	};
	
	private boolean mIsAdjustViewBounds = false;
	
	private boolean mIsMatchWidth = false;
	private boolean mIsMatchHeight = false;
	
	private int mDrawableWidth = 0;
	private int mDrawableHeight = 0;
	
	public RealImageView(Context context) {
		super(context);
		
		doInit(context, null);
	}
	
	public RealImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		doInit(context, attrs);
	}
	
	public RealImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		doInit(context, attrs);
	}
	
	private void doInit(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(
					attrs,
					LAYOUT_ATTRS
				);
			
			int layoutWidth = array.getLayoutDimension(LAYOUT_WIDTH_ID, "layout_width");
			int layoutHeight = array.getLayoutDimension(LAYOUT_HEIGHT_ID, "layout_height");
			
			mIsAdjustViewBounds = array.getBoolean(ADJUST_VIEW_BOUNDS_ID, false);
			
			array.recycle();
			
			mIsMatchWidth = ((layoutWidth == LinearLayout.LayoutParams.MATCH_PARENT) && (layoutHeight == LinearLayout.LayoutParams.WRAP_CONTENT));
			mIsMatchHeight = ((layoutWidth == LinearLayout.LayoutParams.WRAP_CONTENT) && (layoutHeight == LinearLayout.LayoutParams.MATCH_PARENT));
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int desiredWidth = 0;
		int desiredHeight = 0;
		
		float desiredAspect = 0.0f;
		
		boolean isResizeWidth = false;
		boolean isResizeHeight = false;
		
		final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		
		if (this.getDrawable() == null) {
			mDrawableWidth = -1;
			mDrawableHeight = -1;
		} else {
			mDrawableWidth  = this.getDrawable().getIntrinsicWidth();
			mDrawableHeight = this.getDrawable().getIntrinsicHeight();
			
			desiredWidth = mDrawableWidth;
			desiredHeight = mDrawableHeight;
			
			if (desiredWidth < 1) {
				desiredWidth = 1;
			}
			if (desiredHeight < 1) {
				desiredHeight = 1;
			}
			
			if (mIsAdjustViewBounds) {
				isResizeWidth = (widthSpecMode != MeasureSpec.EXACTLY);
				isResizeHeight = (heightSpecMode != MeasureSpec.EXACTLY);
				
				desiredAspect = (float) desiredWidth  / (float) desiredHeight;
			}
		}
		
		int pLeft = this.getPaddingLeft();
		int pTop = this.getPaddingTop();
        int pRight = this.getPaddingRight();
		int pBottom = this.getPaddingBottom();
		
		int widthSize = 0;
		int heightSize = 0;
		
		if ((isResizeWidth || mIsMatchHeight) || (isResizeHeight || mIsMatchWidth)) {
			widthSize = resolveSize(desiredWidth + pLeft + pRight, widthMeasureSpec);
			heightSize = resolveSize(desiredHeight + pTop + pBottom, heightMeasureSpec);
			
			if (desiredAspect != 0.0f) {
				
				boolean isResized = false;
				
				float actualAspect = (float) (widthSize - pLeft - pRight) / (float) (heightSize - pTop - pBottom);
				
				if (isResizeWidth || mIsMatchHeight) {
					//heightSize = MeasureSpec.getSize(heightMeasureSpec);
					//heightSize = this.getMeasuredHeight();
					
					desiredWidth = (int) ((float) (heightSize - pTop - pBottom) * desiredAspect) + pLeft + pRight;
					
					if (mIsMatchHeight || ((Math.abs(actualAspect - desiredAspect) > 0.0000001) && (desiredWidth <= widthSize))) {
						widthSize = desiredWidth;
						
						isResized = true;
					}
				}
				
				if (!isResized && (isResizeHeight || mIsMatchWidth)) {
					//widthSize = MeasureSpec.getSize(widthMeasureSpec);
					//widthSize = this.getMeasuredWidth();
					
					desiredHeight = (int) ((float) (widthSize - pLeft - pRight) / desiredAspect) + pTop + pBottom;
					
					if (mIsMatchWidth || ((Math.abs(actualAspect - desiredAspect) > 0.0000001) && (desiredHeight <= heightSize))) {
						heightSize = desiredHeight;
					}
				}
			}
		} else {
			desiredWidth += pLeft + pRight;
			desiredHeight += pTop + pBottom;
			
			desiredWidth = Math.max(desiredWidth, this.getSuggestedMinimumWidth());
			desiredHeight = Math.max(desiredHeight, this.getSuggestedMinimumHeight());
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				widthSize = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
				heightSize = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);
			} else {
				widthSize = resolveSize(desiredWidth, widthMeasureSpec);
				heightSize = resolveSize(desiredHeight, heightMeasureSpec);
			}
		}
		
		this.setMeasuredDimension(widthSize, heightSize);
	}
	
}
