package com.sunyata.kindmind;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

public abstract class SingleFragmentActivityC extends Activity {

	abstract Fragment createFragment(Object inAttachedData);

	
	//-----------------Lifecycle methods
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(Utils.getClassName(), Utils.getMethodName());
		
		//TODO: Remove
		ListTypeM tmpListType = ListTypeM.valueOf(
				(getIntent().getSerializableExtra(ListFragmentC.EXTRA_LIST_TYPE))
				.toString());
		
		setContentView(R.layout.activity_fragment);
		FragmentManager fm = getFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		
		if(fragment == null){
			fragment = createFragment(tmpListType); //Calling the abstract method
			fm.beginTransaction()
				.add(R.id.fragmentContainer, fragment)
				.commit();
		}
	}
	
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
    @Override
    public void onResume(){
    	super.onResume();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
    @Override
    public void onStart(){
    	super.onStart();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
    @Override
    public void onStop(){
    	super.onStop();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
}
