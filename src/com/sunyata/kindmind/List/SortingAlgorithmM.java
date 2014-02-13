package com.sunyata.kindmind.List;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.PatternTableM;

/*
 * Overview: SortingAlgorithmM handles sorting for the app
 * Details: Sorting is done by extracting values from the database and working with them, then updating values
 *  in the kindsort column in the items table in the database
 * Notes: The reason that this class is a singleton instead of just using a static method for the sorting
 *  is because we want to be able to store the private Pattern class locally
 */
public class SortingAlgorithmM {

	//-------------------------Fields and constructor (private) plus singleton get method
	
	private Context mContext;
	private static SortingAlgorithmM sAlgorithmSingleton;
	private Thread mBackgroundThread;
	
	public static final double PATTERN_MULTIPLIER = 8;
	public static final double SIMPLE_PATTERN_MATCH_ADDITION = 1;

	private SortingAlgorithmM(Context inApplicationContext){
		mContext = inApplicationContext;
	}
	
	//Singelton get method
	public static SortingAlgorithmM get(Context inContext){
		if (sAlgorithmSingleton == null){
			sAlgorithmSingleton = new SortingAlgorithmM(inContext.getApplicationContext());
		}
		return sAlgorithmSingleton;
	}
	
	
	//-------------------------Algorithm / update methods
	
	//PLEASE NOTE: Currently the two methods below are not nessecary since we call join,
	// they have been added for the future when we might change the update
	public void updateSortValuesForListType(){ //[list update]

		//Creating a thread for the resource intensive operation of calculating the kindsort values
		mBackgroundThread = new Thread(new Runnable(){
			@Override
			public void run() {
				SortingAlgorithmM.this.updateOnBackgroundThread();
			}
		});
		
		//Starting the new thread
		mBackgroundThread.start();
		
		//Waiting for thread to finish execution
		// TODO: Move this to some of the other places marked with "[list update]"
		this.joinBackgroundThread();
	}
	public void joinBackgroundThread(){
		try {
			mBackgroundThread.join();
		} catch (InterruptedException e) {
			Log.w(Utils.getClassName(), e.getMessage());
		}
	}
	
	/*
	 * Overview: updateOnBackgroundThread updates the sort values for all list items
	 * Details: These things will have effect on the sort value:
	 *  1. The number of times an item has been marked (this has an effect even when no checkbox is active)
	 *  2. The history of correlations between a checked list item and other items. Ex: If an item has been
	 *  checked and saved with another previously and the first item is now checked, the second will get
	 *  an increase in sort value
	 * Used in: Called when a user checks or uncheks a checkbox
	 * Notes: In cases where the relevance is zero (maybe because we have not checked any of the items yet) we
	 *  still use the SIMPLE_PATTERN_MATCH_ADDITION constant, once for each time that the item has been checked
	 *  and saved into the pattern table
	 * Improvements: To cut down on object creation and thereby memory usage, remove the PatternM private class and
	 *  store "relevance" values in an array or ListArray
	 * Algorithm improvements: Many ideas, one is to use the timestamp from the patterns table to reduce relevance
	 *  for patterns from a long time back
	 */
	private void updateOnBackgroundThread(){
		//1. Go through all checked/active items and store them in an array
		ArrayList<Long> tmpCheckedItems = new ArrayList<Long>();
		String tmpSelection = ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE;
		Cursor tmpItemCur = mContext.getContentResolver().query(
				ContentProviderM.LIST_CONTENT_URI, null, tmpSelection, null, ContentProviderM.sSortType);
		for(tmpItemCur.moveToFirst(); tmpItemCur.isAfterLast() == false; tmpItemCur.moveToNext()){
			tmpCheckedItems.add(Long.parseLong(tmpItemCur.getString(
					tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ID))));
		}

		//2. Go through all patterns and save in matrix with pattern relevance..
		ArrayList<Pattern> tmpPatternMatrix = new ArrayList<Pattern>();
		Cursor tmpPatternCur = mContext.getContentResolver().query(
				ContentProviderM.PATTERN_CONTENT_URI, null, null, null, PatternTableM.COLUMN_CREATE_TIME);
		long tmpOldPatternTime = -2;
		for(tmpPatternCur.moveToFirst(); tmpPatternCur.isAfterLast() == false; tmpPatternCur.moveToNext()){
			
			long tmpNewPatternTime = Long.parseLong(tmpPatternCur.getString(
					tmpPatternCur.getColumnIndexOrThrow(PatternTableM.COLUMN_CREATE_TIME)));
			//-the time is used to identify one specific pattern (and can be the same over several rows)
			
			long tmpItemRefId = Long.parseLong(tmpPatternCur.getString(
					tmpPatternCur.getColumnIndexOrThrow(PatternTableM.COLUMN_ITEM_REFERENCE)));
			//-will be compared to the list from step 1 above
			
			//..check to see if we have gone into a new pattern in the list..
			if(tmpNewPatternTime != tmpOldPatternTime){
				tmpOldPatternTime = tmpNewPatternTime;
				
				//..if so create a new pattern list and add it to the matrix
				Pattern tmpPatternList = new Pattern();
				tmpPatternMatrix.add(tmpPatternList);
			}
			
			//..adding the item reference to the end of the last pattern in the matrix
			tmpPatternMatrix.get(tmpPatternMatrix.size() - 1).list.add(tmpItemRefId);
		}
		
		//3. Go through the newly created matrix and compare with the list from step 1 to update relevance..
		for(Pattern p : tmpPatternMatrix){
			float tmpNumberOfMatches = 0;
			float tmpDivider = (p.list.size() + tmpCheckedItems.size()) / 2;
			if(tmpDivider == 0){
				continue;
			}
			for(Long ci : tmpCheckedItems){
				if(p.list.contains(ci)){ //-PLEASE NOTE: ".contains" tests .equals (not object identity)
					tmpNumberOfMatches++;
				}
			}
			
			//..updating the relevance
			p.relevance = tmpNumberOfMatches / tmpDivider;
		}
		
		//4. Go through all list items and use the relevance to update the kindsortvalue for each item..
		tmpItemCur = mContext.getContentResolver().query(
				ContentProviderM.LIST_CONTENT_URI, null, null, null, ContentProviderM.sSortType);
		for(tmpItemCur.moveToFirst(); tmpItemCur.isAfterLast() == false; tmpItemCur.moveToNext()){
			double tmpNewKindSortValue = 0; 
			long tmpItemId = Long.parseLong(tmpItemCur.getString(
					tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ID)));
			Uri tmpItemUri = Uri.parse(ContentProviderM.LIST_CONTENT_URI + "/" + tmpItemId);
			ContentValues tmpUpdateVal;
			for(Pattern p : tmpPatternMatrix){
				if(p.list.contains(tmpItemId)){
					//..calculating and adding to the kindsort value
					tmpNewKindSortValue = tmpNewKindSortValue
							+ SIMPLE_PATTERN_MATCH_ADDITION
							+ p.relevance * PATTERN_MULTIPLIER;
				}
			}
			
			//..updating the kindsort value in the database
			tmpUpdateVal = new ContentValues();
			tmpUpdateVal.put(ItemTableM.COLUMN_KINDSORTVALUE, tmpNewKindSortValue);
			mContext.getContentResolver().update(tmpItemUri, tmpUpdateVal, null, null);
		}
		
		//Closing cursors
		tmpItemCur.close();
		tmpPatternCur.close();
	}
	
	private class Pattern{
		public float relevance;
		public ArrayList<Long> list;
		public Pattern(){
			relevance = 0;
			list = new ArrayList<Long>();
		}
	}
}