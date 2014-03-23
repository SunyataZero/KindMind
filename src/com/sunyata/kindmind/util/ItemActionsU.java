package com.sunyata.kindmind.util;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sunyata.kindmind.Database.ItemTableM;

public class ItemActionsU {

	
	public static final String ACTIONS_SEPARATOR = ";";

	
	public static ArrayList<String> actionsStringToArrayList(String inActions){
		ArrayList<String> retArrayList = new ArrayList<String>(Arrays.asList(
				inActions.split(ACTIONS_SEPARATOR)));
		
		//Removing any empty strings or nulls
		retArrayList.remove("");
		retArrayList.remove(null);

		return retArrayList;
	}
	
	public static String removeStringFromActions(String inActions, String inActionToRemove){
		String retString = "";
		
		//Split the string into several parts
		String[] tmpStringArray = inActions.split(ACTIONS_SEPARATOR);
		
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
					retString = retString + ACTIONS_SEPARATOR + tmpStringArray[i];
				}
			}
		}
		
		return retString;
	}

	public static int numberOfActions(String inActions) {
		if(inActions == ""){
			return 0;
		}
		int retInt = 1;
		for(int i=0; i < inActions.length(); i++){
			if(ACTIONS_SEPARATOR.charAt(0) == inActions.charAt(i)){
				retInt++;
			}
		}
		return retInt;
	}
	

	public static void addAction(Context iContext, Uri iItemUri, String iFilePathToAdd) {
		if(iItemUri == null){
			Log.w(DbgU.getAppTag(), DbgU.getMethodName() + " iItemUri is null");
			return;
		}
		if(iFilePathToAdd == ""){
			Log.w(DbgU.getAppTag(), DbgU.getMethodName() + " tmpFilePath is empty");
			return;
		}

		//Reading the current string
		String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};
		Cursor tmpItemCur = iContext.getContentResolver().query(
				iItemUri, tmpProjection, null, null, null);
		if(!tmpItemCur.moveToFirst()){
			tmpItemCur.close();
			return;
		}
		String tmpActions = tmpItemCur.getString(tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIONS));
		tmpItemCur.close();

		//Verify that the string to be added does not contain the separator
		if(iFilePathToAdd.contains(ACTIONS_SEPARATOR)){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() +
					" String contains separator character, exiting method");
			return;
		}
		
		
		
		
		// TODO Auto-generated method stub
		if(tmpActions == null || tmpActions.equals("")){
			tmpActions = iFilePathToAdd;
		}else{
			//Updating the string with the appended file path
			tmpActions = tmpActions + ACTIONS_SEPARATOR + iFilePathToAdd;
		}
		
		
		
		//Writing the updated string to the database
		ContentValues tmpContentValues = new ContentValues();
		tmpContentValues.put(ItemTableM.COLUMN_ACTIONS, tmpActions);
		iContext.getContentResolver().update(iItemUri, tmpContentValues, null, null);
		
	}
	
}
