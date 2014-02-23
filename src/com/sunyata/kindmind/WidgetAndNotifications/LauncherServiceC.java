package com.sunyata.kindmind.WidgetAndNotifications;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.sunyata.kindmind.MainActivityC;
import com.sunyata.kindmind.OnClickToastOrActionC;

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
			//Marking the item
			Intent tmpMainActivityIntent = new Intent(getApplicationContext(), MainActivityC.class);
			tmpMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			tmpMainActivityIntent.putExtra(MainActivityC.EXTRA_URI_AS_STRING, tmpItemUri.toString());
			startActivity(tmpMainActivityIntent);
			/*
			FLAG_ACTIVITY_REORDER_TO_FRONT
			Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
			FLAG_ACTIVITY_NEW_TASK
			*/

			//Launching a kind action
			OnClickToastOrActionC.randomKindAction(getApplicationContext(), tmpItemUri);
		}
	}
}
