package com.sunyata.kindmind;

import java.math.BigDecimal;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

public class Utils {

	public static final String LIST_TYPE = "LIST_TYPE";
	
	
	//--------------------(Static) methods for debugging
	
	public static String getMethodName(String inPrefix){
		return "[" + inPrefix + "]" + getMethodName();
	}
	public static String getMethodName(ListTypeM inListType){
		if(inListType != null){
			return Thread.currentThread().getStackTrace()[3].getMethodName() + "[" + inListType.toString() + "]";
		}else{
			return Thread.currentThread().getStackTrace()[3].getMethodName() + "[N/A]";
		}
	}
	public static String getMethodName(){
		return Thread.currentThread().getStackTrace()[3].getMethodName();
	}
	public static String getClassName(){
		String tmpClassWithPackage = Thread.currentThread().getStackTrace()[3].getClassName();
		String[] tmpSplitString = tmpClassWithPackage.split("\\."); //NOTE: Regular experssion so "." means "all"
		//String tmpOrganization = tmpSplitString[tmpSplitString.length-3];
		//String tmpProject = tmpSplitString[tmpSplitString.length-2];
		String tmpComponent = tmpSplitString[tmpSplitString.length-1];
		return tmpComponent;
	}
	
	
	//--------------------Other
	
	public static String formatNumber(double inValue) {
		BigDecimal tmpBigDecimal = new BigDecimal(inValue);
		tmpBigDecimal = tmpBigDecimal.setScale(2, BigDecimal.ROUND_UP);
		return "" + tmpBigDecimal;
	}
}
