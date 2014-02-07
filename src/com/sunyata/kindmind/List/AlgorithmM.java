package com.sunyata.kindmind.List;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.PatternTableM;

public class AlgorithmM {

	//-------------------------Fields and constructor (private) plus singleton get method
	
	private Context mContext;
	private static AlgorithmM sAlgorithm;
	
	public static final double PATTERN_MULTIPLIER = 8;
	public static final double SIMPLE_PATTERN_MATCH_ADDITION = 1;

	private AlgorithmM(Context inApplicationContext){
		mContext = inApplicationContext;
	}
	
	//Singelton get method
	public static AlgorithmM get(Context inContext){
		if (sAlgorithm == null){
			sAlgorithm = new AlgorithmM(inContext.getApplicationContext());
		}
		return sAlgorithm;
	}
	
	
	//-------------------------Algorithm / update methods
	
	/*
	 * Overview: updateSortValuesForListType updates the sort values for all list items
	 * Details: These things will have effect on the sort value:
	 *  1. The number of times an item has been marked (this has an effect even when no checkbox is active)
	 *  2. The history of correlations between a checked list item and other items. Ex: If an item has been
	 *  checked and saved with another previously and the first item is now checked, the second will get
	 *  an increase in sort value
	 * Used in: TODO: Called when a user checks or uncheks a checkbox
	 * Improvements: Do the updates in a background thread instead of on the UI thread (see UI part of Andr Cookbook)
	 * Algorithm improvements: Many ideas, one is to use the timestamp from the patterns table to reduce relevance
	 *  for patterns from a long time back
	 */
	public void updateSortValuesForListType(){

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
			ContentValues tmpUpdateVal;
			Uri tmpUri;
			for(Pattern p : tmpPatternMatrix){
				if(p.list.contains(tmpItemId)){
					
					//..calculating the kindsort value
					tmpNewKindSortValue = tmpNewKindSortValue
							+ p.relevance * PATTERN_MULTIPLIER
							+ SIMPLE_PATTERN_MATCH_ADDITION;
					
					//..updating the kindsort value in the database
					tmpUpdateVal = new ContentValues();
					tmpUpdateVal.put(ItemTableM.COLUMN_KINDSORTVALUE, tmpNewKindSortValue);
					tmpUri = Uri.parse(ContentProviderM.LIST_CONTENT_URI + "/" + tmpItemId);
					mContext.getContentResolver().update(tmpUri, tmpUpdateVal, null, null);
					///tmpSQLiteDatabase.update(ItemTableM.TABLE_ITEM, tmpContentValueForUpdate, ItemTableM.COLUMN_ID + "=" + tmpItemId, null);
				}
			}
			
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
	
	
	//-------------------------Toast
	private String mToastFeelingsString;
	private String mToastNeedsString;
	
	public String getToastString(ListTypeM inListType) {
		//-this method also updates the toast string (can be used for example for sharing)
		
		switch(inListType){
		case FEELINGS:
			mToastFeelingsString =
					getFormattedStringOfActivatedDataListItems(
					getListOfNamesForActivatedData(ListTypeM.FEELINGS))
					.toLowerCase(Locale.getDefault());
			return mToastFeelingsString;
		
		case NEEDS:
			mToastNeedsString =
					getFormattedStringOfActivatedDataListItems(
					getListOfNamesForActivatedData(ListTypeM.NEEDS))
					.toLowerCase(Locale.getDefault());
			return mToastNeedsString;
			
		default:
			Log.e(Utils.getClassName(),
					"Error in getFormattedStringOfActivatedDataListItems: case not covered in switch statement");
			return null;
		}
	}
	private ArrayList<String> getListOfNamesForActivatedData(ListTypeM inListType) {
		ArrayList<String> retActivatedData = new ArrayList<String>();
		String tmpSelection =
				ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE + " AND " +
				ItemTableM.COLUMN_LISTTYPE + "=" + "'" + inListType.toString() + "'";
		//-Please note that we are adding ' signs around the String
		Cursor tmpCursor = mContext.getContentResolver().query(
				ContentProviderM.LIST_CONTENT_URI, null, tmpSelection, null, ContentProviderM.sSortType);
		for(tmpCursor.moveToFirst(); tmpCursor.isAfterLast() == false; tmpCursor.moveToNext()){
			//add name to return list
			String tmpStringToAdd = tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
			retActivatedData.add(tmpStringToAdd);
		}
		
		//tmpCursor.close();
		return retActivatedData;
	}
	//Recursive method
	private String getFormattedStringOfActivatedDataListItems(List<String> inList) {
		if(inList.size() == 0){
			return "";
		}else if(inList.size() == 1){
			return inList.get(0);
		}else if(inList.size() == 2){
			return inList.get(0) + " and " + inList.get(1);
		}else{
			return 
				inList.get(0) +
				", " +
				getFormattedStringOfActivatedDataListItems(inList.subList(1, inList.size()));
		}
	}



	//-----------Sorting
/*
	//Please note that the calculation of the values used for sorting is done in another place
	void sortWithKindness(){
		
		mContext.getContentResolver().
		
		Collections.sort(mList, new KindComparator());
	}
	
	class KindComparator implements Comparator<ItemM>{
		@Override
		public int compare(ItemM lhs, ItemM rhs) {
			
			//First sort by which list data items are activated
			if(lhs.isActive() == false && rhs.isActive() == true){
				return 1;
			}else if(lhs.isActive() == true && rhs.isActive() == false){
				return -1;
			}
			
			//1 and -1 have been switched because we want the order to go from highest number to lowest (cmp w/ alphabetasort)
			if(lhs.getTotalSortValue() < rhs.getTotalSortValue()){
				return 1;
			}else if(rhs.getTotalSortValue() < lhs.getTotalSortValue()){
				return -1;
			}else{
				return 0;
			}
		}
	}
*/
	/*
	void sortAlphabetically(){
		Collections.sort(mList, new AlphaBetaComparator());
	}
	class AlphaBetaComparator implements Comparator<ListDataItemM>{
		@Override
		public int compare(ListDataItemM lhs, ListDataItemM rhs) {
			return lhs.toString().compareToIgnoreCase(rhs.toString());
		}
	}
*/

	
}

