package com.sunyata.kindmind;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

import android.app.Fragment;

public class DataDetailsActivityC extends SingleFragmentActivityC {

	private Fragment refFragment;
	
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	Fragment createFragment(Object inAttachedData){ //Fragment
		refFragment = (Fragment)DataDetailsFragmentC.newInstance((ListTypeM)inAttachedData);
		return refFragment;
	}
}
