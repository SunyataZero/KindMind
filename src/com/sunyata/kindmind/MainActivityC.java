package com.sunyata.kindmind;

import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;
import com.sunyata.kindmind.ListFragmentC.ListFragmentDataAdapterC;

public class MainActivityC extends FragmentActivity implements MainActivityCallbackListenerI{

	//------------------------Fields
	
	private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ListFragmentC mSpecEvListFragment;
    
    private ListFragmentC mSufferingListFragment;
    private ListFragmentC mNeedListFragment;
    private ListFragmentC mKindnessListFragment;

    
    //------------------------Lifecycle methods, including onCreate
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//PLEASE NOTE: This method may be called not only at the start of the application but also
    	// later to recreate the activity, for example after coming back from a details screen.
        super.onCreate(savedInstanceState);
        Log.d(Utils.getClassName(), Utils.getMethodName());
        setContentView(R.layout.activity_main);
        
        // Create the adapter that will return a fragment for each of the sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //mViewPager.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        mViewPager.setOffscreenPageLimit(0);
        //-We are using this becase getAdapter sometimes gives null, for more info, see this link:
        // http://stackoverflow.com/questions/13651262/getactivity-in-arrayadapter-sometimes-returns-null
        //mViewPager.setOffscreenPageLimit(4); //This only partly solves the problem with NPE in onPageScrollStateChanged

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int pos) {
				//updateViewPagerView(ListTypeM.values()[mViewPager.getCurrentItem()]);
				updateViewPagerView(ListTypeM.getEnumListByLevel(mViewPager.getCurrentItem()).get(0));
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				//updateViewPagerView(ListTypeM.getEnumListByLevel(mViewPager.getCurrentItem()).get(0));
			}
			@Override
			public void onPageScrollStateChanged(int inState) {
				//onPageSelected seems to be more helpful to use
				//updateViewPagerView(ListTypeM.getEnumListByLevel(mViewPager.getCurrentItem()).get(0));
			}
		});

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
        //mFeelingListFragment.toString();
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
    
	@Override
	public void onActivityResult(int requestcode, int resultcode, Intent intent){
		Log.d(Utils.getClassName(), Utils.getMethodName());
	}
	
	
	//-------------------Pager adapter
	
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Object instantiateItem (ViewGroup container, int position){
        	switch(position){
        	case 0:
        		mSpecEvListFragment = ListFragmentC.newInstance(ListTypeM.SPECEV,
        				(MainActivityCallbackListenerI)MainActivityC.this);
        		//((DataAdapter)mSpecEvListFragment.getListAdapter()).notifyDataSetChanged();
        		break;
        	case 1:
        		mSufferingListFragment = ListFragmentC.newInstance(ListTypeM.SUFFERING,
        				(MainActivityCallbackListenerI)MainActivityC.this);
        		//((DataAdapter)mSufferingListFragment.getListAdapter()).notifyDataSetChanged();
        		break;
        	case 2:
        		mNeedListFragment = ListFragmentC.newInstance(ListTypeM.NEEDS,
        				(MainActivityCallbackListenerI)MainActivityC.this);
        		//((DataAdapter)mNeedListFragment.getListAdapter()).notifyDataSetChanged();
        		break;
        	case 3:
        		mKindnessListFragment = ListFragmentC.newInstance(ListTypeM.KINDNESS,
        				(MainActivityCallbackListenerI)MainActivityC.this);
        		//((DataAdapter)mKindnessListFragment.getListAdapter()).notifyDataSetChanged();
        		break;
        	default:
        		Log.e(Utils.getClassName(), "Error in instantiateItem: Case not covered");
        		break;
        	}
        	
        	return super.instantiateItem(container, position);
        }
        //getItem is called to instantiate the page for the given position.
        @Override
        public android.support.v4.app.Fragment getItem(int inPosition) {
        	switch (inPosition){
        		case 0: return mSpecEvListFragment;
		    	case 1:	return mSufferingListFragment; //mFeelingListFragment; //Has already been created in the constructor
				case 2: return mNeedListFragment;
		    	case 3: return mKindnessListFragment;
		    	default: Log.e(Utils.getClassName(), "Error in method getItem: case not covered");return null;
        	}
        }
        @Override
        public int getCount() {
            return 4;
        }
        @Override
        public CharSequence getPageTitle(int inPosition) {
            Locale l = Locale.getDefault();
            switch (inPosition) {
            	case 0:
            		return getString(R.string.observations_pager_title).toUpperCase(l);
                case 1:
                    return getString(R.string.feelings_pager_title).toUpperCase(l);
                case 2:
                    return getString(R.string.needs_pager_title).toUpperCase(l);
                case 3:
                    return getString(R.string.requests_pager_title).toUpperCase(l);
            }
            return null;
        }
    }

    
    //----------------------Other methods
    
	void updateViewPagerView(ListTypeM inListType){
		//NOTE: CANNOT CALL getListAdapter() ON THE FRAGMENT FROM HERE BECAUSE THE
		//FRAGMENT'S ONACTIVITYCREATED METHOD HAS NOT SET UP THE FRAGMENT WITH THE ADAPTER.

		KindModelM.get(getApplicationContext()).loadPatternListsFromJsonFiles();
		KindModelM.get(getApplicationContext()).updateSortValuesForListType(inListType);
		KindModelM.get(getApplicationContext()).getListOfType(inListType).sortWithKindness();
		//-Sort with Kindness has been chosen here instead of alphabeta since prioritize giving the user
		// quick access to the most important information

		//Setting the name and updating
		switch(inListType){
		case SPECEV:
			setTitle(R.string.events_top_title);
			
			//[spinner]
			
			updateFragmentList(mSpecEvListFragment);
			break;
		case SUFFERING:
			setTitle(R.string.suffering_top_title);
			updateFragmentList(mSufferingListFragment);
			break;
		case NEEDS:
			setTitle(R.string.needs_top_title);
			updateFragmentList(mNeedListFragment);
			break;
		case KINDNESS:
			setTitle(R.string.strategies_top_title);
			updateFragmentList(mKindnessListFragment);
			break;
		default:
			Log.e(Utils.getClassName(), "Error in updateViewPagerView: Case not covered");
			return;
		}
	}
    
	public void clearActivated(){
		KindModelM.get(this).clearActivatedForAllLists();
	}
	
	public void clearData() { //Called from the test project
		this.clearActivated();
		KindModelM.get(this).clearAllDataLists();
		updateFragmentList(mSpecEvListFragment);
		updateFragmentList(mSufferingListFragment);
		updateFragmentList(mNeedListFragment);
		updateFragmentList(mKindnessListFragment);
	}
	ListFragmentC tmpListFragment = null; //Has to be put outside for it to be accessible inside the Runnable
	private void updateFragmentList(ListFragmentC inListFragment){
		tmpListFragment = inListFragment;
		if(tmpListFragment == null){
			return;
		}
		if(tmpListFragment.getListAdapter() == null){
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try{
					((ListFragmentDataAdapterC)tmpListFragment.getListAdapter()).notifyDataSetChanged();
					tmpListFragment.getListView().invalidateViews();
				}catch(Exception e){
					Log.w(Utils.getClassName(), "Error in updateFragmentList: " + e.getMessage());
				}
			}
		});
	}

	@Override
	public void fireGoLeftmostEvent() {
        mViewPager.setCurrentItem(0, true);
	}
}
