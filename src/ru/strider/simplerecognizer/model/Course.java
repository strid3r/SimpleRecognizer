/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * Model Course Class
 * By © strider 2012. 
 */

package ru.strider.simplerecognizer.model;

/**
 * Model Course Class.
 * 
 * @author strider
 */
public class Course {
	
	//private static final String LOG_TAG = "Course";
	
	private int mId = 0;
	
	private String mTitle = null;
	private String mCategory = null;
	
	private String mCreator = null;
	
	public Course() {
		//
	}
	
	public Course(String title, String category, String creator) {
		mTitle = title;
		mCategory = category;
		
		mCreator = creator;
	}
	
	public Course(int id, String title, String category, String creator) {
		mId = id;
		
		mTitle = title;
		mCategory = category;
		
		mCreator = creator;
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
	
	public String getCreator() {
		return mCreator;
	}
	
	public void setCreator(String creator) {
		mCreator = creator;
	}
	
}
