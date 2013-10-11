package com.sunyata.kindmind;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class ListContentProviderM extends ContentProvider {

	private DatabaseHelperM mDatabaseHelper;
	
	private static final int LIST = 1;
	private static final int LIST_ITEM_ID = 2;

	private static final String AUTHORITY = "com.sunyata.kindmind.provider";
	private static final String LIST_BASE_PATH = "list";
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		sUriMatcher.addURI(AUTHORITY, LIST_BASE_PATH, LIST);
		sUriMatcher.addURI(AUTHORITY, LIST_BASE_PATH + "/#", LIST_ITEM_ID);
	}
	
	//public
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LIST_BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/list";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/list_item";
	
	
	@Override
	public boolean onCreate() {
		
		mDatabaseHelper = new DatabaseHelperM(getContext());
		
		return false;
		//-TODO: Change to true when the provider is working. In the tutorial it is false (why?)
		// The documentation says this: "true if the provider was successfully loaded, false otherwise"
	}
	
	
	@Override
	public Cursor query(
			Uri inUri, String[] inProjection, String inSelection, String[] inSelectionArgs, String inSortOrder) {

		verifyColumns(inProjection);

		SQLiteQueryBuilder tmpQueryBuilder = new SQLiteQueryBuilder();
		tmpQueryBuilder.setTables(ListTableM.TABLE_LIST);
		
		switch(sUriMatcher.match(inUri)){
		case LIST:
			break;
		case LIST_ITEM_ID:
			//Adding the column id from the uri to the where SQL statement
			tmpQueryBuilder.appendWhere(ListTableM.COLUMN_ID + "=" + inUri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Error in method query(): Unknown URI: " + inUri);
		}
		
		SQLiteDatabase tmpSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
		//-Please note:
		// 1. We call this here instead of in onCreate since this will speed up the startup
		//  (explained in the android documentation)
		// 2. The database reference is local, but this method can be called again and will then return the
		//  same database if it already has been created
		
		Cursor retCursor = tmpQueryBuilder.query(
				tmpSQLiteDatabase, inProjection, inSelection, inSelectionArgs, null, null, inSortOrder);
		
		retCursor.setNotificationUri(getContext().getContentResolver(), inUri);
		//-Please note that this differs from the update that is done in (for example) the insert method
		/* From the documentation:
		 * Parameters
		 * cr	The content resolver from the caller's context. The listener attached to this resolver will be notified.
		 * uri	The content URI to watch.
		 */
		
		return retCursor;
	}

	@Override
	public String getType(Uri arg0) {
		//TODO change from null. Why null in tutorial?
		return null;
	}
	



	@Override
	public Uri insert(Uri inUri, ContentValues inContentValues) {
		
		SQLiteDatabase tmpSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		long tmpInsertRowId = 0;
		
		switch(sUriMatcher.match(inUri)){
		case LIST:
			tmpInsertRowId = tmpSQLiteDatabase.insert(ListTableM.TABLE_LIST, null, inContentValues);
			break;
		default:
			throw new IllegalArgumentException("Error in method insert(): Unknown URI: " + inUri);
		}
		
		getContext().getContentResolver().notifyChange(inUri, null);
		//-From the documentation: "CursorAdapter objects will get this notification."

		return Uri.parse(LIST_BASE_PATH + "/" + tmpInsertRowId);
	}

	
	
	@Override
	public int delete(Uri inUri, String inSelection, String[] inSelectionArguments) {

		SQLiteDatabase tmpSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		int tmpNumberOfRowsDeleted = 0;
		
		switch(sUriMatcher.match(inUri)){
		case LIST:
			tmpNumberOfRowsDeleted = tmpSQLiteDatabase.delete(
					ListTableM.TABLE_LIST, inSelection, inSelectionArguments);
			break;
		case LIST_ITEM_ID:
			String tmpDeleteIdFromUri = inUri.getLastPathSegment(); //Q: Why a String?
			if(TextUtils.isEmpty(inSelection)){
				tmpNumberOfRowsDeleted = tmpSQLiteDatabase.delete(
						ListTableM.TABLE_LIST, ListTableM.COLUMN_ID + "=" + tmpDeleteIdFromUri, null);
			}else{
				tmpNumberOfRowsDeleted = tmpSQLiteDatabase.delete(
						ListTableM.TABLE_LIST, ListTableM.COLUMN_ID + "=" + tmpDeleteIdFromUri +
						" and " + inSelection, inSelectionArguments);
			}
			break;
		default:
			throw new IllegalArgumentException("Error in method delete(): Unknown URI: " + inUri);
		}
		
		getContext().getContentResolver().notifyChange(inUri, null);
		//-From the documentation: "CursorAdapter objects will get this notification."

		return tmpNumberOfRowsDeleted;
	}



	@Override
	public int update(
			Uri inUri, ContentValues inContentValues, String inSelection, String[] inSelectionArguments) {
		SQLiteDatabase tmpSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		int tmpNumberOfRowsUpdated = 0;
		
		switch(sUriMatcher.match(inUri)){
		case LIST:
			tmpNumberOfRowsUpdated = tmpSQLiteDatabase.update(
					ListTableM.TABLE_LIST, inContentValues, inSelection, inSelectionArguments);
			break;
		case LIST_ITEM_ID:
			String tmpUpdateIdFromUri = inUri.getLastPathSegment();
			if(TextUtils.isEmpty(inSelection)){
				tmpNumberOfRowsUpdated = tmpSQLiteDatabase.update(
						ListTableM.TABLE_LIST, inContentValues, ListTableM.COLUMN_ID + "=" + tmpUpdateIdFromUri,
						null);
			}else{
				tmpNumberOfRowsUpdated = tmpSQLiteDatabase.update(
						ListTableM.TABLE_LIST, inContentValues, ListTableM.COLUMN_ID + "=" + tmpUpdateIdFromUri
						+ " and " + inSelection, inSelectionArguments);
			}
		default:
			throw new IllegalArgumentException("Error in method delete(): Unknown URI: " + inUri);
		}
		
		getContext().getContentResolver().notifyChange(inUri, null);
		//-From the documentation: "CursorAdapter objects will get this notification."

		return tmpNumberOfRowsUpdated;
	}

	
	private void verifyColumns(String[] inProjectedColumnsAsArray){
		
		ArrayList<String> tmpAvailableColumns = new ArrayList<String>();
		tmpAvailableColumns.add(ListTableM.COLUMN_ID);
		tmpAvailableColumns.add(ListTableM.COLUMN_NAME);
		tmpAvailableColumns.add(ListTableM.COLUMN_LISTTYPE);
		tmpAvailableColumns.add(ListTableM.COLUMN_ACTIVE);
		tmpAvailableColumns.add(ListTableM.COLUMN_FILEORDIRPATH);
		tmpAvailableColumns.add(ListTableM.COLUMN_NOTIFICATIONACTIVE);
		tmpAvailableColumns.add(ListTableM.COLUMN_NOTIFICATIONTIME);

		if(inProjectedColumnsAsArray != null){
			ArrayList<String> tmpProjectedColumns = (ArrayList<String>) Arrays.asList(inProjectedColumnsAsArray);
			if(!tmpAvailableColumns.containsAll(tmpProjectedColumns)){
				throw new IllegalArgumentException(
						"Error in method verifyColumns: Projection contains unknown columns");
			}
		}
	}
	
}
