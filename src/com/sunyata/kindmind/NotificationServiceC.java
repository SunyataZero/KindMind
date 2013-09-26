package com.sunyata.kindmind;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;//for api lvl 15 and downwards
import android.util.Log;

public class NotificationServiceC extends IntentService {

	private static final String TAG = "NotificationServiceC";
	//private static ArrayList<UUID> refIdList;
	private static final String PREFERENCES_NOTIFICATION_LIST = "NotificationList";

	public NotificationServiceC() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Log.d(Utils.getClassName(), "One intent received");
		
		PendingIntent tmpPendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, MainActivityC.class), 0);
		//-TODO: Possibly change class or change so that the file associated is shown
		
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
		
		AlarmManager tmpAlarmManager = (AlarmManager)inContext.getSystemService(Context.ALARM_SERVICE);
		
		if(inIsActive == true){
			//TODO: Change to setInexactRepeating
			tmpAlarmManager.setRepeating(AlarmManager.RTC, inUserTimeInMillseconds, inIntervalInMilliseconds,
					tmpPendingIntentToRepeat);
			//-PLEASE NOTE: Initial time inUserTimeInMillseconds is not modified with TimeZone.getDefault().getRawOffset()
			// in spite of the documentation for AlarmManager.RTC which indicates that UTC is used.
			
			/*
			//Save to preferences file for notificaitons
			SharedPreferences tmpSharedPreferences = inContext.getSharedPreferences(PREFERENCES_NOTIFICATION_LIST, MODE_PRIVATE);
			tmpSharedPreferences
					.edit()
					.putStringSet(arg0, arg1)
					.commit();
			*/
		}else{
			tmpAlarmManager.cancel(tmpPendingIntentToRepeat);
			tmpPendingIntentToRepeat.cancel();
		}
	}
}
