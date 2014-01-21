package com.sunyata.kindmind;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BookmarkChooserFragmentC extends ListFragment {

	static final String EXTRA_RETURN_VALUE_FROM_BOOKMARK_CHOOSER_FRAGMENT = "RETURN_VALUE_FROM_BOOKMARKCHOOSERFRAGMENT";
	
	public static BookmarkChooserFragmentC newInstance(){
		BookmarkChooserFragmentC retListFragment = new BookmarkChooserFragmentC();
		return retListFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inInflater, ViewGroup inParent, Bundle inSavedInstanceState){
    	View retView = super.onCreateView(inInflater, inParent, inSavedInstanceState);
    	return retView;
	}


	//-------------------Methods for LoaderManager.LoaderCallbacks<Cursor>
	
	private SimpleCursorAdapter mCursorAdapter;
	

	private void updateListWithNewData(){
		
		/*
		String[] tmpProjection = new String[]{
				Browser.BookmarkColumns._ID, Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL};
		*/
		String[] tmpDatabaseFrom = new String[]{Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL};
		int[] tmpDatabaseTo = new int[]{android.R.id.text1, android.R.id.text2}; //R.layout.file_list_item
		
		ContentResolver tmpContentResolver = getActivity().getContentResolver(); //<--------
		Cursor tmpCursor = tmpContentResolver.query(
				android.provider.Browser.BOOKMARKS_URI, null, null, null, null);
		//android.provider.Browser.getAllBookmarks(tmpContentResolver);
		
		mCursorAdapter = new SimpleCursorAdapter(
				getActivity(), android.R.layout.simple_list_item_2, tmpCursor,
				tmpDatabaseFrom, tmpDatabaseTo, 0);
		
		setListAdapter(mCursorAdapter);
		
		tmpCursor.close();
	}
	
	@Override
    public void onActivityCreated(Bundle inSavedInstanceState){
    	super.onActivityCreated(inSavedInstanceState);
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    	
    	
    	this.updateListWithNewData();
    	
    	
    	super.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View inView, int inPosition, long inId) {
				
				String tmpBookmarkUrl = (String)((TextView) inView.findViewById(android.R.id.text2)).getText();
				
				Intent tmpIntent = new Intent();
				tmpIntent.putExtra(EXTRA_RETURN_VALUE_FROM_BOOKMARK_CHOOSER_FRAGMENT, tmpBookmarkUrl);
				getActivity().setResult(Activity.RESULT_OK, tmpIntent);
				getActivity().finish();
				
				/*
				CheckBox tmpCheckBox = ((CheckBox)inView.findViewById(R.id.list_item_activeCheckBox));
				
				tmpCheckBox.toggle();

				Uri tmpUri = Uri.parse(ListContentProviderM.CONTENT_URI + "/" + inId);
				ContentValues tmpContentValues = new ContentValues();
				tmpContentValues.put(ItemTableM.COLUMN_ACTIVE, tmpCheckBox.isChecked());
				//-Boolean stored as 0 (false) or 1 (true)
				getActivity().getContentResolver().update(tmpUri, tmpContentValues, null, null);
				
				
				//mToastBehaviour.toast(); //Också för när man klickar på själva checkboxen
				
				Cursor tmpCursor = getActivity().getContentResolver().query(tmpUri, null, null, null, null);
				tmpCursor.moveToFirst();
				String tmpFilePath = tmpCursor.getString(
						tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_FILEORDIRPATH));
				//mKindActionBehaviour.kindAction(tmpFilePath);
				 */
				
				
			}
    	});
    }
}
