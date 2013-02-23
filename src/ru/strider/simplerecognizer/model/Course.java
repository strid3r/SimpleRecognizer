/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * Model Course Class
 * By © strider 2012-2013. 
 */

package ru.strider.simplerecognizer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.List;

import ru.strider.util.Text;

/**
 * Model Course Class.
 * 
 * @author strider
 */
public class Course implements Comparable<Course>, Parcelable {
	
	//private static final String LOG_TAG = Course.class.getSimpleName();
	
	public static final String KEY = Course.class.getSimpleName();
	
	public static final int INIT_VERSION = 1;
	
	public static final Creator<Course> CREATOR = new Creator<Course>() {
			
			@Override
			public Course createFromParcel(Parcel source) {
				return (new Course(source));
			}
			
			@Override
			public Course[] newArray(int size) {
				return (new Course[size]);
			}
			
		};
	
	private long mId = 0L;
	
	private String mTitle = null;
	private String mCategory = null;
	
	private int mVersion = 0;
	
	private String mCreator = null;
	
	private List<Item> mListItem = null;
	
	public Course() {
		//
	}
	
	public Course(String title, String category, int version, String creator) {
		this(0L, title, category, version, creator, null);
	}
	
	public Course(String title, String category, int version, String creator,
			List<Item> listItem) {
		this(0L, title, category, version, creator, listItem);
	}
	
	public Course(long id, String title, String category, int version, String creator) {
		this(id, title, category, version, creator, null);
	}
	
	public Course(long id, String title, String category, int version, String creator,
			List<Item> listItem) {
		mId = id;
		
		mTitle = title;
		mCategory = category;
		
		mVersion = version;
		
		mCreator = creator;
		
		mListItem = listItem;
	}
	
	public Course(Parcel in) {
		mId = in.readLong();
		
		mTitle = in.readString();
		mCategory = in.readString();
		
		mVersion = in.readInt();
		
		mCreator = in.readString();
		
		in.readList(mListItem, Item.class.getClassLoader());
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
	
	public String getCategory() {
		return mCategory;
	}
	
	public void setCategory(String category) {
		mCategory = category;
	}
	
	public int getVersion() {
		return mVersion;
	}
	
	public void setVersion(int version) {
		mVersion = version;
	}
	
	public String getCreator() {
		return mCreator;
	}
	
	public void setCreator(String creator) {
		mCreator = creator;
	}
	
	public List<Item> getListItem() {
		return mListItem;
	}
	
	public void setListItem(List<Item> listItem) {
		mListItem = listItem;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof Course)) {
			return false;
		}
		
		Course course = (Course) obj;
		
		return ((mId == course.mId) && (mVersion == course.mVersion));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		
		int result = 17;
		
		result = prime * result + Long.valueOf(mId).hashCode();
		
		result = prime * result + Integer.valueOf(mVersion).hashCode();
		
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append((!TextUtils.isEmpty(mCategory)) ? mCategory : Text.NOT_AVAILABLE_EXTRA);
		
		sb.append(Text.SEPARATOR);
		
		sb.append((!TextUtils.isEmpty(mTitle)) ? mTitle : Text.NOT_AVAILABLE_EXTRA);
		
		sb.append(Text.SEPARATOR);
		
		sb.append(mVersion);
		
		if (!TextUtils.isEmpty(mCreator)) {
			sb.append(Text.SEPARATOR_BY).append(mCreator);
		}
		
		return sb.toString();
	}
	
	@Override
	public int compareTo(Course course) {
		// Category
		int cmpCategory = mCategory.compareTo(course.mCategory);
		
		if (cmpCategory != 0) {
			return cmpCategory;
		} else {
			// Title
			int cmpTitle = mTitle.compareTo(course.mTitle);
			
			if (cmpTitle != 0) {
				return cmpTitle;
			} else {
				// Creator
				int cmpCreator = ((mCreator == null) ? ((course.mCreator == null) ? 0 : 1)
						: ((course.mCreator == null) ? -1 : mCreator.compareTo(course.mCreator)));
				
				if (cmpCreator != 0) {
					return cmpCreator;
				} else {
					// Version
					return ((mVersion < course.mVersion) ? -1
							: ((mVersion == course.mVersion) ? 0 : 1));
				}
			}
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		
		dest.writeString(mTitle);
		dest.writeString(mCategory);
		
		dest.writeInt(mVersion);
		
		dest.writeString(mCreator);
		
		dest.writeList(mListItem);
	}
	
}
