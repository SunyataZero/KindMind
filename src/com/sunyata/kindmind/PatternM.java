package com.sunyata.kindmind;

import java.util.ArrayList;

public class PatternM{
	
	//-----------------------Fields and constructor

	ArrayList<ItemM> mList;
	private double mRelevance;
	private int mCurrentPosition = 0;
	
	public PatternM(ArrayList<ItemM> inList){
		//mList = new ArrayList<ItemM>();
		mList = inList;
		//mIterator = new ListDataItemIterator(mList);
	}
	
	
	//-----------------------Get and set
	
	public ArrayList<ItemM> get(){return mList;}
	
	public ArrayList<ItemM> getUntilInVal(ListTypeM inListType) {
		ArrayList<ItemM> retArrayList = new ArrayList<ItemM>();
		for(ItemM ldi : mList){
			if(ldi.getListType().getLevel() < inListType.getLevel()){
				retArrayList.add(ldi);
			}
		}
		return retArrayList;
	}
	
	public void setRelevance(double inNumberOfMatches, double inLengthOfList){
		//if(inRelevance ) //Remove this and let negative associations filter things away?
		//mRelevance = mRelevance + inRelevance;
		
		//Using an algorithm to set the new relevance of the pattern
		double tmpRelevance = inNumberOfMatches / inLengthOfList;
		tmpRelevance = Math.pow(tmpRelevance, 0.5); //Square root
		
		mRelevance = tmpRelevance; //Maybe math formula here like Math.pow(inRelevance, 2)
	}
	public double getRelevance(){return mRelevance;}

	public int getCurrentPosition() {return mCurrentPosition;}
}