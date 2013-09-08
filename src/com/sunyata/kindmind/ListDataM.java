package com.sunyata.kindmind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import android.content.Context;
import android.util.Log;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

public class ListDataM{

	//-------------------Fields and constructor
	
	private ArrayList<ListDataItemM> mList;
	private JsonSerializerM mJsonSerializer;
	private ListTypeM refListType;
	
	ListDataM(ListTypeM inListType, String inFileName, Context inContext) {
		this.loadDataFromJson(inListType, inFileName, inContext);
		refListType = inListType;
	}
	

	//------------Add, get, set, delete, clear methods
	
	//Often called after getListOfActivatedData in KindModel
	ArrayList<ListDataItemM> getListOfActivatedData() {
		ArrayList<ListDataItemM> retActivatedData = new ArrayList<ListDataItemM>();
		for(ListDataItemM ld : mList){
			if (ld.isActive()){
				retActivatedData.add(ld);
			}
		}
		return retActivatedData;
	}
	
	ListDataItemM getItem(int inPosition) {
		return mList.get(inPosition);
	}
	ListDataItemM getItem(UUID inUUID){
		for(int i = 0; i < mList.size(); i++){
			if(mList.get(i).getId().equals(inUUID)){
				return mList.get(i);
			}
		}
		Log.w(Utils.getClassName(), "Warning in method getDataItem: List data item could not be found");
		return null;
	}

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
	
	ArrayList<ListDataItemM> getListOfData() {
		return mList;
	}
	
	void clearActivated() {
		for(ListDataItemM ld : mList){
			ld.setActive(false);
		}
	}
	
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

	void delete(ListDataItemM inItem){
		mList.remove(inItem);
	}
	
	
	//-----------Save, load, toString methods
	
	boolean saveToJson(boolean inSaveActive){
		try{
			mJsonSerializer.saveData(mList, inSaveActive);
			return true;
		}catch(Exception e){
			Log.e(Utils.getClassName(), "Error in method saveToJson: Could not save data to Json file");
			return false;
		}
	}

	//private since it is only called from the constructor in this class
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
	
	String toFormattedString(){
		String retFormattedString = "List type: " + refListType + "\n";
		for(ListDataItemM ldi : mList){
			retFormattedString = retFormattedString + ldi.toFormattedString();
		}
		retFormattedString = retFormattedString + "\n\n";
		return retFormattedString;
	}


	//-----------Sorting

	//Please note that the calculation of the values used for sorting is done in another place
	void sortWithKindness(){
		Collections.sort(mList, new KindComparator());
	}
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

	void sortAlphabetically(){
		Collections.sort(mList, new AlphaBetaComparator());
	}
	class AlphaBetaComparator implements Comparator<ListDataItemM>{
		@Override
		public int compare(ListDataItemM lhs, ListDataItemM rhs) {
			return lhs.toString().compareToIgnoreCase(rhs.toString());
		}
	}


}
