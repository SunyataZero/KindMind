package com.sunyata.kindmind;

import android.app.Fragment;

public class DetailsActivityC extends SingleFragmentActivityC {

	private Fragment refFragment;
	
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	Fragment createFragment(Object inAttachedData){ //Fragment
		refFragment = (Fragment)DetailsFragmentC.newInstance(inAttachedData);
		return refFragment;
	}
}
