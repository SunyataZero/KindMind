package com.sunyata.kindmind.util;

import java.io.File;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sunyata.kindmind.SortTypeM;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.ListTypeM;

public class DatabaseU {
	
	//public static final String PREF_IS_FIRST_TIME_APP_STARTED = "IsFirstTimeApplicationStarted";

	
	
	/**
	 *
	 * We can use the following lines for safely using a cursor:
\code{.java}

String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};
Cursor tCr = DatabaseU.getCursor(inContext, inItemUri, tmpProjection, null);

String temporaryValue = "";
try{
	temporaryValue = tCr.getString(tItemCr.getColumnIndexOrThrow(
			TableM.COLUMN));
}catch(Exception e){Log.wtf(DbgU.getAppTag(), DbgU.getMethodName());
}finally{tCr.close();}

if(temporaryValue.equals("")){handle error}

\endcode
	 * @param iCt
	 * @param iUri
	 * @param iProj
	 * @param iSel
	 * @return
	 */
	/*
	public static Cursor getCursor(Context iCt, Uri iUri, String[] iProj, String iSel,
			String[] iSelArgs, String iSortOrder, boolean iNeedsToContainOneOrMore){
		Cursor rCr = iCt.getContentResolver().query(iUri, iProj, iSel, null, null);
		
		//Verifying the cursor and moving to the first position
		if(rCr != null){
			if(iNeedsToContainOneOrMore){
				if(rCr.getCount() > 0 && rCr.moveToFirst()){
					return rCr;
				}else{
					Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Setup of Cursor failed"
							+ " when iNeedsToContainOneOrMore was set to true");
					return null;
				}
			}else{
				rCr.moveToFirst();
				return rCr;
			}
		}else{
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Setup of Cursor failed");
			return null;
		}
		
		// && rCr.getCount() > 0 && rCr.moveToFirst()
		
	}
	*/
	/*
	 * We can use the following code
	 */
	
	
	
	
	
	public static boolean sqlToBoolean(Cursor inCursor, String inColumn){
		long tmpItemIsActiveInteger = inCursor.getLong(inCursor.getColumnIndexOrThrow(inColumn));
		if(tmpItemIsActiveInteger == ItemTableM.FALSE){
			return false;
		}else{
			return true;
		}
	}
	
	//Cmp with method getListOfNamesForActivatedData
	public static int getActiveListItemCount(Context inContext, int inListTypeInt){
		int retCount = DbgU.NO_VALUE_SET;
		
		String tSel =
				ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE + " AND " +
				ItemTableM.COLUMN_LIST_TYPE + "=" + inListTypeInt;
		Cursor tmpCursor = inContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tSel, null, ContentProviderM.sSortType);
		try{
			if(tmpCursor != null){
				
				retCount = tmpCursor.getCount();
				
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor is null",
						new Exception());
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), e);
		}finally{
			if(tmpCursor != null){
				tmpCursor.close();
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), new Exception());
			}
		}
		if(retCount == DbgU.NO_VALUE_SET){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), new Exception());
		}
		
		return retCount;
	}
	
	public static Long getIdFromUri(Uri inUri){
		return Long.parseLong(inUri.toString().substring(inUri.toString().lastIndexOf("/") + 1));
	}
	public static Uri getItemUriFromId(long inId){
		return Uri.withAppendedPath(ContentProviderM.ITEM_CONTENT_URI, String.valueOf(inId));
	}
	/*
	 * Overview: databaseBackupInternal does a backup of the database file to internal storage
	 * Details: The name of the backup file includes version and date/time
	 * Used in: DatabaseHelperM.onUpgrade()
	 * Uses app internal: Utils.copyFile()
	 */
	public static void databaseBackupInternal(Context inContext, String inDataBaseName, int inOldVersion){
		Log.d(DbgU.getAppTag(),"Database backup");

		//Construction of the dir path and file name for the backup file
		String tmpDestinationPath = inContext.getDir("db_backup", Context.MODE_PRIVATE).toString();
		Calendar tmpCal = Calendar.getInstance();
		String tmpTimeString = "-"
				+ tmpCal.get(Calendar.YEAR) + "-"
				+ tmpCal.get(Calendar.MONTH) + "-"
				+ tmpCal.get(Calendar.DAY_OF_MONTH) + "-"
				+ tmpCal.get(Calendar.HOUR_OF_DAY) + "-"
				+ tmpCal.get(Calendar.MINUTE) + "-"
				+ tmpCal.get(Calendar.SECOND);
		String tmpVersionString = "-DatabaseVer" + inOldVersion;
		
		//Creating the new dir and file and getting reference to the existing database file
		File tmpSourceFile = inContext.getDatabasePath(inDataBaseName);
		File tmpDestinationFile = new File(tmpDestinationPath,
				"kindmind-" + tmpVersionString + tmpTimeString + ".db");
		//-tmpDestinationPath will be created internally but automatically gets an "app_" prefix.
		// Please note that standard directories (like /databases) are not be available for security reasons

		//Copying the file
		FileU.copyFile(tmpSourceFile, tmpDestinationFile);
	}
	
	public static Context getContentProviderContext(Context inOtherContext) {
		Context retContext = null;
		String tmpPackageName = "com.sunyata.kindmind";
		try {
			retContext = inOtherContext.createPackageContext(tmpPackageName, Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName()
					+ "Package name " + tmpPackageName + " not found");
			e.printStackTrace();
		}
		return retContext;
	}
	
	
	public static void setItemTableSortType(SortTypeM inSortType) {
		switch(inSortType){
		case ALPHABETASORT:
			ContentProviderM.sSortType = ItemTableM.COLUMN_NAME + " ASC";
			break;
		case KINDSORT:
			ContentProviderM.sSortType = ItemTableM.COLUMN_ACTIVE + " DESC" + ", "
					+ ItemTableM.COLUMN_KINDSORT_VALUE + " DESC";
			break;
		default:
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Case not covered");
			break;
		}
	}
	
	///@name Adding new items
	///@{
	
	/*
	public static boolean isFirstTimeApplicationStarted(Context inContext){
		boolean retVal = PreferenceManager.getDefaultSharedPreferences(inContext).getBoolean(
				PREF_IS_FIRST_TIME_APP_STARTED, true); //Default is true (if no value has been written)
		return retVal;
	}
	*/

	public static void createAllStartupItems(Context inContext) {
		Log.i(DbgU.getAppTag(), "Creating startup items");
		
		createStartupItem(inContext, ListTypeM.FEELINGS, "Angry");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Anxious");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Concerned");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Depressed");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Dissapointed");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Embarrassed");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Frustrated");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Guilty");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Hurt");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Overwhelmed");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Resentful");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Sad");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Tired");
		createStartupItem(inContext, ListTypeM.FEELINGS, "Uncomfortable");
		//Vulnarable
		//Suspicious
		//Shameful

		createStartupItem(inContext, ListTypeM.NEEDS, "Acceptance");
		createStartupItem(inContext, ListTypeM.NEEDS, "Appreciation");
		createStartupItem(inContext, ListTypeM.NEEDS, "Authenticity");
		createStartupItem(inContext, ListTypeM.NEEDS, "Connection");
		createStartupItem(inContext, ListTypeM.NEEDS, "Consideration");
		createStartupItem(inContext, ListTypeM.NEEDS, "Contribution");
		createStartupItem(inContext, ListTypeM.NEEDS, "Creativity");
		createStartupItem(inContext, ListTypeM.NEEDS, "Emotional safety");
		createStartupItem(inContext, ListTypeM.NEEDS, "Empathy");
		createStartupItem(inContext, ListTypeM.NEEDS, "Freedom");
		createStartupItem(inContext, ListTypeM.NEEDS, "Fun");
		createStartupItem(inContext, ListTypeM.NEEDS, "Inspiration");
		createStartupItem(inContext, ListTypeM.NEEDS, "Love");
		createStartupItem(inContext, ListTypeM.NEEDS, "Mourning");
		createStartupItem(inContext, ListTypeM.NEEDS, "Physical comfort");
		createStartupItem(inContext, ListTypeM.NEEDS, "Rest");
		createStartupItem(inContext, ListTypeM.NEEDS, "Support");
		createStartupItem(inContext, ListTypeM.NEEDS, "Trust");
		createStartupItem(inContext, ListTypeM.NEEDS, "Understanding");
		//Self-worth, to matter
		//Community

		createStartupItem(inContext, ListTypeM.KINDNESS, "Awareness of a feeling in the body");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Calling a friend");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Enumerating good things recently");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Finding a new way to do something");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Focusing on a need");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Following the breath");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Looking at a plant or a tree nearby");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Seeing alternative ways to respond");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Seeing the bigger perspective");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Stretching");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Thinking about a time when you helped someone");
		createStartupItem(inContext, ListTypeM.KINDNESS, "Thinking about someone who shares your experience");

		/*
		PreferenceManager.getDefaultSharedPreferences(inContext)
		.edit()
		.putBoolean(PREF_IS_FIRST_TIME_APP_STARTED, false)
		.commit();
		*/
	}
	private static void createStartupItem(Context iContext, int iListTypeInt, String iName){
		ContentValues tContentValsToIns = new ContentValues();

		//Using the name to see if the item has already been added
		String tName = "";
		Cursor tItemCr = iContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, null, null, null);
		try{
			if(tItemCr != null && tItemCr.moveToFirst()){

				for(tItemCr.moveToFirst(); tItemCr.isAfterLast() == false; tItemCr.moveToNext()){
					tName = tItemCr.getString(tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
					if(iName.compareTo(tName) == 0){
						//item with same name already exists, exiting without adding item
						return;
					}
				}

			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor null or empty", new Exception());
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Exception when using cursor", e);
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}else{
				Log.w(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor null when trying to close");
			}
		}

		tContentValsToIns.put(ItemTableM.COLUMN_LIST_TYPE, iListTypeInt);
		tContentValsToIns.put(ItemTableM.COLUMN_NAME, iName);
		iContext.getContentResolver().insert(ContentProviderM.ITEM_CONTENT_URI, tContentValsToIns);
	}
	
	///@}
	
}