/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Util ImagePHash Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Util ImagePHash Class.
 * 
 * @author strider
 */
public class ImagePHash {
	
	private static final String LOG_TAG = "ImagePHash";
	
	public static final int DCT_SIZE_FAST = 16;
	public static final int DCT_LOW_SIZE = 8;
	
	public static final int HAMMING_DISTANCE_THRESHOLD = 15;
	
	private static final float SATURATION_GRAYSCALE = 0.0f;
	
	private static final float CANVAS_BITMAP_POS = 0.0f;
	
	private int mSize = 32;
	private int mLowSize = 8;
	
	private double[] mCoeff = null;
	
	private long mInitTime = 0L;
	private long mWorkTime = 0L;
	
	public ImagePHash() {
		initCoefficients();
	}
	
	public ImagePHash(int size, int smallSize) {
		mSize = size;
		mLowSize = smallSize;
		
		initCoefficients();
	}
	
	private void initCoefficients() {
		mCoeff = new double[mSize];
		
		for (int i = 1; i < mSize; i++) {
			mCoeff[i] = 1.0;
		}
		
		mCoeff[0] = 1.0 / Math.sqrt(2.0);
	}
	
	private double[][] applyDCT(double[][] in) {
		double[][] DCT = new double[mSize][mSize];
		
		for (int u = 0; u < mSize; u++) {
			for (int v = 0; v < mSize; v++) {
				double sum = 0.0;
				
				for (int i = 0; i < mSize; i++) {
					for (int j = 0; j < mSize; j++) {
						sum += in[i][j] * Math.cos((2 * i + 1) / (2.0 * mSize) * u * Math.PI) * Math.cos((2 * j + 1) / (2.0 * mSize) * v * Math.PI);
					}
				}
				
				sum *= mCoeff[u]* mCoeff[v] / 4.0;
				
				DCT[u][v] = sum;
			}
		}
		
		return DCT;
	}
	
	/**
	 * Returns Hex String of a "binary string" (like, 001010111011100010),
	 * which is easy to do a Hamming Distance on.
	 * 
	 * @return Hex String pHash of the image from InputStream,
	 *         or null if the image data could not be decoded.
	 */
	public String getPHash(InputStream is) {
		Bitmap bitmap = null;
		
		if (is != null) {
			bitmap = BitmapFactory.decodeStream(is);
			
			try {
				is.close();
				is = null;
			} catch (IOException e) {
				//
			}
		}
		
		return getPHash(bitmap);
	}
	
	/**
	 * Returns Hex String of a "binary string" (like, 001010111011100010),
	 * which is easy to do a Hamming Distance on.
	 * 
	 * @return Hex String pHash of the image from byte[] array,
	 *         or null if the image data could not be decoded.
	 */
	public String getPHash(byte[] data) {
		return getPHash((data != null) ? BitmapFactory.decodeByteArray(data, 0, data.length) : null);
	}
	
	private String getPHash(Bitmap bitmapImage) {
		if (bitmapImage == null) {
			return null;
		}
		
		/* 1. Reduce size. 
		 * 
		 * Like Average Hash, pHash starts with a small image. 
		 * However, the image is larger than 8x8; 32x32 is a good size. 
		 * This is really done to simplify the DCT computation and not 
		 * because it is needed to reduce the high frequencies.
		 */
		bitmapImage = Bitmap.createScaledBitmap(bitmapImage, mSize, mSize, true);
		
		/* 2. Reduce color. 
		 * 
		 * The image is reduced to a grayscale just to further simplify 
		 * the number of computations.
		 */
		bitmapImage = convertToGrayscale(bitmapImage);
		
		double[][] blue = new double[mSize][mSize];
		
		for (int i = 0; i < bitmapImage.getWidth(); i++) {
			for (int j = 0; j < bitmapImage.getHeight(); j++) {
				blue[i][j] = Color.blue(bitmapImage.getPixel(i, j));
			}
		}
		
		bitmapImage.recycle();
		bitmapImage = null;
		
		/* 3. Compute the DCT. 
		 * 
		 * The DCT separates the image into a collection of frequencies 
		 * and scalars. While JPEG uses an 8x8 DCT, this algorithm uses 
		 * a 32x32 DCT.
		 */
		if(BuildConfig.DEBUG) {
			mInitTime = SystemClock.elapsedRealtime();
		}
		
		double[][] dctValue = applyDCT(blue);
		
		if(BuildConfig.DEBUG) {
			mWorkTime = SystemClock.elapsedRealtime() - mInitTime;
			
			Log.d(LOG_TAG, "Time DCT: " + Long.toString(mWorkTime) + " ms.");
		}
		
		/* 4. Reduce the DCT. 
		 * 
		 * This is the magic step. While the DCT is 32x32, just keep the 
		 * top-left 8x8. Those represent the lowest frequencies in the 
		 * picture.
		 * 
		 * 5. Compute the average value. 
		 * 
		 * Like the Average Hash, compute the mean DCT value (using only 
		 * the 8x8 DCT low-frequency values and excluding the first term 
		 * since the DC coefficient can be significantly different from 
		 * the other values and will throw off the average).
		 */
		double dctTotal = 0;
		
		for (int i = 0; i < mLowSize; i++) {
			for (int j = 0; j < mLowSize; j++) {
				dctTotal += dctValue[i][j];
			}
		}
		
		dctTotal -= dctValue[0][0];
		
		double dctAverage = dctTotal / (double) ((mLowSize * mLowSize) - 1);
		
		/* 6. Further reduce the DCT. 
		 * 
		 * This is the magic step. Set the 64 hash bits to 0 or 1 
		 * depending on whether each of the 64 DCT values is above or 
		 * below the average value. The result doesn't tell us the 
		 * actual low frequencies; it just tells us the very-rough 
		 * relative scale of the frequencies to the mean. The result 
		 * will not vary as long as the overall structure of the image 
		 * remains the same; this can survive gamma and color histogram 
		 * adjustments without a problem.
		 */
		StringBuilder pHash = new StringBuilder();
		
		for (int i = 0; i < mLowSize; i++) {
			for (int j = 0; j < mLowSize; j++) {
				if ((i != 0) && (j != 0)) {
					pHash.append((dctValue[i][j] > dctAverage) ? 1 : 0);
				}
			}
		}
		
		long pHashValue = Long.parseLong(pHash.toString(), 2);
		
		return Long.toHexString(pHashValue).toUpperCase();
	}
	
	private Bitmap convertToGrayscale(Bitmap bitmap) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.setSaturation(SATURATION_GRAYSCALE);
		
		ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(matrix);
		
		Paint paint = new Paint();
		paint.setColorFilter(colorFilter);
		
		Bitmap bitmapGrayscale = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
		
		Canvas canvas = new Canvas(bitmapGrayscale);
		canvas.drawBitmap(bitmap, CANVAS_BITMAP_POS, CANVAS_BITMAP_POS, paint);
		
		return bitmapGrayscale;
	}
	
	/**
	 * Returns a Hamming Distance between two pHash Hex Strings.
	 * 
	 * @return int Hamming Distance,
	 *         or -1 if any pHash data could not be decoded.
	 */
	public static int getHammingDistance(String pHashHexSource, String pHashHexObject) {
		if ((pHashHexSource == null) || (pHashHexObject == null)) {
			return -1;
		} else {
			Long pHashSourceValue = Long.parseLong(pHashHexSource, 16);
			Long pHashObjectValue = Long.parseLong(pHashHexObject, 16);
			
			String pHashSource = Long.toBinaryString(pHashSourceValue);
			String pHashObject = Long.toBinaryString(pHashObjectValue);
			
			int lengthDelta = pHashSource.length() - pHashObject.length();
			
			for (int i = 0; i < Math.abs(lengthDelta); i++) {
				if (lengthDelta < 0) {
					pHashSource = "0" + pHashSource;
				} else if (lengthDelta > 0) {
					pHashObject = "0" + pHashObject;
				}
			}
			
			int hammingDistance = 0;
			
			for (int i = 0; i < pHashSource.length() ; i++) {
				if (pHashSource.charAt(i) != pHashObject.charAt(i)) {
					hammingDistance++;
				}
			}
			
			return hammingDistance;
		}
	}
	
}
