package com.sunyata.kindmind.WidgetAndNotifications;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.sunyata.kindmind.List.OnClickToastOrActionC;

/*
 * Overview: LauncherServiceC starts actions using OnClickToastOrAction.kindAction after the user has
 *  (1) clicked on a notification for an item with an attached action or (2) clicked in a widget list
 *  where the item had an attached action
 * Extends: IntentService
 * Used in: NotificationServiceC and RemoteViewsFactoryC (in RemoteViewsServiceC.java)
 * Notes: Please note that even though NotificationServiceC has its own onHandleIntent method, this cannot be used
 *  for launching since that method is used for displaying the notification itself
 * Improvements: 
 * Documentation: 
 */
public class LauncherServiceC extends IntentService {

	private static final String TAG = "LauncherServiceC";
	
	public LauncherServiceC() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Uri tmpItemUri = inIntent.getData();
		if(tmpItemUri != null){
			//Launching the kind action
			OnClickToastOrActionC.randomKindAction(getApplicationContext(), tmpItemUri);
		}
	}
}