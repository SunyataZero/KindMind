package com.sunyata.kindmind;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelperM extends SQLiteOpenHelper{

	private static final String DATABASE_NAME = "kindmind.db";
	private static final int DATABASE_VERSION = 18;
	
	
	public DatabaseHelperM(Context inContext) {
		super(inContext, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase inDatabase) {
		ItemTableM.onCreate(inDatabase);
		PatternTableM.onCreate(inDatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		ItemTableM.onUpgrade(inDatabase, inOldVersion, inNewVersion);
		PatternTableM.onUpgrade(inDatabase, inOldVersion, inNewVersion);
	}
}
