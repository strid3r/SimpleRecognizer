/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * ViewGroup Camera Preview Class
 * By © strider 2012.
 */
package ru.strider.simplerecognizer;

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

import ru.strider.widget.GridSurfaceView;

/**
 * ViewGroup Camera Preview Class.
 * 
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 * 
 * @author strider
 */
public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
	
	private static final String LOG_TAG = "CameraPreview";
	
	private Camera mCamera = null;
	
	private GridSurfaceView mSurfaceView = null;
	private SurfaceHolder mHolder = null;
	
	private Size mPreviewSize = null;
	private List<Size> mSupportedPreviewSizes = null;
	
	private boolean mIsSurfaceCreated = false;
	
	public CameraPreview(Context context) {
		super(context);
		
		doInit(context);
	}
	
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		doInit(context);
	}
	
	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		doInit(context);
	}
	
	private void doInit(Context context) {
		mSurfaceView = new GridSurfaceView(context);
		
		this.addView(mSurfaceView);
		
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void setCamera(Camera camera) {
		mCamera = camera;
		
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			
			if (mIsSurfaceCreated) {
				this.requestLayout();
			}
		}
	}
	
	public void switchCamera() {
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(LOG_TAG, "IOException caused by setPreviewDisplay()" + e.getMessage());
		}
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(holder);
				//mCamera.startPreview();
			} catch (IOException e) {
				Log.d(LOG_TAG, "IOException caused by setPreviewDisplay()" + e.getMessage());
			}
		}
		
		if (mPreviewSize == null) {
			this.requestLayout();
		}
		
		mIsSurfaceCreated = true;
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "surfaceCreated(...) called");
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mHolder.getSurface() == null) {
			return;
		}
		
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			//
		}
		
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		
		this.requestLayout();
		
		mCamera.setParameters(parameters);
		
		try {
			mCamera.setPreviewDisplay(mHolder);//TODO: NEW
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(LOG_TAG, "IOException caused by setPreviewDisplay()" + e.getMessage());
		}
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "surfaceChanged(...) called");
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			//mCamera.stopPreview();
		}
		
		//
		
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "surfaceDestroyed(...) called");
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthSize = resolveSize(this.getSuggestedMinimumWidth(), widthMeasureSpec);
		final int heightSize = resolveSize(this.getSuggestedMinimumHeight(), heightMeasureSpec);
		
		this.setMeasuredDimension(widthSize, heightSize);
		
		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, widthSize, heightSize);
		}
		
		if (mCamera != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			
			try {
				mCamera.setParameters(parameters);
			} catch (Exception e) {
				//FIXME: Temp Handler For Strange Behavior
			}
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
	
	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		
		double targetRatio = (double) w / (double) h;
		
		if (sizes == null) {
			return null;
		}
		
		Size optimalSize = null;
		
		double minDiff = Double.MAX_VALUE;
		
		int targetHeight = h;
		
		for (Size size : sizes) {
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
			
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		
		return optimalSize;
	}
	
}
