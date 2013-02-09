/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * Model Item Class
 * By © strider 2012-2013. 
 */

package ru.strider.simplerecognizer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.List;

import ru.strider.util.Text;

/**
 * Model Item Class.
 * 
 * @author strider
 */
public class Item implements Parcelable {
	
	//private static final String LOG_TAG = Item.class.getSimpleName();
	
	public static final String KEY = Item.class.getSimpleName();
	
	public static final Creator<Item> CREATOR = new Creator<Item>() {
			
			@Override
			public Item createFromParcel(Parcel source) {
				return (new Item(source));
			}
			
			@Override
			public Item[] newArray(int size) {
				return (new Item[size]);
			}
			
		};
	
	private long mId = 0L;
	
	private String mTitle = null;
	private String mContent = null;
	private String mVideoUri = null;
	
	private long mCourseId = 0L;
	
	private List<PHash> mListPHash = null;
	
	public Item() {
		//
	}
	
	public Item(String title, String content, String videoUri, long courseId) {
		this(0L, title, content, videoUri, courseId, null);
	}
	
	public Item(String title, String content, String videoUri, long courseId,
			List<PHash> listPHash) {
		this(0L, title, content, videoUri, courseId, listPHash);
	}
	
	public Item(long id, String title, String content, String videoUri, long courseId) {
		this(id, title, content, videoUri, courseId, null);
	}
	
	public Item(long id, String title, String content, String videoUri, long courseId,
			List<PHash> listPHash) {
		mId = id;
		
		mTitle = title;
		mContent = content;
		mVideoUri = videoUri;
		
		mCourseId = courseId;
		
		mListPHash = listPHash;
	}
	
	public Item(Parcel in) {
		mId = in.readLong();
		
		mTitle = in.readString();
		mContent = in.readString();
		mVideoUri = in.readString();
		
		mCourseId = in.readLong();
		
		in.readList(mListPHash, PHash.class.getClassLoader());
	}
	
	public long getId() {
		return mId;
	}
	
	public void setId(long id) {
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
	
	public long getCourseId() {
		return mCourseId;
	}
	
	public void setCourseId(long courseId) {
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
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof Item)) {
			return false;
		}
		
		Item item = (Item) obj;
		
		return ((mId == item.mId) && (mCourseId == item.mCourseId));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		
		int result = 17;
		
		result = prime * result + Long.valueOf(mId).hashCode();
		
		result = prime * result + Long.valueOf(mCourseId).hashCode();
		
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (!TextUtils.isEmpty(mTitle)) {
			sb.append(mTitle);
		} else {
			sb.append(Text.NOT_AVAILABLE_EXTRA);
			
			sb.append(Text.SEPARATOR);
			
			sb.append(mCourseId);
		}
		
		return sb.toString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		
		dest.writeString(mTitle);
		dest.writeString(mContent);
		dest.writeString(mVideoUri);
		
		dest.writeLong(mCourseId);
		
		dest.writeList(mListPHash);
	}
	
	/**
	 * Finds PHash object with Min Hamming Distance Value
	 * in given List<PHash> listPHash.
	 * 
	 * @return PHash object with Min Hamming Distance in given listPHash,
	 *         or null if listPHash is either null or empty.
	 */
	public static PHash findPHashMin(List<PHash> listPHash) {
		PHash pHashMin = null;
		
		if ((listPHash != null) && (!listPHash.isEmpty())) {
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
