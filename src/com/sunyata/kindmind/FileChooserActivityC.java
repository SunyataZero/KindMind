package com.sunyata.kindmind;

import android.app.Fragment;

//Please note: FragmentActivity is only for support
public class FileChooserActivityC extends SingleFragmentActivityC {

	private Fragment refFragment;
	
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	Fragment createFragment(Object inAttachedData){
		refFragment = (Fragment)FileChooserFragmentC.newInstance(); //Attached data is not used in this subclass
		return refFragment;
	}
	
	
}
