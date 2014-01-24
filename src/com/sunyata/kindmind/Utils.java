package com.sunyata.kindmind;

import java.math.BigDecimal;

import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.KindMindContentProviderM;
import com.sunyata.kindmind.List.ListTypeM;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

public class Utils {

	//TODO: Log
	
	public static final String LIST_TYPE = "LIST_TYPE";
	
	public static final String PREF_IS_FIRST_TIME_APP_STARTED = "IsFirstTimeApplicationStarted";
	
	
	
	
	//--------------------(Static) methods for debugging
	
	public static String getMethodName(String inPrefix){
		return "[" + inPrefix + "]" + getMethodName();
	}
	public static String getMethodName(ListTypeM inListType){
		if(inListType != null){
			return Thread.currentThread().getStackTrace()[3].getMethodName() + "[" + inListType.toString() + "]";
		}else{
			return Thread.currentThread().getStackTrace()[3].getMethodName() + "[N/A]";
		}
	}
	public static String getMethodName(){
		return Thread.currentThread().getStackTrace()[3].getMethodName();
	}
	public static String getClassName(){
		String tmpClassWithPackage = Thread.currentThread().getStackTrace()[3].getClassName();
		String[] tmpSplitString = tmpClassWithPackage.split("\\."); //NOTE: Regular experssion so "." means "all"
		//String tmpOrganization = tmpSplitString[tmpSplitString.length-3];
		//String tmpProject = tmpSplitString[tmpSplitString.length-2];
		String tmpComponent = tmpSplitString[tmpSplitString.length-1];
		return tmpComponent;
	}
	
	
	//--------------------Adding new items to the lists
	
	public static boolean isFirstTimeApplicationStarted(Context inContext){
		boolean retVal = PreferenceManager.getDefaultSharedPreferences(inContext).getBoolean(
				PREF_IS_FIRST_TIME_APP_STARTED, true); //Default is true (if no value has been written)
		return retVal;
	}
	public static void createAllStartupItems(Context inContext) {
		// TODO Auto-generated method stub
		
    	createStartupItem(inContext, ListTypeM.FEELINGS, "Sad");
    	createStartupItem(inContext, ListTypeM.FEELINGS, "Angry");
    	createStartupItem(inContext, ListTypeM.FEELINGS, "Nervous");
    	createStartupItem(inContext, ListTypeM.FEELINGS, "Tired");
    	createStartupItem(inContext, ListTypeM.FEELINGS, "Dissapointed");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Rest");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Connection");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Peace");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Love");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Sustinence");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Air");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Safety");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Trust");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Understanding");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Freedom");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Creativity");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Contribution");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Fun");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Movement");
    	createStartupItem(inContext, ListTypeM.ACTIONS, "Following the breath");
		
    	PreferenceManager.getDefaultSharedPreferences(inContext)
    			.edit()
    			.putBoolean(PREF_IS_FIRST_TIME_APP_STARTED, false)
    			.commit();
	}
	private static void createStartupItem(Context inContext, ListTypeM inListType, String inColumnName){
		ContentValues tmpContentValuesToInsert = new ContentValues();
    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_LISTTYPE, inListType.toString());
    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_NAME, inColumnName);
    	inContext.getContentResolver().insert(KindMindContentProviderM.LIST_CONTENT_URI, tmpContentValuesToInsert);
		Log.i(Utils.getClassName(),
				"Added " + inColumnName + " with type " + inListType.toString() + " to the database");
	}
	
	
	//--------------------Other
	
	public static String formatNumber(double inValue) {
		BigDecimal tmpBigDecimal = new BigDecimal(inValue);
		tmpBigDecimal = tmpBigDecimal.setScale(2, BigDecimal.ROUND_UP);
		return "" + tmpBigDecimal;
	}

	public static String getKindMindDirectory(){
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/KindMind";
		//return Environment.getRootDirectory().getAbsolutePath() + "/KindMind";
	}
	
	
	
	public static String getFilePathFromIntent(Context inContext, Intent inIntent){
		Uri tmpUri = inIntent.getData();
		String retFilePath = "";
		Cursor tmpCursor = null;
		try{
			tmpCursor = inContext.getContentResolver().query(tmpUri, null, null, null, null);
			//-Please note that the sorttype is set to null here because this method will be
			// used for other content providers than our own
			tmpCursor.moveToFirst();
			retFilePath = tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
		}finally{
			//tmpCursor.close();
		}
		return retFilePath;
	}
	
/*
	static boolean sqlToBoolean(Context inContext, Uri inItemUri, String inColumn, int inFalseAsInt)
			throws Exception{
		Cursor tmpCursor = inContext.getContentResolver().query(inItemUri, null, null, null, Utils.sSortType);
		if(tmpCursor.getCount() == 0){
			tmpCursor.close();
			throw new Exception("Error in Utils.sqlToBoolean: Cursor empty");
		}
		tmpCursor.moveToFirst();
		
		boolean retItemNotificationIsActive = true;
		long tmpItemTimeInMilliSeconds = tmpCursor.getLong(
				tmpCursor.getColumnIndexOrThrow(inColumn));
		if(tmpItemTimeInMilliSeconds == inFalseAsInt ){
			retItemNotificationIsActive = false;
		}
		
		tmpCursor.close();
		return retItemNotificationIsActive;
	}
	*/
	
	public static boolean sqlToBoolean(Cursor inCursor, String inColumn){
		long tmpItemIsActiveInteger = inCursor.getLong(inCursor.getColumnIndexOrThrow(inColumn));
		if(tmpItemIsActiveInteger == ItemTableM.FALSE){
			return false;
		}else{
			return true;
		}
	}
	
	public static int getListItemCount(Context inContext, ListTypeM inListType){
		int retCount;
		String tmpSelection = ItemTableM.COLUMN_LISTTYPE + " = ?";
		String[] tmpSelectionArguments = {inListType.toString()};
		Cursor tmpCursor = inContext.getContentResolver().query(
				KindMindContentProviderM.LIST_CONTENT_URI, null, tmpSelection, tmpSelectionArguments, KindMindContentProviderM.sSortType);
		retCount = tmpCursor.getCount();
		tmpCursor.close();
		/* -PLEASE NOTE: This cursor has to be closed (why this and not others?) otherwise we will
		 *  get four of the following warning log messages:
		 * 01-21 21:08:32.975: W/CursorWrapperInner(7757): Cursor finalized without prior close()
		 *  According to Diane Hackborn:
		 *  "A content provider is created when its hosting process is created, and remains around for as long
		 *  as the process does, so there is no need to close the database -- it will get closed as part of the
		 *  kernel cleaning up the process's resources when the process is killed."
		 *  http://stackoverflow.com/questions/4547461/closing-the-database-in-a-contentprovider
		 *  We have also tried leaving other cursors open and have seen no problems there 
		 */
		return retCount;
	}
	
	//Cmp with method getListOfNamesForActivatedData
	public static int getActiveListItemCount(Context inContext, ListTypeM inListType){
		int retCount;
		String tmpSelection =
				ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE + " AND " +
				ItemTableM.COLUMN_LISTTYPE + "=" + "'" + inListType.toString() + "'";
		Cursor tmpCursor = inContext.getContentResolver().query(
				KindMindContentProviderM.LIST_CONTENT_URI, null, tmpSelection, null, KindMindContentProviderM.sSortType);
		retCount = tmpCursor.getCount();
		tmpCursor.close();
		//-PLEASE NOTE: This cursor has to be closed (see comments in method getListItemCount)
		return retCount;
	}
}
