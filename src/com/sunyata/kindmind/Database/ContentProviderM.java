package com.sunyata.kindmind.Database;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.sunyata.kindmind.SortTypeM;
import com.sunyata.kindmind.Utils;

public class ContentProviderM extends ContentProvider {

	private DatabaseHelperM mDatabaseHelper;

	private static final String AUTHORITY = "com.sunyata.kindmind.contentprovider";

	private static final String LIST_BASE_PATH = "list";
	static final int LIST = 11;
	private static final int LIST_ITEM_ID = 12;

	private static final String PATTERN_BASE_PATH = "pattern";
	private static final int PATTERN = 21;

	private static final String EXTENDED_DATA_BASE_PATH = "extended_data";
	private static final int EXTENDED_DATA = 31;

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		sUriMatcher.addURI(AUTHORITY, LIST_BASE_PATH, LIST);
		sUriMatcher.addURI(AUTHORITY, LIST_BASE_PATH + "/#", LIST_ITEM_ID);
		sUriMatcher.addURI(AUTHORITY, PATTERN_BASE_PATH, PATTERN);
		sUriMatcher.addURI(AUTHORITY, EXTENDED_DATA_BASE_PATH, EXTENDED_DATA);
	}
	
	public static final Uri LIST_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LIST_BASE_PATH);
	public static final String LIST_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/list";
	public static final String LIST_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/list_item";
	
	public static final Uri PATTERN_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATTERN_BASE_PATH);
	
	public static final Uri EXTENDED_DATA_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EXTENDED_DATA_BASE_PATH);
	
	public static String sSortType;
	
	//public static final String PATTERN_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/pattern";
	
	/*
	//Please note: Not public, only used inside the application
	static DatabaseHelperM getDatabaseHelper(){
		return mDatabaseHelper;
	}
	*/
	
	@Override
	public boolean onCreate() {
		
		mDatabaseHelper = DatabaseHelperM.get(getContext());
		
		Utils.setSortType(SortTypeM.KINDSORT);
		
		return false;
		//-TODO: Change to true when the provider is working. In the tutorial it is false (why?)
		// The documentation says this: "true if the provider was successfully loaded, false otherwise"
	}
	
	
	@Override
	public Cursor query(
			Uri inUri, String[] inProjection, String inSelection, String[] inSelectionArgs, String inSortOrder) {

		verifyColumns(inUri, inProjection);

		SQLiteQueryBuilder tmpQueryBuilder = new SQLiteQueryBuilder();
		//Moved down: tmpQueryBuilder.setTables(ItemTableM.TABLE_ITEM);
		
		switch(sUriMatcher.match(inUri)){
		case LIST:
			tmpQueryBuilder.setTables(ItemTableM.TABLE_ITEM);
			break;
		case LIST_ITEM_ID:
			tmpQueryBuilder.setTables(ItemTableM.TABLE_ITEM);
			//Adding the column id from the uri to the where SQL statement
			tmpQueryBuilder.appendWhere(ItemTableM.COLUMN_ID + "=" + inUri.getLastPathSegment());
			break;
		case PATTERN:
			tmpQueryBuilder.setTables(PatternTableM.TABLE_PATTERN);
			break;
		case EXTENDED_DATA:
			tmpQueryBuilder.setTables(ExtendedDataTableM.TABLE_EXTENDED_DATA);
			break;
		default:
			throw new IllegalArgumentException("Error in method ListContentProviderM.query(): Unknown URI: " + inUri);
		}
		
		SQLiteDatabase tmpSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
		//-Please note:
		// 1. We call this here instead of in onCreate since this will speed up the startup
		//  (explained in the android documentation)
		// 2. The database reference is local, but this method can be called again and will then return the
		//  same database if it already has been created
		
		Cursor retCursor = tmpQueryBuilder.query(
				tmpSQLiteDatabase, inProjection, inSelection, inSelectionArgs, null, null, inSortOrder);//ItemTableM.COLUMN_NAME
		/*
		 * Why can't we use Utils.sSortType here? according do the debgging we have the expected value in Utils.sSortType,
		 *  but it only works ok when setting the value here.
		 */
		
		retCursor.setNotificationUri(getContext().getContentResolver(), inUri);
		//-Please note that this differs from the update that is done in (for example) the insert method
		/* From the documentation:
		 * Parameters
		 * cr	The content resolver from the caller's context. The listener attached to this resolver will be notified.
		 * uri	The content URI to watch.
		 */
		
		return retCursor;
	}

	/*
	 * Overview: getType returns the type for a given Uri
	 * Improvements: If we want to make data available to other applications through the content provider, we can
	 *  change this method so it returns other values than null, see Reto's book, p 273 for an example
	 * Documentation: 
	 *  http://developer.android.com/reference/android/content/ContentProvider.html#getType%28android.net.Uri%29
	 */
	@Override
	public String getType(Uri arg0) {
		return null;
	}
	
	@Override
	public Uri insert(Uri inUri, ContentValues inContentValues) {
		
		SQLiteDatabase tmpSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		long tmpInsertRowId = 0;
		String uriReturnString = "";
		
		switch(sUriMatcher.match(inUri)){
		case LIST:
			tmpInsertRowId = tmpSQLiteDatabase.insert(ItemTableM.TABLE_ITEM, null, inContentValues);
			uriReturnString = LIST_CONTENT_URI + "/" + tmpInsertRowId;
			break;
		case PATTERN:
			tmpInsertRowId = tmpSQLiteDatabase.insert(PatternTableM.TABLE_PATTERN, null, inContentValues);
			uriReturnString = PATTERN_CONTENT_URI + "/" + tmpInsertRowId;
			break;
		case EXTENDED_DATA:
			tmpInsertRowId = tmpSQLiteDatabase.insert(ExtendedDataTableM.TABLE_EXTENDED_DATA, null, inContentValues);
			uriReturnString = EXTENDED_DATA_CONTENT_URI + "/" + tmpInsertRowId;
			break;
		default:
			throw new IllegalArgumentException(
					"Error in method ListContentProviderM.insert(): Unknown URI: " + inUri);
		}
		
		getContext().getContentResolver().notifyChange(inUri, null);
		//-From the documentation: "CursorAdapter objects will get this notification."

		return Uri.parse(uriReturnString); //LIST_BASE_PATH //CONTENT_URI
	}

	
	@Override
	public int delete(Uri inUri, String inSelection, String[] inSelectionArguments) {

		SQLiteDatabase tmpSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		int tmpNumberOfRowsDeleted = 0;
		
		switch(sUriMatcher.match(inUri)){
		case LIST:
			tmpNumberOfRowsDeleted = tmpSQLiteDatabase.delete(
					ItemTableM.TABLE_ITEM, inSelection, inSelectionArguments);
			break;
		case LIST_ITEM_ID:
			String tmpDeleteIdFromUri = inUri.getLastPathSegment(); //Q: Why a String?
			if(TextUtils.isEmpty(inSelection)){
				tmpNumberOfRowsDeleted = tmpSQLiteDatabase.delete(
						ItemTableM.TABLE_ITEM, ItemTableM.COLUMN_ID + "=" + tmpDeleteIdFromUri, null);
			}else{
				tmpNumberOfRowsDeleted = tmpSQLiteDatabase.delete(
						ItemTableM.TABLE_ITEM, ItemTableM.COLUMN_ID + "=" + tmpDeleteIdFromUri +
						" and " + inSelection, inSelectionArguments);
			}
			break;
		case PATTERN:
			tmpNumberOfRowsDeleted = tmpSQLiteDatabase.delete(
					PatternTableM.TABLE_PATTERN, inSelection, inSelectionArguments);
			break;
		case EXTENDED_DATA:
			tmpNumberOfRowsDeleted = tmpSQLiteDatabase.delete(
					ExtendedDataTableM.TABLE_EXTENDED_DATA, inSelection, inSelectionArguments);
			break;
		default:
			throw new IllegalArgumentException(
					"Error in method ListContentProviderM.delete(): Unknown URI: " + inUri);
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
					ItemTableM.TABLE_ITEM, inContentValues, inSelection, inSelectionArguments);
			break;
		case LIST_ITEM_ID:
			String tmpUpdateIdFromUri = inUri.getLastPathSegment();
			if(TextUtils.isEmpty(inSelection)){
				tmpNumberOfRowsUpdated = tmpSQLiteDatabase.update(
						ItemTableM.TABLE_ITEM, inContentValues, ItemTableM.COLUMN_ID + "=" + tmpUpdateIdFromUri,
						null);
			}else{
				tmpNumberOfRowsUpdated = tmpSQLiteDatabase.update(
						ItemTableM.TABLE_ITEM, inContentValues, ItemTableM.COLUMN_ID + "=" + tmpUpdateIdFromUri
						+ " and " + inSelection, inSelectionArguments);
			}
			break;
		case PATTERN:
			tmpNumberOfRowsUpdated = tmpSQLiteDatabase.update(
					ItemTableM.TABLE_ITEM, inContentValues, inSelection, inSelectionArguments);
			break;
		case EXTENDED_DATA:
			tmpNumberOfRowsUpdated = tmpSQLiteDatabase.update(
					ItemTableM.TABLE_ITEM, inContentValues, inSelection, inSelectionArguments);
			break;
		default:
			throw new IllegalArgumentException(
					"Error in method ListContentProviderM.update(): Unknown URI: " + inUri);
		}
		
		getContext().getContentResolver().notifyChange(inUri, null);
		//-From the documentation: "CursorAdapter objects will get this notification."

		return tmpNumberOfRowsUpdated;
	}

	
	private void verifyColumns(Uri inUri, String[] inProjectedColumnsAsArray){
		
		HashSet<String> tmpAvailableColumns = new HashSet<String>();
		
		switch(sUriMatcher.match(inUri)){
		case LIST:
			//Please note: No break statement, so we continue to the next case
		case LIST_ITEM_ID:
			tmpAvailableColumns.add(ItemTableM.COLUMN_ID);
			tmpAvailableColumns.add(ItemTableM.COLUMN_CREATE_TIME);
			tmpAvailableColumns.add(ItemTableM.COLUMN_MODIFICATION_TIME);
			tmpAvailableColumns.add(ItemTableM.COLUMN_NAME);
			tmpAvailableColumns.add(ItemTableM.COLUMN_LISTTYPE);
			tmpAvailableColumns.add(ItemTableM.COLUMN_ACTIVE);
			tmpAvailableColumns.add(ItemTableM.COLUMN_FILEORDIRPATH);
			tmpAvailableColumns.add(ItemTableM.COLUMN_NOTIFICATION);
			tmpAvailableColumns.add(ItemTableM.COLUMN_KINDSORTVALUE);
			tmpAvailableColumns.add(ItemTableM.COLUMN_DETAILS);
			break;
		case PATTERN:
			tmpAvailableColumns.add(PatternTableM.COLUMN_ID);
			tmpAvailableColumns.add(PatternTableM.COLUMN_CREATE_TIME);
			tmpAvailableColumns.add(PatternTableM.COLUMN_ITEM_REFERENCE);
			break;
		case EXTENDED_DATA:
			tmpAvailableColumns.add(ExtendedDataTableM.COLUMN_ID);
			tmpAvailableColumns.add(ExtendedDataTableM.COLUMN_DATA);
			tmpAvailableColumns.add(ExtendedDataTableM.COLUMN_ITEM_REFERENCE);
			break;
		default:
			throw new IllegalArgumentException("Error in method KindMindContentProviderM.verifyColumns(): Unknown URI: " + inUri);
		}
		
		if(inProjectedColumnsAsArray != null){
			HashSet<String> tmpProjectedColumns = new HashSet<String>(Arrays.asList(inProjectedColumnsAsArray));
			//.class Arrays.asList(inProjectedColumnsAsArray);
			if(!tmpAvailableColumns.containsAll(tmpProjectedColumns)){
				throw new IllegalArgumentException(
						"Error in method verifyColumns: Projection contains unknown columns");
			}
		}
	}	
}