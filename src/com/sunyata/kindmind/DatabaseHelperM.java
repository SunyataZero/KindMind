package com.sunyata.kindmind;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelperM extends SQLiteOpenHelper{

	private static final String DATABASE_NAME = "kindmind.db";
	private static final int DATABASE_VERSION = 1;
	

	
	
	
	public DatabaseHelperM(Context inContext) {
		super(inContext, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase inDatabase) {
		ListTableM.onCreate(inDatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase inDataBase, int inOldVersion, int inNewVersion) {
		ListTableM.onUpgrade(inDataBase, inOldVersion, inNewVersion);
	}
	
	
	
}
