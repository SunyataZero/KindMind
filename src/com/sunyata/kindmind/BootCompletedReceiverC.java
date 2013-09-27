package com.sunyata.kindmind;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

public class BootCompletedReceiverC extends BroadcastReceiver {

	@Override
	public void onReceive(Context inContext, Intent inIntent) {
		Log.d(Utils.getClassName(), "onReceive(Context inContext, Intent inIntent)");
		
		
		
		//Load data from JSON files
		ArrayList<ListDataItemM> tmpList = loadDataFromJson(
				ListTypeM.KINDNESS, KindModelM.JSON_REQUESTS_KINDNESS_FILE_NAME, inContext);
		
		//TODO Is this too much computation for the onReceive method? Maybe putting this on another thread?
		Log.i(Utils.getClassName(), "tmpList.size() = " + tmpList.size());
		for(ListDataItemM ldi : tmpList){
			
			setServiceNotification(
					inContext, 0, ldi.isNotificationActive(), ldi.getUserTimeInMilliSeconds(), AlarmManager.INTERVAL_DAY);
			//-TODO: Change the 0
			Log.i(Utils.getClassName(), "ldi.isNotificationActive() = " + ldi.isNotificationActive());
			Log.i(Utils.getClassName(), "ldi.getUserTimeInMilliSeconds() = " + ldi.getUserTimeInMilliSeconds());
		}
		
		/*
		AlarmManager tmpAlarmManager = (AlarmManager)inContext.getSystemService(Context.ALARM_SERVICE);
		Intent tmpIntent = new Intent(inContext, NotificationReceiver.class);
		PendingIntent tmpPendingIntentToBroadcastReceiver = PendingIntent.getBroadcast(
				inContext, 0, tmpIntent, 0);
		tmpAlarmManager.setRepeating(AlarmManager.RTC, new Date().getTime() + 10000,
				10000, tmpPendingIntentToBroadcastReceiver);
		*/
	}
	/*
	@Override
	public void onReceive(Context inContext, Intent inIntent) {
		Log.d(Utils.getClassName(), "onReceive(Context inContext, Intent inIntent)");
		
		//Load data from JSON files
		ArrayList<ListDataItemM> tmpList = loadDataFromJson(
				ListTypeM.KINDNESS, KindModelM.JSON_REQUESTS_KINDNESS_FILE_NAME, inContext);
		
		//TODO Is this too much computation for the onReceive method? Maybe putting this on another thread?
		Log.i(Utils.getClassName(), "tmpList.size() = " + tmpList.size());
		for(ListDataItemM ldi : tmpList){
			
			NotificationServiceC.setServiceNotification(
					inContext, 0, ldi.isNotificationActive(), ldi.getUserTimeInMilliSeconds(), AlarmManager.INTERVAL_DAY);
			//-TODO: Change the 0
			Log.i(Utils.getClassName(), "ldi.isNotificationActive() = " + ldi.isNotificationActive());
			Log.i(Utils.getClassName(), "ldi.getUserTimeInMilliSeconds() = " + ldi.getUserTimeInMilliSeconds());
		}
		
		//AlarmManager tmpAlarmManager = (AlarmManager)inContext.getSystemService(inContext.ALARM_SERVICE);
		
	}
	 */

	//TODO: Remove this method and use the one in ListDataM instead?
	private ArrayList<ListDataItemM> loadDataFromJson(ListTypeM inListType, String inFileName, Context inContext) {
		JsonSerializerM tmpJsonSerializer = new JsonSerializerM(inContext, inFileName);
		ArrayList<ListDataItemM> retList;
		try{
			Log.i(Utils.getClassName(), "Try loading from JSON file");
			retList = tmpJsonSerializer.loadData();
			Log.i(Utils.getClassName(), "Done loading from JSON file");
			//return true;
		}catch(Exception e){
			//This will happen when we don't have any file yet
			retList = new ArrayList<ListDataItemM>();
			//return false;
		}
		return retList;
	}
	
	
	static void setServiceNotification(
			Context inContext, int inRequestCode, boolean inIsActive,
			long inUserTimeInMillseconds, long inIntervalInMilliseconds){
		
		/*
		AlarmManager tmpAlarmManager = (AlarmManager)inContext.getSystemService(Context.ALARM_SERVICE);
		Intent tmpIntent = new Intent(inContext, NotificationReceiver.class);
		PendingIntent tmpPendingIntentToBroadcastReceiver = PendingIntent.getBroadcast(
				inContext, 0, tmpIntent, 0);
		tmpAlarmManager.setRepeating(AlarmManager.RTC, new Date().getTime() + 10000,
				10000, tmpPendingIntentToBroadcastReceiver);
		*/
		
		Intent tmpIntent = new Intent(inContext, NotificationReceiver.class);
		PendingIntent tmpPendingIntentToRepeat = PendingIntent.getBroadcast(
				inContext, inRequestCode, tmpIntent, 0);

		Log.i(Utils.getClassName(), "inContext = " + inContext.toString());
		
	
		AlarmManager tmpAlarmManager = (AlarmManager)inContext.getSystemService(Context.ALARM_SERVICE);
		
		if(inIsActive == true){
			Log.i(Utils.getClassName(), "tmpAlarmManager.setRepeating(");
			Log.i(Utils.getClassName(), "inUserTimeInMillseconds = " + inUserTimeInMillseconds);
			Log.i(Utils.getClassName(), "date = " + new Date(inUserTimeInMillseconds));
			Log.i(Utils.getClassName(), "inIntervalInMilliseconds = " + inIntervalInMilliseconds);
			Log.i(Utils.getClassName(), "tmpPendingIntentToRepeat = " + tmpPendingIntentToRepeat);
			tmpAlarmManager.setRepeating(AlarmManager.RTC, inUserTimeInMillseconds, inIntervalInMilliseconds,
					tmpPendingIntentToRepeat);
			//-PLEASE NOTE: Initial time inUserTimeInMillseconds is not modified with TimeZone.getDefault().getRawOffset()
			// in spite of the documentation for AlarmManager.RTC which indicates that UTC is used.

		}else{
			/*
			tmpAlarmManager.cancel(tmpPendingIntentToRepeat);
			tmpPendingIntentToRepeat.cancel();
			*/
		}
	}
	
}
