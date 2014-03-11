package com.sunyata.kindmind;

import com.sunyata.kindmind.List.ListFragmentC;

import com.sunyata.kindmind.R;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

public abstract class SingleFragmentActivityC extends Activity {

	public abstract Fragment createFragment(Object inAttachedData);

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(Utils.getAppTag(), Utils.getMethodName());
		
		setContentView(R.layout.activity_fragment);
		FragmentManager fm = getFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if(fragment == null){
			fragment = createFragment(getIntent().getSerializableExtra(ListFragmentC.EXTRA_ITEM_URI));
			///fragment.setArguments(args)
			//-calling the abstract method
			fm.beginTransaction()
				.add(R.id.fragmentContainer, fragment)
				.commit();
		}
	}
}
