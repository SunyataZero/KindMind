package com.sunyata.kindmind;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.ListTypeM;
import com.sunyata.kindmind.WidgetAndNotifications.WidgetProviderC;

public class Utils {

	public static final String LIST_TYPE = "LIST_TYPE";
	public static final String PREF_IS_FIRST_TIME_APP_STARTED = "IsFirstTimeApplicationStarted";
	public static final String ACTIONS_SEPARATOR = ";";
	public static final int MAX_NR_OF_PATTERN_ROWS = 2000;
	private static final int STACK_TRACE_LINE = 3;
	private static final String APP_TAG = "kindmind";

	//--------------------(Static) methods for debugging
	
	public static String getMethodName(String inPrefix){
		return "[" + inPrefix + "]" + getMethodName();
	}
	public static String getMethodName(int inListType){
		if(inListType != ListTypeM.NOT_SET){
			return Thread.currentThread().getStackTrace()[3].getMethodName()
					+ "[" + ListTypeM.getListTypeString(inListType) + "]";
		}else{
			return Thread.currentThread().getStackTrace()[3].getMethodName() + "[N/A]";
		}
	}
	//Used for filtering with logcat, the class name has been moved into getMethodName
	public static String getAppTag(){
		return APP_TAG;
	}
	public static String getMethodName(){
		//Extracting the class ("component") name
		String tmpClassWithPackage = Thread.currentThread().getStackTrace()[STACK_TRACE_LINE].getClassName();
		String[] tmpSplitString = tmpClassWithPackage.split("\\."); //NOTE: Regular experssion so "." means "all"
		//String tmpOrganization = tmpSplitString[tmpSplitString.length-3];
		//String tmpProject = tmpSplitString[tmpSplitString.length-2];
		String tmpComponent = tmpSplitString[tmpSplitString.length-1];
		
		//Extracting the method name
		String tmpMethodName = Thread.currentThread().getStackTrace()[3].getMethodName();
		
		return tmpComponent + "." + tmpMethodName;
	}
	
	
	//--------------------Adding new items to the lists
	
	public static boolean isFirstTimeApplicationStarted(Context inContext){
		boolean retVal = PreferenceManager.getDefaultSharedPreferences(inContext).getBoolean(
				PREF_IS_FIRST_TIME_APP_STARTED, true); //Default is true (if no value has been written)
		return retVal;
	}
	public static void createAllStartupItems(Context inContext) {
		Log.i(Utils.getAppTag(), "Creating startup items");
		
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

		
    	PreferenceManager.getDefaultSharedPreferences(inContext)
    			.edit()
    			.putBoolean(PREF_IS_FIRST_TIME_APP_STARTED, false)
    			.commit();
	}
	private static void createStartupItem(Context inContext, int inListTypeInt, String inColumnName){
		ContentValues tmpContentValuesToInsert = new ContentValues();
    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_LIST_TYPE, inListTypeInt);
    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_NAME, inColumnName);
    	inContext.getContentResolver().insert(ContentProviderM.ITEM_CONTENT_URI, tmpContentValuesToInsert);
	}
	
	
	//--------------------Other
	
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
			tmpCursor.close();
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
	
	public static int getListItemCount(Context inContext, int inListType){
		int retCount;
		String tmpSelection = ItemTableM.COLUMN_LIST_TYPE + " = ?";
		String[] tmpSelectionArguments = {String.valueOf(inListType)};
		Cursor tmpCursor = inContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tmpSelection, tmpSelectionArguments, ContentProviderM.sSortType);
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
	public static int getActiveListItemCount(Context inContext, int inListTypeInt){
		int retCount;
		String tmpSelection =
				ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE + " AND " +
				ItemTableM.COLUMN_LIST_TYPE + "=" + inListTypeInt;
		Cursor tmpCursor = inContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tmpSelection, null, ContentProviderM.sSortType);
		retCount = tmpCursor.getCount();
		tmpCursor.close();
		//-PLEASE NOTE: This cursor has to be closed (see comments in method getListItemCount)
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
		Utils.copyFile(tmpSourceFile, tmpDestinationFile);
		
		Log.i(Utils.getAppTag(),"Database backup successful");
	}
	
	
	/*
	 * Overview: copyFile copies one file to another place, possibly with another file name
	 */
	public static void copyFile(File inInFile, File inOutFile){
		try {
			inOutFile.createNewFile(); //-creating the new file
			FileInputStream tmpSourceStream = new FileInputStream(inInFile);
			FileOutputStream tmpDestinationStream = new FileOutputStream(inOutFile);
			FileChannel tmpSourceChannel = tmpSourceStream.getChannel();
			FileChannel tmpDestinationChannel = tmpDestinationStream.getChannel();
			tmpDestinationChannel.transferFrom(tmpSourceChannel, 0, tmpSourceChannel.size()); //-copying
			tmpSourceStream.close();
			tmpDestinationStream.close();
			tmpSourceChannel.close();
			tmpDestinationChannel.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	
	//-------------------------Toast
	
	public static String getToastString(Context inContext, int inListType) {
		//-this method also updates the toast string (can be used for example for sharing)

		String mToastFeelingsString;
		String mToastNeedsString;

		switch(inListType){
		case ListTypeM.FEELINGS:
			mToastFeelingsString =
					getFormattedStringOfActivatedDataListItems(
					getListOfNamesForActivatedData(inContext, ListTypeM.FEELINGS))
					.toLowerCase(Locale.getDefault());
			return mToastFeelingsString;
		
		case ListTypeM.NEEDS:
			mToastNeedsString =
					getFormattedStringOfActivatedDataListItems(
					getListOfNamesForActivatedData(inContext, ListTypeM.NEEDS))
					.toLowerCase(Locale.getDefault());
			return mToastNeedsString;
			
		default:
			Log.e(Utils.getAppTag(),
					"Error in getFormattedStringOfActivatedDataListItems: case not covered in switch statement");
			return null;
		}
	}
	private static ArrayList<String> getListOfNamesForActivatedData(Context inContext, int inListType) {
		ArrayList<String> retActivatedData = new ArrayList<String>();
		String tmpSelection =
				ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE + " AND " +
				ItemTableM.COLUMN_LIST_TYPE + "=" + inListType;
		//-Please note that we are adding ' signs around the String
		Cursor tmpCursor = inContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tmpSelection, null, ContentProviderM.sSortType);
		for(tmpCursor.moveToFirst(); tmpCursor.isAfterLast() == false; tmpCursor.moveToNext()){
			//add name to return list
			String tmpStringToAdd = tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
			retActivatedData.add(tmpStringToAdd);
		}
		
		tmpCursor.close();
		return retActivatedData;
	}
	//Recursive method
	private static String getFormattedStringOfActivatedDataListItems(List<String> inList) {
		if(inList.size() == 0){
			return "";
		}else if(inList.size() == 1){
			return inList.get(0);
		}else if(inList.size() == 2){
			return inList.get(0) + " and " + inList.get(1);
		}else{
			return 
				inList.get(0) +
				", " +
				getFormattedStringOfActivatedDataListItems(inList.subList(1, inList.size()));
		}
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
			Log.e(Utils.getAppTag(), "Error in setSortType: Case not covered");
			break;
		}
	}
	
	public static int longToIntCutOff(long inLong) {
		int retIntVal = (int) (inLong & 0x0000FFFF);
		return retIntVal;
	}
	
	public static ArrayList<String> actionsStringToArrayList(String inActions){
		ArrayList<String> retArrayList = new ArrayList<String>(Arrays.asList(inActions.split(Utils.ACTIONS_SEPARATOR)));
		
		//Removing any empty strings or nulls
		retArrayList.remove("");
		retArrayList.remove(null);

		return retArrayList;
	}
	public static String removeStringFromActions(String inActions, String inActionToRemove){
		String retString = "";
		
		//Split the string into several parts
		String[] tmpStringArray = inActions.split(Utils.ACTIONS_SEPARATOR);
		
		boolean tmpOneItemHasBeenRemoved = false;
		
		//Rebuild the string..
		for(int i=0; i<tmpStringArray.length; i++){
			if(tmpStringArray[i].equals(inActionToRemove) && tmpOneItemHasBeenRemoved == false){
				//..but remove the first match
				tmpOneItemHasBeenRemoved = true;
			}else{
				//..but add all other parts
				if(retString.equals("")){
					retString = tmpStringArray[i];
				}else{
					retString = retString + Utils.ACTIONS_SEPARATOR + tmpStringArray[i];
				}
			}
		}
		
		return retString;
	}
	//return Long.parseLong(inUri.toString().substring(inUri.toString().lastIndexOf("/") + 1));
	/*
	private static String cleanString(String inString, String inCharacterToRemove){
		String retString = inString;
		
		retString.replace(oldChar, newChar)
		
	}
	*/
	/*
	public static int numberOfCharacterAppearances(String inString, char inCharacter){
		int retInt = 0;
		for(int i=0; i < inString.length(); i++){
			if(inCharacter == inString.charAt(i)){
				retInt++;
			}
		}
		return retInt;
	}
	*/
	public static int numberOfActions(String inActions) {
		if(inActions == ""){
			return 0;
		}
		int retInt = 1;
		for(int i=0; i < inActions.length(); i++){
			if(Utils.ACTIONS_SEPARATOR.charAt(0) == inActions.charAt(i)){
				retInt++;
			}
		}
		return retInt;
	}
	
	/*
	 * Overview: sendAsEmail sends an email with title, text and optionally an attachment
	 * Used in: Helper method for onOptionsItemSelected above
	 * Uses app internal: Utils.copyFile
	 * Notes: File must be stored on the external storage to be accessible by email applications (not enough to
	 *  use the internal cache dir for example)
	 */
	public static void sendAsEmail(Context inContext, String inTitle, String inTextContent, File inFileWithPath){
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, inTitle);
		i.putExtra(Intent.EXTRA_TEXT, inTextContent);
		File tmpExtCacheDir = inContext.getExternalCacheDir();
		if(inFileWithPath != null && tmpExtCacheDir != null){
			String tmpFileName = inFileWithPath.toString().substring(inFileWithPath.toString().lastIndexOf("/") + 1);
			Utils.copyFile(inFileWithPath, new File(tmpExtCacheDir + "/" + tmpFileName));
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(tmpExtCacheDir.toString() + "/" + tmpFileName)));
		}
		inContext.startActivity(i);
	}
	
	public static boolean isReleaseVersion(Context inContext){
		//Checking if this is a beta or alpha version
		PackageInfo tmpPackageInfo = null;
		try {
			tmpPackageInfo = inContext.getPackageManager().getPackageInfo(inContext.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e(Utils.getAppTag(), e.getMessage());
		}
		String tmpVerName = tmpPackageInfo.versionName;
		if(tmpVerName.contains("alpha") || tmpVerName.contains("Alpha") || tmpVerName.contains("ALPHA")
				|| tmpVerName.contains("beta") || tmpVerName.contains("Beta") || tmpVerName.contains("BETA")){
			return false;
		}
		
		//Checking if this is a debug build
		if(BuildConfig.DEBUG){
			return false;
		}
		
		return true;
	}
	
	public static int getMaxNumberOfPatternRows(){
		return Utils.MAX_NR_OF_PATTERN_ROWS;
	}
	
	public static void updateWidgets(Context inContext) {
		AppWidgetManager tmpAppWidgetManager = AppWidgetManager.getInstance(inContext);
		ComponentName tmpComponentName = new ComponentName(inContext, WidgetProviderC.class);
		int[] tmpWidgetIds = tmpAppWidgetManager.getAppWidgetIds(tmpComponentName);
		tmpAppWidgetManager.notifyAppWidgetViewDataChanged(tmpWidgetIds, R.id.widget_listview);
	}
	
	
	public static String formatNumber(double inValue) {
		BigDecimal tmpBigDecimal = new BigDecimal(inValue);
		tmpBigDecimal = tmpBigDecimal.setScale(2, BigDecimal.ROUND_UP);
		return "" + tmpBigDecimal;
	}
	public static Context getContentProviderContext(Context inOtherContext) {
		Context retContext = null;
		String tmpPackageName = "com.sunyata.kindmind";
		try {
			retContext = inOtherContext.createPackageContext(tmpPackageName, Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.e(Utils.getAppTag(), "Package name " + tmpPackageName + " not found");
			e.printStackTrace();
		}
		return retContext;
	}
	public static void waitForCondition(int inStepTime, int inNumberOfSteps, int inCurrentStepNumber) {
		try {
			Thread.sleep(inStepTime);
		} catch (InterruptedException e) {}
		if(inCurrentStepNumber > inNumberOfSteps){
			String tmpErrorMessage = "Error in CursorAdapter.getView, waited a long time for cursor to open";
			Log.e(Utils.getAppTag(), tmpErrorMessage);
			try {
				throw new Exception(tmpErrorMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
