/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * Model PHash Class
 * By © strider 2012-2013. 
 */

package ru.strider.simplerecognizer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import ru.strider.simplerecognizer.util.ImagePHash;
import ru.strider.util.Text;

/**
 * Model PHash Class.
 * 
 * @author strider
 */
public class PHash implements Comparable<PHash>, Parcelable {
	
	//private static final String LOG_TAG = PHash.class.getSimpleName();
	
	public static final String KEY = PHash.class.getSimpleName();
	
	public static final Creator<PHash> CREATOR = new Creator<PHash>() {
			
			@Override
			public PHash createFromParcel(Parcel source) {
				return (new PHash(source));
			}
			
			@Override
			public PHash[] newArray(int size) {
				return (new PHash[size]);
			}
			
		};
	
	private long mId = 0;
	
	private String mHexValue = null;
	private String mComment = null;
	
	private long mItemId = 0;
	
	private int mHammingDistance = ImagePHash.HAMMING_DISTANCE_THRESHOLD;
	
	public PHash() {
		//
	}
	
	public PHash(String hexValue, String comment, long itemId) {
		this(0L, hexValue, comment, itemId);
	}
	
	public PHash(long id, String hexValue, String comment, long itemId) {
		mId = id;
		
		mHexValue = hexValue;
		mComment = comment;
		
		mItemId = itemId;
	}
	
	public PHash(Parcel in) {
		mId = in.readLong();
		
		mHexValue = in.readString();
		mComment = in.readString();
		
		mItemId = in.readLong();
		
		mHammingDistance = in.readInt();
	}
	
	public long getId() {
		return mId;
	}
	
	public void setId(long id) {
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
	
	public long getItemId() {
		return mItemId;
	}
	
	public void setItemId(long itemId) {
		mItemId = itemId;
	}
	
	public int getHammingDistance() {
		return mHammingDistance;
	}
	
	public void setHammingDistance(String pHashHex) {
		mHammingDistance = ImagePHash.getHammingDistance(mHexValue, pHashHex);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof PHash)) {
			return false;
		}
		
		PHash pHash = (PHash) obj;
		
		return ((mId == pHash.mId) && (mItemId == pHash.mItemId));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		
		int result = 17;
		
		result = prime * result + Long.valueOf(mId).hashCode();
		
		result = prime * result + Long.valueOf(mItemId).hashCode();
		
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (!TextUtils.isEmpty(mHexValue)) {
			sb.append(mHexValue);
		} else {
			sb.append(Text.NOT_AVAILABLE_EXTRA);
			
			sb.append(Text.SEPARATOR);
			
			sb.append(mItemId);
		}
		
		return sb.toString();
	}
	
	@Override
	public int compareTo(PHash pHash) {
		// ItemId
		int cmpItemId = ((mItemId < pHash.mItemId) ? -1
				: ((mItemId == pHash.mItemId) ? 0 : 1));
		
		if (cmpItemId != 0) {
			return cmpItemId;
		} else {
			// HexValue
			return mHexValue.compareTo(pHash.mHexValue);
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		
		dest.writeString(mHexValue);
		dest.writeString(mComment);
		
		dest.writeLong(mItemId);
		
		dest.writeInt(mHammingDistance);
	}
	
}
