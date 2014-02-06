package com.sunyata.kindmind.List;

import java.util.ArrayList;

import com.sunyata.kindmind.Utils;


import android.util.Log;

public enum ListTypeM{
	FEELINGS(0),
	NEEDS(1),
	KINDNESS(2);//Change name to strategies?
	private final int mLevel; //Currently corresponds to the order from the left (starting at zero) in viewpager
	ListTypeM(int inLevel){
		mLevel = inLevel;
	}
	public int getLevel(){
		return mLevel;
	}

	//TODO: Currently not used, remove?
	public static ArrayList<ListTypeM> getEnumListByLevel(int inLevel){
		ArrayList<ListTypeM> retArrayList = new ArrayList<ListTypeM>();
		switch(inLevel){
		case 0: retArrayList.add(ListTypeM.FEELINGS); break;
		case 1: retArrayList.add(ListTypeM.NEEDS); break;
		case 2: retArrayList.add(ListTypeM.KINDNESS); break;
		default: Log.e(Utils.getClassName(), "Error in method getEnumByLevel: case not covered"); return null;
		}
		return retArrayList;
	}
}

