/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Util ImagePHash Class
 * By © strider 2012.
 */

package ru.strider.simplerecognizer.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;

import java.io.InputStream;
import java.util.Locale;

import ru.strider.util.BuildConfig;
import ru.strider.widget.util.Image;

/**
 * Util ImagePHash Class.
 * 
 * @author strider
 */
public class ImagePHash {
	
	private static final String LOG_TAG = ImagePHash.class.getSimpleName();
	
	public static final int DCT_SIZE = 32;
	public static final int DCT_SIZE_FAST = 16;
	
	public static final int DCT_LOW_SIZE = 8;
	
	public static final int HAMMING_DISTANCE_THRESHOLD = 15;
	
	private static double[] sCoeff = null;
	
	private static int sSize = DCT_SIZE;
	private static int sLowSize = DCT_LOW_SIZE;
	
	static {
		initCoefficients();
	}
	
	public static synchronized void setDCTSize(int size, int lowSize) {
		sSize = size;
		sLowSize = lowSize;
		
		initCoefficients();
	}
	
	private static void initCoefficients() {
		sCoeff = new double[sSize];
		
		for (int i = 1; i < sSize; i++) {
			sCoeff[i] = 1.0;
		}
		
		sCoeff[0] = 1.0 / Math.sqrt(2.0);
	}
	
	private static double[][] applyDCT(double[][] in) {
		double[][] DCT = new double[sSize][sSize];
		
		for (int u = 0; u < sSize; u++) {
			for (int v = 0; v < sSize; v++) {
				double sum = 0.0;
				
				for (int i = 0; i < sSize; i++) {
					for (int j = 0; j < sSize; j++) {
						sum += in[i][j]
								* Math.cos((2 * i + 1) / (2.0 * sSize) * u * Math.PI)
								* Math.cos((2 * j + 1) / (2.0 * sSize) * v * Math.PI);
					}
				}
				
				sum *= sCoeff[u] * sCoeff[v] / 4.0;
				
				DCT[u][v] = sum;
			}
		}
		
		return DCT;
	}
	
	/**
	 * Returns Hex String of a "binary string" (like, 001010111011100010),
	 * which is easy to do a Hamming Distance on.
	 * 
	 * @return Hex String pHash of the image from File by pathName,
	 *         or null if the image data could not be decoded.
	 */
	public static String getPHash(String pathName) {
		return getPHash(Image.decodeSampledBitmap(pathName, sSize, sSize));
	}
	
	/**
	 * Returns Hex String of a "binary string" (like, 001010111011100010),
	 * which is easy to do a Hamming Distance on.
	 * 
	 * @return Hex String pHash of the image from InputStream,
	 *         or null if the image data could not be decoded.
	 */
	public static String getPHash(InputStream is) {
		return getPHash(Image.decodeSampledBitmap(is, sSize, sSize));
	}
	
	/**
	 * Returns Hex String of a "binary string" (like, 001010111011100010),
	 * which is easy to do a Hamming Distance on.
	 * 
	 * @return Hex String pHash of the image from byte[] array,
	 *         or null if the image data could not be decoded.
	 */
	public static String getPHash(byte[] data) {
		return getPHash(Image.decodeSampledBitmap(data, sSize, sSize));
	}
	
	/**
	 * Returns Hex String of a "binary string" (like, 001010111011100010),
	 * which is easy to do a Hamming Distance on.
	 * 
	 * @return Hex String pHash of the image from Bitmap,
	 *         or null if bitmap image is null.
	 */
	public static String getPHash(Bitmap image) {
		if (image == null) {
			return null;
		}
		
		/* 1. Reduce size. 
		 * 
		 * Like Average Hash, pHash starts with a small image.
		 * However, the image is larger than 8x8; 32x32 is a good size.
		 * This is really done to simplify the DCT computation and not
		 * because it is needed to reduce the high frequencies.
		 */
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, sSize, sSize, true);
		
		if (scaledBitmap != image) {
			image.recycle();
		}
		
		image = scaledBitmap;
		
		/* 2. Reduce color. 
		 * 
		 * The image is reduced to a grayscale just to further simplify
		 * the number of computations.
		 */
		Bitmap grayscale = Image.convertToGrayscale(image);
		
		image.recycle();
		
		image = grayscale;
		
		double[][] blue = new double[sSize][sSize];
		
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				blue[i][j] = Color.blue(image.getPixel(i, j));
			}
		}
		
		image.recycle();
		
		/* 3. Compute the DCT. 
		 * 
		 * The DCT separates the image into a collection of frequencies
		 * and scalars. While JPEG uses an 8x8 DCT, this algorithm uses
		 * a 32x32 DCT.
		 */
		long initTime = 0L;
		
		if(BuildConfig.DEBUG) {
			initTime = SystemClock.elapsedRealtime();
		}
		
		double[][] dctValue = applyDCT(blue);
		
		if(BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Time DCT: "
					+ Long.toString(SystemClock.elapsedRealtime() - initTime) + " ms.");
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
		
		for (int i = 0; i < sLowSize; i++) {
			for (int j = 0; j < sLowSize; j++) {
				dctTotal += dctValue[i][j];
			}
		}
		
		dctTotal -= dctValue[0][0];
		
		double dctAverage = dctTotal / (double) ((sLowSize * sLowSize) - 1);
		
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
		
		for (int i = 0; i < sLowSize; i++) {
			for (int j = 0; j < sLowSize; j++) {
				if ((i != 0) && (j != 0)) {
					pHash.append((dctValue[i][j] > dctAverage) ? 1 : 0);
				}
			}
		}
		
		long pHashValue = Long.parseLong(pHash.toString(), 2);
		
		return Long.toHexString(pHashValue).toUpperCase(Locale.ENGLISH);
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
