/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Model PHash Class
 * By © strider 2012. 
 */

package ru.strider.simplerecognizer.model;

import ru.strider.simplerecognizer.util.ImagePHash;

/**
 * Model PHash Class.
 * 
 * @author strider
 */
public class PHash {
	
	//private static final String LOG_TAG = "PHash";
	
	private int mId = 0;
	
	private String mHexValue = null;
	private String mComment = null;
	
	private int mItemId = 0;
	
	private int mHammingDistance = ImagePHash.HAMMING_DISTANCE_THRESHOLD;
	
	public PHash() {
		//
	}
	
	public PHash(String hex_value, String comment, int itemId) {
		mHexValue = hex_value;
		mComment = comment;
		
		mItemId = itemId;
	}
	
	public PHash(int id, String hex_value, String comment, int itemId) {
		mId = id;
		
		mHexValue = hex_value;
		mComment = comment;
		
		mItemId = itemId;
	}
	
	public int getId() {
		return mId;
	}
	
	public void setId(int id) {
		mId = id;
	}
	
	public String getHexValue() {
		return mHexValue;
	}
	
	public void setHexValue(String hexValue) {
		mHexValue = hexValue;
	}
	
	public String getComment() {
		return mComment;
	}
	
	public void setComment(String comment) {
		mComment = comment;
	}
	
	public int getItemId() {
		return mItemId;
	}
	
	public void setItemId(int itemId) {
		mItemId = itemId;
	}
	
	public int getHammingDistance() {
		return mHammingDistance;
	}
	
	public void setHammingDistance(String pHashHex) {
		mHammingDistance = ImagePHash.getHammingDistance(mHexValue, pHashHex);
	}
	
}
