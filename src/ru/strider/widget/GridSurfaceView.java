/*
 * Copyright (C) 2012-2013 strider
 * 
 * Widget
 * SurfaceView GridSurfaceView Class
 * By © strider 2012-2013.
 */

package ru.strider.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceView;

import ru.strider.simplerecognizer.R;
import ru.strider.widget.util.Font;

/**
 * SurfaceView GridSurfaceView Class.
 * 
 * @author strider
 */
public class GridSurfaceView extends SurfaceView {
	
	//private static final String LOG_TAG = GridSurfaceView.class.getSimpleName();
	
	private static final int MAX_SIZE_STEPS = 5;
	
	private static final long FPS_INTERVAL = 1000L;
	private static final float FPS_OFFSET_DP = 10.0f;
	
	private Paint mPaintGrid = null;
	
	private Paint mPaintFps = null;
	
	private float mOffsetFps = 0.0f; 
	
	private long mLastTime = 0L;
	private int mFrameCount = 0;
	private int mFps = 0;
	
	public GridSurfaceView(Context context) {
		super(context);
		
		doInit(null);
	}
	
	public GridSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		doInit(attrs);
	}
	
	public GridSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		doInit(attrs);
	}
	
	private void doInit(AttributeSet attrs) {
		this.setWillNotDraw(false);
		
		mPaintGrid = new Paint();
		mPaintGrid.setStyle(Paint.Style.STROKE);
		//mPaintGrid.setStrokeWidth(2.0f);
		mPaintGrid.setColor(this.getResources().getColor(R.color.black));
		
		mPaintFps = new Paint();
		mPaintFps.setTextSize(32.0f);
		//mPaintFps.setStyle(Paint.Style.STROKE); // FIXME: TEST TEXT STYLE
		//mPaintFps.setStrokeWidth(2.0f);
		mPaintFps.setColor(this.getResources().getColor(R.color.blue_dodger));
		
		if (attrs != null) {
			Typeface typeface = Font.getTypeface(this.getContext(), attrs);
			
			if (typeface != null) {
				mPaintFps.setTypeface(typeface);
			}
		}
		
		mOffsetFps = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				FPS_OFFSET_DP,
				this.getResources().getDisplayMetrics()
			);
	}
	
	public int getFps() {
		return mFps;
	}
	
	public void setTypeface(Typeface typeface) {
		mPaintFps.setTypeface(typeface);
	}
	
	private void updateFps() {
		final long currentTime = SystemClock.elapsedRealtime();
		
		mFrameCount++;
		
		if ((currentTime - mLastTime) > FPS_INTERVAL) {
			mFps = mFrameCount;
			
			mLastTime = currentTime;
			
			mFrameCount = 0;
		}
	}
	
	private void drawFps(Canvas canvas) {
		canvas.drawText(
				Integer.toString(mFps),
				mOffsetFps,
				(this.getHeight() - mOffsetFps),
				mPaintFps
			);
	}
	
	private boolean isEven(int number) {
		return ((number % 2) == 0);
	}
	
	private void drawGrid(Canvas canvas) {
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
			canvas.drawLine(i, 0.0f, i, (float) height, mPaintGrid);
		}
		
		for (float j = startHeight; j < height; j += step) {
			canvas.drawLine(0.0f, j, (float) width, j, mPaintGrid);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		updateFps();
		
		super.onDraw(canvas);
		
		// Grid
		drawGrid(canvas);
		
		// FPS
		drawFps(canvas);
	}
	
}
