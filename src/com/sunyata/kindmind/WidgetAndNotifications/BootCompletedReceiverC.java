package com.sunyata.kindmind.WidgetAndNotifications;

import com.sunyata.kindmind.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiverC extends BroadcastReceiver {

	@Override
	public void onReceive(Context inContext, Intent inIntent) {
		Log.d(Utils.getClassName(), "onReceive(Context inContext, Intent inIntent)");
		
		
		//TODO: We could try to get a reference to the application context here with the Utils method
		
		
		NotificationServiceC.setServiceNotificationAll(inContext);
	}
}
