package com.sunyata.kindmind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

public class ListTableM {

	static final String TABLE_LIST = "list"; 
	static final String COLUMN_ID = BaseColumns._ID;
	static final String COLUMN_NAME = "name";
	static final String COLUMN_LISTTYPE = "listtype";
	static final String COLUMN_ACTIVE = "active";
	static final String COLUMN_FILEORDIRPATH = "fileordirpath";
	static final String COLUMN_NOTIFICATIONACTIVE = "notificationactive";
	static final String COLUMN_NOTIFICATIONTIME = "notificationtime";
	//Please remember to update the verifyColumns method and the updrade method when we add new columns
	
	private static final String CREATE_DATABASE =
			"CREATE TABLE " + TABLE_LIST + "("
			+ COLUMN_ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " STRING NOT NULL "
			+ COLUMN_LISTTYPE + " STRING NOT NULL "
			+ COLUMN_ACTIVE + " INTEGER NOT NULL "
			+ COLUMN_FILEORDIRPATH + " STRING "
			+ COLUMN_NOTIFICATIONACTIVE + " INTEGER NOT NULL "
			+ COLUMN_NOTIFICATIONTIME + " INTEGER"
			+ ");";
	
	static void onCreate(SQLiteDatabase inDataBase){
		inDataBase.execSQL(CREATE_DATABASE);
	}
	
	static void onUpgrade(SQLiteDatabase inDataBase, int inOldVersion, int inNewVersion){
		
		Log.w(Utils.getClassName(), "Upgrade removed the database with a previous version and created a new one, " +
				"all data was deleted");
		
		inDataBase.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
		onCreate(inDataBase);
		/*TODO: Change this method
		 * 1. Backup
		 * and/or
		 * 2. Write upgrade code
		 */
	}
	
	//======================================================================================================
	
	
	//Often called after getListOfActivatedData in KindModel
	ArrayList<ListDataItemM> getListOfActivatedData() {
		ArrayList<ListDataItemM> retActivatedData = new ArrayList<ListDataItemM>();
		/*
		for(ListDataItemM ld : mList){
			if (ld.isActive()){
				retActivatedData.add(ld);
			}
		}
		*/
		return retActivatedData;
	}
	
	
	
	//------------Add, get, set, delete, clear methods
	

	/*
	ListDataItemM getItem(int inPosition) {
		return mList.get(inPosition);
	}
	*/
	/*
	ListDataItemM getItem(UUID inUUID){
		for(int i = 0; i < mList.size(); i++){
			if(mList.get(i).getId().equals(inUUID)){
				return mList.get(i);
			}
		}
		Log.w(Utils.getClassName(), "Warning in method getDataItem: List data item could not be found");
		return null;
	}
	*/

	/*
	boolean addItem(ListDataItemM inListDataItem, boolean inUserIsAddingThroughGui){
		boolean tmpItemWithNameAlreadyExists = false;
		//Only add a new item if its name (not id) does not already exist in the list
		for(ListDataItemM ldi : mList){
			String tmpListName = ldi.getName();
			String tmpInName = inListDataItem.getName();
			if(tmpListName.compareTo(tmpInName) == 0){
				tmpItemWithNameAlreadyExists = true;
			}
		}
		//..now that we have made the check we can add
		if(!tmpItemWithNameAlreadyExists || inUserIsAddingThroughGui){
			return mList.add(inListDataItem);
		}
		return false;
	}
	*/
	/*
	ArrayList<ListDataItemM> getListOfData() {
		return mList;
	}
	*/
	/*
	void clearActivated() {
		for(ListDataItemM ld : mList){
			ld.setActive(false);
		}
	}
	*/
	/*
	void clearData() {
		//Please note: We have gotten ConcurrentModificationException here so therefore we use another solution
		//-than simply using the "remove()" method.
		ArrayList<ListDataItemM> tmpNewScaledDownList = new ArrayList<ListDataItemM>();
		for(ListDataItemM ldi : mList){
			if(ldi.isHardCoded() == true){
				tmpNewScaledDownList.add(ldi);
			}
		}
		mList = tmpNewScaledDownList;
	}
	*/

	/*
	void delete(ListDataItemM inItem){
		mList.remove(inItem);
	}
	*/
	
	
	//-----------Save, load, toString methods
	/*
	boolean saveToJson(boolean inSaveActive){
		try{
			mJsonSerializer.saveData(mList, inSaveActive);
			return true;
		}catch(Exception e){
			Log.e(Utils.getClassName(), "Error in method saveToJson: Could not save data to Json file");
			return false;
		}
	}
*/
	
	//private since it is only called from the constructor in this class
	/*
	private void loadDataFromJson(ListTypeM inListType, String inFileName, Context inContext) {
		mJsonSerializer = new JsonSerializerM(inContext, inFileName);
		try{
			mList = mJsonSerializer.loadData();
			Log.i(Utils.getClassName(), "Done loading from JSON file");
			//return true;
		}catch(Exception e){
			//This will happen when we don't have any file yet
			mList = new ArrayList<ListDataItemM>();
			//return false;
		}
	}
	*/
	
	/*
	String toFormattedString(){
		String retFormattedString = "List type: " + refListType + "\n";
		for(ListDataItemM ldi : mList){
			retFormattedString = retFormattedString + ldi.toFormattedString();
		}
		retFormattedString = retFormattedString + "\n\n";
		return retFormattedString;
	}
	*/


	//-----------Sorting

	//Please note that the calculation of the values used for sorting is done in another place
	/*
	void sortWithKindness(){
		Collections.sort(mList, new KindComparator());
	}
	*/
	
	class KindComparator implements Comparator<ListDataItemM>{
		@Override
		public int compare(ListDataItemM lhs, ListDataItemM rhs) {
			
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

	
	
	//===================================FROM KindModelM===============================================
	
	//-------------------------Toast

	/*
	String getToastString(ListTypeM inListType) {
		//-this method also updates the toast string (can be used for example for sharing)
		switch(inListType){
		case SUFFERING:
			mToastFeelingsString =
					getFormattedStringOfActivatedDataListItems(
					getListOfType(ListTypeM.SUFFERING).getListOfActivatedData())
					.toLowerCase(Locale.getDefault());
			return mToastFeelingsString;
					
		case NEEDS:
			mToastNeedsString =
					getFormattedStringOfActivatedDataListItems(
					getListOfType(ListTypeM.NEEDS).getListOfActivatedData())
					.toLowerCase(Locale.getDefault());
			return mToastNeedsString;
			
		default:
			Log.e(Utils.getClassName(),
					"Error in getFormattedStringOfActivatedDataListItems: case not covered in switch statement");
			return null;
		}
	}
	*/

	//Recursive method
	private String getFormattedStringOfActivatedDataListItems(List<ListDataItemM> inList) {
		if(inList.size() == 0){
			return "";
		}else if(inList.size() == 1){
			return inList.get(0).getName();
		}else if(inList.size() == 2){
			return inList.get(0).getName() + " and " + inList.get(1).getName();
		}else{
			return 
				inList.get(0).getName() +
				", " +
				getFormattedStringOfActivatedDataListItems(inList.subList(1, inList.size()));
		}
	}
	
	
	
	/*
	private ArrayList<ListDataItemM> getCombinedListOfActivatedDataUntilInVal(ListTypeM inListType) {

		ArrayList<ListDataItemM> retArrayList = new ArrayList<ListDataItemM>();
		
		//Using switch without break
		switch(inListType.getLevel()){
		case 3:
			retArrayList.addAll(mNeeds.getListOfActivatedData());
			//no break, will continue
		case 2:
			retArrayList.addAll(mSuffering.getListOfActivatedData());
			//no break, will continue
		case 1:
			retArrayList.addAll(mSpecEv.getListOfActivatedData());
			//no break, will continue
		case 0:
			//do nothing since there is nothing before this
		}
		return retArrayList;
	}
	*/
	

	
	
	//-------------------------Get, clear

	/*
	//Often called after the singleton get() call to access a specific list
	ListDataM getListOfType(ListTypeM inListType) {
		switch(inListType){
			case SPECEV: return mSpecEv;
			case SUFFERING: return mSuffering;
			case NEEDS: return mNeeds;
			case KINDNESS: return mKindness;
			//more
			default: Log.e(Utils.getMethodName(), "Error in method getListOfType: ListTypeM not covered");
			return null;
		}
	}
	*/

/*	
	void clearActivatedForAllLists(){
		__________.clearActivated();
	}
	*/

	/*
	void clearAllDataLists(){
		mSpecEv.clearData();
		mSuffering.clearData();
		mNeeds.clearData();
		mKindness.clearData();
	}
	*/
	
	/*
	String getFormattedStringWithAllLists(){
		String retString = "";
		retString = retString + ___________.toFormattedString();
		return retString;
	}
	*/

	
}
