package com.sunyata.kindmind.List;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ResultReceiver;
import android.util.Log;

import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.PatternsTableM;
import com.sunyata.kindmind.util.DatabaseU;
import com.sunyata.kindmind.util.DbgU;

/**
 * Overview: SortingAlgorithmM handles sorting for the three instances of \ref ListFragmentC contained in the
 * (single instance of) \ref MainActivityC
 * 
 * Sorting is done by extracting values from the database and working with them, then updating values
 * in the kindsort column in the items table in the database
 * 
 * Notes:
 * + We use an IntentService which runs the calculations in the background
 */
public class SortingAlgorithmServiceM extends IntentService {

	public static final double PATTERN_MULTIPLIER = 8;
	public static final double SIMPLE_PATTERN_MATCH_ADDITION = 1;
	public static final int UPDATE_SERVICE_DONE = 89742;
	
	private static final String TAG = "SortingAlgorithmServiceM";
	
	public SortingAlgorithmServiceM() {
		super(TAG);
	}

	
	/**
	 * \brief updateOnBackgroundThread updates the sort values for all list items
	 * 
	 * These things will have effect on the sort value:
	 * + 1. The number of times an item has been marked (this has an effect even when no checkbox is active)
	 * + 2. The history of correlations between a checked list item and other items. Ex: If an item has been
	 * checked and saved with another previously and the first item is now checked, the second will get
	 * an increase in sort value
	 *  
	 * Used in:
	 * + Called when a user checks or uncheks a checkbox
	 * 
	 * Uses Android libs:
	 * 
	 * Notes:
	 * + In cases where the relevance is zero (maybe because we have not checked any of the items yet) we
	 * still use the SIMPLE_PATTERN_MATCH_ADDITION constant, once for each time that the item has been checked
	 * and saved into the pattern table
	 *  
	 * Improvements:
	 * + To cut down on object creation and thereby memory usage, remove the PatternM private class and
	 * store "relevance" values in an array to reduce object creation
	 * + Updating the sort values after the cursor has been closed
	 * http://stackoverflow.com/questions/11633581/attempt-to-re-open-an-already-closed-object-java-lang-illegalstateexception
	 * + Algorithm improvements: Many ideas, one is to use the timestamp from the patterns table to reduce relevance
	 * for patterns from a long time back
	 */
	@Override
	protected void onHandleIntent(Intent inIntent) {
		ArrayList<Long> tmpCheckedItems = new ArrayList<Long>();
		ArrayList<Double> tmpUpdateListSortValue = new ArrayList<Double>();
		ArrayList<Uri> tmpUpdateListUri = new ArrayList<Uri>();

		
		//1. Go through all checked/active items and store them in an array
		String tItemSel = ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE;
		Cursor tmpItemCur1 = getApplicationContext().getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tItemSel, null, null);
		tmpItemCur1.moveToFirst();
		try{
			for(; tmpItemCur1.isAfterLast() == false; tmpItemCur1.moveToNext()){
				tmpCheckedItems.add(Long.parseLong(tmpItemCur1.getString(
						tmpItemCur1.getColumnIndexOrThrow(ItemTableM.COLUMN_ID))));
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName());
		}finally{
			if(tmpItemCur1 != null){
				tmpItemCur1.close();
			}
		}
		
		
		//2. Go through all patterns and save in matrix with pattern relevance..
		ArrayList<Pattern> tmpPatternMatrix = new ArrayList<Pattern>();
		Cursor tmpPatternCur = getApplicationContext().getContentResolver().query(
				ContentProviderM.PATTERNS_CONTENT_URI, null, null, null,
				PatternsTableM.COLUMN_CREATE_TIME);
		tmpPatternCur.moveToFirst();
		try{
			long tmpOldPatternTime = -2;
			for(; tmpPatternCur.isAfterLast() == false; tmpPatternCur.moveToNext()){
				
				long tmpNewPatternTime = Long.parseLong(tmpPatternCur.getString(
						tmpPatternCur.getColumnIndexOrThrow(PatternsTableM.COLUMN_CREATE_TIME)));
				//-the time is used to identify one specific pattern (and can be the same over several rows)
				
				long tmpItemRefId = Long.parseLong(tmpPatternCur.getString(
						tmpPatternCur.getColumnIndexOrThrow(PatternsTableM.COLUMN_ITEM_REFERENCE)));
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
		}catch(Exception e){Log.wtf(DbgU.getAppTag(), DbgU.getMethodName());
		}finally{
			if(tmpPatternCur != null){
				tmpPatternCur.close();
			}
		}
		

		//3. Go through the newly created matrix and compare with the list from step 1 to update relevance..
		for(Pattern p : tmpPatternMatrix){
			float tmpNumberOfMatches = 0;
			float tmpDivider = (p.list.size() + tmpCheckedItems.size());
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
		Cursor tmpItemCur2 = getApplicationContext().getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, null, null, ContentProviderM.sSortType);
		tmpItemCur2.moveToFirst();
		try{
			for(; tmpItemCur2.isAfterLast() == false; tmpItemCur2.moveToNext()){
				double tmpNewKindSortValue = 0; 
				long tmpItemId = Long.parseLong(tmpItemCur2.getString(
						tmpItemCur2.getColumnIndexOrThrow(ItemTableM.COLUMN_ID)));
				Uri tmpItemUri = Uri.parse(ContentProviderM.ITEM_CONTENT_URI + "/" + tmpItemId);
				
				for(Pattern p : tmpPatternMatrix){
					if(p.list.contains(tmpItemId)){
						//..calculating and adding to the kindsort value
						tmpNewKindSortValue = tmpNewKindSortValue
								+ SIMPLE_PATTERN_MATCH_ADDITION
								+ p.relevance * PATTERN_MULTIPLIER;
					}
				}
				//..adding value to list (will be written to database after the cursor has been closed)
				tmpUpdateListSortValue.add(tmpNewKindSortValue);
				tmpUpdateListUri.add(tmpItemUri);
			}
		}catch(Exception e){Log.wtf(DbgU.getAppTag(), DbgU.getMethodName());
		}finally{
			if(tmpItemCur2 != null){
				tmpItemCur2.close();
			}
		}
		
		
		//5. Updating the kindsort values in the database
		ContentValues tmpUpdateVal;
		for(int i = 0; i < tmpUpdateListSortValue.size(); i++){
			tmpUpdateVal = new ContentValues();
			tmpUpdateVal.put(ItemTableM.COLUMN_KINDSORT_VALUE, tmpUpdateListSortValue.get(i));
			getApplicationContext().getContentResolver().update(tmpUpdateListUri.get(i), tmpUpdateVal, null, null);
		}
		
		//Communicate the result (used for showing a progress bar (aka "loading spinner"))
		ResultReceiver tmpResultReceiver = inIntent.getParcelableExtra(ListFragmentC.EXTRA_KINDSORT_RESULT);
		if(tmpResultReceiver != null){
			tmpResultReceiver.send(UPDATE_SERVICE_DONE, null);
		}
	}
	
	private static class Pattern{
		public float relevance;
		public ArrayList<Long> list;
		public Pattern(){
			relevance = 0;
			list = new ArrayList<Long>();
		}
	}
}
