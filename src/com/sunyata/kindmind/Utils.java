package com.sunyata.kindmind;

import java.math.BigDecimal;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sunyata.kindmind.contentprovider.ListContentProviderM;

public class Utils {

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
	
	
	//--------------------Other
	
	public static String formatNumber(double inValue) {
		BigDecimal tmpBigDecimal = new BigDecimal(inValue);
		tmpBigDecimal = tmpBigDecimal.setScale(2, BigDecimal.ROUND_UP);
		return "" + tmpBigDecimal;
	}
	
	public static boolean isFirstTimeApplicationStarted(Context inContext){
		boolean retVal = PreferenceManager.getDefaultSharedPreferences(inContext).getBoolean(
				PREF_IS_FIRST_TIME_APP_STARTED, true);
		return retVal;
	}

	static String getKindMindDirectory(){
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/KindMind";
		//return Environment.getRootDirectory().getAbsolutePath() + "/KindMind";
	}

	public static void createAllStartupItems(Context inContext) {
		// TODO Auto-generated method stub
		
    	ContentValues tmpContentValuesToInsert;

    	createStartupItem(inContext, ListTypeM.SPECEV, "Negative thinking");
    	createStartupItem(inContext, ListTypeM.SUFFERING, "Tired");
    	createStartupItem(inContext, ListTypeM.NEEDS, "Rest");
    	createStartupItem(inContext, ListTypeM.KINDNESS, "Napping");
		
    	PreferenceManager.getDefaultSharedPreferences(inContext)
    			.edit()
    			.putBoolean(PREF_IS_FIRST_TIME_APP_STARTED, false)
    			.commit();
	}
	private static void createStartupItem(Context inContext, ListTypeM inListType, String inColumnName){
		ContentValues tmpContentValuesToInsert = new ContentValues();
    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_LISTTYPE, inListType.toString());
    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_NAME, inColumnName);
    	inContext.getContentResolver().insert(ListContentProviderM.CONTENT_URI, tmpContentValuesToInsert);
		Log.i(Utils.getClassName(),
				"Added " + inColumnName + " with type " + inListType.toString() + " to the database");
	}
	
	
	
	
}
