package com.sunyata.kindmind;

import java.util.Calendar;
import java.util.UUID;

public class ItemM {


	
	//======================================================================================================
	
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

	
	static final String NO_NAME_SET = "no_name_set";
	
	private String mActionFileOrDirPath = "";

	//private ListDataItemNotificationM mNotification;
	//private NotificationServiceC mNotificationService = null;
	private boolean mNotificationActive = false;
	private int mHourOfDay = -1;
	private int mMinute = -1;

	void setUserTime(int inHourOfDay, int inMinute){
		mHourOfDay = inHourOfDay;
		mMinute = inMinute;
	}
	long getUserTimeInMilliSeconds() {
		Calendar c = Calendar.getInstance();
		long retTimeInMilliSeconds;
		
		//Check if the time has not been set yet
		if(mHourOfDay == -1 || mMinute == -1){
			//..if so use the current time
			retTimeInMilliSeconds = c.getTimeInMillis();
		}else{
			//..otherwise use the time set in the time picker
			c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
					mHourOfDay, mMinute, 0);
			retTimeInMilliSeconds = c.getTimeInMillis();
			
			/*
			//Check if the set time is previously in the same day..
			if(retTimeInMilliSeconds < Calendar.getInstance().getTimeInMillis()){
				//..if so we add another day to the result
				retTimeInMilliSeconds = retTimeInMilliSeconds + 24 * 3600 * 1000;
			}
			*/
		}
		
		return retTimeInMilliSeconds;
	}
	boolean isNotificationActive() {
		return mNotificationActive;
	}
	void setNotificationActive(boolean inNotificationOn) {
		mNotificationActive = inNotificationOn;
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
	
	String getActionFilePath() {return mActionFileOrDirPath;}
	void setActionFilePath(String inActionFilePath) {mActionFileOrDirPath = inActionFilePath;}
	
	
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



}
