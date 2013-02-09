/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * Database Adapter Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.model.Item;
import ru.strider.simplerecognizer.model.PHash;

/**
 * Database Adapter Class.
 * 
 * @author strider
 */
public class DataBaseAdapter {
	
	private static final String LOG_TAG = DataBaseAdapter.class.getSimpleName();
	
	public static final String TABLE_COURSE = "Course";
	public static final String KEY_COURSE_ID = "_id";
	public static final String KEY_COURSE_TITLE = "title";
	public static final String KEY_COURSE_CATEGORY = "category";
	public static final String KEY_COURSE_VERSION = "version";
	public static final String KEY_COURSE_CREATOR = "creator";
	
	public static final String TABLE_ITEM = "Item";
	public static final String KEY_ITEM_ID = "_id";
	public static final String KEY_ITEM_TITLE = "title";
	public static final String KEY_ITEM_CONTENT = "content";
	public static final String KEY_ITEM_VIDEO_URI = "video_uri";
	public static final String KEY_ITEM_COURSE_ID = "course_id";
	
	public static final String TABLE_PHASH = "PHash";
	public static final String KEY_PHASH_ID = "_id";
	public static final String KEY_PHASH_HEX_VALUE = "hex_value";
	public static final String KEY_PHASH_COMMENT = "comment";
	public static final String KEY_PHASH_ITEM_ID = "item_id";
	
	private static final int DB_VERSION = 1;
	private static final int DB_VERSION_EMPTY = 1;
	
	private static final String DB_NAME = "SimpleRecognizer.db";
	private static final String DB_NAME_EMPTY = "SimpleRecognizer_Empty.db";
	
	private static final String SQL_QUERY_PRAGMA_FK_ON = "PRAGMA foreign_keys=ON;";
	
	private static volatile DataBaseAdapter sInstance = null;
	
	private DataBaseHelper mDataBaseHelper = null;
	
	private SQLiteDatabase mDataBase = null;
	
	private DataBaseAdapter(Activity activity) {
		mDataBaseHelper = new DataBaseHelper(
				((Context) activity).getApplicationContext(),
				DB_NAME,
				DB_NAME,
				DB_VERSION
			);
		
		createDataBase(activity);
	}
	
	public DataBaseAdapter(Activity activity, String dbName) {
		mDataBaseHelper = new DataBaseHelper(
				((Context) activity).getApplicationContext(),
				DB_NAME_EMPTY,
				dbName,
				DB_VERSION_EMPTY
			);
		
		createDataBase(activity);
	}
	
	public static DataBaseAdapter getInstance(Activity activity) {
		DataBaseAdapter localInstance = sInstance;
		
		if (localInstance == null) {
			synchronized (DataBaseAdapter.class) {
				localInstance = sInstance;
				
				if (localInstance == null) {
					sInstance = localInstance = new DataBaseAdapter(activity);
				}
			}
		}
		
		return localInstance;
	}
	
	public static synchronized void release() {
		if (sInstance != null) {
			sInstance.close();
			
			sInstance = null;
		}
	}
	
	public DataBaseAdapter createDataBase(Activity activity) {
		try {
			mDataBaseHelper.createDataBase(activity);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Unable To Create Database >> " + e.toString());
			throw new Error("UnableToCreateDatabase");
		}
		
		return this;
	}
	
	public DataBaseAdapter open() throws SQLException {
		try {
			mDataBaseHelper.openDataBase();
			mDataBaseHelper.close();
			
			mDataBase = mDataBaseHelper.getReadableDatabase();
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "Open Database >> " + sqle.toString());
			throw sqle;
		}
		
		return this;
	}
	
	public DataBaseAdapter write() throws SQLException {
		try {
			mDataBaseHelper.openDataBase();
			mDataBaseHelper.close();
			
			mDataBase = mDataBaseHelper.getWritableDatabase();
			mDataBase.execSQL(SQL_QUERY_PRAGMA_FK_ON);
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "Write Database >> " + sqle.toString());
			throw sqle;
		}
		
		return this;
	}
	
	public DataBaseAdapter delete() throws SQLException {
		try {
			mDataBaseHelper.openDataBase();
			mDataBaseHelper.close();
			
			if (mDataBaseHelper.deleteDataBase()) {
				Log.w(LOG_TAG, "Database deleted");
			} else {
				Log.e(LOG_TAG, "Database not deleted");
			}
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "Delete Database >> " + sqle.toString());
			throw sqle;
		}
		
		return this;
	}
	
	public synchronized void close() {
		if (mDataBaseHelper != null) {
			mDataBaseHelper.close();
			
			mDataBase = null;
		}
	}
	
	public void copyFrom(String srcPath) throws IOException {
		FileInputStream srcStream = null;
		FileInputStream dstStream = null;
		
		FileChannel srcChannel = null;
		FileChannel dstChannel = null;
		
		try {
			srcStream = new FileInputStream(srcPath);
			dstStream = new FileInputStream(mDataBase.getPath());
			
			srcChannel = srcStream.getChannel();
			dstChannel = dstStream.getChannel();
			
			dstChannel.transferFrom(srcChannel, 0L, srcChannel.size());
		} finally {
			if (srcStream != null) {
				try {
					srcStream.close();
				} catch (IOException e) {
					//
				} finally {
					srcStream = null;
				}
			}
			
			if (dstStream != null) {
				try {
					dstStream.close();
				} catch (IOException e) {
					//
				} finally {
					dstStream = null;
				}
			}
			
			if (srcChannel != null) {
				try {
					srcChannel.close();
				} catch (IOException e) {
					//
				} finally {
					srcChannel = null;
				}
			}
			
			if (dstChannel != null) {
				try {
					dstChannel.close();
				} catch (IOException e) {
					//
				} finally {
					dstChannel = null;
				}
			}
		}
	}
	
	public void copyTo(String dstPath) throws IOException {
		FileInputStream srcStream = null;
		FileInputStream dstStream = null;
		
		FileChannel srcChannel = null;
		FileChannel dstChannel = null;
		
		try {
			srcStream = new FileInputStream(mDataBase.getPath());
			dstStream = new FileInputStream(dstPath);
			
			srcChannel = srcStream.getChannel();
			dstChannel = dstStream.getChannel();
			
			dstChannel.transferFrom(srcChannel, 0L, srcChannel.size());
		} finally {
			if (srcStream != null) {
				try {
					srcStream.close();
				} catch (IOException e) {
					//
				} finally {
					srcStream = null;
				}
			}
			
			if (dstStream != null) {
				try {
					dstStream.close();
				} catch (IOException e) {
					//
				} finally {
					dstStream = null;
				}
			}
			
			if (srcChannel != null) {
				try {
					srcChannel.close();
				} catch (IOException e) {
					//
				} finally {
					srcChannel = null;
				}
			}
			
			if (dstChannel != null) {
				try {
					dstChannel.close();
				} catch (IOException e) {
					//
				} finally {
					dstChannel = null;
				}
			}
		}
	}
	
	public Cursor getAllCourse() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToFirst();
			}
			
			return cursor;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getAllCourse() >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public Cursor getAllItem() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToFirst();
			}
			
			return cursor;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getAllItem() >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public Cursor getAllPHash() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToFirst();
			}
			
			return cursor;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getAllPHash() >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public Course getCourse(long id) {
		try {
			Course course = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Long.toString(id) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					course = new Course(
							cursor.getLong(cursor.getColumnIndex(KEY_COURSE_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY)),
							cursor.getInt(cursor.getColumnIndex(KEY_COURSE_VERSION)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CREATOR))
						);
				}
				
				cursor.close();
			}
			
			return course;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getCourse(long id) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public Course getCourse(String category, String title) {
		try {
			Course course = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_CATEGORY + "=?"
							+ " AND " + KEY_COURSE_TITLE + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { category, title });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					course = new Course(
							cursor.getLong(cursor.getColumnIndex(KEY_COURSE_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY)),
							cursor.getInt(cursor.getColumnIndex(KEY_COURSE_VERSION)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CREATOR))
						);
				}
				
				cursor.close();
			}
			
			return course;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getCourse(String category, String title) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public Course initCourseData(Course course, boolean isWithPHash) {
		if (course != null) {
			List<Item> listItem = getListItem(course.getId());
			
			if (isWithPHash) {
				initListItemData(listItem);
			}
			
			course.setListItem(listItem);
		}
		
		return course;
	}
	
	private List<Course> getListCourse(Cursor cursor) {
		List<Course> listCourse = null;
		
		if (cursor != null) {
			listCourse = new ArrayList<Course>();
			
			if (cursor.moveToFirst()) {
				do {
					Course course = new Course(
							cursor.getLong(cursor.getColumnIndex(KEY_COURSE_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY)),
							cursor.getInt(cursor.getColumnIndex(KEY_COURSE_VERSION)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CREATOR))
						);
					
					listCourse.add(course);
				} while (cursor.moveToNext());
				
				//Collections.sort(listCourse, Course.ORDER_<>);//TODO: equals + hashCode...
			}
			
			cursor.close();
		}
		
		return listCourse;
	}
	
	public List<Course> getListCourse() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			return getListCourse(cursor);
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCourse() >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public List<Course> getListCourse(String category) {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_CATEGORY + "=?"
							+ " GROUP BY " + KEY_COURSE_CATEGORY
							+ " ORDER BY " + KEY_COURSE_CATEGORY;
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { category });
			
			return getListCourse(cursor);
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCourse(String category) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public List<Course> initListCourseData(List<Course> listCourse, boolean isWithPHash) {
		if (listCourse != null) {
			for (Course course : listCourse) {
				initCourseData(course, isWithPHash);
			}
		}
		
		return listCourse;
	}
	
	public List<String> getListCategory() {
		try {
			List<String> listCategory = null;
			
			String sqlQuery = "SELECT DISTINCT " + KEY_COURSE_CATEGORY
							+ " FROM " + TABLE_COURSE;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					listCategory = new ArrayList<String>();
					
					do {
						String category = new String(
								cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY))
							);
						
						listCategory.add(category);
					} while (cursor.moveToNext());
					
					Collections.sort(listCategory);
				}
				
				cursor.close();
			}
			
			return listCategory;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCategory() >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public List<String> getListTitle(String category) {
		try {
			List<String> listTitle = null;
			
			String sqlQuery = "SELECT DISTINCT " + KEY_COURSE_TITLE
							+ " FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_CATEGORY + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { category });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					listTitle = new ArrayList<String>();
					
					do {
						String course = cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE));
						
						listTitle.add(course);
					} while (cursor.moveToNext());
					
					Collections.sort(listTitle);
				}
				
				cursor.close();
			}
			
			return listTitle;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListTitle(String category) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int updateCourse(Course course) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_COURSE_TITLE, course.getTitle());
		cv.put(KEY_COURSE_CATEGORY, course.getCategory());
		cv.put(KEY_COURSE_VERSION, course.getVersion());
		cv.put(KEY_COURSE_CREATOR, course.getCreator());
		
		try {
			return mDataBase.update(TABLE_COURSE, cv, KEY_COURSE_ID + "=?",
					new String[] { Long.toString(course.getId()) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "updateCourse(Course course) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public long addCourse(Course course) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_COURSE_TITLE, course.getTitle());
		cv.put(KEY_COURSE_CATEGORY, course.getCategory());
		cv.put(KEY_COURSE_VERSION, course.getVersion());
		cv.put(KEY_COURSE_CREATOR, course.getCreator());
		
		try {
			return mDataBase.insert(TABLE_COURSE, null, cv);
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "addCourse(Course course) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public long addCourse(Course course, boolean isWithData) {
		long rowId = addCourse(course);
		
		if (isWithData && (rowId != -1)) {
			long id = getCourse(course.getCategory(), course.getTitle()).getId();
			
			for (Item item : course.getListItem()) {
				item.setCourseId(id);
			}
			
			addListItem(course.getListItem(), true);
		}
		
		return rowId;
	}
	
	public int addListCourse(List<Course> listCourse) {
		for (Course course : listCourse) {
			addCourse(course);
		}
		
		return listCourse.size();
	}
	
	public int addListCourse(List<Course> listCourse, boolean isWithData) {
		for (Course course : listCourse) {
			addCourse(course, isWithData);
		}
		
		return listCourse.size();
	}
	
	public int deleteCourse(long id) {
		try {
			return mDataBase.delete(TABLE_COURSE, KEY_COURSE_ID + "=?",
					new String[] { Long.toString(id) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteCourse(long id) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int getCourseCount() {
		try {
			int count = 0;
			
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				count = cursor.getCount();
				
				cursor.close();
			}
			
			return count;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getCourseCount() >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public Item getItem(long id) {
		try {
			Item item = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM
							+ " WHERE " + KEY_ITEM_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Long.toString(id) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					item = new Item(
							cursor.getLong(cursor.getColumnIndex(KEY_ITEM_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_CONTENT)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_VIDEO_URI)),
							cursor.getLong(cursor.getColumnIndex(KEY_ITEM_COURSE_ID))
						);
				}
				
				cursor.close();
			}
			
			return item;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getItem(long id) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public Item getItem(String title, long courseId) {
		try {
			Item item = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM
							+ " WHERE " + KEY_ITEM_TITLE + "=?"
							+ " AND " + KEY_ITEM_COURSE_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { title, Long.toString(courseId) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					item = new Item(
							cursor.getLong(cursor.getColumnIndex(KEY_ITEM_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_CONTENT)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_VIDEO_URI)),
							cursor.getLong(cursor.getColumnIndex(KEY_ITEM_COURSE_ID))
						);
				}
				
				cursor.close();
			}
			
			return item;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getItem(String title, long courseId) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public Item initItemData(Item item) {
		if (item != null) {
			item.setListPHash(getListPHash(item.getId()));
		}
		
		return item;
	}
	
	public List<Item> getListItem(long courseId) {
		try {
			List<Item> listItem = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM
							+ " WHERE " + KEY_ITEM_COURSE_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Long.toString(courseId) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					listItem = new ArrayList<Item>();
					
					do {
						Item item = new Item(
								cursor.getLong(cursor.getColumnIndex(KEY_ITEM_ID)),
								cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)),
								cursor.getString(cursor.getColumnIndex(KEY_ITEM_CONTENT)),
								cursor.getString(cursor.getColumnIndex(KEY_ITEM_VIDEO_URI)),
								cursor.getLong(cursor.getColumnIndex(KEY_ITEM_COURSE_ID))
							);
						
						listItem.add(item);
					} while (cursor.moveToNext());
					
					//Collections.sort(listItem);//TODO: equals + hashCode...
				}
				
				cursor.close();
			}
			
			return listItem;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListItem(long courseId) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public List<Item> getListItem(long courseId, boolean isWithPHash) {
		List<Item> listItem = getListItem(courseId); 
		
		if (isWithPHash && (listItem != null)) {
			for (Item item : listItem) {
				item.setListPHash(getListPHash(item.getId()));
			}
		}
		
		return listItem;
	}
	
	public List<Item> initListItemData(List<Item> listItem) {
		if (listItem != null) {
			for (Item item : listItem) {
				initItemData(item);
			}
		}
		
		return listItem;
	}
	
	public int updateItem(Item item) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_ITEM_TITLE, item.getTitle());
		cv.put(KEY_ITEM_CONTENT, item.getContent());
		cv.put(KEY_ITEM_VIDEO_URI, item.getVideoUri());
		cv.put(KEY_ITEM_COURSE_ID, item.getCourseId());
		
		try {
			return mDataBase.update(TABLE_ITEM, cv, KEY_ITEM_ID + "=?",
					new String[] { Long.toString(item.getId()) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "updateItem(Item item) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public long addItem(Item item) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_ITEM_TITLE, item.getTitle());
		cv.put(KEY_ITEM_CONTENT, item.getContent());
		cv.put(KEY_ITEM_VIDEO_URI, item.getVideoUri());
		cv.put(KEY_ITEM_COURSE_ID, item.getCourseId());
		
		try {
			return mDataBase.insert(TABLE_ITEM, null, cv);
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "addItem(Item item) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public long addItem(Item item, boolean isWithPHash) {
		long rowId = addItem(item);
		
		if (isWithPHash && (rowId != -1)) {
			long id = getItem(item.getTitle(), item.getCourseId()).getId();
			
			for (PHash pHash : item.getListPHash()) {
				pHash.setItemId(id);
			}
			
			addListPHash(item.getListPHash());
		}
		
		return rowId;
	}
	
	public int addListItem(List<Item> listItem) {
		for (Item item : listItem) {
			addItem(item);
		}
		
		return listItem.size();
	}
	
	public int addListItem(List<Item> listItem, boolean isWithPHash) {
		for (Item item : listItem) {
			addItem(item, isWithPHash);
		}
		
		return listItem.size();
	}
	
	public int deleteItem(long id) {
		try {
			return mDataBase.delete(TABLE_ITEM, KEY_ITEM_ID + "=?",
					new String[] { Long.toString(id) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteItem(long id) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int deleteListItem(long courseId) {
		try {
			return mDataBase.delete(TABLE_ITEM, KEY_ITEM_COURSE_ID + "=?",
					new String[] { Long.toString(courseId) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteListItem(long courseId) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int getItemCount() {
		try {
			int count = 0;
			
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				count = cursor.getCount();
				
				cursor.close();
			}
			
			return count;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getItemCount() >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int getItemCount(long courseId) {
		try {
			int count = 0;
			
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM
							+ " WHERE " + KEY_ITEM_COURSE_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Long.toString(courseId) });
			
			if (cursor != null) {
				count = cursor.getCount();
				
				cursor.close();
			}
			
			return count;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getItemCount(long courseId) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public PHash getPHash(long id) {
		try {
			PHash pHash = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH
							+ " WHERE " + KEY_PHASH_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Long.toString(id) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					pHash = new PHash(
							cursor.getLong(cursor.getColumnIndex(KEY_PHASH_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_PHASH_HEX_VALUE)),
							cursor.getString(cursor.getColumnIndex(KEY_PHASH_COMMENT)),
							cursor.getLong(cursor.getColumnIndex(KEY_PHASH_ITEM_ID))
						);
				}
				
				cursor.close();
			}
			
			return pHash;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getPHash(long id) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public List<PHash> getListPHash(long itemId) {
		try {
			List<PHash> listPHash = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH
							+ " WHERE " + KEY_PHASH_ITEM_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Long.toString(itemId) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					listPHash = new ArrayList<PHash>();
					
					do {
						PHash pHash = new PHash(
								cursor.getLong(cursor.getColumnIndex(KEY_PHASH_ID)),
								cursor.getString(cursor.getColumnIndex(KEY_PHASH_HEX_VALUE)),
								cursor.getString(cursor.getColumnIndex(KEY_PHASH_COMMENT)),
								cursor.getLong(cursor.getColumnIndex(KEY_PHASH_ITEM_ID))
							);
						
						listPHash.add(pHash);
					} while (cursor.moveToNext());
					
					//Collections.sort(listPHash);//TODO: equals + hashCode...
				}
				
				cursor.close();
			}
			
			return listPHash;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListPHash(long itemId) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int updatePHash(PHash pHash) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_PHASH_HEX_VALUE, pHash.getHexValue());
		cv.put(KEY_PHASH_COMMENT, pHash.getComment());
		cv.put(KEY_PHASH_ITEM_ID, pHash.getItemId());
		
		try {
			return mDataBase.update(TABLE_PHASH, cv, KEY_PHASH_ID + "=?",
					new String[] { Long.toString(pHash.getId()) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "updatePHash(PHash pHash) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public long addPHash(PHash pHash) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_PHASH_HEX_VALUE, pHash.getHexValue());
		cv.put(KEY_PHASH_COMMENT, pHash.getComment());
		cv.put(KEY_PHASH_ITEM_ID, pHash.getItemId());
		
		try {
			return mDataBase.insert(TABLE_PHASH, null, cv);
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "addPHash(PHash pHash) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int addListPHash(List<PHash> listPHash) {
		for (PHash pHash : listPHash) {
			addPHash(pHash);
		}
		
		return listPHash.size();
	}
	
	public int deletePHash(long id) {
		try {
			return mDataBase.delete(TABLE_PHASH, KEY_PHASH_ID + "=?",
					new String[] { Long.toString(id) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deletePHash(long id) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int deleteListPHash(long itemId) {
		try {
			return mDataBase.delete(TABLE_PHASH, KEY_PHASH_ITEM_ID + "=?",
					new String[] { Long.toString(itemId) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteListPHash(long itemId) >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int getPHashCount() {
		try {
			int count = 0;
			
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				count = cursor.getCount();
				
				cursor.close();
			}
			
			return count;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getPHashCount() >> " + sqle.toString());
			throw sqle;
		}
	}
	
	public int getPHashCount(long itemId) {
		try {
			int count = 0;
			
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH
							+ " WHERE " + KEY_PHASH_ITEM_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Long.toString(itemId) });
			
			if (cursor != null) {
				count = cursor.getCount();
				
				cursor.close();
			}
			
			return count;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getPHashCount(long itemId) >> " + sqle.toString());
			throw sqle;
		}
	}
	
}
