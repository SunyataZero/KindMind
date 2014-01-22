package com.sunyata.kindmind.Details;

import com.sunyata.kindmind.SingleFragmentActivityC;

import android.app.Fragment;

//Please note: FragmentActivity is only for support
public class FileChooserActivityC extends SingleFragmentActivityC{

	private Fragment refFragment;
	
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	public Fragment createFragment(Object inAttachedData){
		refFragment = (Fragment)FileChooserFragmentC.newInstance(); //Attached data is not used in this subclass
		return refFragment;
	}
}
