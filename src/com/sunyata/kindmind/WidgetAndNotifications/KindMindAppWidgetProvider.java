package com.sunyata.kindmind.WidgetAndNotifications;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sunyata.kindmind.R;

//Documentation: http://developer.android.com/guide/topics/appwidgets/index.html
public class KindMindAppWidgetProvider extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context inContext, AppWidgetManager inAppWidgetManager, int[] inAppWidgetIds){
		
		//Going through all widgets placed (could be more than one)
		for(int i = 0; i < inAppWidgetIds.length; i++){
			
			/*
			Intent tmpIntent = new Intent(inContext, MainActivityC.class);
			PendingIntent tmpPendingIntent = PendingIntent.getActivity(inContext, 0, tmpIntent, 0);
			
			RemoteViews tmpRemoteViews = new RemoteViews(inContext.getPackageName(), R.layout.kindmind_widget);
			//tmpRemoteViews.setOnClickPendingIntent(R.id.button, pendingIntent)
			*/
			
			Intent tmpRVServiceIntent = new Intent(inContext, KindMindRemoteViewsService.class);
			tmpRVServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, inAppWidgetIds[i]);
			tmpRVServiceIntent.setData(Uri.parse(tmpRVServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));
			
			RemoteViews tmpRemoteViews = new RemoteViews(inContext.getPackageName(), R.layout.widget);
			//tmpRemoteViews.setRemoteAdapter(inAppWidgetIds[i], R.id.widget_listview, tmpRVServiceIntent);
			tmpRemoteViews.setRemoteAdapter(R.id.widget_listview, tmpRVServiceIntent);
			
			tmpRemoteViews.setEmptyView(R.id.widget_listview, R.id.widget_empty_view);
			
			inAppWidgetManager.updateAppWidget(inAppWidgetIds[i], tmpRemoteViews);
		}
		super.onUpdate(inContext, inAppWidgetManager, inAppWidgetIds);
	}
}
