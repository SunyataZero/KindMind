package com.sunyata.kindmind.List;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunyata.kindmind.BuildConfig;
import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ItemTableM;

/**
 * \brief CursorAdapterM maps database values into list item views, views are available through getView()
 * 
 * Documentation: http://developer.android.com/reference/android/widget/SimpleCursorAdapter.html
 */
public class CursorAdapterM extends SimpleCursorAdapter{

	Context mContext;
	int mListType;
	
	public CursorAdapterM(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, int inListType) {
		super(context, layout, c, from, to, flags);
		mContext = context;
		mListType = inListType;
	}

	/**
	 * \brief getView updates the checkbox child view inside a list item view. This method is overridden
	 * so that we can update the status of the checkboxes in the list
	 * 
	 * @param[in] inPosition The position in the list that we are going to update
	 * @param[in,out] modConvertView The list item View holding the CheckBox we are going update
	 * @param[in] inParent The parent of the list item (so it is the "grand parent" of the checkbox)
	 * @return The modified list item view (unknown if this or is used by Android)
	 */
	@Override
	public View getView(int inPosition, View modConvertView, ViewGroup inParent){

		//Getting the SQL cursor (will not be closed)..
    	Cursor tmpLoaderItemCur = getCursor();

    	//Waiting for the cursor to be available
    	for(int i = 0; tmpLoaderItemCur.isClosed(); i++){
    		Utils.waitForCondition(500, 10, i);
    	}
    	/* -the lines above were added because of the following problem:
java.lang.IllegalStateException: attempt to re-open an already-closed object: android.database.sqlite.SQLiteQuery (mSql = SELECT _id, name, active, kindsort_value, actions FROM item WHERE (list_type=?) ORDER BY active DESC, kindsort_value DESC)
at android.database.sqlite.SQLiteClosable.acquireReference(SQLiteClosable.java:33)
at android.database.sqlite.SQLiteQuery.fillWindow(SQLiteQuery.java:82)
at android.database.sqlite.SQLiteCursor.fillWindow(SQLiteCursor.java:164)
at android.database.sqlite.SQLiteCursor.onMove(SQLiteCursor.java:147)
at android.database.AbstractCursor.moveToPosition(AbstractCursor.java:178)
at android.database.CursorWrapper.moveToPosition(CursorWrapper.java:162)
at android.support.v4.widget.CursorAdapter.getView(CursorAdapter.java:247)
at com.sunyata.kindmind.List.CursorAdapterM.getView(CursorAdapterM.java:60)
at android.widget.AbsListView.obtainView(AbsListView.java:2045)
    	 */
    	
    	//Getting the view that we like to modify
		modConvertView = super.getView(inPosition, modConvertView, inParent);
		
    	//Moving to the current position (position in database is matched by position in gui list)
    	tmpLoaderItemCur.moveToPosition(inPosition);


    	
    	/*
		//Setting status of the checkbox (checked / not checked)
    	// The other child views of this view have already been changed by the mapping done by SimpleCursorAdapter
    	// above in the super.getView() method
		long tmpActive = Long.parseLong(
				tmpLoaderItemCur.getString(tmpLoaderItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIVE)));
		CheckBox tmpCheckBox = ((CheckBox)modConvertView.findViewById(R.id.list_item_activeCheckBox));
		if (tmpCheckBox != null){
    		tmpCheckBox.setChecked(tmpActive != ItemTableM.FALSE);
		}
		*/
    	

    	
		//Updating the action indications
		String tmpActions = tmpLoaderItemCur.getString(
				tmpLoaderItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIONS));
		LinearLayout tmpRectangle = (LinearLayout)modConvertView.findViewById(R.id.list_item_indicatorRectangle);
		if(tmpActions == null || tmpActions.equals("")){
			tmpRectangle.setVisibility(View.INVISIBLE); //.setBackgroundColor(mContext.getResources().getColor(R.color.no_action));
		}else if(Utils.numberOfActions(tmpActions) == 1){
			tmpRectangle.setVisibility(View.VISIBLE);
			tmpRectangle.setBackgroundColor(mContext.getResources().getColor(R.color.one_action));
		}else if(Utils.numberOfActions(tmpActions) > 1){
			tmpRectangle.setVisibility(View.VISIBLE);
			tmpRectangle.setBackgroundColor(mContext.getResources().getColor(R.color.multiple_actions));
		}

		if(BuildConfig.DEBUG){
			//Add the numbers to the end of the name of the list item
			TextView tmpTextView = ((TextView)modConvertView.findViewById(R.id.list_item_titleTextView));
			double tmpKindSortValue = tmpLoaderItemCur.getDouble(
					tmpLoaderItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_KINDSORT_VALUE));
			String tmpTextToAppend = " [" + Utils.formatNumber(tmpKindSortValue) + "]";
			tmpTextView.append(tmpTextToAppend);
		}
		
		//Cursor not closed since the loader handles the cursor
		
		return modConvertView;
	}

	/**
	 * \brief getViewTypeCount returns the number of different types of elements for the list
	 * 
	 * Details: This information is used by the Loader (<- verify this), if the number is lower than
	 * the number of items in the list Android can reuse the item view for repainting at a lower
	 * performance cost. We can still see different names and the reason is that the Loader knows about
	 * the name (see method onCreateLoader in ListFragmentC).
	 * 
	 * In our case we have chosen to return the total number of elements because we otherwise run into
	 * a problem for the checkboxes which can't be included in the onCreateLoader mapping. The problem
	 * shows itself in a strange way: After we have checked one checkbox and then scroll down, we can
	 * see another checked checkbox in the same relative position.
	 * 
	 * Used: getViewTypeCount is called after the setListAdapter call:
		ListFragmentC$CustomCursorAdapter.getViewTypeCount() line: 172	
		ListView.setAdapter(ListAdapter) line: 466	
		ListFragmentC(ListFragment).setListAdapter(ListAdapter) line: 182	
		ListFragmentC.updateListWithNewData() line: 267	
	 * 
	 * @return The number of distinct views
	 * 
	 * Notes:
	 * + Please note that super.getCount() can not be used here (as suggested in threads on stackoverflow)
	 * since the Loader has not finished when the call to this method is made. (In the cases where it is
	 * presented loaders are not used, only adapters)
	 * + The check against < 1 is done because of the following problem (only seen on physical device):
	01-20 22:24:26.398: E/AndroidRuntime(25136): java.lang.IllegalArgumentException: Can't have a viewTypeCount < 1
	01-20 22:24:26.398: E/AndroidRuntime(25136): 	at android.widget.AbsListView$RecycleBin.setViewTypeCount(AbsListView.java:5817)
	01-20 22:24:26.398: E/AndroidRuntime(25136): 	at android.widget.ListView.setAdapter(ListView.java:466)
	01-20 22:24:26.398: E/AndroidRuntime(25136): 	at android.support.v4.app.ListFragment.setListAdapter(ListFragment.java:182)
	01-20 22:24:26.398: E/AndroidRuntime(25136): 	at com.sunyata.kindmind.ListFragmentC.updateListWithNewData(ListFragmentC.java:253)
	 * + This method is used in conjunction with getItemViewType (see below)
	 * 
	 * Improvements: 
	 * + This may be a more efficient solution:
	 * http://www.lalit3686.blogspot.in/2012/06/today-i-am-going-to-show-how-to-deal.html
	 * We may end up having to use this solution since we need to solve the problem of showing the
	 * checkmarks in the first place (after database loading and before any click has been done)
	 * + An alternative solution (very popular: 37+ votes) is presented by Vikas Patidar on StackOverflow:
	 * http://stackoverflow.com/questions/4803756/android-cursoradapter-listview-and-checkbox
	 */
	@Override
	public int getViewTypeCount(){
		int retViewTypeCount = 1;
		//Utils.getListItemCount(mContext, mListType);
		if(this.mCursor != null){
			retViewTypeCount = this.mCursor.getCount();
		}else{
			retViewTypeCount = this.getCount();
		}
		if(retViewTypeCount < 1){
			retViewTypeCount = 1;
		}
		return retViewTypeCount; //retViewTypeCount + 3; //1000; //retViewTypeCount + 1;
	}
	
	@Override
	public int getItemViewType(int inPosition){
		return inPosition;
	}
}
