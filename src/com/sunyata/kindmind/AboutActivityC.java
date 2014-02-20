package com.sunyata.kindmind;

import android.app.Fragment;

public class AboutActivityC extends SingleFragmentActivityC {

	@Override
	public Fragment createFragment(Object inAttachedData) {
		// TODO Auto-generated method stub
		return AboutFragmentC.newInstance();
	}

}
