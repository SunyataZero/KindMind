package com.sunyata.kindmind;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class ItemTableM {

	public static final String TABLE_LIST = "list"; 
	public static final String COLUMN_ID = BaseColumns._ID;
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_LISTTYPE = "listtype";
	public static final String COLUMN_ACTIVE = "active";
	public static final String COLUMN_FILEORDIRPATH = "fileordirpath";
	public static final String COLUMN_NOTIFICATIONACTIVE = "notificationactive";
	public static final String COLUMN_NOTIFICATIONTIME = "notificationtime";
	//Please remember to update the verifyColumns method and the updrade method when we add new columns
	
	
	private static final String CREATE_DATABASE =
			"CREATE TABLE "
			+ TABLE_LIST + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL DEFAULT 'no_name_set', "
			+ COLUMN_LISTTYPE + " TEXT NOT NULL DEFAULT 'SUFFERING', " //TODO: remove default
			+ COLUMN_ACTIVE + " INTEGER NOT NULL DEFAULT -1, "
			+ COLUMN_FILEORDIRPATH + " TEXT NOT NULL DEFAULT -1, "
			+ COLUMN_NOTIFICATIONTIME + " INTEGER NOT NULL DEFAULT -1"
			+ ");";
		//TODO: NOT NULL
	//+ COLUMN_NOTIFICATIONACTIVE + " INTEGER, "
	
	public static void onCreate(SQLiteDatabase inDatabase) {
		//ItemTableM.onCreate(inDatabase);
		
		inDatabase.execSQL(CREATE_DATABASE);
		
		Log.i(Utils.getClassName(), "Database version = " + inDatabase.getVersion());
		//-TODO: Is this for the database or for SQLite?
	}
	
	public static void onUpgrade(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		
		Log.w(Utils.getClassName(), "Upgrade removed the database with a previous version and created a new one, " +
				"all data was deleted");
		
		inDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
		onCreate(inDatabase);
		
		//
		//http://stackoverflow.com/questions/3505900/sqliteopenhelper-onupgrade-confusion-android
		//
		

		/*TODO: Change this method
		 * 1. Backup
		 * and/or
		 * 2. Write upgrade code
		 */
	}
}
