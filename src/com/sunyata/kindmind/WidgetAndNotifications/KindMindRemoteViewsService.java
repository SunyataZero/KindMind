package com.sunyata.kindmind.WidgetAndNotifications;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.SortingAlgorithmM;

public class KindMindRemoteViewsService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent inIntent) {
		return new KindMindRemoteViewsFactory(this.getApplicationContext(), inIntent);
	}
}

/*
 * Overview: KindMindRemoteViewsFactory
 * 
 * Details: 
 * 
 * Extends: 
 * 
 * Implements: RemoteViewsService.RemoteViewsFactory which is a this wrapper for an Adapter
 * 
 * Sections:
 * 
 * Used in: 
 * 
 * Uses app internal: 
 * 
 * Uses Android lib: 
 * 
 * In: 
 * 
 * Out: 
 * 
 * Does: 
 * 
 * Shows user: 
 * 
 * Notes: 
 * 
 * Improvements: 
 * 
 * Documentation: 
 *  See Reto's book p596-597
 */
class KindMindRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

	Context mContext;
	Cursor mItemCursor;
	int mWidgetId;
	
	KindMindRemoteViewsFactory(Context inContext, Intent inIntent){
		mContext = inContext;
		mWidgetId = inIntent.getExtras().getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		if(mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID){
			Log.e(Utils.getClassName(), "Error in constructor KindMindRemoteViewsFactory: INVALID_APPWIDGET_ID");
			return;
		}
	}

	@Override
	public void onCreate() {
		Log.d(Utils.getClassName(), "onCreate()");

		//Updating sort values
		SortingAlgorithmM.get(mContext).updateSortValuesForListType();

		//Setting the type of list we like to display
		String tmpListType = mContext.getSharedPreferences(WidgetConfigActivityC.WIDGET_CONFIG_LIST_TYPE,
				Context.MODE_PRIVATE).getString(String.valueOf(mWidgetId),
							WidgetConfigActivityC.PREFERENCE_LIST_TYPE_DEFAULT);
		if(tmpListType.equals(WidgetConfigActivityC.PREFERENCE_LIST_TYPE_DEFAULT)){
			Log.e(Utils.getClassName(), "Error in onCreate: no list type given");
			return;
		}
		
		//Getting and saving a reference to the cursor
		String tmpSortType = ItemTableM.COLUMN_KINDSORT_VALUE + " DESC";
		String tmpSelection = ItemTableM.COLUMN_LIST_TYPE + " = ?";
		String[] tmpSelectionArguments = {tmpListType};
		mItemCursor = mContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tmpSelection, tmpSelectionArguments, tmpSortType);
		
	}
	@Override
	public void onDestroy() {
		mItemCursor.close();
	}

	@Override
	public int getCount() {
		if(mItemCursor != null){
			return mItemCursor.getCount();
		}else{
			return 0;
		}
	}

	@Override
	public long getItemId(int inPosition) {
		if(mItemCursor != null){
			return mItemCursor.getLong(mItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
		}else{
			return inPosition;
		}
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public int getViewTypeCount() {
		return 1;
	}
	
	@Override
	public RemoteViews getLoadingView() {
		return null;
	}
	
	/*
	 * Overview: onDataSetChanged is called when WidgetManager.notifyAppWidgetViewDataChanged has been invoked
	 *  and can be used for updating the list of data (it is the most efficient way since the other three alternatives
	 *  all recreate the whole widget).
	 * Notes: Will always be called before the Widget is updated
	 * Improvements: In the future we may want to implement this method as another way to update the widget
	 *  (currently the widget is updated after an interval)
	 *  Documentation: PA4AD p598
	 */
	@Override
	public void onDataSetChanged() {
		
	}
	
	@Override
	public RemoteViews getViewAt(int inPosition) {
		mItemCursor.moveToPosition(inPosition);
		String tmpName = mItemCursor.getString(mItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
		
		RemoteViews retRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_listitem);
		
		retRemoteViews.setTextViewText(R.id.widget_listitem, tmpName);

		return retRemoteViews;
	}
}