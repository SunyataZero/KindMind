/*
 * Patterns: Command pattern, ______ [Patterns can be alternatives to inheritance]
 */

package com.sunyata.kindmind;

import java.util.ArrayList;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class ListDataItemM{ //implements StorableInJsonI
	
	//-------------------Fields and Constructors
	
	private UUID mId;
	private String mName;
	//protected int pClickCount = 0; //Now the patterns are used instead (includes current list and correlations with other lists)
	private double mSortValueTotal = 0;
	//private double mSortValueSingleClickFromCurrentGui = 0; //Please note that this is not saved
	private double mTempNumberOfTimesThisItemOccursInListOfPatterns = 0; //Not saved, only used temporarily for updating mTotalSortValue
	private boolean mActive = false;
	private ListTypeM mListType; //Currently not saved (not needed)

	private boolean mHardCoded = false;

	private static final String JSON_ID = "id";
	private static final String JSON_NAME = "name";
	private static final String JSON_ACTIVE = "active";
	private static final String JSON_LISTTYPE = "listtype";
	static final String NO_NAME_SET = "no_name_set";

	//Constructor for creating a ListDataItem from the GUI
	ListDataItemM(ListTypeM inListType) {
		mId = UUID.randomUUID();
		mName = NO_NAME_SET; //User has the possibilty to enter a name in the details view that is launched
		mListType = inListType;
	}
	
	//Special constructor for ListData that are added in the code
	//Therefore the name is given as parameter and a flag is set indicating that these KindActions will not be saved.
	ListDataItemM(String inName, ListTypeM inListType) {
		mId = UUID.randomUUID();
		mName = inName;
		mListType = inListType;
		mHardCoded = true;
	}
	
	//Constructor for loading from JSON
	ListDataItemM(JSONObject inJsonObject) throws JSONException {
		//this(inListType);
		mId = UUID.fromString(inJsonObject.getString(JSON_ID));
		mName = inJsonObject.getString(JSON_NAME);
		mListType = ListTypeM.valueOf(inJsonObject.getString(JSON_LISTTYPE));
		mActive = inJsonObject.getBoolean(JSON_ACTIVE);
		//mHardCoded = inJsonObject.getBoolean(JSON_HARDCODED);
	}
	
	
	//------------------------------Enum for type of list
	
	enum ListTypeM{
		SPECEV		(0),
		SUFFERING	(1),
		//HAPPINESS	(1),
		NEEDS		(2),
		KINDNESS	(3);//Change name to strategies?
		private final int mLevel; //Currently corresponds to the order from the left (starting at zero) in viewpager
		ListTypeM(int inLevel){
			mLevel = inLevel;
		}
		public int getLevel(){
			return mLevel;
		}

		public static ArrayList<ListTypeM> getEnumListByLevel(int inLevel){
			ArrayList<ListTypeM> retArrayList = new ArrayList<ListTypeM>();
			switch(inLevel){
			case 0: retArrayList.add(ListTypeM.SPECEV); break;
			case 1: retArrayList.add(ListTypeM.SUFFERING); break;//retArrayList.add(ListTypeM.HAPPINESS);
			case 2: retArrayList.add(ListTypeM.NEEDS); break;
			case 3: retArrayList.add(ListTypeM.KINDNESS); break;
			default: Log.e(Utils.getClassName(), "Error in method getEnumByLevel: case not covered"); return null;
			}
			return retArrayList;
		}
	}
	
	
	//-------------------Getter and setter methods

	String getName() {return mName;}
	void setName(String inName) {mName = inName;}
	
	boolean isActive() {return mActive;}
	void setActive(boolean active) {mActive = active;}
	
	UUID getId(){return mId;}
	
	String getSimilar() {return "Similar Auto gen, Similar set at start";}

	boolean isHardCoded() {return mHardCoded;}
	
	double getTempNumberOfTimesThisItemOccursInListOfPatterns() {return mTempNumberOfTimesThisItemOccursInListOfPatterns;}
	void setTempNumberOfTimesThisItemOccursInListOfPatterns(double inVal) {mTempNumberOfTimesThisItemOccursInListOfPatterns = inVal;}
	void incrementTempNumberOfTimesThisItemOccursInListOfPatterns(){mTempNumberOfTimesThisItemOccursInListOfPatterns++;}
	double getTotalSortValue() {return mSortValueTotal;}
	void setTotalSortValue(double totalSortValue) {mSortValueTotal = totalSortValue;}

	ListTypeM getListType() {return mListType;}
	
	
	
	//--------------------Methods for representing the object in other ways (than by reference)
	
	@Override
	public String toString(){
		return mName;
	}
	
	String toFormattedString(){
		String retFormattedString;
		retFormattedString = mName + "\n";
		return retFormattedString;
	}

	//For saving to JSON
	Object toJson(boolean inSaveActive) throws JSONException{
		JSONObject retJsonObject = new JSONObject();
		retJsonObject.put(JSON_ID, mId.toString()); //String (from UUID)
		retJsonObject.put(JSON_NAME, mName); //String
		try{
			retJsonObject.put(JSON_LISTTYPE, mListType.name()); //String (from ListTypeM) 
		}catch(Exception e){
			Log.e(Utils.getClassName(), "Error in method toJson. Stacktrace:\n");
			e.printStackTrace(); //[Refactor: remove stacktrace]
		}
		retJsonObject.put(JSON_ACTIVE, mActive); //boolean
		//retJsonObject.put(JSON_HARDCODED, mHardCoded); //boolean
		return retJsonObject;
	}

	
	//-------------------Other methods
	/*
	void incrementSingleClickSortValueFromCurrentRun(){
		mSortValueSingleClickFromCurrentGui++;
		
		//Updating the total sort value
		mSortValueTotal++;
	}

	public double getSingleClickSortValueFromCurrentRun() {
		return mSortValueSingleClickFromCurrentGui;
	}
	 */
}
