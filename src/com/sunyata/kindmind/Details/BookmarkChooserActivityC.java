package com.sunyata.kindmind.Details;

import android.app.Fragment;

import com.sunyata.kindmind.SingleFragmentActivityC;

public class BookmarkChooserActivityC extends SingleFragmentActivityC{

	private Fragment refFragment;
	
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	public Fragment createFragment(Object inAttachedData){
		refFragment = (Fragment)BookmarkChooserFragmentC.newInstance(); //Attached data is not used in this subclass
		return refFragment;
	}
	
	//abstract Fragment createFragment(Object inAttachedData);


}
