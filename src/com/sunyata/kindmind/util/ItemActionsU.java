package com.sunyata.kindmind.util;

import java.util.ArrayList;
import java.util.Arrays;

public class ItemActionsU {

	public static ArrayList<String> actionsStringToArrayList(String inActions){
		ArrayList<String> retArrayList = new ArrayList<String>(Arrays.asList(inActions.split(OtherU.ACTIONS_SEPARATOR)));
		
		//Removing any empty strings or nulls
		retArrayList.remove("");
		retArrayList.remove(null);

		return retArrayList;
	}
	
	public static String removeStringFromActions(String inActions, String inActionToRemove){
		String retString = "";
		
		//Split the string into several parts
		String[] tmpStringArray = inActions.split(OtherU.ACTIONS_SEPARATOR);
		
		boolean tmpOneItemHasBeenRemoved = false;
		
		//Rebuild the string..
		for(int i=0; i<tmpStringArray.length; i++){
			if(tmpStringArray[i].equals(inActionToRemove) && tmpOneItemHasBeenRemoved == false){
				//..but remove the first match
				tmpOneItemHasBeenRemoved = true;
			}else{
				//..but add all other parts
				if(retString.equals("")){
					retString = tmpStringArray[i];
				}else{
					retString = retString + OtherU.ACTIONS_SEPARATOR + tmpStringArray[i];
				}
			}
		}
		
		return retString;
	}

	public static int numberOfActions(String inActions) {
		if(inActions == ""){
			return 0;
		}
		int retInt = 1;
		for(int i=0; i < inActions.length(); i++){
			if(OtherU.ACTIONS_SEPARATOR.charAt(0) == inActions.charAt(i)){
				retInt++;
			}
		}
		return retInt;
	}
	
}
