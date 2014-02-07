package com.sunyata.kindmind.List;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.DatabaseHelperM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.PatternTableM;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AlgorithmM {

	//-------------------------Fields and constructor (private) plus singleton get method
	
	private static AlgorithmM sKindModel;
	
	//private ArrayList<PatternM> mPatternList;
	
	//private ListOfPatterns mListOfPatterns; //Please note the this is a higher level than the ListDataM fields above
	
	//These are saved to separate files since we may want to load from them separately during runtime
	//and want to save some time, also there could be some advantage in the future of saving these in this way,
	//for example some other user may have his own list that he could supply to another user
	//(even though that would mean breaking the pattern connentions)
	public static final double PATTERN_MULTIPLIER = 8;
	
	private Context mContext;

	//private singleton constructor
	private AlgorithmM(Context inApplicationContext){
		mContext = inApplicationContext;
	}
	
	//Singelton get method
	public static AlgorithmM get(Context inContext){
		if (sKindModel == null){
			sKindModel = new AlgorithmM(inContext.getApplicationContext());
		}
		return sKindModel;
	}
	
	
	//-------------------------Algorithm / update methods
	
	//TODO: Change to sort all lists at the same time?
	
	/*
	 * Overview: updateSortValuesForListType updates the sort values for each list
	 * 
	 * Details: 
	 * 
	 * Extends: 
	 * 
	 * Implements: 
	 * 
	 * Sections:
	 * 
	 * Used in: TODO: Called when a user checks or uncheks a checkbox
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
	 * Improvements: Do the updates in a background thread instead of on the UI thread
	 * 
	 * Documentation: 
	 * 
	 */
	public static void updateSortValuesForListType(Context inContext){

		SQLiteDatabase tmpSQLiteDatabase = DatabaseHelperM.get(inContext).getWritableDatabase();
				//ListContentProviderM.getDatabaseHelper().getWritableDatabase();
		tmpSQLiteDatabase.beginTransaction();

		Cursor tmpItemCursor = inContext.getContentResolver().query(
				ContentProviderM.LIST_CONTENT_URI, null, null, null, ContentProviderM.sSortType);

		Cursor tmpPatternCursor = null;
		//= mContext.getContentResolver().query(ListContentProviderM.PATTERN_CONTENT_URI, null, null, null, Utils.sSortType);
		long tmpItemId;
		String tmpPatternSelection;
		ContentValues tmpContentValueForUpdate;
		int tmpNumberOfMatchesInPatternTable = 0;

		try{

			for(tmpItemCursor.moveToFirst(); tmpItemCursor.isAfterLast() == false; tmpItemCursor.moveToNext()){
				
				//Clearing the count
				tmpNumberOfMatchesInPatternTable = 0;
				
				tmpItemId = tmpItemCursor.getLong(
						tmpItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
				if(tmpItemId==4){
					for(int i = 0 ; i < 1;){
						i++;
					}
				}
				tmpPatternSelection = PatternTableM.COLUMN_ITEM_REFERENCE + "=" + "'" + tmpItemId + "'";
				//-PLEASE NOTE: COLUMN_ITEM_ID (not COLUMN_ID)

				tmpPatternCursor = inContext.getContentResolver().query(
						ContentProviderM.PATTERN_CONTENT_URI, null, tmpPatternSelection, null, null);

		
				for(tmpPatternCursor.moveToFirst(); tmpPatternCursor.isAfterLast() == false; tmpPatternCursor.moveToNext()){
					tmpNumberOfMatchesInPatternTable++;
				}

				
				tmpContentValueForUpdate = new ContentValues();
				tmpContentValueForUpdate.put(ItemTableM.COLUMN_KINDSORTVALUE, tmpPatternCursor.getCount());
				//tmpUri = Uri.parse(ListContentProviderM.LIST_CONTENT_URI + "/" + tmpItemId);
				//mContext.getContentResolver().update(tmpUri, tmpContentValueForUpdate, null, null);
				tmpSQLiteDatabase.update(
						ItemTableM.TABLE_ITEM, tmpContentValueForUpdate, ItemTableM.COLUMN_ID + "=" + tmpItemId, null);
			}

		}catch(Exception e){
			Log.w(Utils.getClassName(), "Warning in updateSortValuesForListType, see stacktrace for details");
			e.printStackTrace();
		}finally{
			tmpSQLiteDatabase.setTransactionSuccessful();
			tmpSQLiteDatabase.endTransaction();
			
			/////TODO: getContext().getContentResolver().notifyChange(tmpUri, null);
			inContext.getContentResolver().notifyChange(ContentProviderM.LIST_CONTENT_URI, null);
		}

		//tmpItemCursor.close();
		if(tmpPatternCursor != null){
			//tmpPatternCursor.close();
		}
		
		/*
		//Clear all the temporary click values
		for(ItemM guiLdi : this.getListOfType(inListType).getListOfData()){
			guiLdi.setTempNumberOfTimesThisItemOccursInListOfPatterns(0);
		}
		
		//First we check the number of times that each of the items in our current list (inTypeList) occurs in
		// the list of patterns and use this to do a simple update.
		for(PatternM p : mPatternList){
			ArrayList<ItemM> tmpPattern = p.get();
			for(ItemM i : tmpPattern){
				if(i.getListType() == inListType){
					//Search for the ListDataItem using the id
					ItemM refDataItem = this.getListOfType(inListType).getItem(i.getId());
					if(refDataItem != null){
						refDataItem.incrementTempNumberOfTimesThisItemOccursInListOfPatterns();
					}
				}
			}
		}
		
		//Now we update the sort values using correlations..
	
		//..to do this we first go through the previous lists to set a value on the
		//-relevance/reliability of each of the patterns..
		for(PatternM p : mPatternList){
			double tmpNumberOfMatchesBtwGuiAndPatternLdi = 0;
			double tmpLengthOfPatternDataList = p.getUntilInVal(inListType).size(); //"Number of guesses"
			if(tmpLengthOfPatternDataList == 0){
				continue;
			}
			//..go through each activated element in previous gui lists to see if it is represented
			//-in the current pattern
			prevGuiLdiList: for(ItemM prevGuiLdi : this.getCombinedListOfActivatedDataUntilInVal(inListType)){
				for(ItemM patternLdi : p.get()){
					if(prevGuiLdi.getId().equals(patternLdi.getId())){
						tmpNumberOfMatchesBtwGuiAndPatternLdi++;
						continue prevGuiLdiList;
					}
				}
			}
			p.setRelevance(tmpNumberOfMatchesBtwGuiAndPatternLdi, tmpLengthOfPatternDataList);
		}

		//..and now we use these values to set the new sort values (please note the order of the for statements)
		guiList: for(ItemM guiLdi : this.getListOfType(inListType).getListOfData()){ //Whole list is used
			for(PatternM p : mPatternList){
				for(ItemM patternLdi : p.get()){
					if(patternLdi.getId().equals(guiLdi.getId())){ //Only update the ones that we have in the patterns list
						guiLdi.setTotalSortValue(
								guiLdi.getTempNumberOfTimesThisItemOccursInListOfPatterns()
								+ PATTERN_MULTIPLIER * p.getRelevance());
						continue guiList;
					}
				}
			}
			//If we could find no match for the gui list data item, we simply set the sort value to the click value
			guiLdi.setTotalSortValue(guiLdi.getTempNumberOfTimesThisItemOccursInListOfPatterns());
		}
		
		*/
		

	}
	
	private class PatternList{
		public double relevance;
		public ArrayList<Integer> pattern;
		public PatternList(){
			relevance = 0;
			pattern = new ArrayList<Integer>();
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

