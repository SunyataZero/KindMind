package com.sunyata.kindmind.List;

/*
 * Overview: ListTypeM represents the type of list. Each item in the item database table gets one of these
 *  values associated with it when created
 * Improvements: Making it possible to use something like "ListTypeM.FEELINGS" as the int 0, this could
 *  be useful for clarity in switch statements in for example MainActivityC
 * Documentation:
 *  http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
 */
public enum ListTypeM{
	FEELINGS(0),
	NEEDS(1),
	KINDNESS(2);
	private final int mLevel; //-Corresponds to the order from the left (starting at zero) in viewpager
	ListTypeM(int inLevel){
		mLevel = inLevel;
	}
	public int getLevel(){
		return mLevel;
	}
}
