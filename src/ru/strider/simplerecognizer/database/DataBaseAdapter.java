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

import java.io.IOException;
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
	
	private static final String SQL_QUERY_PRAGMA_FK_ON = "PRAGMA foreign_keys=ON;";
	
	private static final String TABLE_COURSE = "Course";
	private static final String KEY_COURSE_ID = "_id";
	private static final String KEY_COURSE_TITLE = "title";
	private static final String KEY_COURSE_CATEGORY = "category";
	private static final String KEY_COURSE_CREATOR = "creator";
	
	private static final String TABLE_ITEM = "Item";
	private static final String KEY_ITEM_ID = "_id";
	private static final String KEY_ITEM_TITLE = "title";
	private static final String KEY_ITEM_CONTENT = "content";
	private static final String KEY_ITEM_COURSE_ID = "course_id";
	
	private static final String TABLE_PHASH = "PHash";
	private static final String KEY_PHASH_ID = "_id";
	private static final String KEY_PHASH_HEX_VALUE = "hex_value";
	private static final String KEY_PHASH_COMMENT = "comment";
	private static final String KEY_PHASH_ITEM_ID = "item_id";
	
	private final Context mContext;
	
	private SQLiteDatabase mDataBase;
	private DataBaseHelper mDataBaseHelper;
	
	public DataBaseAdapter(Context context) {
		mContext = context;
		
		mDataBaseHelper = new DataBaseHelper(mContext);
	}
	
	public DataBaseAdapter createDataBase(Activity activityUI) {
		try {
			mDataBaseHelper.createDataBase(activityUI);
		} catch (IOException ioe) {
			Log.e(LOG_TAG, "Unable To Create Database >> " + ioe.toString());
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
			Log.e(LOG_TAG, "Open DataBase >> " + sqle.toString());
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
			Log.e(LOG_TAG, "Write DataBase >> " + sqle.toString());
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
			Log.e(LOG_TAG, "Delete DataBase >> " + sqle.toString());
			throw sqle;
		}
		
		return this;
	}
	
	public void close() {
		mDataBaseHelper.close();
	}
	
	public Cursor getAllCourse() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToNext();
			}
			
			return cursor;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getAllCourse() >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public Cursor getAllItem() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToNext();
			}
			
			return cursor;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getAllItem() >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public Cursor getAllPHash() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToNext();
			}
			
			return cursor;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getAllPHash() >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public Course getCourse(int id) {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_ID + "=" + Integer.toString(id);
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToFirst();
			}
			
			Course course = new Course(
					cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
					cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE)),
					cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY)),
					cursor.getString(cursor.getColumnIndex(KEY_COURSE_CREATOR))
				);
			
			cursor.close();
			
			return course;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getCourse(int id) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public Course getCourse(String category, String title) {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_CATEGORY + "='" + category + "'"
							+ " AND " + KEY_COURSE_TITLE + "='" + title + "'";
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToFirst();
			}
			
			Course course = new Course(
					cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
					cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE)),
					cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY)),
					cursor.getString(cursor.getColumnIndex(KEY_COURSE_CREATOR))
				);
			
			cursor.close();
			
			return course;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getCourse(String category, String title) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public List<Course> getListCourse() {
		try {
			List<Course> listCourse = new ArrayList<Course>();
			
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor.moveToFirst()) {
				do {
					Course course = new Course(
							cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CREATOR))
						);
					
					listCourse.add(course);
				} while (cursor.moveToNext());
			}
			
			cursor.close();
			
			return listCourse;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCourse() >> "+ sqle.toString());
			throw sqle;
		}
	}
	/*
	public List<Course> getListCourse(String category) {
		try {
			List<Course> listCourse = new ArrayList<Course>();
			
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_CATEGORY + "='" + category + "'"
							+ " GROUP BY " + KEY_COURSE_CATEGORY
							+ " ORDER BY " + KEY_COURSE_CATEGORY;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor.moveToFirst()) {
				do {
					Course course = new Course(
							cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY)),
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CREATOR))
						);
					
					listCourse.add(course);
				} while (cursor.moveToNext());
			}
			
			cursor.close();
			
			return listCourse;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCourse(String category) >> "+ sqle.toString());
			throw sqle;
		}
	}
	*/
	
	public List<String> getListCategory() {
		try {
			List<String> listCategory = new ArrayList<String>();
			
			String sqlQuery = "SELECT DISTINCT " + KEY_COURSE_CATEGORY
							+ " FROM " + TABLE_COURSE;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor.moveToFirst()) {
				do {
					String category = new String(
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_CATEGORY))
						);
					
					listCategory.add(category);
				} while (cursor.moveToNext());
			}
			
			cursor.close();
			
			return listCategory;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCategory() >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public List<String> getListCourse(String category) {
		try {
			List<String> listCourse = new ArrayList<String>();
			
			String sqlQuery = "SELECT DISTINCT " + KEY_COURSE_TITLE
							+ " FROM " + TABLE_COURSE
							+ " WHERE " + KEY_COURSE_CATEGORY + "='" + category + "'";
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor.moveToFirst()) {
				do {
					String course = new String(
							cursor.getString(cursor.getColumnIndex(KEY_COURSE_TITLE))
						);
					
					listCourse.add(course);
				} while (cursor.moveToNext());
			}
			
			cursor.close();
			
			return listCourse;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListCourse(String category) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public int updateCourse(Course course) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_COURSE_TITLE, course.getTitle());
		cv.put(KEY_COURSE_CATEGORY, course.getCategory());
		cv.put(KEY_COURSE_CREATOR, course.getCreator());
		
		try {
			return mDataBase.update(TABLE_COURSE, cv, KEY_COURSE_ID + "=?",
					new String[] { Integer.toString(course.getId()) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "updateCourse(Course course) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public long addCourse(Course course) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_COURSE_TITLE, course.getTitle());
		cv.put(KEY_COURSE_CATEGORY, course.getCategory());
		cv.put(KEY_COURSE_CREATOR, course.getCreator());
		
		try {
			return mDataBase.insert(TABLE_COURSE, null, cv);
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "addCourse(Course course) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public int deleteCourse(int id) {
		try {
			return mDataBase.delete(TABLE_COURSE, KEY_COURSE_ID + "=?",
					new String[] { Integer.toString(id) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteCourse(int id) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public int getCourseCount() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_COURSE;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			final int courseCount = cursor.getCount();
			
			cursor.close();
			
			return courseCount;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getCourseCount() >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public Item getItem(int id) {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM
							+ " WHERE " + KEY_ITEM_ID + "=" + Integer.toString(id);
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToFirst();
			}
			
			Item item = new Item(
					cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID)),
					cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)),
					cursor.getString(cursor.getColumnIndex(KEY_ITEM_CONTENT)),
					cursor.getInt(cursor.getColumnIndex(KEY_ITEM_COURSE_ID))
				);
			
			cursor.close();
			
			return item;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getItem(int id) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public List<Item> getListItem(int courseId) {
		return getListItem(courseId, false);
	}
	
	public List<Item> getListItem(int courseId, boolean isWithPHash) {
		try {
			List<Item> listItem = new ArrayList<Item>();
			
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM
							+ " WHERE " + KEY_ITEM_COURSE_ID + "=" + Integer.toString(courseId);
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor.moveToFirst()) {
				do {
					Item item = new Item(
							cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)),
							cursor.getString(cursor.getColumnIndex(KEY_ITEM_CONTENT)),
							cursor.getInt(cursor.getColumnIndex(KEY_ITEM_COURSE_ID))
						);
					
					if (isWithPHash) {
						item.setListPHash(getListPHash(item.getId()));
					}
					
					listItem.add(item);
				} while (cursor.moveToNext());
			}
			
			cursor.close();
			
			return listItem;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListItem(int courseId) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public int updateItem(Item item) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_ITEM_TITLE, item.getTitle());
		cv.put(KEY_ITEM_CONTENT, item.getContent());
		cv.put(KEY_ITEM_COURSE_ID, item.getCourseId());
		
		try {
			return mDataBase.update(TABLE_ITEM, cv, KEY_ITEM_ID + "=?",
					new String[] { Integer.toString(item.getId()) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "updateItem(Item item) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public long addItem(Item item) {
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_ITEM_TITLE, item.getTitle());
		cv.put(KEY_ITEM_CONTENT, item.getContent());
		cv.put(KEY_ITEM_COURSE_ID, item.getCourseId());
		
		try {
			return mDataBase.insert(TABLE_ITEM, null, cv);
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "addItem(Item item) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public int deleteItem(int id) {
		try {
			return mDataBase.delete(TABLE_ITEM, KEY_ITEM_ID + "=?",
					new String[] { Integer.toString(id) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteItem(int id) >> "+ sqle.toString());
			throw sqle;
		}
	}
	/*
	public int deleteListItem(int courseId) {
		try {
			return mDataBase.delete(TABLE_ITEM, KEY_ITEM_COURSE_ID + "=?",
					new String[] { Integer.toString(courseId) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteListItem(int courseId) >> "+ sqle.toString());
			throw sqle;
		}
	}
	*/
	public int getItemCount() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_ITEM;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			final int itemCount = cursor.getCount();
			
			cursor.close();
			
			return itemCount;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getItemCount() >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public PHash getPHash(int id) {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH
							+ " WHERE " + KEY_PHASH_ID + "=" + Integer.toString(id);
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor != null) {
				cursor.moveToFirst();
			}
			
			PHash pHash = new PHash(
					cursor.getInt(cursor.getColumnIndex(KEY_PHASH_ID)),
					cursor.getString(cursor.getColumnIndex(KEY_PHASH_HEX_VALUE)),
					cursor.getString(cursor.getColumnIndex(KEY_PHASH_COMMENT)),
					cursor.getInt(cursor.getColumnIndex(KEY_PHASH_ITEM_ID))
				);
			
			cursor.close();
			
			return pHash;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getPHash(int id) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public List<PHash> getListPHash(int itemId) {
		try {
			List<PHash> listPHash = new ArrayList<PHash>();
			
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH
							+ " WHERE " + KEY_PHASH_ITEM_ID + "=" + Integer.toString(itemId);
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			if (cursor.moveToFirst()) {
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
			
			return listPHash;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getListPHash(int itemId) >> "+ sqle.toString());
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
			Log.e(LOG_TAG, "updatePHash(PHash pHash) >> "+ sqle.toString());
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
			Log.e(LOG_TAG, "addPHash(PHash pHash) >> "+ sqle.toString());
			throw sqle;
		}
	}
	
	public int deletePHash(int id) {
		try {
			return mDataBase.delete(TABLE_PHASH, KEY_PHASH_ID + "=?",
					new String[] { Integer.toString(id) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deletePHash(int id) >> "+ sqle.toString());
			throw sqle;
		}
	}
	/*
	public int deleteListPHash(int itemId) {
		try {
			return mDataBase.delete(TABLE_PHASH, KEY_PHASH_ITEM_ID + "=?",
					new String[] { Integer.toString(itemId) });
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "deleteListPHash(int itemId) >> "+ sqle.toString());
			throw sqle;
		}
	}
	*/
	public int getPHashCount() {
		try {
			String sqlQuery = "SELECT * FROM " + TABLE_PHASH;
			Cursor cursor = mDataBase.rawQuery(sqlQuery, null);
			
			final int pHashCount = cursor.getCount();
			
			cursor.close();
			
			return pHashCount;
		} catch (SQLException sqle) {
			Log.e(LOG_TAG, "getPHashCount() >> "+ sqle.toString());
			throw sqle;
		}
	}
	
}
