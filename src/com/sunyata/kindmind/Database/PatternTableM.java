package com.sunyata.kindmind.Database;

import com.sunyata.kindmind.Utils;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class PatternTableM {

	public static final String TABLE_PATTERN = "patterns"; 
	public static final String COLUMN_ID = BaseColumns._ID; //Could maybe remove this and use time as key instead
	public static final String COLUMN_TIME = "time";
	//public static final String COLUMN_RELEVANCE = "relevance";
	public static final String COLUMN_ITEM_REFERENCE = "item_id";
	
	//TODO: Please remember to update the verifyColumns method and the updrade method when we add new columns
	
	private static final String CREATE_DATABASE =
			"CREATE TABLE " + TABLE_PATTERN + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_TIME + " INTEGER NOT NULL, " //Potentially used for two things: Grouping and relevance
			+ COLUMN_ITEM_REFERENCE + " INTEGER REFERENCES " + ItemTableM.TABLE_ITEM + "(" + BaseColumns._ID + ")"
			+ ");";
	//+ COLUMN_RELEVANCE + " INTEGER NOT NULL DEFAULT 0, "
	//TODO: NOT NULL for COLUMN_ITEM_REFERENCE
	
	public static void onCreate(SQLiteDatabase inDatabase) {
		inDatabase.execSQL(CREATE_DATABASE);
		Log.i(Utils.getClassName(), "Database version = " + inDatabase.getVersion());
	}

	public static void onUpgrade(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		
		Log.w(Utils.getClassName(), "Upgrade removed the database with a previous version and created a new one, " +
				"all data was deleted");
		
		inDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PATTERN);
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
