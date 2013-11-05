package com.sunyata.kindmind;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

public class BookmarkChooserActivityC extends SingleFragmentActivityC{

	private Fragment refFragment;
	
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	Fragment createFragment(Object inAttachedData){
		refFragment = (Fragment)BookmarkChooserFragmentC.newInstance(); //Attached data is not used in this subclass
		return refFragment;
	}
	
	//abstract Fragment createFragment(Object inAttachedData);


}
