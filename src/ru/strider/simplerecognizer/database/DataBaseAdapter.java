/*
 * Copyright (C) 2012 strider
 * 
 * Simple Recognizer
 * DataBase Adapter Class
 * By © strider 2012.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import ru.strider.simplerecognizer.model.Course;
import ru.strider.simplerecognizer.model.Item;
import ru.strider.simplerecognizer.model.PHash;

/**
 * DataBase Adapter Class.
 * 
 * @author strider
 */
public class DataBaseAdapter {
	
	private static final String LOG_TAG = "DataBaseAdapter";
	
	private static final int DB_VERSION = 1;
	private static final int DB_VERSION_EMPTY = 1;
	
	private static final String DB_NAME = "SimpleRecognizer.db";
	private static final String DB_NAME_EMPTY = "SimpleRecognizer_Empty.db";
	
	private static final String SQL_QUERY_PRAGMA_FK_ON = "PRAGMA foreign_keys=ON;";
	
	private static final String TABLE_COURSE = "Course";
	private static final String KEY_COURSE_ID = "_id";
	private static final String KEY_COURSE_TITLE = "title";
	private static final String KEY_COURSE_CATEGORY = "category";
	private static final String KEY_COURSE_VERSION = "version";
	private static final String KEY_COURSE_CREATOR = "creator";
	
	private static final String TABLE_ITEM = "Item";
	private static final String KEY_ITEM_ID = "_id";
	private static final String KEY_ITEM_TITLE = "title";
	private static final String KEY_ITEM_CONTENT = "content";
	private static final String KEY_ITEM_VIDEO_URI = "video_uri";
	private static final String KEY_ITEM_COURSE_ID = "course_id";
	
	private static final String TABLE_PHASH = "PHash";
	private static final String KEY_PHASH_ID = "_id";
	private static final String KEY_PHASH_HEX_VALUE = "hex_value";
	private static final String KEY_PHASH_COMMENT = "comment";
	private static final String KEY_PHASH_ITEM_ID = "item_id";
	
	private Context mContext = null;
	
	private DataBaseHelper mDataBaseHelper = null;
	
	private SQLiteDatabase mDataBase = null;
	
	public DataBaseAdapter(Context context) {
		mContext = context;
		
		mDataBaseHelper = new DataBaseHelper(mContext, DB_NAME, DB_NAME, DB_VERSION);
	}
	
	public DataBaseAdapter(Context context, String dbName) {
		mContext = context;
		
		mDataBaseHelper = new DataBaseHelper(mContext, DB_NAME_EMPTY, dbName, DB_VERSION_EMPTY);
	}
	
	public DataBaseAdapter createDataBase(Activity activityUI) {
		try {
			mDataBaseHelper.createDataBase(activityUI);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Unable To Create Database >> " + e.getMessage());
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
			Log.e(LOG_TAG, "Open DataBase >> " + sqle.getMessage());
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
			Log.e(LOG_TAG, "Write DataBase >> " + sqle.getMessage());
			throw sqle;
		}
		
		return this;
	}
	
	public DataBaseAdapter delete() throws SQLException {
		try {
			mDataBaseHelper.openDataBase();
			mDataBaseHelper.close();
			if (mDataBaseHelper.deleteDataBase()) {
				Log.w(LOG_TAG, "DataBase Deleted");
			} else {
				Log.e(LOG_TAG, "DataBase Not Deleted");
			}
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "Delete DataBase >> " + sqle.getMessage());
			throw sqle;
		}
		
		return this;
	}
	
	public void close() {
		mDataBaseHelper.close();
	}
	
	public void copyFrom(String srcPath) throws IOException {
		FileChannel srcChannel = null;
		FileChannel dstChannel = null;
		
		try {
			srcChannel = new FileInputStream(srcPath).getChannel();
			dstChannel = new FileOutputStream(mDataBase.getPath()).getChannel();
			
			dstChannel.transferFrom(srcChannel, 0L, srcChannel.size());
		} finally {
			if (srcChannel != null) {
				srcChannel.close();
				srcChannel = null;
			}
			
			if (dstChannel != null) {
				dstChannel.close();
				dstChannel = null;
			}
		}
	}
	
	public void copyTo(String dstPath) throws IOException {
		FileChannel srcChannel = null;
		FileChannel dstChannel = null;
		
		try {
			srcChannel = new FileInputStream(mDataBase.getPath()).getChannel();
			dstChannel = new FileOutputStream(dstPath).getChannel();
			
			dstChannel.transferFrom(srcChannel, 0L, srcChannel.size());
		} finally {
			if (srcChannel != null) {
				srcChannel.close();
				srcChannel = null;
			}
			
			if (dstChannel != null) {
				dstChannel.close();
				dstChannel = null;
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
			Log.e(LOG_TAG, "getAllCourse() >> " + sqle.getMessage());
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
			Log.e(LOG_TAG, "getAllItem() >> " + sqle.getMessage());
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
			Log.e(LOG_TAG, "getAllPHash() >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public Course getCourse(int id) {
		try {
			Course course = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Integer.toString(id) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					course = new Course(
							cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
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
			Log.e(LOG_TAG, "getCourse(int id) >> " + sqle.getMessage());
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
							cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
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
			Log.e(LOG_TAG, "getCourse(String category, String title) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public Course getCourse(String category, String title, boolean isWithData) {
		Course course = getCourse(category, title);
		
		if (isWithData && (course != null)) {
			course.setListItem(getListItem(course.getId(), true));
		}
		
		return course;
	}
	
	public List<Course> getListCourse() {
		try {
			List<Course> listCourse = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					listCourse = new ArrayList<Course>();
					
					do {
						Course course = new Course(
								cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
								cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE)),
								cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY)),
								cursor.getInt(cursor.getColumnIndex(KEY_COURSE_VERSION)),
								cursor.getString(cursor.getColumnIndex(KEY_COURSE_CREATOR))
							);
						
						listCourse.add(course);
					} while (cursor.moveToNext());
				}
				
				cursor.close();
			}
			
			return listCourse;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCourse() >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public List<Course> getListCourse(boolean isWithData) {
		List<Course> listCourse = getListCourse();
		
		if (isWithData && (listCourse != null)) {
			for (Course course : listCourse) {
				course.setListItem(getListItem(course.getId(), true));
			}
		}
		
		return listCourse;
	}
	/*
	public List<Course> getListCourse(String category) {
		try {
			List<Course> listCourse = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_CATEGORY + "=?"
							+ " GROUP BY " + KEY_COURSE_CATEGORY
							+ " ORDER BY " + KEY_COURSE_CATEGORY;
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { category });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					listCourse = new ArrayList<Course>();
					
					do {
						Course course = new Course(
								cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
								cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE)),
								cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY)),
								cursor.getInt(cursor.getColumnIndex(KEY_COURSE_VERSION)),
								cursor.getString(cursor.getColumnIndex(KEY_COURSE_CREATOR))
							);
						
						listCourse.add(course);
					} while (cursor.moveToNext());
				}
				
				cursor.close();
			}
			
			return listCourse;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCourse(String category) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	*/
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
				}
				
				cursor.close();
			}
			
			return listCategory;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCategory() >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public List<String> getListCourse(String category) {
		try {
			List<String> listCourse = null;
			
			String sqlQuery = "SELECT DISTINCT " + KEY_COURSE_TITLE
							+ " FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_CATEGORY + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { category });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					listCourse = new ArrayList<String>();
					
					do {
						String course = new String(
								cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE))
							);
						
						listCourse.add(course);
					} while (cursor.moveToNext());
				}
				
				cursor.close();
			}
			
			return listCourse;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCourse(String category) >> " + sqle.getMessage());
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
					new String[] { Integer.toString(course.getId()) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "updateCourse(Course course) >> " + sqle.getMessage());
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
			Log.e(LOG_TAG, "addCourse(Course course) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public long addCourse(Course course, boolean isWithData) {
		long rowId = addCourse(course);
		
		if (isWithData && (rowId != -1)) {
			int id = getCourse(course.getCategory(), course.getTitle()).getId();
			
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
	
	public int deleteCourse(int id) {
		try {
			return mDataBase.delete(TABLE_COURSE, KEY_COURSE_ID + "=?",
					new String[] { Integer.toString(id) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteCourse(int id) >> " + sqle.getMessage());
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
			Log.e(LOG_TAG, "getCourseCount() >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public Item getItem(int id) {
		try {
			Item item = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM
							+ " WHERE " + KEY_ITEM_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Integer.toString(id) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					item = new Item(
							cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_CONTENT)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_VIDEO_URI)),
							cursor.getInt(cursor.getColumnIndex(KEY_ITEM_COURSE_ID))
						);
				}
				
				cursor.close();
			}
			
			return item;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getItem(int id) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public Item getItem(String title, int courseId) {
		try {
			Item item = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM
							+ " WHERE " + KEY_ITEM_TITLE + "=?"
							+ " AND " + KEY_ITEM_COURSE_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { title, Integer.toString(courseId) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					item = new Item(
							cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_CONTENT)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_VIDEO_URI)),
							cursor.getInt(cursor.getColumnIndex(KEY_ITEM_COURSE_ID))
						);
				}
				
				cursor.close();
			}
			
			return item;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getItem(String title, int courseId) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public List<Item> getListItem(int courseId) {
		try {
			List<Item> listItem = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM
							+ " WHERE " + KEY_ITEM_COURSE_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Integer.toString(courseId) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					listItem = new ArrayList<Item>();
					
					do {
						Item item = new Item(
								cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID)),
								cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)),
								cursor.getString(cursor.getColumnIndex(KEY_ITEM_CONTENT)),
								cursor.getString(cursor.getColumnIndex(KEY_ITEM_VIDEO_URI)),
								cursor.getInt(cursor.getColumnIndex(KEY_ITEM_COURSE_ID))
							);
						
						listItem.add(item);
					} while (cursor.moveToNext());
				}
				
				cursor.close();
			}
			
			return listItem;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListItem(int courseId) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public List<Item> getListItem(int courseId, boolean isWithPHash) {
		List<Item> listItem = getListItem(courseId); 
		
		if (isWithPHash && (listItem != null)) {
			for (Item item : listItem) {
				item.setListPHash(getListPHash(item.getId()));
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
					new String[] { Integer.toString(item.getId()) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "updateItem(Item item) >> " + sqle.getMessage());
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
			Log.e(LOG_TAG, "addItem(Item item) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public long addItem(Item item, boolean isWithPHash) {
		long rowId = addItem(item);
		
		if (isWithPHash && (rowId != -1)) {
			int id = getItem(item.getTitle(), item.getCourseId()).getId();
			
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
	
	public int deleteItem(int id) {
		try {
			return mDataBase.delete(TABLE_ITEM, KEY_ITEM_ID + "=?",
					new String[] { Integer.toString(id) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteItem(int id) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	/*
	public int deleteListItem(int courseId) {
		try {
			return mDataBase.delete(TABLE_ITEM, KEY_ITEM_COURSE_ID + "=?",
					new String[] { Integer.toString(courseId) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteListItem(int courseId) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	*/
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
			Log.e(LOG_TAG, "getItemCount() >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public PHash getPHash(int id) {
		try {
			PHash pHash = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH
							+ " WHERE " + KEY_PHASH_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Integer.toString(id) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					pHash = new PHash(
							cursor.getInt(cursor.getColumnIndex(KEY_PHASH_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_PHASH_HEX_VALUE)),
							cursor.getString(cursor.getColumnIndex(KEY_PHASH_COMMENT)),
							cursor.getInt(cursor.getColumnIndex(KEY_PHASH_ITEM_ID))
						);
				}
				
				cursor.close();
			}
			
			return pHash;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getPHash(int id) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public List<PHash> getListPHash(int itemId) {
		try {
			List<PHash> listPHash = null;
			
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH
							+ " WHERE " + KEY_PHASH_ITEM_ID + "=?";
			Cursor cursor = mDataBase.rawQuery(sqlQuery,
					new String[] { Integer.toString(itemId) });
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					listPHash = new ArrayList<PHash>();
					
					do {
						PHash pHash = new PHash(
								cursor.getInt(cursor.getColumnIndex(KEY_PHASH_ID)),
								cursor.getString(cursor.getColumnIndex(KEY_PHASH_HEX_VALUE)),
								cursor.getString(cursor.getColumnIndex(KEY_PHASH_COMMENT)),
								cursor.getInt(cursor.getColumnIndex(KEY_PHASH_ITEM_ID))
							);
						
						listPHash.add(pHash);
					} while (cursor.moveToNext());
				}
				
				cursor.close();
			}
			
			return listPHash;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListPHash(int itemId) >> " + sqle.getMessage());
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
					new String[] { Integer.toString(pHash.getId()) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "updatePHash(PHash pHash) >> " + sqle.getMessage());
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
			Log.e(LOG_TAG, "addPHash(PHash pHash) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
	public int addListPHash(List<PHash> listPHash) {
		for (PHash pHash : listPHash) {
			addPHash(pHash);
		}
		
		return listPHash.size();
	}
	
	public int deletePHash(int id) {
		try {
			return mDataBase.delete(TABLE_PHASH, KEY_PHASH_ID + "=?",
					new String[] { Integer.toString(id) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deletePHash(int id) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	/*
	public int deleteListPHash(int itemId) {
		try {
			return mDataBase.delete(TABLE_PHASH, KEY_PHASH_ITEM_ID + "=?",
					new String[] { Integer.toString(itemId) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteListPHash(int itemId) >> " + sqle.getMessage());
			throw sqle;
		}
	}
	*/
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
			Log.e(LOG_TAG, "getPHashCount() >> " + sqle.getMessage());
			throw sqle;
		}
	}
	
}
