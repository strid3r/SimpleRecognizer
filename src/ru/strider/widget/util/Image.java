/*
 * Copyright (C) 2013 strider
 * 
 * Widget
 * Util Image Class
 * By © strider 2013.
 */

package ru.strider.widget.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Util Image Class.
 * 
 * @author strider
 */
public class Image {
	
	//private static final String LOG_TAG = Image.class.getSimpleName();
	
	//private static final String ASSETS_IMAGES = "Images" + File.separator;
	
	private static final float SATURATION_GRAYSCALE = 0.0f;
	
	private Image() {
		//
	}
	
	public static Bitmap getBitmap(ImageView imageView) {
		Bitmap image = null;
		
		Drawable drawable = imageView.getDrawable();
		
		if (drawable != null) {
			if (drawable instanceof BitmapDrawable) {
				image = ((BitmapDrawable) drawable).getBitmap();
			}
		}
		
		return image;
	}
	
	/**
	 * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object
	 * when decoding bitmaps using the decode* methods from {@link BitmapFactory}.
	 * This implementation calculates the closest inSampleSize that will result in
	 * the final decoded bitmap having a width and height equal to or larger than
	 * the requested width and height. This implementation does not ensure a power
	 * of 2 is returned for inSampleSize which can be faster when decoding but
	 * results in a larger bitmap which isn't as useful for caching purposes.
	 * 
	 * @param options : an options object with out* params already populated
	 *        (run through a decode* method with inJustDecodeBounds == true).
	 * @param reqWidth : the requested width of the resulting bitmap.
	 * @param reqHeight : the requested height of the resulting bitmap.
	 * 
	 * @return The value to be used for inSampleSize.
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		int inSampleSize = 1;
		
		final int width = options.outWidth;
		final int height = options.outHeight;
		
		if ((width > reqWidth) || (height > reqHeight)) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
			
			final int totalPixels = width * height;
			
			final int totalReqPixelsCap = reqWidth * reqHeight * 2;
			
			while (((float) totalPixels / (inSampleSize * inSampleSize)) > totalReqPixelsCap) {
				inSampleSize++;
			}
		}
		
		return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmap(String pathName, int reqWidth, int reqHeight) {
		if (pathName != null) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(pathName, options);
			
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options.inJustDecodeBounds = false;
			
			return BitmapFactory.decodeFile(pathName, options);
		}
		
		return null;
	}
	
	public static Bitmap decodeSampledBitmap(InputStream is, int reqWidth, int reqHeight) {
		if (is != null) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, options);
			
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options.inJustDecodeBounds = false;
			
			return BitmapFactory.decodeStream(is, null, options);
		}
		
		return null;
	}
	
	public static Bitmap decodeSampledBitmap(byte[] data, int reqWidth, int reqHeight) {
		if (data != null) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, options);
			
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options.inJustDecodeBounds = false;
			
			return BitmapFactory.decodeByteArray(data, 0, data.length, options);
		}
		
		return null;
	}
	
	public static Bitmap decodeSampledBitmap(Resources res, int resId, int reqWidth, int reqHeight) {
		if (resId != 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(res, resId, options);
			
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options.inJustDecodeBounds = false;
			
			return BitmapFactory.decodeResource(res, resId, options);
		}
		
		return null;
	}
	
	/**
	 * Converts bitmap to grayscale.
	 * 
	 * @return New grayscale Bitmap.
	 */
	public static Bitmap convertToGrayscale(Bitmap source) {
		Bitmap grayscale = null;
		
		if (source != null) {
			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(SATURATION_GRAYSCALE);
			
			ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(matrix);
			
			Paint paint = new Paint();
			paint.setColorFilter(colorFilter);
			
			grayscale = Bitmap.createBitmap(
					source.getWidth(),
					source.getHeight(),
					Bitmap.Config.RGB_565
				);
			
			Canvas canvas = new Canvas(grayscale);
			canvas.drawBitmap(source, 0.0f, 0.0f, paint);
		}
		
		return grayscale;
	}
	
}
