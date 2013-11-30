package com.sunyata.kindmind;

import java.util.ArrayList;
import java.util.Date;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
		Log.d(Utils.getClassName(), "In method onHandleIntent: One intent received");
		
		PendingIntent tmpPendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, MainActivityC.class), 0);
		//-Please note: Request code not used by the class (see the documentation)
		
		String tmpUuidStringFromListDataItem = inIntent.getStringExtra(NOTIFICATION_UUID);
		String tmpTitleStringFromListDataItem = inIntent.getStringExtra(NOTIFICATION_TITLE);

		Notification tmpNotification = new NotificationCompat.Builder(this)
				.setTicker("Ticker text " + tmpTitleStringFromListDataItem)
				.setSmallIcon(R.drawable.kindmind_icon)
				.setContentTitle(tmpTitleStringFromListDataItem)
				.setContentText(tmpTitleStringFromListDataItem)
				.setContentIntent(tmpPendingIntent)
				.setAutoCancel(true)
				.build();
		
		NotificationManager tmpNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		tmpNotificationManager.notify(tmpUuidStringFromListDataItem, 0, tmpNotification); //TODO: Change the 0
	}
	
	static void setServiceNotificationSingle(
			Context inContext, Uri inItemUri, long inIntervalInMilliSeconds){
		
		Cursor tmpCursor = inContext.getContentResolver().query(inItemUri, null, null, null, null);
		if(tmpCursor.getCount() == 0){
			tmpCursor.close();
			return;
		}
		tmpCursor.moveToFirst();
		
		String tmpItemIdAsString = Long.valueOf(
				tmpCursor.getLong(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ID)))
						.toString();
		String tmpItemName = tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
		boolean tmpItemNotificationIsActive = true;
		long tmpItemTimeInMilliSeconds = tmpCursor.getLong(
				tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NOTIFICATION));
		if(tmpItemTimeInMilliSeconds == -1 ){
			tmpItemNotificationIsActive = false;
		}
		
		Intent tmpIntent = new Intent(inContext, NotificationServiceC.class);
		tmpIntent.setType(tmpItemIdAsString); //This is what makes the intents differ
		tmpIntent.putExtra(NOTIFICATION_UUID, tmpItemIdAsString);
		tmpIntent.putExtra(NOTIFICATION_TITLE, tmpItemName);

		PendingIntent tmpPendingIntentToRepeat = PendingIntent.getService(
				inContext, 0, tmpIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		AlarmManager tmpAlarmManager = (AlarmManager)inContext.getSystemService(Context.ALARM_SERVICE);
		
		if(tmpItemNotificationIsActive == true){
			Log.i(Utils.getClassName(), "date = " + new Date(tmpItemTimeInMilliSeconds));
			tmpAlarmManager.setRepeating(AlarmManager.RTC, tmpItemTimeInMilliSeconds, inIntervalInMilliSeconds,
					tmpPendingIntentToRepeat);
			//-PLEASE NOTE: Initial time inUserTimeInMillseconds is not modified with TimeZone.getDefault().getRawOffset()
			// in spite of the documentation for AlarmManager.RTC which indicates that UTC is used.
		}else{
			tmpAlarmManager.cancel(tmpPendingIntentToRepeat);
			tmpPendingIntentToRepeat.cancel();
		}
		tmpCursor.close();
	}
	
	//Called only from BootCompleteReceiverC
	static void setServiceNotificationAll(Context inContext){
		long tmpNotification = -1;
		Uri tmpItemUri = null;
		Cursor tmpCursor = inContext.getContentResolver().query(
				ListContentProviderM.LIST_CONTENT_URI, null, null, null, null);
		if(tmpCursor.getCount() == 0){
			tmpCursor.close();
			return;
		}
		for(tmpCursor.moveToFirst(); tmpCursor.isAfterLast() == false; tmpCursor.moveToNext()){
			tmpNotification = tmpCursor.getLong(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NOTIFICATION));
			tmpItemUri = Uri.withAppendedPath(
					ListContentProviderM.LIST_CONTENT_URI,
					"/" +
					(tmpCursor.getLong(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ID))));
			if(tmpNotification > -1){
				setServiceNotificationSingle(inContext, tmpItemUri, tmpNotification);
			}
		}
		tmpCursor.close();
	}
}
