package com.sunyata.kindmind.Details;

import com.sunyata.kindmind.SingleFragmentActivityC;

import android.app.Fragment;

public class ItemSetupActivityC extends SingleFragmentActivityC {

	private Fragment refFragment;
	
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	public Fragment createFragment(Object inAttachedData){ //Fragment
		refFragment = (Fragment)ItemSetupFragmentC.newInstance(inAttachedData);
		return refFragment;
	}
}
