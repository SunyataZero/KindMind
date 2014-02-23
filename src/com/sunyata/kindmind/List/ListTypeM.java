package com.sunyata.kindmind.List;

import android.util.Log;

import com.sunyata.kindmind.Utils;

public class ListTypeM {
	public static final int NOT_SET = -1;
	public static final int FEELINGS = 0;
	public static final int NEEDS = 1;
	public static final int KINDNESS = 2;
	
	public static String getListTypeString(int inListType){
		switch(inListType){
		case FEELINGS: return "Feelings";
		case NEEDS: return "Needs";
		case KINDNESS: return "Kindness";
		case NOT_SET:
		default:
			Log.e(Utils.getClassName(), "Error in getListTypeString: Case not Covered or value has not been set");
			return "";
		}
	}
}
