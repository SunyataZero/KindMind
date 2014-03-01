package com.sunyata.kindmind;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.sunyata.kindmind.List.ListTypeM;

//Created to hold the position as a static variable. We cannot save the position in an extra/bundle since
// the activity may be restarted when calling from outside (notification or widget click) and we have
// to use android:launchMode="standard", "singleTop" will not give the same intent to MainActivityC
// and sometimes enters the onCreate and sometimes the onNewIntent method. The solution is to store the
// position as a static variable, and that is done here in this class since we want to hide and modify
// the getter and setter methods from the outside. (An alternative would have been to override the ViewPager
// class but this was not successful because of how the ViewPager is created with findViewById)
public class ViewPagerM extends ViewPager{
	
	public ViewPagerM(Context context) {
		super(context);
	}
	public ViewPagerM(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private static int sPosition = ListTypeM.FEELINGS;
	

	@Override
	public int getCurrentItem(){
		assert(sPosition == super.getCurrentItem());
		return sPosition;
	}
	
	public void setCurrentItem(int inPos){
		sPosition = inPos;
		super.setCurrentItem(sPosition);
	}
	public void CurrentItem(int inPos, boolean inSmoothScroll){
		sPosition = inPos;
		super.setCurrentItem(sPosition, inSmoothScroll);
	}
}
