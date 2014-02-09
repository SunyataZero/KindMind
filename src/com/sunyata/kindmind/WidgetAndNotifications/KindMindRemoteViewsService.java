package com.sunyata.kindmind.WidgetAndNotifications;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.List.SortingAlgorithmM;
import com.sunyata.kindmind.List.ListTypeM;

public class KindMindRemoteViewsService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent inIntent) {
		return new KindMindRemoteViewsFactory(this.getApplicationContext(), inIntent);
	}
	
}

class KindMindRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

/////ArrayList<ItemM> refList;
	Context mContext;
	
	KindMindRemoteViewsFactory(Context inContext, Intent inIntent){
		mContext = inContext;
	}

	@Override
	public int getCount() {
		/////return refList.size();
		
		String tmpSortType = ItemTableM.COLUMN_KINDSORTVALUE;
		String tmpSelection = ItemTableM.COLUMN_LISTTYPE + " = ?";
		String[] tmpSelectionArguments = {ListTypeM.NEEDS.toString()};
		Cursor tmpCursor = mContext.getContentResolver().query(
				ContentProviderM.LIST_CONTENT_URI, null, tmpSelection, tmpSelectionArguments, tmpSortType);
		
		//tmpCursor.close();
		return tmpCursor.getCount();
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
		
		SortingAlgorithmM.get(mContext).updateSortValuesForListType();

		String tmpSortType = ItemTableM.COLUMN_KINDSORTVALUE + " DESC";
		String tmpSelection = ItemTableM.COLUMN_LISTTYPE + " = ?";
		String[] tmpSelectionArguments = {ListTypeM.NEEDS.toString()};
		Cursor tmpCursor = mContext.getContentResolver().query(
				ContentProviderM.LIST_CONTENT_URI, null, tmpSelection, tmpSelectionArguments, tmpSortType);
		tmpCursor.moveToPosition(position);
		String tmpName = tmpCursor.getString(
				tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
		
		RemoteViews retRemoteViews = new RemoteViews(
				mContext.getPackageName(), R.layout.widget_listitem); //Please note: R.layout
		retRemoteViews.setTextViewText(R.id.widget_listitem, tmpName);

		//tmpCursor.close();
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

		/*
		refList = NotificationServiceC.loadDataFromJson(
				ListTypeM.KINDNESS, KindModelM.JSON_REQUESTS_KINDNESS_FILE_NAME, mContext);
		*/
	}

	@Override
	public void onDataSetChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() {
		
	}
	
}