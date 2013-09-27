package com.sunyata.kindmind;

import java.util.Date;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
//for api lvl 15 and downwards

public class NotificationServiceC extends IntentService {

	private static final String TAG = "NotificationServiceC";
	//private static ArrayList<UUID> refIdList;
	static final String PREFERENCES_NOTIFICATION_LIST = "NotificationList";

	public NotificationServiceC() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Log.d(Utils.getClassName(), "One intent received");
		
		//////////this.app = getApplication();
		
		PendingIntent tmpPendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, MainActivityC.class), 0);
		//REMOVED: this, __
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
}
