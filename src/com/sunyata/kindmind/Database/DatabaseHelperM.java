package com.sunyata.kindmind.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelperM extends SQLiteOpenHelper{

	private static final String DATABASE_NAME = "kindmind.db";
	private static final int DATABASE_VERSION = 30;
	//-PLEASE BE CAREFUL WHEN UPDATING THIS
	// 1. Check the onUpgrade method and implement the change there
	// 2. Make a backup of the current file (in onUpgrade?)
	
	private static DatabaseHelperM sDatabaseHelper;
	
	//Singelton get method
	public static DatabaseHelperM get(Context inContext){
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
		ItemTableM.onUpgrade(inDatabase, inOldVersion, inNewVersion);
		PatternTableM.onUpgrade(inDatabase, inOldVersion, inNewVersion);
		ExtendedDataTableM.onUpgrade(inDatabase, inOldVersion, inNewVersion);
	}
}
