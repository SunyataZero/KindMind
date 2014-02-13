package com.sunyata.kindmind.Database;

import com.sunyata.kindmind.Utils;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

/*
 * Overview: ItemTableM
 * 
 * Details: 
 * 
 * Extends: 
 * 
 * Implements: 
 * 
 * Sections:
 * 
 * Used in: 
 * 
 * Uses app internal: 
 * 
 * Uses Android lib: 
 * 
 * In: 
 * 
 * Out: 
 * 
 * Does: 
 * 
 * Shows user: 
 * 
 * Notes: Please remember to update the verifyColumns method and the upgrade method when we add new columns
 * 
 * Improvements: 
 * 
 * Documentation: 
 * 
 */
public class ItemTableM {

	//-------------------Column constants
	
	public static final String TABLE_ITEM = "item"; 
	public static final String COLUMN_ID = BaseColumns._ID;
	public static final String COLUMN_CREATE_TIME = "create_time"; //-unused
	public static final String COLUMN_MODIFICATION_TIME = "modification_time"; //-unused
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DETAILS = "details"; //PLEASE NOTE: Unused
	public static final String COLUMN_LIST_TYPE = "list_type";
	public static final String COLUMN_ACTIVE = "active";
	public static final String COLUMN_ACTIONS = "actions"; //-list of actions, each ends with the pipe ('|') character
	public static final String COLUMN_NOTIFICATION = "notification";
	//-Contains both active or not, and the time, not active is stored as -1
	public static final String COLUMN_KINDSORT_VALUE = "kindsort_value";
	//-Alternative: Not storing this value here, but instead locally
	
	
	//-------------------Create table constant
	
	private static final String CREATE_DATABASE =
			"CREATE TABLE " + TABLE_ITEM + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_CREATE_TIME + " INTEGER NOT NULL DEFAULT 0, "
			+ COLUMN_MODIFICATION_TIME + " INTEGER NOT NULL DEFAULT 0, "
			+ COLUMN_NAME + " TEXT NOT NULL DEFAULT 'no_name_set', "
			+ COLUMN_DETAILS + " TEXT NOT NULL DEFAULT '-', "
			+ COLUMN_LIST_TYPE + " TEXT NOT NULL DEFAULT 'no_listtype_set', "
			+ COLUMN_ACTIVE + " INTEGER NOT NULL DEFAULT -1, "
			+ COLUMN_ACTIONS + " TEXT NOT NULL DEFAULT '', "
			+ COLUMN_NOTIFICATION + " INTEGER NOT NULL DEFAULT -1, "
			+ COLUMN_KINDSORT_VALUE + " REAL NOT NULL DEFAULT 0"
			+ ");";
	
	
	//-------------------Other constants
	
	public static final int FALSE = -1; //-All other values means TRUE
	
	
	//-------------------Lifecycle methods
	public static void createTable(SQLiteDatabase inDatabase) {
		inDatabase.execSQL(CREATE_DATABASE);
		Log.i(Utils.getClassName(), "Database version = " + inDatabase.getVersion());
	}
	
	public static void upgradeTable(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		Log.w(Utils.getClassName(), "Upgrade removed the database with a previous version and created a new one, " +
				"all data was deleted");
		
		inDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
		createTable(inDatabase);
	}
}
