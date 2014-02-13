package com.sunyata.kindmind.ToastsAndActions;

import java.io.File;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.sunyata.kindmind.Utils;


//Documentation:
// http://www.iana.org/assignments/media-types/media-types.xhtml
public class MediaFileActionBehaviour implements ActionBehaviour{
	@Override
	public void kindAction(Context inContext, String inKindActionFilePath) {
		Log.d(Utils.getClassName(), "inKindActionFilePath = " + inKindActionFilePath);

		//If the file/dir string has been cleared (or not set) exiting..
		if(inKindActionFilePath.equals("")){
			return;
		}else{ //..otherwise ___________
			 
			File tmpFileOrDirectoryFromString = new File(inKindActionFilePath);

			Log.d(Utils.getClassName(), "tmpFileOrDirectoryFromString.isDirectory() = "
					+ tmpFileOrDirectoryFromString.isDirectory());
			if(tmpFileOrDirectoryFromString.isDirectory()){
				this.doRandomKindActionFromSetOfFiles(inContext, tmpFileOrDirectoryFromString);
			}else{
				this.doKindAction(inContext, inKindActionFilePath);
			}
		}
	}

	private void doKindAction(Context inContext, String inFileFromString){
		Log.d(Utils.getClassName(), "inFileFromString = " + inFileFromString);

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

		if(inFileFromString.toString().startsWith("content://")){
			//==========Contacts==========

			tmpIntent = new Intent(Intent.ACTION_VIEW);
			tmpUri = Uri.parse(inFileFromString);
			tmpIntent.setData(tmpUri); //doesn't work
			//-PLEASE NOTE that setDataAndType(tmpUri, "*/*") doesn't work any longer, but now setData
			// has started working instead

			
		}else if(inFileFromString.toString().startsWith("http://")
				|| inFileFromString.toString().startsWith("https://")){
			//==========Bookmarks==========
			
			tmpIntent = new Intent(Intent.ACTION_VIEW);
			tmpUri = Uri.parse(inFileFromString);
			tmpIntent.setData(tmpUri);
			//PLEASE NOTE: setDataAndType(tmpUri, "*/*") doesn't work any longer
			
		}else{
			//==========Media files==========
			tmpFileOrDirectoryFromString = new File(inFileFromString);

			if(
					inFileFromString.toString().endsWith(".jpg")||
					inFileFromString.toString().endsWith(".jpeg")||
					inFileFromString.toString().endsWith(".png")||
					inFileFromString.toString().endsWith(".gif")){
				tmpTypeString = "image/*";
			}else if(
					inFileFromString.toString().endsWith(".ogg")||
					inFileFromString.toString().endsWith(".mp3")){

				if(tmpAudioManager.isWiredHeadsetOn() == false || tmpAudioManager.isSpeakerphoneOn() == true){
				/*
				isWiredHeadsetOn is used even though it is deprecated:
				"
				This method was deprecated in API level 14.
				Use only to check is a headset is connected or not.
				"
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
					inFileFromString.toString().endsWith(".mp4")||
					inFileFromString.toString().endsWith(".avi")){
				if(tmpAudioManager.isWiredHeadsetOn() == false || tmpAudioManager.isSpeakerphoneOn() == true){
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
			inContext.startActivity(tmpIntent);
		}else{
			Toast.makeText(inContext,
					"Currently no app supports this file type on this device, " +
							"please install an app that supports this operation",
							Toast.LENGTH_LONG)
							.show();
		}
	}
	private void doRandomKindActionFromSetOfFiles(Context inContext, File inDirectoryFromString){
		Log.d(Utils.getClassName(), "inDirectoryFromString = " + inDirectoryFromString);

		String[] tmpListOfFilesInDirectory = inDirectoryFromString.list();
		Random tmpRandomNumberGenerator = new Random();
		int tmpNumberOfFilesInDirectory = tmpListOfFilesInDirectory.length;
		int tmpRandomNumber = tmpRandomNumberGenerator.nextInt(tmpNumberOfFilesInDirectory);

		File tmpRandomlyGivenFile = new File(
				inDirectoryFromString + "/"
						+ tmpListOfFilesInDirectory[tmpRandomNumber]);
		this.doKindAction(inContext, tmpRandomlyGivenFile.toString());
	}
}


