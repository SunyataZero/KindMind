package com.sunyata.kindmind.Setup;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.SimpleCursorAdapter;

public class VideoChooserActivity extends ListActivity {

	
	private SimpleCursorAdapter mCursorAdapter;
	
	
	/*
	public FileChooserActivity(String iTitleColumn, String iUriColumn){
		mTitleColumn = iTitleColumn;
		mUriColumn = iUriColumn;
	}
	*/
	
	@Override
	public void onCreate(Bundle iSavedInstanceState){
		super.onCreate(iSavedInstanceState);
		
		String[] tFrom = new String[]{MediaStore.Video.Media.TITLE};
		int[] tTo = new int[]{android.R.id.text1};
		
		Cursor tMediaCursorForAdapter = getContentResolver().query(
				MediaStore.Video.Media.INTERNAL_CONTENT_URI, null, null, null, null);
		// The selection "android.provider.Browser.BOOKMARKS_URI" gives only the bookmarks (not any history)
		// "Browser.getAllBookmarks(tmpContentResolver);" will only give the urls
		
		mCursorAdapter = new SimpleCursorAdapter(
				this, android.R.layout.simple_list_item_2, tMediaCursorForAdapter,
				tFrom, tTo, 0);
		
		setListAdapter(mCursorAdapter);
	}
}