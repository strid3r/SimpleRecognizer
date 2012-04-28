/*
 * Copyright (C) 2012 strider
 * 
 * Widget
 * SurfaceView GridSurfaceView Class
 * By © strider 2012.
 */

package ru.strider.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

import ru.strider.simplerecognizer.R;

/**
 * Widget SurfaceView GridSurfaceView Class.
 * 
 * @author strider
 */
public class GridSurfaceView extends SurfaceView {
	
	//private static final String LOG_TAG = "GridSurfaceView";
	
	private static final int MAX_SIZE_STEPS = 5;
	
	private Context mContext = null;
	
	private Paint mPaint = null;
	
	public GridSurfaceView(Context context) {
		super(context);
		
		doInit(context);
	}
	
	public GridSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		doInit(context);
	}
	
	public GridSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		doInit(context);
	}
	
	private void doInit(Context context) {
		mContext = context;
		
		this.setWillNotDraw(false);
		
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		//mPaint.setStrokeWidth(2.0f);
		mPaint.setColor(mContext.getResources().getColor(R.color.black));
	}
	
	private boolean isEven(int number) {
		return (((number % 2) == 0) ? true : false);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		final int width = this.getWidth();
		final int height = this.getHeight();
		
		int maxSize = 0;
		int minSize = 0;
		
		boolean isTrueLandscape = (height < width);
		
		if (isTrueLandscape) {
			maxSize = width;
			minSize = height;
		} else {
			maxSize = height;
			minSize = width;
		}
		
		final float step = (float) maxSize / (float) MAX_SIZE_STEPS;
		
		final int countInt = minSize / (int) step;
		final float countFloat = (float) minSize / step;
		
		boolean isStepNotFits = (countInt < countFloat);
		
		float startWidth = step;
		float startHeight = step;
		
		if (!isStepNotFits && isEven(countInt) || (isStepNotFits && !isEven(countInt))) {
			if (isTrueLandscape) {
				startHeight = step / 2.0f;
			} else {
				startWidth = step / 2.0f;
			}
		}
		
		if (isStepNotFits) {
			final float offset = (1.0f - (countFloat - (float) countInt)) / 2.0f * step;
			
			if (isTrueLandscape) {
				startHeight -= offset;
			} else {
				startWidth -= offset;
			}
		}
		
		for (float i = startWidth; i < width; i += step) {
			canvas.drawLine(i, 0.0f, i, (float) height, mPaint);
		}
		
		for (float j = startHeight; j < height; j += step) {
			canvas.drawLine(0.0f, j, (float) width, j, mPaint);
		}
	}
	
}
