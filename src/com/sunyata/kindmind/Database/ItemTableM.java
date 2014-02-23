package com.sunyata.kindmind.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.sunyata.kindmind.Utils;

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

	
	//-------------------Other constants
	
	public static final int FALSE = -1; //-All other values means TRUE
	public static final String NO_NAME = "";

	
	//-------------------Create table constant
	
	private static final String CREATE_DATABASE =
			"CREATE TABLE " + TABLE_ITEM + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL DEFAULT '" + NO_NAME + "', "
			+ COLUMN_DETAILS + " TEXT NOT NULL DEFAULT '" + NO_NAME + "', "
			+ COLUMN_LIST_TYPE + " INTEGER NOT NULL, "
			+ COLUMN_ACTIVE + " INTEGER NOT NULL DEFAULT " + String.valueOf(FALSE) + ", "
			+ COLUMN_ACTIONS + " TEXT NOT NULL DEFAULT '" + NO_NAME + "', "
			+ COLUMN_NOTIFICATION + " INTEGER NOT NULL DEFAULT " + String.valueOf(FALSE) + ", "
			+ COLUMN_KINDSORT_VALUE + " REAL NOT NULL DEFAULT 0"
			+ ");";
	
	/*
			+ COLUMN_CREATE_TIME + " INTEGER NOT NULL DEFAULT 0, "
			+ COLUMN_MODIFICATION_TIME + " INTEGER NOT NULL DEFAULT 0, "
	 */
	
	
	//-------------------Lifecycle methods
	public static void createTable(SQLiteDatabase inDatabase) {
		inDatabase.execSQL(CREATE_DATABASE);
		Log.i(Utils.getClassName(), "Database version = " + inDatabase.getVersion());
	}
	
	

	public static void upgradeTable(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		//Upgrading the database by changing the action separator character from " " to ";"
		if(inOldVersion == 46 && inNewVersion == 47){
			Cursor tmpItemCursor = inDatabase.query(ItemTableM.TABLE_ITEM, null, null, null, null, null, null);
			if(tmpItemCursor.getCount() == 0){
				tmpItemCursor.close();
				return;
			}
			final String OLD_SEPARATOR = " ";
			for(tmpItemCursor.moveToFirst(); tmpItemCursor.isAfterLast() == false; tmpItemCursor.moveToNext()){
				String tmpActions = tmpItemCursor.getString(tmpItemCursor.getColumnIndexOrThrow(COLUMN_ACTIONS));
				String tmpId = tmpItemCursor.getString(tmpItemCursor.getColumnIndexOrThrow(COLUMN_ID));
				tmpActions = tmpActions.replace(OLD_SEPARATOR, Utils.ACTIONS_SEPARATOR);
				ContentValues tmpContentValues = new ContentValues();
				tmpContentValues.put(ItemTableM.COLUMN_ACTIONS, tmpActions);
				inDatabase.update(ItemTableM.TABLE_ITEM, tmpContentValues, COLUMN_ID + "=" + tmpId, null);
			}
			tmpItemCursor.close();
			
		}else{
			Log.w(Utils.getClassName(), "Upgrade removed the database with a previous version and created a new one, " +
					"all data was deleted");
			inDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
			createTable(inDatabase);
		}
	}
}
