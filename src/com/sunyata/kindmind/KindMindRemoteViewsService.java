package com.sunyata.kindmind;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

public class KindMindRemoteViewsService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent inIntent) {
		return new KindMindRemoteViewsFactory(this.getApplicationContext(), inIntent);
	}
	
}

class KindMindRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

	ArrayList<ListDataItemM> refList;
	Context mContext;
	
	KindMindRemoteViewsFactory(Context inContext, Intent inIntent){
		mContext = inContext;
	}

	@Override
	public int getCount() {
		return refList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		
		/*
		RemoteViews retRemoteViews = new RemoteViews(
				mContext.getPackageName(), R.id.widget_listitem);
		retRemoteViews.setTextViewText(R.id.widget_listitem, refList.get(position).getName());
		retRemoteViews.setTextViewText(R.id.widget_listitem, "asdf");
		*/

		RemoteViews retRemoteViews = new RemoteViews(
				mContext.getPackageName(), R.layout.widget_listitem); //Please note: R.layout
		retRemoteViews.setTextViewText(R.id.widget_listitem, refList.get(position).getName());

		return retRemoteViews;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onCreate() {
		Log.i(Utils.getClassName(), "onCreate()");
		
		refList = NotificationServiceC.loadDataFromJson(
				ListTypeM.KINDNESS, KindModelM.JSON_REQUESTS_KINDNESS_FILE_NAME, mContext);
		//for(ListDataItemM ldi : tmpList){}
		
	}

	@Override
	public void onDataSetChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() {
		
	}
	
}