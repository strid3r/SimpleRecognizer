/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * ViewGroup CameraPreview Class
 * By © strider 2012-2013.
 */

package ru.strider.widget;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

import ru.strider.simplerecognizer.SimpleRecognizer;
import ru.strider.widget.util.Font;

/**
 * ViewGroup CameraPreview Class.
 * 
 * A simple wrapper around a Camera and a SurfaceView that renders
 * a centered preview of the Camera to the surface. We need to center
 * the SurfaceView because not all devices have cameras that support
 * preview sizes at the same aspect ratio as the device's display.
 * 
 * @author strider
 */
public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
	
	private static final String LOG_TAG = CameraPreview.class.getSimpleName();
	
	private GridSurfaceView mSurfaceView = null;
	private SurfaceHolder mSurfaceHolder = null;
	
	private Camera mCamera = null;
	
	private Size mPreviewSize = null;
	private List<Size> mListPreviewSize = null;
	
	private boolean mIsSurfaceAlive = false;
	
	public CameraPreview(Context context) {
		super(context);
		
		doInit();
	}
	
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		doInit();
	}
	
	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		doInit();
	}
	
	private void doInit() {
		mSurfaceView = new GridSurfaceView(this.getContext());
		mSurfaceView.setTypeface(Font.getTypeface(this.getResources(), Font.AGENCY_FB_BOLD));
		
		this.addView(mSurfaceView);
		
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		
		// Deprecated setting, but required on Android versions prior to 3.0.
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void setCamera(Camera camera) {
		if (camera != null) {
			mCamera = camera;
			
			mListPreviewSize = camera.getParameters().getSupportedPreviewSizes();
			
			if (mIsSurfaceAlive) {
				setPreviewSize();
			}
		} else {
			releaseCamera();
		}
	}
	
	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			
			mCamera.release();
			
			mCamera = null;
			
			mPreviewSize = null;
			mListPreviewSize = null;
		}
	}
	
	public void switchCamera() {
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			
			mCamera.startPreview();
		} catch (IOException e) {
			Log.e(LOG_TAG, "setPreviewDisplay(...) >> " + e.toString());
		}
	}
	
	public void setPreviewSize() {
		this.requestLayout();
		
		if (mPreviewSize != null) {
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				//
			}
			
			Camera.Parameters parameters = mCamera.getParameters();
			
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			
			mCamera.setParameters(parameters);
		}
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "surfaceCreated(...) called");
		
		mIsSurfaceAlive = true;
		
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(holder);
				
				//mCamera.startPreview();
			} catch (IOException e) {
				Log.e(LOG_TAG, "setPreviewDisplay(...) >> " + e.toString());
			}
		}
		
		setPreviewSize();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "surfaceChanged(...) called");
		
		if (mSurfaceHolder.getSurface() == null) {
			return;
		}
		
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			//
		}
		
		setPreviewSize();
		
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			
			mCamera.startPreview();
		} catch (IOException e) {
			Log.e(LOG_TAG, "setPreviewDisplay(...)" + e.toString());
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "surfaceDestroyed(...) called");
		
		mIsSurfaceAlive = false;
		
		// Take care of releasing the Camera preview in activity.
		/*if (mCamera != null) {
			//mCamera.stopPreview();
		}*/
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if (mListPreviewSize != null) {
			mPreviewSize = getOptimalPreviewSize(
					mListPreviewSize,
					this.getMeasuredWidth(),
					this.getMeasuredHeight()
				);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (this.getChildCount() > 0) {
			final View child = this.getChildAt(0);
			
			final int width = r - l;
			final int height = b - t;
			
			int previewWidth = width;
			int previewHeight = height;
			
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}
			
			if ((width * previewHeight) > (height * previewWidth)) {
				final int scaledChildWidth = previewWidth * height / previewHeight;
				
				child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width / previewWidth;
				
				child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
			}
		}
	}
	
	public static Size getOptimalPreviewSize(List<Size> listSize, int width, int height) {
		if (listSize == null) {
			return null;
		}
		
		final double ASPECT_TOLERANCE = 0.1;
		
		double targetRatio = (double) width / (double) height;
		
		Size optimalSize = null;
		
		double minDiff = Double.MAX_VALUE;
		
		int targetHeight = height;
		
		for (Size size : listSize) {
			double ratio = (double) size.width / size.height;
			
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
				continue;
			}
			
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			
			for (Size size : listSize) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		
		return optimalSize;
	}
	
}
