package com.sunyata.kindmind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context inContext, Intent inIntent) {
		Log.d(Utils.getClassName(), "onReceive(Context inContext, Intent inIntent)");
		Intent tmpServiceIntent = new Intent(inContext, NotificationServiceC.class);
		inContext.startService(tmpServiceIntent);
	}

}
