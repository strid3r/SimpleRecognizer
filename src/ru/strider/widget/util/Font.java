/*
 * Copyright (C) 2012-2013 strider
 * 
 * Widget
 * Util Font Class
 * By © strider 2012-2013.
 */

package ru.strider.widget.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import ru.strider.simplerecognizer.R;

/**
 * Util Font Class.
 * 
 * Helper for acquiring font from assets. Lazy loads and keeps fonts cached.
 * 
 * @author strider
 */
public class Font {
	
	private static final String LOG_TAG = Font.class.getSimpleName();
	
	public static final String AGENCY_FB_BOLD = "AgencyFB_B.ttf";
	public static final String AGENCY_FB_REGULAR = "AgencyFB_R.ttf";
	public static final String AGENCY_FB_REGULAR_CYRILLIC = "AgencyFB_R_Cyr.ttf";
	
	private static final String ASSETS_FONTS = "Fonts" + File.separator;
	
	private static final Hashtable<String, SoftReference<Typeface>> sKeyCache
			= new Hashtable<String, SoftReference<Typeface>>();
	
	private Font() {
		//
	}
	
	/**
	 * Add the type-face to cache.
	 * 
	 * @param font : font of the type-face being added.
	 * @param typeface : type-face being added.
	 */
	public static void addToCache(String font, Typeface typeface) {
		synchronized (sKeyCache) {
			Log.v(LOG_TAG, "Adding font to cache :: " + font);
			
			sKeyCache.put(font, new SoftReference<Typeface>(typeface));
		}
	}
	
	/**
	 * Search for a type-face based on font in the cache.
	 * 
	 * @param font : font of the type-face that is searched for.
	 * 
	 * @return The value of typeface with the specified font or null if no typeface
	 *         for the specified font is found or typreface was cleared from cache.
	 */
	public static Typeface getFromCache(String font) {
		synchronized (sKeyCache) {
			if (sKeyCache.get(font) != null) {
				return sKeyCache.get(font).get();
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Retrieve a type-face. Does not load twice, uses lazy loading.
	 * 
	 * @return The typeface.
	 */
	public static Typeface getTypeface(Resources res, String font) {
		Typeface typeface = getFromCache(font);
		
		if (typeface == null) {
			// Ensure the global context is used. Just in case.
			//context = context.getApplicationContext();
			
			typeface = Typeface.createFromAsset(res.getAssets(), ASSETS_FONTS + font);
			
			if (typeface != null) {
				addToCache(font, typeface);
			}
		}
		
		return typeface;
	}
	
	public static Typeface getTypeface(Context context, AttributeSet attrs) {
		Typeface typeface = null;
		
		if (attrs != null) {
			TypedArray array = context
					.obtainStyledAttributes(attrs, R.styleable.MainView, R.attr.typeface, 0);
			
			String font = array.getString(R.styleable.MainView_typeface);
			
			array.recycle();
			
			if (!TextUtils.isEmpty(font)) {
				typeface = getTypeface(context.getResources(), font);
				
				if (typeface == null) {
					Log.w(LOG_TAG, "No typeface " + font
							+ " was found in :: assets" + File.separator + ASSETS_FONTS);
				}
			}
		}
		
		return typeface;
	}
	
	public static void initTypeface(TextView textView, AttributeSet attrs) {
		if (!textView.isInEditMode()) {
			Typeface typeface = getTypeface(textView.getContext(), attrs);
			
			if (typeface != null) {
				textView.setTypeface(typeface);
			}
		}
	}
	
}
