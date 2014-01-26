package com.sunyata.kindmind.Database;

import com.sunyata.kindmind.Utils;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class ItemTableM {

	//-------------------Column constants
	public static final String TABLE_ITEM = "item"; 
	public static final String COLUMN_ID = BaseColumns._ID;
	public static final String COLUMN_CREATE_TIME = "create_time";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_LISTTYPE = "listtype";
	public static final String COLUMN_ACTIVE = "active";
	public static final String COLUMN_FILEORDIRPATH = "fileordirpath";
	public static final String COLUMN_NOTIFICATION = "notification";
	//-Contains both active or not, and the time, not active is stored as -1
	public static final String COLUMN_KINDSORTVALUE = "kindsortvalue";
	//-Alternative: Not storing this value here, but instead locally
	public static final String COLUMN_TAGS = "tags"; //PLEASE NOTE: Unused

	//TODO: Please remember to update the verifyColumns method and the updrade method when we add new columns


	//-------------------Create table constant
	private static final String CREATE_DATABASE =
			"CREATE TABLE " + TABLE_ITEM + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_CREATE_TIME + " INTEGER NOT NULL DEFAULT 0, "
			+ COLUMN_NAME + " TEXT NOT NULL DEFAULT 'no_name_set', "
			+ COLUMN_LISTTYPE + " TEXT NOT NULL DEFAULT 'SUFFERING', " //TODO: remove default
			+ COLUMN_ACTIVE + " INTEGER NOT NULL DEFAULT -1, "
			+ COLUMN_FILEORDIRPATH + " TEXT NOT NULL DEFAULT -1, "
			+ COLUMN_NOTIFICATION + " INTEGER NOT NULL DEFAULT -1, "
			+ COLUMN_KINDSORTVALUE + " REAL NOT NULL DEFAULT 0, "
			+ COLUMN_TAGS + " TEXT NOT NULL DEFAULT '-----------'"
			+ ");";
	//+ COLUMN_KINDSORTVALUE + " REAL NOT NULL DEFAULT 0"
	//TODO: NOT NULL
	//+ COLUMN_NOTIFICATIONACTIVE + " INTEGER, "
	
	
	
	//-------------------Other constants
	public static final int FALSE = -1; //-All other values means TRUE
	
	
	
	//-------------------Lifecycle methods
	public static void onCreate(SQLiteDatabase inDatabase) {
		inDatabase.execSQL(CREATE_DATABASE);
		Log.i(Utils.getClassName(), "Database version = " + inDatabase.getVersion());
	}
	
	public static void onUpgrade(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		
		Log.w(Utils.getClassName(), "Upgrade removed the database with a previous version and created a new one, " +
				"all data was deleted");
		
		inDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
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
