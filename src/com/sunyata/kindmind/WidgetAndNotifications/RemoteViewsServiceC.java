package com.sunyata.kindmind.WidgetAndNotifications;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.ListTypeM;
import com.sunyata.kindmind.List.SortingAlgorithmServiceM;

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
			Log.e(Utils.getAppTag(), "Error in constructor KindMindRemoteViewsFactory: INVALID_APPWIDGET_ID");
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
		Log.d(Utils.getAppTag(), "onCreate()");

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
	
	/*
	 * Overview: getViewAt (1) updates the template intent with an URI which can be used for launching actions,
	 *  and (2) updates and returns a RemoteViews view hierarchy (in our case only one view)
	 * Notes: The plural for RemoteViews comes from the fact that it is a View hieraracy (in our case it happens
	 *  to be only one view)
	 * Documentation: 
	 *  https://developer.android.com/reference/android/widget/RemoteViewsService.RemoteViewsFactory.html#getViewAt%28int%29
	 *  https://developer.android.com/reference/android/widget/RemoteViews.html
	 */
	@Override
	public RemoteViews getViewAt(int inPosition) {
		//Moving the cursor to the current position
		mItemCursor.moveToPosition(inPosition);
		
		//Extracting values from the database
		String tmpName = mItemCursor.getString(mItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
		long tmpItemId = mItemCursor.getLong(mItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
		Uri tmpItemUri = Utils.getItemUriFromId(tmpItemId);
		
		//Setting up the remote views object
		RemoteViews retRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_listitem);
		retRemoteViews.setTextViewText(R.id.widget_listitem_textView, tmpName);
		
		//Adding action URI to the intent template which was set for all the list rows in WidgetProviderC.onUpdate
		Intent tmpFillInIntent = new Intent();
		tmpFillInIntent.setData(tmpItemUri);
		retRemoteViews.setOnClickFillInIntent(R.id.widget_listitem_textView, tmpFillInIntent);

		return retRemoteViews;
	}
	
	/*
	 * Overview: createItemCursor (1) updates sort values with KindSort, and (2) returns a cursor pointing to
	 *  a data set for one of the three ListTypeM values
	 */
	private Cursor createItemCursor(){
		//Updating sort values
		///SortingAlgorithmM.get(mContext).updateSortValuesForListType();
		mContext.startService(new Intent(mContext, SortingAlgorithmServiceM.class));

		//Setting the type of list we like to display
		int tmpListType = mContext.getSharedPreferences(
				WidgetConfigActivityC.WIDGET_CONFIG_LIST_TYPE_PREFERENCES,
				Context.MODE_PRIVATE).getInt(String.valueOf(mWidgetId),
				ListTypeM.NOT_SET);
		if(tmpListType == ListTypeM.NOT_SET){
			Log.e(Utils.getAppTag(), "Error in onCreate: no list type given");
			return null;
		}
		
		//Getting and saving a reference to the cursor
		String tmpSortType = ItemTableM.COLUMN_KINDSORT_VALUE + " DESC";
		String tmpSelection = ItemTableM.COLUMN_LIST_TYPE + "=?";
		String[] tmpSelectionArguments = {String.valueOf(tmpListType)};
		return mContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tmpSelection, tmpSelectionArguments, tmpSortType);
	}
}