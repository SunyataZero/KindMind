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

public class RemoteViewsServiceC extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent inIntent) {
		return new RemoteViewsFactoryC(this.getApplicationContext(), inIntent);
	}
}

/*
 * Overview: RemoteViewsFactoryC works as an adapter giving the (for example) home screen process
 *  views that can be displayed (the data is taken from the database)
 * Implements: RemoteViewsService.RemoteViewsFactory which is a thin wrapper for an Adapter
 * Used in: Called by RemoteViewsServiceC.onGetViewFactory above
 * In: ApplicationContext, Intent containing the widget id
 * Notes: 
 * Improvements: 
 * Documentation: See Reto's book p596-597
 */
class RemoteViewsFactoryC implements RemoteViewsService.RemoteViewsFactory{

	Context mContext;
	Cursor mItemCursor;
	int mWidgetId;
	
	RemoteViewsFactoryC(Context inContext, Intent inIntent){
		mContext = inContext;
		mWidgetId = inIntent.getExtras().getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		if(mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID){
			Log.e(Utils.getClassName(), "Error in constructor KindMindRemoteViewsFactory: INVALID_APPWIDGET_ID");
			return;
		}
	}

	/*
	 * Overview: onCreate
	 * In: The type of list has - in the widget configuration activity - been stored in a special
	 *  preferences file which is read in this method
	 * Notes: 
	 * Improvements: 
	 * Documentation: 
	 */
	@Override
	public void onCreate() {
		Log.d(Utils.getClassName(), "onCreate()");

		mItemCursor = createItemCursor();
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
		//Creating a new cursor
		mItemCursor = createItemCursor();
	}
	
	@Override
	public RemoteViews getViewAt(int inPosition) {
		mItemCursor.moveToPosition(inPosition);
		String tmpName = mItemCursor.getString(mItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
		
		RemoteViews retRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_listitem);
		
		retRemoteViews.setTextViewText(R.id.widget_listitem, tmpName);

		return retRemoteViews;
	}
	
	private Cursor createItemCursor(){
		//Updating sort values
		SortingAlgorithmM.get(mContext).updateSortValuesForListType();

		//Setting the type of list we like to display
		String tmpListType = mContext.getSharedPreferences(WidgetConfigActivityC.WIDGET_CONFIG_LIST_TYPE_PREFERENCES,
				Context.MODE_PRIVATE).getString(String.valueOf(mWidgetId),
							WidgetConfigActivityC.WIDGET_CONFIG_LIST_TYPE_PREFERENCES_DEFAULT);
		if(tmpListType.equals(WidgetConfigActivityC.WIDGET_CONFIG_LIST_TYPE_PREFERENCES_DEFAULT)){
			Log.e(Utils.getClassName(), "Error in onCreate: no list type given");
			return null;
		}
		
		//Getting and saving a reference to the cursor
		String tmpSortType = ItemTableM.COLUMN_KINDSORT_VALUE + " DESC";
		String tmpSelection = ItemTableM.COLUMN_LIST_TYPE + " = ?";
		String[] tmpSelectionArguments = {tmpListType};
		return mContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tmpSelection, tmpSelectionArguments, tmpSortType);
	}
}