package com.sunyata.kindmind;

import java.util.ArrayList;
import java.util.Date;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;//for api lvl 15 and downwards
import android.util.Log;

public class NotificationServiceC extends IntentService {

	private static final String TAG = "NotificationServiceC";
	static final String PREFERENCES_NOTIFICATION_LIST = "NotificationList";
	private static final String NOTIFICATION_STACK_COUNTER = "NotificationStackCounter";

	public NotificationServiceC() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Log.d(Utils.getClassName(), "One intent received");
		
		PendingIntent tmpPendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, MainActivityC.class), 0);
		//-Please note: Request code not used by Android
		//-TODO: Possibly change class or change so that the file associated is shown
		
		int tmpNotificationStackCounter = inIntent.getIntExtra(NOTIFICATION_STACK_COUNTER, -1);
		if(tmpNotificationStackCounter == -1){
			Log.e(Utils.getClassName(), "Error in method onHandleIntent: NOTIFICATION_STACK_COUNTER not set");
		}

		Notification tmpNotification = new NotificationCompat.Builder(this)
		.setTicker("Ticker text " + tmpNotificationStackCounter)
		.setSmallIcon(R.drawable.kindmind_icon)
		.setContentTitle("Content title " + tmpNotificationStackCounter)
		.setContentText("Content text" + tmpNotificationStackCounter)
		.setContentIntent(tmpPendingIntent)
		.setAutoCancel(true)
		.build();

		
		NotificationManager tmpNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		tmpNotificationManager.notify(tmpNotificationStackCounter, tmpNotification); //TODO: Change the 0
	}
	
	static void setServiceNotificationSingle(
			Context inContext, boolean inIsActive,
			long inUserTimeInMillseconds, long inIntervalInMilliseconds){
		
		Intent tmpIntent = new Intent(inContext, NotificationServiceC.class);
		/*
		Intent tmpIntent = new Intent(
				null, Uri.parse(Long.toString(Utils.getTemporaryInternalIdentifier())),
				inContext, NotificationServiceC.class);
		*/
		tmpIntent.setData(Uri.parse(Long.toString(Utils.getTemporaryInternalIdentifier())));
		tmpIntent.putExtra(NOTIFICATION_STACK_COUNTER, Utils.getTemporaryInternalIdentifier());
		PendingIntent tmpPendingIntentToRepeat = PendingIntent.getService(inContext, 0, tmpIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		//PendingIntent.FLAG_UPDATE_CURRENT
		//Intent.FLAG_ACTIVITY_NEW_TASK
		
		AlarmManager tmpAlarmManager = (AlarmManager)inContext.getSystemService(Context.ALARM_SERVICE);
		
		if(inIsActive == true){
			Log.i(Utils.getClassName(), "date = " + new Date(inUserTimeInMillseconds));
			tmpAlarmManager.setRepeating(AlarmManager.RTC, inUserTimeInMillseconds, inIntervalInMilliseconds,
					tmpPendingIntentToRepeat);
			//-PLEASE NOTE: Initial time inUserTimeInMillseconds is not modified with TimeZone.getDefault().getRawOffset()
			// in spite of the documentation for AlarmManager.RTC which indicates that UTC is used.

		}else{
			tmpAlarmManager.cancel(tmpPendingIntentToRepeat);
			tmpPendingIntentToRepeat.cancel();
		}
	}
	
	static void setServiceNotificationAll(Context inContext){

		//Load data from JSON files
		ArrayList<ListDataItemM> tmpList = loadDataFromJson(
				ListTypeM.KINDNESS, KindModelM.JSON_REQUESTS_KINDNESS_FILE_NAME, inContext);
		
		//TODO Is this too much computation for the onReceive method? Maybe putting this on another thread?
		for(ListDataItemM ldi : tmpList){
			NotificationServiceC.setServiceNotificationSingle(
					inContext, ldi.isNotificationActive(), ldi.getUserTimeInMilliSeconds(),
					AlarmManager.INTERVAL_DAY);
			//-TODO: Change the 0
			Log.i(Utils.getClassName(), "ldi.isNotificationActive() = " + ldi.isNotificationActive());
		}
	}
	
	//TODO: Remove this method and use the one in ListDataM instead?
	static private ArrayList<ListDataItemM> loadDataFromJson(ListTypeM inListType, String inFileName, Context inContext) {
		JsonSerializerM tmpJsonSerializer = new JsonSerializerM(inContext, inFileName);
		ArrayList<ListDataItemM> retList;
		try{
			Log.i(Utils.getClassName(), "Try loading from JSON file");
			retList = tmpJsonSerializer.loadData();
			Log.i(Utils.getClassName(), "Done loading from JSON file");
		}catch(Exception e){
			retList = new ArrayList<ListDataItemM>();
			//-This will happen when we don't have any file yet
		}
		return retList;
	}
}
