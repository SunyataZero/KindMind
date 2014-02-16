package com.sunyata.kindmind.List;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ItemTableM;

public class OnClickToastOrActionC {

	public static void feelingsToast(Context inContext) {
		String tmpToastFeelingsString = Utils.getToastString(inContext, ListTypeM.FEELINGS);
		if(tmpToastFeelingsString.length() > 0){
			Toast.makeText(
					inContext, "I am feeling " + tmpToastFeelingsString, Toast.LENGTH_LONG)
					.show();
		}
	}
	
	public static void needsToast(Context inContext) {
		String tmpToastFeelingsString = Utils.getToastString(inContext, ListTypeM.FEELINGS);
		String tmpToastNeedsString = Utils.getToastString(inContext, ListTypeM.NEEDS);
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
	
	public static void kindAction(Context inContext, Uri inItemUri) {
		///Log.d(Utils.getClassName(), "inActionsString = " + tmpActions);
		
		//Extracting the actions string from the database
		String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};
		Cursor tmpItemCur = inContext.getContentResolver().query(
				inItemUri, tmpProjection, null, null, null);
		tmpItemCur.moveToFirst();
		String tmpActions = tmpItemCur.getString(tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIONS));
		tmpItemCur.close();
		
		//If the string has been cleared (or not set) exiting
		if(tmpActions.equals("")){
			return;
		}
			
		ArrayList<String> tmpActionList = Utils.actionsStringToArrayList(tmpActions);
		
		Random tmpRandomNumberGenerator = new Random();
		int tmpRandomNumber = tmpRandomNumberGenerator.nextInt(tmpActionList.size());

		String tmpRandomlyGivenAction = tmpActionList.get(tmpRandomNumber);
		Log.d(Utils.getClassName(), "tmpRandomlyGivenAction = " + tmpRandomlyGivenAction);

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

		if(tmpRandomlyGivenAction.toString().startsWith("content://")){
			//==========Contacts==========

			tmpIntent = new Intent(Intent.ACTION_VIEW);
			tmpUri = Uri.parse(tmpRandomlyGivenAction);
			tmpIntent.setData(tmpUri); //doesn't work
			//-PLEASE NOTE that setDataAndType(tmpUri, "*/*") doesn't work any longer, but now setData
			// has started working instead

			
		}else if(tmpRandomlyGivenAction.toString().startsWith("http://")
				|| tmpRandomlyGivenAction.toString().startsWith("https://")){
			//==========Bookmarks==========
			
			//Checking if we are conntected to the internet
			ConnectivityManager tmpConnectivityManager =
					(ConnectivityManager)inContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo tmpNetworkInfo = tmpConnectivityManager.getActiveNetworkInfo();
			if(tmpNetworkInfo != null && tmpNetworkInfo.isConnectedOrConnecting()){
				tmpIntent = new Intent(Intent.ACTION_VIEW);
				tmpUri = Uri.parse(tmpRandomlyGivenAction);
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
			tmpFileOrDirectoryFromString = new File(tmpRandomlyGivenAction);

			if(
					tmpRandomlyGivenAction.toString().endsWith(".jpg")||
					tmpRandomlyGivenAction.toString().endsWith(".jpeg")||
					tmpRandomlyGivenAction.toString().endsWith(".png")||
					tmpRandomlyGivenAction.toString().endsWith(".gif")){
				tmpTypeString = "image/*";
			}else if(
					tmpRandomlyGivenAction.toString().endsWith(".ogg")||
					tmpRandomlyGivenAction.toString().endsWith(".mp3")){

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
					tmpRandomlyGivenAction.toString().endsWith(".mp4")||
					tmpRandomlyGivenAction.toString().endsWith(".avi")){
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


		/*
		TODO:
		choice of file
		choice of number/contact (nerd book)
		choice online url
		future: pinterest api, other apis
		 */

		//Verifying that we have at least one app that can handle this intent before starting
		PackageManager tmpPackageManager = inContext.getApplicationContext().getPackageManager();
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
}
