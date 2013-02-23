/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * SQLiteOpenHelper DataBaseHelper Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.strider.simplerecognizer.SimpleRecognizer;
import ru.strider.util.Text;

/**
 * SQLiteOpenHelper DataBaseHelper Class.
 * 
 * @author strider
 */
public class DataBaseHelper extends SQLiteOpenHelper {
	
	private static final String LOG_TAG = DataBaseHelper.class.getSimpleName();
	
	private static final String ASSETS_DB_PATH = "Databases" + File.separator;
	
	private static final String DB_PATH = SimpleRecognizer.getPackageContext().getApplicationInfo()
			.dataDir + File.separator + "databases" + File.separator;
	
	private static final String DB_NAME = "SimpleRecognizer";
	private static final String DB_NAME_EMPTY = "SimpleRecognizer_Empty";
	
	private static final String DB_EXTENSION = ".db";
	
	private static final int DB_VERSION = 1;
	private static final int DB_VERSION_EMPTY = 1;
	
	private String mDbNameSrc = null;
	private String mDbName = null;
	private int mDbVersion = 0;
	
	private SQLiteDatabase mDataBase = null; 
	
	private DataBaseHelper() {
		super(SimpleRecognizer.getPackageContext(), DB_NAME + DB_EXTENSION, null, DB_VERSION);
		
		mDbNameSrc = mDbName = DB_NAME + DB_EXTENSION;
		mDbVersion = DB_VERSION;
	}
	
	public DataBaseHelper(String dbName) {
		super(SimpleRecognizer.getPackageContext(), dbName, null, DB_VERSION_EMPTY);
		
		mDbNameSrc = DB_NAME_EMPTY + DB_EXTENSION; 
		mDbName = dbName;
		mDbVersion = DB_VERSION_EMPTY;
	}
	
	public static DataBaseHelper getInstance() {
		return DataBaseHolder.INSTANCE;
	}
	
	/**
	 * Creates an empty database on the system and rewrites it with own database.
	 * 
	 * @return True if the database exists and valid, false otherwise.
	 * 
	 * @throws IOException
	 */
	public boolean createDataBase() throws IOException {
		SQLiteDatabase db = null;
		
		if (checkDataBase()) {
			try {
				db = this.getWritableDatabase();
			} catch (SQLException sqle) {
				//
			} finally {
				if (db != null) {
					db.close();
				}
			}
		}
		
		boolean isInvalid = false;
		
		if (!checkDataBase()) {
			db = this.getReadableDatabase();
			
			if (db != null) {
				db.close();
			}
			
			try {
				copyDataBase();
				
				Log.i(LOG_TAG, ("Database" + Text.SEPARATOR + "Copied"));
			} catch (IOException e) {
				Log.e(LOG_TAG, ("Database" + Text.SEPARATOR + "Not copied"));
				Log.w(LOG_TAG, ("Error copying Database >> " + e.toString()));
				//throw (new IllegalStateException("Error copying DataBase"));
			}
			
			if (checkDataBase()) {
				if (!checkSize()) {
					isInvalid = true;
					
					deleteDataBase();
				}
			} else {
				isInvalid = true;
			}
		} else {
			SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, ("Database already exists >> "
					+ "dbVersion = " + Integer.toString(mDbVersion)));
		}
		
		return (!isInvalid);
	}
	
	/**
	 * Checks if the database already exist to avoid re-copying the file
	 * each time application opened.
	 * 
	 * @return True if the database exists, false otherwise.
	 */
	private boolean checkDataBase() {
		boolean isExists = false;
		
		File dbFile = new File(DB_PATH + mDbName);
		
		if (dbFile.exists()) {
			SQLiteDatabase db = null;
			
			try {
				db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
			} catch (SQLException sqle) {
				//
			}
			
			if (db != null) {
				isExists = true;
				
				db.close();
			}
		}
		
		SimpleRecognizer.logIfDebug(
				Log.VERBOSE,
				"dbFile",
				(dbFile.getPath() + Text.SEPARATOR + isExists)
			);
		
		return isExists;
	}
	
	/**
	 * Checks if the database file size equals to assets file to avoid
	 * opening corrupted file.
	 * 
	 * @return True if size is valid, false otherwise.
	 */
	private boolean checkSize() {
		boolean isValid = false;
		
		File dbFile = new File(DB_PATH + mDbName);
		
		if (dbFile.exists() && dbFile.isFile()) {
			String dbPath = ASSETS_DB_PATH + mDbNameSrc;
			
			int dbSize = -1;
			
			InputStream is = null;
			
			try {
				is = SimpleRecognizer.getPackageContext().getAssets().open(dbPath);
				
				dbSize = is.available();
			} catch (IOException e) {
				Log.e(LOG_TAG, ("Error loading Database from Assets >> assets/" + dbPath));
				Log.w(LOG_TAG, e.toString());
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						//
					}
				}
			}
			
			if (dbFile.length() == dbSize) {
				isValid = true;
			} else {
				Log.w("dbFile", (dbFile.getPath() + Text.SEPARATOR + "Bad File Size"));
			}
		}
		
		return isValid;
	}
	
	/**
	 * Copies database from local assets-folder to the just created empty
	 * database in the system folder, from where it can be accessed and
	 * handled. This is done by transferring stream of bytes.
	 */
	private void copyDataBase() throws IOException {
		File dbDir = new File(DB_PATH);
		
		if (!dbDir.exists()) {
			dbDir.mkdirs();
		}
		
		InputStream is = SimpleRecognizer.getPackageContext().getAssets()
				.open(ASSETS_DB_PATH + mDbNameSrc);
		OutputStream os = new FileOutputStream(DB_PATH + mDbName);
		
		byte[] buffer = new byte[1024];
		int length = 0;
		
		while ((length = is.read(buffer)) != -1) {
			os.write(buffer, 0, length);
		}
		
		os.flush();
		
		os.close();
		is.close();
	}
	
	/**
	 * Opens database.
	 * 
	 * @return True on success, false otherwise.
	 */
	public boolean openDataBase() throws SQLException {
		String dbPath = DB_PATH + mDbName;
		
		mDataBase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
		
		SimpleRecognizer.logIfDebug(Log.VERBOSE, "dbFullPath", dbPath);
		
		return (mDataBase != null);
	}
	
	/**
	 * Deletes database.
	 * 
	 * @return True if the database was successfully deleted, false otherwise.
	 */
	public boolean deleteDataBase() throws SQLException {
		return SimpleRecognizer.getPackageContext().deleteDatabase(mDbName);
	}
	
	/**
	 * Closes any open database object.
	 */
	@Override
	public synchronized void close() {
		if (mDataBase != null) {
			mDataBase.close();
			
			mDataBase = null;
		}
		
		super.close();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onCreate() called");
		/*
		String CREATE_TABLE_COURSE = "CREATE TABLE Course ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "title TEXT NOT NULL,"
				+ "category TEXT,"
				+ "creator TEXT"
			+ ");";
		db.execSQL(CREATE_TABLE_COURSE);
		
		String CREATE_TABLE_ITEM = "CREATE TABLE Item ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "title TEXT NOT NULL,"
				+ "content TEXT NOT NULL,"
				+ "video_uri TEXT,"
				+ "course_id INTEGER NOT NULL,"
				+ "FOREIGN KEY (course_id) REFERENCES Course(_id)"
			+ ");";
		db.execSQL(CREATE_TABLE_ITEM);
		
		String CREATE_TABLE_PHASH = "CREATE TABLE PHash ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "hex_value TEXT NOT NULL,"
				+ "comment TEXT,"
				+ "item_id INTEGER NOT NULL,"
				+ "FOREIGN KEY (item_id) REFERENCES Item(_id)"
			+ ");";
		db.execSQL(CREATE_TABLE_PHASH;
		
		String CREATE_TRIGGER_ITEMCOURSE_COURSE_ID = "CREATE TRIGGER fk_ItemCourse_course_id"
				+ "BEFORE INSERT ON Item"
				+ "FOR EACH ROW BEGIN"
					+ "SELECT CASE WHEN ((SELECT _id FROM Course WHERE _id=new.course_id) IS NULL)"
					+ "THEN RAISE (ABORT, 'Foreign Key Violation') END;"
				+"END;";
		db.execSQL(CREATE_TRIGGER_ITEMCOURSE_COURSE_ID);
		
		String CREATE_TRIGGER_PHASHITEM_ITEM_ID = "CREATE TRIGGER fk_PHashItem_item_id"
				+ "BEFORE INSERT ON PHash"
				+ "FOR EACH ROW BEGIN"
					+ "SELECT CASE WHEN ((SELECT _id FROM Item WHERE _id=new.item_id) IS NULL)"
					+ "THEN RAISE (ABORT, 'Foreign Key Violation') END;"
				+"END;";
		db.execSQL(CREATE_TRIGGER_PHASHITEM_ITEM_ID);
		
		String INSERT_COURSE = "INSERT INTO Course(title, category, creator)"
				+ "VALUES ('Course', 'Category', 'strider.stankov@gmail.com');";
		db.execSQL(INSERT_COURSE);
		
		String INSERT_ITEM = "INSERT INTO Item(title, content, video_uri, course_id)"
				+ "VALUES ('Item', 'This is temp Item.<br /><br />For more info blahblahblah...', 'http://www.youtube.com/watch?v=rT2LzCLhbOE', 1);";
		db.execSQL(INSERT_ITEM);
		
		String INSERT_PHASH = "INSERT INTO PHash(hex_value, comment, item_id)"
				+ "VALUES ('FFFFFFFFFFFF', 'This is temp pHash value.<br /><br />For more info blahblahblah...', 1);";
		db.execSQL(INSERT_PHASH);*/
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "onUpgrade() called");
		
		if (newVersion > oldVersion) {
			/*if (oldVersion == 1) {
				// 1 —› X
				try {
					if (deleteDataBase()) {
						Log.i(LOG_TAG, ("On upgrade Database deleted >> oldVersion = " + Integer.toString(oldVersion)
								+ ", newVersion = " + Integer.toString(newVersion)));
					} else {
						Log.w(LOG_TAG, ("On upgrade Database not deleted >> oldVersion = " + Integer.toString(oldVersion)
								+ ", newVersion = " + Integer.toString(newVersion)));
					}
				} catch (SQLException sqle) {
					Log.e(LOG_TAG, ("Delete Database on upgrade >> oldVersion = " + Integer.toString(oldVersion)
							+ ", newVersion = " + Integer.toString(newVersion) + " >> " + sqle.toString()));
					throw sqle;
				}
			} else {
				if (oldVersion == 2) {
					//
				}
				
				if (oldVersion <= 3) {
					//
				}
				
				// etc.
			}*/
		}
		/*
		db.execSQL("DROP TABLE IF EXISTS PHash;");
		db.execSQL("DROP TABLE IF EXISTS Item;");
		db.execSQL("DROP TABLE IF EXISTS Course;");
		db.execSQL("DROP TRIGGER IF EXISTS fk_PHashItem_item_id;");
		db.execSQL("DROP TRIGGER IF EXISTS fk_ItemCourse_course_id;");
		
		onCreate(db);*/
	}
	
	/**
	 * DataBaseHelper DataBaseHolder Class.
	 * 
	 * @author strider
	 */
	private static class DataBaseHolder {
		
		private static final DataBaseHelper INSTANCE = new DataBaseHelper();
		
	}
	
}
