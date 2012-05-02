/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Model Course Class
 * By © strider 2012. 
 */

package ru.strider.simplerecognizer.model;

import java.util.List;

/**
 * Model Course Class.
 * 
 * @author strider
 */
public class Course {
	
	//private static final String LOG_TAG = "Course";
	
	public static final int INIT_VERSION = 1;
	
	private int mId = 0;
	
	private String mTitle = null;
	private String mCategory = null;
	
	private int mVersion = 0;
	
	private String mCreator = null;
	
	private List<Item> mListItem = null;
	
	public Course() {
		//
	}
	
	public Course(String title, String category, int version, String creator) {
		mTitle = title;
		mCategory = category;
		
		mVersion = version;
		
		mCreator = creator;
	}
	
	public Course(String title, String category, int version, String creator, List<Item> listItem) {
		mTitle = title;
		mCategory = category;
		
		mVersion = version;
		
		mCreator = creator;
		
		mListItem = listItem;
	}
	
	public Course(int id, String title, String category, int version, String creator) {
		mId = id;
		
		mTitle = title;
		mCategory = category;
		
		mVersion = version;
		
		mCreator = creator;
	}
	
	public Course(int id, String title, String category, int version, String creator, List<Item> listItem) {
		mId = id;
		
		mTitle = title;
		mCategory = category;
		
		mVersion = version;
		
		mCreator = creator;
		
		mListItem = listItem;
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
	
}
