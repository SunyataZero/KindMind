package com.sunyata.kindmind;

import java.util.ArrayList;

import android.content.Context;

public class KindModelM {

	//-------------------------Fields and constructor (private) plus singleton get method
	
	private static KindModelM sKindModel;
	
	private ArrayList<PatternM> mPatternList;
	
	//private ListOfPatterns mListOfPatterns; //Please note the this is a higher level than the ListDataM fields above
	
	//These are saved to separate files since we may want to load from them separately during runtime
	//and want to save some time, also there could be some advantage in the future of saving these in this way,
	//for example some other user may have his own list that he could supply to another user
	//(even though that would mean breaking the pattern connentions)
	public static final double PATTERN_MULTIPLIER = 8;
	
	private String mToastFeelingsString;
	private String mToastNeedsString;
	
	private Context mContext;

	//private singleton constructor
	private KindModelM(Context inApplicationContext){
		mContext = inApplicationContext;

		/*
		mSpecEv = mFactory.createListData(ListTypeM.SPECEV);
		mSuffering = mFactory.createListData(ListTypeM.SUFFERING);
		mNeeds = mFactory.createListData(ListTypeM.NEEDS);
		mKindness = mFactory.createListData(ListTypeM.KINDNESS);
		*/
		
		//Load patterns
		mPatternList = new ArrayList<PatternM>();
		this.loadPatternListsFromJsonFiles();
		
	}
	
	//Singelton get method
	static KindModelM get(Context inContext){
		if (sKindModel == null){
			sKindModel = new KindModelM(inContext.getApplicationContext());
		}
		return sKindModel;
	}
	
	

	
	
	//-------------------------Algorithm / update methods
	
	//This methods updates the update sort values for each item in the list where we are at the moment
	void updateSortValuesForListType(ListTypeM inListType){
		
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

	
	
	//----------------------JSON methods
	
	void savePatternListToJson() {
		ArrayList<ItemM> tmpPatternToSave = new ArrayList<ItemM>();
		
		//Get all the activated items from the whole list
		
		//Cursor tmpCursor = mContext.getContentResolver().query
		
		
		//tmpPatternToSave.addAll(mSpecEv.getListOfActivatedData());
	}

	//Please note: This method loads all the Json pattern files
	void loadPatternListsFromJsonFiles(){
		/*
		mPatternList.clear();
		File[] tmpFileListArray = mContext.getFilesDir().listFiles();
		for(int i = 0; i < tmpFileListArray.length; i++){
			File f = tmpFileListArray[i];
			if(f.getName().contains("pattern")){
				try{
					JsonSerializerM tmpJsonSerializer = new JsonSerializerM(mContext, f.getName());
					//ArrayList<ItemM> tmpListToAdd = tmpJsonSerializer.loadData(ListTypeM.PATTERNS);
					PatternM tmpNewPatternToAdd = new PatternM(
							tmpJsonSerializer.loadData());
					mPatternList.add(tmpNewPatternToAdd);
				}catch(Exception e){
					Log.w(Utils.getClassName(), "Error in method LoadPatternListsFromJsonFiles");
				}
			}
		}
		*/
	}
	
	
	
	
	
	//=======================================MOVED FROM ListDataM=============================================
	

	
}
