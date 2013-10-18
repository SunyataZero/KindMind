package com.sunyata.kindmind;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
//NotificationCompat is for api lvl 15 and downwards

public class NotificationServiceC extends IntentService {

	private static final String TAG = "NotificationServiceC";
	static final String PREFERENCES_NOTIFICATION_LIST = "NotificationList";
	private static final String NOTIFICATION_UUID = "NotificationUUID";
	private static final String NOTIFICATION_TITLE = "NotificationTitle";

	public NotificationServiceC() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Log.d(Utils.getClassName(), "One intent received");
		
		PendingIntent tmpPendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, MainActivityC.class), 0);
		//-Please note: Request code not used by the class (see the documentation)
		
		String tmpUuidStringFromListDataItem = inIntent.getStringExtra(NOTIFICATION_UUID);
		String tmpTitleStringFromListDataItem = inIntent.getStringExtra(NOTIFICATION_TITLE);

		Notification tmpNotification = new NotificationCompat.Builder(this)
				.setTicker("Ticker text " + tmpTitleStringFromListDataItem)
				.setSmallIcon(R.drawable.kindmind_icon)
				.setContentTitle("Content title " + tmpTitleStringFromListDataItem)
				.setContentText("Content text" + tmpTitleStringFromListDataItem)
				.setContentIntent(tmpPendingIntent)
				.setAutoCancel(true)
				.build();
		
		NotificationManager tmpNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		tmpNotificationManager.notify(tmpUuidStringFromListDataItem, 0, tmpNotification); //TODO: Change the 0
	}
	
	static void setServiceNotificationSingle(
			Context inContext, boolean inIsActive,
			long inUserTimeInMillseconds, long inIntervalInMilliseconds,
			UUID inListDataItemUUID, String inListDataItemName){
		
		Intent tmpIntent = new Intent(inContext, NotificationServiceC.class);
		tmpIntent.setType(inListDataItemUUID.toString());
		//-This is what makes the intents differ
		tmpIntent.putExtra(NOTIFICATION_UUID, inListDataItemUUID.toString());
		tmpIntent.putExtra(NOTIFICATION_TITLE, inListDataItemName);

		PendingIntent tmpPendingIntentToRepeat = PendingIntent.getService(inContext, 0, tmpIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
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
		/*
		ArrayList<ItemM> tmpList = loadDataFromJson(
				ListTypeM.KINDNESS, KindModelM.JSON_REQUESTS_KINDNESS_FILE_NAME, inContext);
		for(ItemM ldi : tmpList){
			NotificationServiceC.setServiceNotificationSingle(
					inContext, ldi.isNotificationActive(), ldi.getUserTimeInMilliSeconds(),
					AlarmManager.INTERVAL_DAY, ldi.getId(), ldi.getName());
			Log.i(Utils.getClassName(), "ldi.isNotificationActive() = " + ldi.isNotificationActive());
		}
		*/
	}
	//TODO: Remove this method and use the one in ListDataM instead?
	static ArrayList<ItemM> loadDataFromJson(ListTypeM inListType, String inFileName, Context inContext) {
		ArrayList<ItemM> retList = new ArrayList<ItemM>();
		/*
		JsonSerializerM tmpJsonSerializer = new JsonSerializerM(inContext, inFileName);
		try{
			Log.i(Utils.getClassName(), "Try loading from JSON file");
			retList = tmpJsonSerializer.loadData();
			Log.i(Utils.getClassName(), "Done loading from JSON file");
		}catch(Exception e){
			retList = new ArrayList<ItemM>();
			//-This will happen when we don't have any file yet
		}
		*/
		return retList;
	}
}
