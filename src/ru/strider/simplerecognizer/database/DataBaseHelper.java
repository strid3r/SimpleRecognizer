/*
 * Copyright (C) 2012-2013 strider
 * 
 * Simple Recognizer
 * SQLiteOpenHelper DataBaseHelper Class
 * By © strider 2012-2013.
 */

package ru.strider.simplerecognizer.database;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.strider.simplerecognizer.R;
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
	private static final String PACKAGE_DB_PATH = "databases" + File.separator;
	
	private static String DB_NAME_SRC = null;
	
	private static String DB_NAME = null;
	private static String DB_PATH = null;
	
	private Context mContext = null;
	
	private SQLiteDatabase mDataBase = null; 
	
	private int mDataBaseSize = -1;
	
	/**
	 * Takes and keeps a reference of the passed context in order
	 * to access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DataBaseHelper(Context context, String dbNameSrc, String dbName, int dbVersion) {
		super(context, dbName, null, dbVersion);
		
		mContext = context;
		
		DB_NAME_SRC = dbNameSrc;
		
		DB_NAME = dbName;
		DB_PATH = mContext.getApplicationInfo().dataDir + File.separator + PACKAGE_DB_PATH;
		
		AssetManager assetManager = mContext.getAssets();
		
		InputStream is = null;
		
		try {
			is = assetManager.open(ASSETS_DB_PATH + DB_NAME_SRC);
			
			mDataBaseSize = is.available();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error Loading DataBase From Assets >> Assets/" + ASSETS_DB_PATH + DB_NAME_SRC);
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
	}
	
	/**
	 * Creates an empty database on the system and rewrites it with own database.
	 * 
	 * @throws IOException
	 */
	public void createDataBase(final Activity activity) throws IOException {
		SQLiteDatabase db = null;
		
		boolean isExists = checkDataBase();
		if (isExists) {
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
		
		isExists = checkDataBase();
		if (isExists && !checkDataBaseSize()) {
			deleteDataBase();
		}
		
		isExists = checkDataBase();
		
		db = this.getReadableDatabase();
		
		if (!isExists) {
			if (db != null) {
				db.close();
			}
			
			try {
				copyDataBase();
				
				Log.i(LOG_TAG, "Database copied");
			} catch (IOException e) {
				Log.e(LOG_TAG, "Database not copied");
				Log.w(LOG_TAG, "Error copying Database >> " + e.toString());
				//throw new Error("ErrorCopyingDataBase");
			}
			
			isExists = checkDataBase();
			if (!isExists || (isExists && !checkDataBaseSize())) {
				activity.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Resources res = mContext.getResources();
							
							StringBuilder sb = new StringBuilder();
							sb.append(res.getString(R.string.database_create_error_tip_1))
									.append(Text.LF);
							sb.append(res.getString(R.string.database_create_error_tip_1_help))
									.append(Text.LF).append(Text.LF);
							sb.append(res.getString(R.string.database_create_error_tip_2));
							
							AlertDialog.Builder builder = new AlertDialog.Builder((Context) activity);
							builder.setTitle(R.string.database_create_error_title);
							builder.setMessage(sb.toString());
							builder.setCancelable(false);
							builder.setPositiveButton(R.string.dialog_button_close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Log.i(LOG_TAG, "Exiting Application...");
									
									activity.finish();
									
									Process.killProcess(Process.myPid());
								}
								
							});
							
							AlertDialog alert = builder.create();
							alert.setCanceledOnTouchOutside(false);
							alert.show();
						}
						
					});
				
				synchronized (activity) {
					while (true) {
						try {
							activity.wait();
						} catch (InterruptedException e) {
							Log.w(LOG_TAG, "InterruptedException >> " + e.toString());
						}
					}
				}
			}
		} else {
			SimpleRecognizer.logIfDebug(Log.INFO, LOG_TAG, "Database already exists"
					+ " >> dbVersion = " + Integer.toString(db.getVersion()));
			
			db.close();
		}
	}
	
	/**
	 * Check if the database already exist to avoid re-copying the file
	 * each time application opened.
	 * 
	 * @return true if it exists, false otherwise.
	 */
	private boolean checkDataBase() {
		boolean isExists = false;
		
		SQLiteDatabase db = null;
		
		File dbFile = new File(DB_PATH + DB_NAME);
		
		if (dbFile.exists()) {
			try {
				db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
			} catch (SQLException sqle) {
				//
			} finally {
				if (db != null) {
					isExists = true;
					
					db.close();
				}
			}
		}
		
		SimpleRecognizer.logIfDebug(Log.VERBOSE, "dbFile", dbFile + Text.SEPARATOR + isExists);
		
		return isExists;
	}
	
	/**
	 * Check if the database file size equals to assets file to avoid
	 * opening corrupted file.
	 * 
	 * @return true if it equals, false otherwise.
	 */
	private boolean checkDataBaseSize() {
		boolean isEquals = false;
		
		File dbFile = new File(DB_PATH + DB_NAME);
		
		if (dbFile.exists() && (dbFile.length() != mDataBaseSize)) {
			Log.w("dbFile", dbFile + Text.SEPARATOR + "Bad File Size");
		} else {
			isEquals = true;
		}
		
		return isEquals;
	}
	
	/**
	 * Copies database from local assets-folder to the just created empty
	 * database in the system folder, from where it can be accessed and
	 * handled. This is done by transferring stream of bytes.
	 */
	private void copyDataBase() throws IOException {
		AssetManager assetManager = mContext.getAssets();
		
		File dbDir = new File(DB_PATH);
		
		if (!dbDir.exists()) {
			dbDir.mkdirs();
		}
		
		InputStream is = assetManager.open(ASSETS_DB_PATH + DB_NAME_SRC);
		OutputStream os = new FileOutputStream(DB_PATH + DB_NAME);
		
		byte[] buffer = new byte[1024];
		int length = -1;
		
		while ((length = is.read(buffer)) != -1) {
			os.write(buffer, 0, length);
		}
		
		os.flush();
		
		os.close();
		is.close();
	}
	
	/**
	 * Opens database.
	 */
	public boolean openDataBase() throws SQLException {
		mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
		
		SimpleRecognizer.logIfDebug(Log.VERBOSE, "dbFullPath", DB_PATH + DB_NAME);
		
		return (mDataBase != null);
	}
	
	public boolean deleteDataBase() throws SQLException {
		return mContext.deleteDatabase(DB_NAME);
	}
	
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
			if (oldVersion == 1) {
				// 1 —› X
				try {
					boolean isDeleted = deleteDataBase();
					
					if (isDeleted) {
						Log.i(LOG_TAG, "On upgrade Database deleted >> oldVersion = " + Integer.toString(oldVersion)
								+ ", newVersion = " + Integer.toString(newVersion));
					} else {
						Log.w(LOG_TAG, "On upgrade Database not deleted >> oldVersion = " + Integer.toString(oldVersion)
								+ ", newVersion = " + Integer.toString(newVersion));
					}
				} catch (SQLException sqle) {
					Log.e(LOG_TAG, "Delete Database on upgrade >> oldVersion = " + Integer.toString(oldVersion)
							+ ", newVersion = " + Integer.toString(newVersion) + " >> " + sqle.toString());
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
			}
		}
		/*
		db.execSQL("DROP TABLE IF EXISTS PHash;");
		db.execSQL("DROP TABLE IF EXISTS Item;");
		db.execSQL("DROP TABLE IF EXISTS Course;");
		db.execSQL("DROP TRIGGER IF EXISTS fk_PHashItem_item_id;");
		db.execSQL("DROP TRIGGER IF EXISTS fk_ItemCourse_course_id;");
		
		onCreate(db);*/
	}
	
}
