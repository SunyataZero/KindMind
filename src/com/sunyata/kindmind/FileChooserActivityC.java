package com.sunyata.kindmind;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

import android.app.Fragment;

//PLEASE NOTE: FragmentActivity is only for support
public class FileChooserActivityC extends SingleFragmentActivityC {

	private Fragment refFragment;
	
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	Fragment createFragment(ListTypeM inListType){ //Fragment
		refFragment = (Fragment)FileChooserListFragmentC.newInstance();
		return refFragment;
	}
}
