package com.sunyata.kindmind;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.ListTypeM;
import com.sunyata.kindmind.util.DatabaseU;
import com.sunyata.kindmind.util.DbgU;
import com.sunyata.kindmind.util.ItemActionsU;

public class OnClickToastOrActionC {

	public static void feelingsToast(Context inContext) {
		String tmpToastFeelingsString = getToastString(inContext, ListTypeM.FEELINGS);
		if(tmpToastFeelingsString.length() > 0){
			Toast.makeText(
					inContext, "I am feeling " + tmpToastFeelingsString, Toast.LENGTH_LONG)
					.show();
		}
	}
	
	public static void needsToast(Context inContext) {
		String tmpToastFeelingsString = getToastString(inContext, ListTypeM.FEELINGS);
		String tmpToastNeedsString = getToastString(inContext, ListTypeM.NEEDS);
		if(tmpToastFeelingsString.length() > 0 & tmpToastNeedsString.length() > 0){
			Toast.makeText(
					inContext,
					"I am feeling " + tmpToastFeelingsString +
					" because I am needing " + tmpToastNeedsString, Toast.LENGTH_LONG)
					.show();
		}else if(tmpToastNeedsString.length() > 0){
				Toast.makeText(
						inContext,
						"I am needing " + tmpToastNeedsString, Toast.LENGTH_LONG)
						.show();
		}
	}
	
	public static void randomKindAction(Context inContext, Uri inItemUri) {
		///Log.d(Utils.getClassName(), "inActionsString = " + tmpActions);
		
		String tmpActions = "";
		String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};
		Cursor tItemCr = inContext.getContentResolver().query(
				inItemUri, tmpProjection, null, null, null);
		try{
			if(tItemCr != null && tItemCr.moveToFirst()){
				
				//Extracting the actions string from the database
				tmpActions = tItemCr.getString(tItemCr.getColumnIndexOrThrow(
						ItemTableM.COLUMN_ACTIONS));    		

			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor is null or empty",
						new Exception());
				return;
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Exception for cursor", e);
			return;
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}
		}
		//If the string has been cleared (or not set) exiting
		if(tmpActions.equals("")){return;}
		
		/*
02-27 22:29:51.187: E/AndroidRuntime(20502): android.database.CursorIndexOutOfBoundsException: Index 0 requested, with a size of 0
02-27 22:29:51.187: E/AndroidRuntime(20502): 	at android.database.AbstractCursor.checkPosition(AbstractCursor.java:400)
02-27 22:29:51.187: E/AndroidRuntime(20502): 	at android.database.AbstractWindowedCursor.checkPosition(AbstractWindowedCursor.java:136)
02-27 22:29:51.187: E/AndroidRuntime(20502): 	at android.database.AbstractWindowedCursor.getString(AbstractWindowedCursor.java:50)
02-27 22:29:51.187: E/AndroidRuntime(20502): 	at android.database.CursorWrapper.getString(CursorWrapper.java:114)
02-27 22:29:51.187: E/AndroidRuntime(20502): 	at com.sunyata.kindmind.OnClickToastOrActionC.randomKindAction(OnClickToastOrActionC.java:58)
02-27 22:29:51.187: E/AndroidRuntime(20502): 	at com.sunyata.kindmind.WidgetAndNotifications.LauncherServiceC.onHandleIntent(LauncherServiceC.java:40)
		 */
		
		
		
		ArrayList<String> tmpActionList = ItemActionsU.actionsStringToArrayList(tmpActions);
		
		Random tmpRandomNumberGenerator = new Random();
		int tmpRandomNumber = tmpRandomNumberGenerator.nextInt(tmpActionList.size());

		String tmpRandomlyGivenAction = tmpActionList.get(tmpRandomNumber);
		Log.d(DbgU.getAppTag(), "tmpRandomlyGivenAction = " + tmpRandomlyGivenAction);

		kindAction(inContext, tmpRandomlyGivenAction);
	}
	public static void kindAction(Context inContext, String inRandomlyGivenAction) {
		
		/*
		//Ok, works well!
		Intent tmpIntent = new Intent(Intent.ACTION_DIAL);
		tmpIntent.setData(Uri.parse("tel:123"));
		 */
		
		AudioManager tmpAudioManager = (AudioManager)inContext.getSystemService(Context.AUDIO_SERVICE);
		String tmpTypeString = "*/*";

		Intent tmpIntent;
		Uri tmpUri;
		File tmpFileOrDirectoryFromString;

		if(inRandomlyGivenAction.toString().startsWith("content://")){
			//==========Contacts==========

			tmpIntent = new Intent(Intent.ACTION_VIEW);
			tmpUri = Uri.parse(inRandomlyGivenAction);
			tmpIntent.setData(tmpUri); //doesn't work
			//-PLEASE NOTE that setDataAndType(tmpUri, "*/*") doesn't work any longer, but now setData
			// has started working instead

			
		}else if(inRandomlyGivenAction.toString().startsWith("http://")
				|| inRandomlyGivenAction.toString().startsWith("https://")){
			//==========Bookmarks==========
			
			//Checking if we are conntected to the internet
			ConnectivityManager tmpConnectivityManager =
					(ConnectivityManager)inContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo tmpNetworkInfo = tmpConnectivityManager.getActiveNetworkInfo();
			if(tmpNetworkInfo != null && tmpNetworkInfo.isConnectedOrConnecting()){
				tmpIntent = new Intent(Intent.ACTION_VIEW);
				tmpUri = Uri.parse(inRandomlyGivenAction);
				tmpIntent.setData(tmpUri);
				//PLEASE NOTE: setDataAndType(tmpUri, "*/*") doesn't work any longer
			}else{
				Toast.makeText(
						inContext,
						"Not launching website since there is no internet connectivity",
						Toast.LENGTH_LONG)
						.show();
				return;
			}
			
			
		}else{
			//==========Media files==========
			tmpFileOrDirectoryFromString = new File(inRandomlyGivenAction);

			if(
					inRandomlyGivenAction.toString().endsWith(".jpg")||
					inRandomlyGivenAction.toString().endsWith(".jpeg")||
					inRandomlyGivenAction.toString().endsWith(".png")||
					inRandomlyGivenAction.toString().endsWith(".gif")){
				tmpTypeString = "image/*";
			}else if(
					inRandomlyGivenAction.toString().endsWith(".ogg")||
					inRandomlyGivenAction.toString().endsWith(".mp3")){

				
				if(tmpAudioManager.isWiredHeadsetOn() == false || tmpAudioManager.isSpeakerphoneOn() == true){
				/*
				PLEASE NOTE: Half deprecated but this method can still be used for checking connectivity:
				"
				This method was deprecated in API level 14.
				***Use only to check is a headset is connected or not.***
				" (my emphasis)
				https://developer.android.com/reference/android/media/AudioManager.html#isWiredHeadsetOn%28%29
				http://stackoverflow.com/questions/2764733/android-checking-if-headphones-are-plugged-in
				*/
					Toast.makeText(
							inContext,
							"Not playing audio since headset is not connected or speaker phone is on",
							Toast.LENGTH_LONG)
							.show();
					return;
				}
			
				tmpTypeString = "audio/*";

			}else if(
					inRandomlyGivenAction.toString().endsWith(".mp4")||
					inRandomlyGivenAction.toString().endsWith(".avi")){
				if(tmpAudioManager.isWiredHeadsetOn() == false || tmpAudioManager.isSpeakerphoneOn() == true){
					//-See comments above about isWiredHeadsetOn()
					Toast.makeText(
							inContext,
							"Not playing video since headset is not connected or speaker phone is on",
							Toast.LENGTH_LONG)
							.show();
					return;
				}

				tmpTypeString = "video/*";

			}else{
				//Continue with "*/*"
			}

			//For all media files:
			tmpIntent = new Intent(Intent.ACTION_VIEW);
			tmpUri = Uri.fromFile(tmpFileOrDirectoryFromString);
			//tmpIntent.setData(tmpUri); //doesn't work
			tmpIntent.setDataAndType(tmpUri, tmpTypeString);
			//-NOTE: THIS IS OK, BUT SPLITTING DATA AND TYPE DOES NOT WORK
		}


		//Verifying that we have at least one app that can handle this intent before starting
		Context tmpAppContext = inContext.getApplicationContext();
		PackageManager tmpPackageManager = tmpAppContext.getPackageManager();
		List<ResolveInfo> tmpListOfAllPosibleAcitivtiesForStarting =
				tmpPackageManager.queryIntentActivities(tmpIntent, 0);
		if(tmpListOfAllPosibleAcitivtiesForStarting.size() > 0){
			//===================Starting the activity===================
			////ActivityOptions tmpOptions = new ActivityOptions();
			tmpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			inContext.startActivity(tmpIntent);
		}else{
			Toast.makeText(inContext,
					"Currently no app supports this file type on this device, " +
							"please install an app that supports this operation",
							Toast.LENGTH_LONG)
							.show();
		}
	}
	
	private static String getToastString(Context inContext, int inListType) {
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
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Case not covered");
			return null;
		}
	}
	private static ArrayList<String> getListOfNamesForActivatedData(Context inContext, int inListType) {
		ArrayList<String> retActivatedData = new ArrayList<String>();
		String tmpSelection =
				ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE + " AND " +
				ItemTableM.COLUMN_LIST_TYPE + "=" + inListType;
		//-Please note that we are adding ' signs around the String
		
		
		String tmpStringToAdd = "";
		Cursor tItemCr = inContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tmpSelection, null,
				ContentProviderM.sSortType);
		try{
			if(tItemCr != null && tItemCr.moveToFirst()){

				for(tItemCr.moveToFirst(); tItemCr.isAfterLast() == false; tItemCr.moveToNext()){
					tmpStringToAdd = tItemCr.getString(tItemCr.getColumnIndexOrThrow(
							ItemTableM.COLUMN_NAME));
					
					//add name to return list
					retActivatedData.add(tmpStringToAdd);
				}
				
			}else{
				Log.d(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor is null or empty" +
						" (this can be the case if we have no items that are active and is not an error)");
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Exception for cursor");
			return null;
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}
		}

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
}