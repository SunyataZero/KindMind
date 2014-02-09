package com.sunyata.kindmind.List;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.sunyata.kindmind.BuildConfig;
import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ItemTableM;

/*
 * Overview: CursorAdapterM maps database values into list item views, views are available through getView()
 * Extends: SimpleCursorAdapter
 * Documentation: 
 *  http://developer.android.com/reference/android/widget/SimpleCursorAdapter.html
 */
public class CursorAdapterM extends SimpleCursorAdapter{

	Context mContext;
	ListTypeM mListType;
	
	public CursorAdapterM(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, ListTypeM inListType) {
		super(context, layout, c, from, to, flags);
		mContext = context;
		mListType = inListType;
	}

	/*
	 * Overview: getView is overridden so that we can update the status of the checkboxes in the list
	 * In: position is the position in the list
	 *  convertView is a reference to the (parent) view that we get the checkbox view from;
	 *  parent is the parent of the list item (so it is the "grand parent" of the checkbox);
	 * Out: The updated View
	 * Does: Updates the checkbox child view inside a list item view
	 * Notes: A long time was spent on this method and it has been saved in the cloud as well as
	 *  part of the CustomCursorAdapter
	 * The cursor will remain open, we will get an error if we try to close it:
		01-21 20:55:18.033: E/AndroidRuntime(6357): FATAL EXCEPTION: main
		01-21 20:55:18.033: E/AndroidRuntime(6357): java.lang.IllegalStateException: attempt to re-open an already-closed object: android.database.sqlite.SQLiteQuery (mSql = SELECT _id, name, tags, active FROM item WHERE (listtype = ?)) 
		01-21 20:55:18.033: E/AndroidRuntime(6357): 	at android.database.sqlite.SQLiteClosable.acquireReference(SQLiteClosable.java:33)
		01-21 20:55:18.033: E/AndroidRuntime(6357): 	at android.database.sqlite.SQLiteQuery.fillWindow(SQLiteQuery.java:82)
		01-21 20:55:18.033: E/AndroidRuntime(6357): 	at android.database.sqlite.SQLiteCursor.fillWindow(SQLiteCursor.java:164)
		01-21 20:55:18.033: E/AndroidRuntime(6357): 	at android.database.sqlite.SQLiteCursor.onMove(SQLiteCursor.java:147)
		01-21 20:55:18.033: E/AndroidRuntime(6357): 	at android.database.AbstractCursor.moveToPosition(AbstractCursor.java:178)
		01-21 20:55:18.033: E/AndroidRuntime(6357): 	at android.database.CursorWrapper.moveToPosition(CursorWrapper.java:162)
		01-21 20:55:18.033: E/AndroidRuntime(6357): 	at android.widget.CursorAdapter.getView(CursorAdapter.java:241)
		01-21 20:55:18.033: E/AndroidRuntime(6357): 	at com.sunyata.kindmind.ListFragmentC$CustomCursorAdapter.getView(ListFragmentC.java:120)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		
		//Getting the view that we like to modify
		convertView = super.getView(position, convertView, parent);
		
    	//Getting the SQL cursor..
    	Cursor tmpLoaderItemCur = getCursor();
    	
    	//..moving to the current position (position in database is matched by position in gui list)
    	tmpLoaderItemCur.moveToPosition(position);

		//Setting status of the checkbox (checked / not checked)
    	// The other child views of this view have already been changed by the mapping done by SimpleCursorAdapter
    	// above in the super.getView() method
		long tmpActive = Long.parseLong(
				tmpLoaderItemCur.getString(tmpLoaderItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIVE)));
		CheckBox tmpCheckBox = ((CheckBox)convertView.findViewById(R.id.list_item_activeCheckBox));
		if (tmpCheckBox != null){
    		tmpCheckBox.setChecked(tmpActive != ItemTableM.FALSE);
		}
		
		if(BuildConfig.DEBUG){
			//Add the numbers to the end of the name of the list item
			TextView tmpTextView = ((TextView)convertView.findViewById(R.id.list_item_titleTextView));
			double tmpKindSortValue = Double.parseDouble(tmpLoaderItemCur.getString(
					tmpLoaderItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_KINDSORTVALUE)));
			String tmpTextToAppend = " [" + tmpKindSortValue + "]";
			tmpTextView.append(tmpTextToAppend);
		}
		
		//Cursor not closed since the loader handles the cursor
		return convertView;
	}

	/*
	 * Overview: getViewTypeCount returns the number of different types of elements for the list
	 * Details: This information is used by the Loader (<- verify this), if the number is lower than
	 *  the number of items in the list Android can reuse the item view for repainting at a lower
	 *  performance cost. We can still see different names and the reason is that the Loader knows about
	 *  the name (see method onCreateLoader in ListFragmentC).
	 * In our case we have chosen to return the total number of elements because we otherwise run into
	 *  a problem for the checkboxes which can't be included in the onCreateLoader mapping. The problem
	 *  shows itself in a strange way: After we have checked one checkbox and then scrolls down, we can
	 *  see another checked checkbox in the same relative position.
	 * Used: getViewTypeCount is called after the setListAdapter call:
		ListFragmentC$CustomCursorAdapter.getViewTypeCount() line: 172	
		ListView.setAdapter(ListAdapter) line: 466	
		ListFragmentC(ListFragment).setListAdapter(ListAdapter) line: 182	
		ListFragmentC.updateListWithNewData() line: 267	
	 * Out: The number of distinct views
	 * Notes: Please note that super.getCount() can not be used here (as suggested in threads on stackoverflow)
	 *  since the Loader has not finished when the call to this method is made. (In the cases where it is
	 *  presented loaders are not used, only adapters)
	 * The check against < 1 is done because of the following problem (only seen on physical device):
	01-20 22:24:26.398: E/AndroidRuntime(25136): java.lang.IllegalArgumentException: Can't have a viewTypeCount < 1
	01-20 22:24:26.398: E/AndroidRuntime(25136): 	at android.widget.AbsListView$RecycleBin.setViewTypeCount(AbsListView.java:5817)
	01-20 22:24:26.398: E/AndroidRuntime(25136): 	at android.widget.ListView.setAdapter(ListView.java:466)
	01-20 22:24:26.398: E/AndroidRuntime(25136): 	at android.support.v4.app.ListFragment.setListAdapter(ListFragment.java:182)
	01-20 22:24:26.398: E/AndroidRuntime(25136): 	at com.sunyata.kindmind.ListFragmentC.updateListWithNewData(ListFragmentC.java:253)
	 * This method is used in conjunction with getItemViewType (see below)
	 * Improvements: 
	 * 1. This may be a more efficient solution:
	 *  http://www.lalit3686.blogspot.in/2012/06/today-i-am-going-to-show-how-to-deal.html
	 *  We may end up having to use this solution since we need to solve the problem of showing the
	 *  checkmarks in the first place (after database loading and before any click has been done)
	 * 2. An alternative solution (very popular: 37+ votes) is presented by Vikas Patidar on StackOverflow:
	 *  http://stackoverflow.com/questions/4803756/android-cursoradapter-listview-and-checkbox
	 */
	@Override
	public int getViewTypeCount(){
		int retViewTypeCount = Utils.getListItemCount(mContext, mListType);
		if(retViewTypeCount < 1){retViewTypeCount = 1;}
		return retViewTypeCount;
	}
	
	@Override
	public int getItemViewType(int inPosition){
		return inPosition;
	}
}
