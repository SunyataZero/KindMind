package com.sunyata.kindmind.Database;

import com.sunyata.kindmind.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelperM extends SQLiteOpenHelper{

	public static final String DATABASE_NAME = "kindmind.db";
	private static final int DATABASE_VERSION = 35;
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
