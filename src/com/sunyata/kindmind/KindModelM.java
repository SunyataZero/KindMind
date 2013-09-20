package com.sunyata.kindmind;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

import android.content.Context;
import android.util.Log;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

public class KindModelM {

	//-------------------------Fields and constructor (private) plus singleton get method
	
	private static KindModelM sKindModel;
	
	private ListDataM mSpecEv;
	private ListDataM mSuffering; //__________-
	private ListDataM mHappiness;
	private ListDataM mNeeds;
	private ListDataM mKindness;
	//more
	private ArrayList<PatternM> mPatternList;
	
	//private ListOfPatterns mListOfPatterns; //Please note the this is a higher level than the ListDataM fields above
	
	//These are saved to separate files since we may want to load from them separately during runtime
	//and want to save some time, also there could be some advantage in the future of saving these in this way,
	//for example some other user may have his own list that he could supply to another user
	//(even though that would mean breaking the pattern connentions)
	static final String JSON_OBSERVATIONS_SPECEV_FILE_NAME = "observations_specev.json";
	static final String JSON_FEELINGS_SUFFERING_FILE_NAME = "feelings_suffering.json";
	static final String JSON_NEEDS_FILE_NAME = "needs.json";
	static final String JSON_REQUESTS_KINDNESS_FILE_NAME = "requests_kindness.json";
	public static final double PATTERN_MULTIPLIER = 8;
	
	private String mToastFeelingsString;
	private String mToastNeedsString;
	
	private Context mContext;

	private KindModelFactoryM mFactory;

	//private singleton constructor
	private KindModelM(Context inApplicationContext){
		mContext = inApplicationContext;
		mFactory = new KindModelFactoryM(mContext);

		mSpecEv = mFactory.createListData(ListTypeM.SPECEV);
		mSuffering = mFactory.createListData(ListTypeM.SUFFERING);
		mNeeds = mFactory.createListData(ListTypeM.NEEDS);
		mKindness = mFactory.createListData(ListTypeM.KINDNESS);
		
		//Load patterns
		mPatternList = new ArrayList<PatternM>();
		//this.clearDataLists();
		this.loadPatternListsFromJsonFiles();
	}
	
	//Singelton get method
	static KindModelM get(Context inContext){
		if (sKindModel == null){
			sKindModel = new KindModelM(inContext.getApplicationContext());
		}
		return sKindModel;
	}
	
	
	//-------------------------Get, clear
	
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

	void clearActivatedForAllLists(){
		mSpecEv.clearActivated();
		mSuffering.clearActivated();
		mNeeds.clearActivated();
		mKindness.clearActivated();
		//more (can a pattern be used here?)
	}
	
	void clearAllDataLists(){
		mSpecEv.clearData();
		mSuffering.clearData();
		mNeeds.clearData();
		mKindness.clearData();
	}
	
	String getFormattedStringWithAllLists(){
		String retString = "";
		retString = retString + mSpecEv.toFormattedString();
		retString = retString + mSuffering.toFormattedString();
		retString = retString + mNeeds.toFormattedString();
		retString = retString + mKindness.toFormattedString();
		return retString;
	}
	
	
	//-------------------------Algorithm / update methods
	
	//This methods updates the update sort values for each item in the list where we are at the moment
	void updateSortValuesForListType(ListTypeM inListType){
		
		//Clear all the temporary click values
		for(ListDataItemM guiLdi : this.getListOfType(inListType).getListOfData()){
			guiLdi.setTempNumberOfTimesThisItemOccursInListOfPatterns(0);
		}
		
		//First we check the number of times that each of the items in our current list (inTypeList) occurs in
		// the list of patterns and use this to do a simple update.
		for(PatternM p : mPatternList){
			ArrayList<ListDataItemM> tmpPattern = p.get();
			for(ListDataItemM i : tmpPattern){
				if(i.getListType() == inListType){
					//Search for the ListDataItem using the id
					ListDataItemM refDataItem = this.getListOfType(inListType).getItem(i.getId());
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
			prevGuiLdiList: for(ListDataItemM prevGuiLdi : this.getCombinedListOfActivatedDataUntilInVal(inListType)){
				for(ListDataItemM patternLdi : p.get()){
					if(prevGuiLdi.getId().equals(patternLdi.getId())){
						tmpNumberOfMatchesBtwGuiAndPatternLdi++;
						continue prevGuiLdiList;
					}
				}
			}
			p.setRelevance(tmpNumberOfMatchesBtwGuiAndPatternLdi, tmpLengthOfPatternDataList);
		}

		//..and now we use these values to set the new sort values (please note the order of the for statements)
		guiList: for(ListDataItemM guiLdi : this.getListOfType(inListType).getListOfData()){ //Whole list is used
			for(PatternM p : mPatternList){
				for(ListDataItemM patternLdi : p.get()){
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
	}
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
	
	
	//-------------------------Toast
	
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
	
	
	//----------------------JSON methods
	
	void savePatternListToJson() {
		ArrayList<ListDataItemM> tmpPatternToSave = new ArrayList<ListDataItemM>();
		tmpPatternToSave.addAll(mSpecEv.getListOfActivatedData());
		tmpPatternToSave.addAll(mSuffering.getListOfActivatedData());
		tmpPatternToSave.addAll(mNeeds.getListOfActivatedData());
		tmpPatternToSave.addAll(mKindness.getListOfActivatedData());
		
		//Building the filename string
		SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.US);
		String tmpFileName = tmpSimpleDateFormat.format(new Date()); //"new Date()" gives the current time
		tmpFileName = "pattern_" + tmpFileName + ".json";
		
		JsonSerializerM tmpJsonSerializer = new JsonSerializerM(mContext, tmpFileName);
		try {
			tmpJsonSerializer.saveData(tmpPatternToSave, false);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		public boolean saveToJson(ListTypeM inListType, boolean inSaveActive){
				pJsonSerializer.saveData(pList, inListType, inSaveActive);
				return true;
		}
		*/
	}

	//Please note: This method loads all the Json pattern files
	void loadPatternListsFromJsonFiles(){
		mPatternList.clear();
		File[] tmpFileListArray = mContext.getFilesDir().listFiles();
		for(int i = 0; i < tmpFileListArray.length; i++){
			File f = tmpFileListArray[i];
			if(f.getName().contains("pattern")){
				try{
					JsonSerializerM tmpJsonSerializer = new JsonSerializerM(mContext, f.getName());
					//ArrayList<ListDataItemM> tmpListToAdd = tmpJsonSerializer.loadData(ListTypeM.PATTERNS);
					PatternM tmpNewPatternToAdd = new PatternM(
							tmpJsonSerializer.loadData());
					mPatternList.add(tmpNewPatternToAdd);
				}catch(Exception e){
					Log.w(Utils.getClassName(), "Error in method LoadPatternListsFromJsonFiles");
				}
			}
		}
	}
}
