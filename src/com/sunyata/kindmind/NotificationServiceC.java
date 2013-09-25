package com.sunyata.kindmind;

import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationServiceC extends IntentService {

	private static final String TAG = "NotificationServiceC";
	
	public NotificationServiceC() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Log.d(Utils.getClassName(), "One intent received");
		
		PendingIntent tmpPendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, MainActivityC.class), 0);
		
		Notification tmpNotification = new NotificationCompat.Builder(this)
			.setTicker("Ticker text")
			.setSmallIcon(R.drawable.kindmind_icon)
			.setContentTitle("Content title")
			.setContentText("Content text")
			.setContentIntent(tmpPendingIntent)
			.setAutoCancel(true)
			.build();
		
		NotificationManager tmpNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		tmpNotificationManager.notify(0, tmpNotification); //TODO: Change the 0
	}
	
	static void setServiceNotification(
			Context inContext, int inRequestCode, boolean inIsActive,
			long inUserTimeInMillseconds, long inIntervalInMilliseconds){
		
		Intent tmpIntent = new Intent(inContext, NotificationServiceC.class);
		PendingIntent tmpPendingIntentToRepeat = PendingIntent.getService(inContext, inRequestCode, tmpIntent, 0);
		
		AlarmManager tmpAlarmManager = (AlarmManager)inContext.getSystemService(inContext.ALARM_SERVICE);
		
		if(inIsActive == true){
			//TODO: Change to setInexactRepeating
			tmpAlarmManager.setRepeating(AlarmManager.RTC, inUserTimeInMillseconds, inIntervalInMilliseconds,
					tmpPendingIntentToRepeat);
			//-PLEASE NOTE: Initial time inUserTimeInMillseconds is not modified with TimeZone.getDefault().getRawOffset()
			// in spite of the documentation for AlarmManager.RTC which indicates that UTC is used.
		}else{
			tmpAlarmManager.cancel(tmpPendingIntentToRepeat);
			tmpPendingIntentToRepeat.cancel();
		}
	}
	/*
	static void stopServiceNotification(Context inContext, int inRequestCode,
			long inTimeInMillseconds, long inIntervalInMilliseconds){
		setServiceNotification(inContext, inRequestCode, true, inTimeInMillseconds, inIntervalInMilliseconds);
	}
	static void stopServiceNotification(Context inContext, int inRequestCode){
		setServiceNotification(inContext, inRequestCode, false, 0, 0);
	}
	*/
}
