package com.sunyata.kindmind.Database;

import com.sunyata.kindmind.Utils;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

/*
 * Overview: ExtendedDataTableM
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
public class ExtendedDataTableM {

	public static final String TABLE_EXTENDED_DATA = "extented_data_table"; 
	public static final String COLUMN_ID = BaseColumns._ID;
	public static final String COLUMN_DATA = "data";
	public static final String COLUMN_ITEM_REFERENCE = "item_id";
	
	private static final String CREATE_DATABASE =
			"CREATE TABLE " + TABLE_EXTENDED_DATA + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_DATA + " TEXT NOT NULL, " //Potentially used for two things: Grouping and relevance
			+ COLUMN_ITEM_REFERENCE + " INTEGER REFERENCES " + ItemTableM.TABLE_ITEM + "(" + BaseColumns._ID + ")"
				+ " NOT NULL"
			+ ");";
	
	public static void createTable(SQLiteDatabase inDatabase) {
		inDatabase.execSQL(CREATE_DATABASE);
		Log.i(Utils.getClassName(), "Database version = " + inDatabase.getVersion());
	}

	public static void upgradeTable(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		Log.w(Utils.getClassName(), "Upgrade removed the database with a previous version and created a new one, " +
				"all data was deleted");
		
		inDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXTENDED_DATA);
		createTable(inDatabase);
	}
}
