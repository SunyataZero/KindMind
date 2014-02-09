package com.sunyata.kindmind.Database;

import com.sunyata.kindmind.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelperM extends SQLiteOpenHelper{

	public static final String DATABASE_NAME = "kindmind.db";
	private static final int DATABASE_VERSION = 45;
	//-PLEASE BE CAREFUL WHEN UPDATING THIS
	// 1. Check the onUpgrade method and implement the change there
	// 2. Make a backup of the current file (in onUpgrade?)
	
	private static DatabaseHelperM sDatabaseHelper;
	
	private static Context sContext = null; //-Used for file handling in onUpgrade
	
	//Singelton get method
	public static DatabaseHelperM get(Context inContext){
		sContext = inContext;
		if (sDatabaseHelper == null){
			sDatabaseHelper = new DatabaseHelperM(inContext.getApplicationContext());
		}
		return sDatabaseHelper;
	}
	
	private DatabaseHelperM(Context inContext) {
		super(inContext, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase inDatabase) {
		ItemTableM.createTable(inDatabase);
		PatternTableM.createTable(inDatabase);
		ExtendedDataTableM.createTable(inDatabase);
	}

	/*
	 * Overview: onUpgrade backs up the database and then upgrades each of the tables.
	 *  PLEASE NOTE: Currently we drop all tables
	 * Improvements: Here is a discussion of onUpgrade on stackoverflow:
	 *  http://stackoverflow.com/questions/3505900/sqliteopenhelper-onupgrade-confusion-android
	 *  There may be other ideas in "Enterprise Android"
	 */
	@Override
	public void onUpgrade(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		//Making a backup of the previous version of the database file
		Utils.databaseBackupInternal(sContext, DATABASE_NAME, inOldVersion);
		
		//Upgrading for all the tables
		ItemTableM.upgradeTable(inDatabase, inOldVersion, inNewVersion);
		PatternTableM.upgradeTable(inDatabase, inOldVersion, inNewVersion);
		ExtendedDataTableM.upgradeTable(inDatabase, inOldVersion, inNewVersion);
	}
}
