/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Model Item Class
 * By © strider 2012. 
 */

package ru.strider.simplerecognizer.model;

import java.util.List;

/**
 * Model Item Class.
 * 
 * @author strider
 */
public class Item {
	
	//private static final String LOG_TAG = "Item";
	
	private int mId = 0;
	
	private String mTitle = null;
	private String mContent = null;
	private String mVideoUri = null;
	
	private int mCourseId = 0;
	
	private List<PHash> mListPHash = null;
	
	public Item() {
		//
	}
	
	public Item(String title, String content, String videoUri, int courseId) {
		mTitle = title;
		mContent = content;
		mVideoUri = videoUri;
		
		mCourseId = courseId;
	}
	
	public Item(String title, String content, String videoUri, int courseId, List<PHash> listPHash) {
		mTitle = title;
		mContent = content;
		mVideoUri = videoUri;
		
		mCourseId = courseId;
		
		mListPHash = listPHash;
	}
	
	public Item(int id, String title, String content, String videoUri, int courseId) {
		mId = id;
		
		mTitle = title;
		mContent = content;
		mVideoUri = videoUri;
		
		mCourseId = courseId;
	}
	
	public Item(int id, String title, String content, String videoUri, int courseId, List<PHash> listPHash) {
		mId = id;
		
		mTitle = title;
		mContent = content;
		mVideoUri = videoUri;
		
		mCourseId = courseId;
		
		mListPHash = listPHash;
	}
	
	public int getId() {
		return mId;
	}
	
	public void setId(int id) {
		mId = id;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String getContent() {
		return mContent;
	}
	
	public void setContent(String content) {
		mContent = content;
	}
	
	public String getVideoUri() {
		return mVideoUri;
	}
	
	public void setVideoUri(String videoUri) {
		mVideoUri = videoUri;
	}
	
	public int getCourseId() {
		return mCourseId;
	}
	
	public void setCourseId(int courseId) {
		mCourseId = courseId;
	}
	
	public List<PHash> getListPHash() {
		return mListPHash;
	}
	
	public void setListPHash(List<PHash> listPHash) {
		mListPHash = listPHash;
	}
	
	/**
	 * Initializes Hamming Distance with given String pHashHex
	 * for All Item PHash objects in list for further use.
	 */
	public void initAllHammingDistance(String pHashHex) {
		if ((mListPHash != null) && !mListPHash.isEmpty()) {
			for (PHash pHash : mListPHash) {
				pHash.setHammingDistance(pHashHex);
			}
		}
	}
	
	/**
	 * Finds PHash object with Min Hamming Distance Value
	 * in given List<PHash> listPHash.
	 * 
	 * @return PHash object with Min Hamming Distance in given listPHash,
	 *         or null if listPHash is either null or empty
	 */
	public static PHash findPHashMin(List<PHash> listPHash) {
		PHash pHashMin = null;
		
		if ((listPHash != null) && !listPHash.isEmpty()) {
			pHashMin = listPHash.get(0);
			
			for (PHash pHash : listPHash) {
				if (pHash.getHammingDistance() < pHashMin.getHammingDistance()) {
					pHashMin = pHash;
				}
			}
		}
		
		return pHashMin;
	}
	
}
