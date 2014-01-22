package com.sunyata.kindmind;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivityC extends FragmentActivity implements MainActivityCallbackListenerI{

	//------------------------Fields
	
	private CustomPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private static int sViewPagerPosition;
    //-Important that this is static since the whole instance of the
    // class is recreated when going back from the details screens
    private ListFragmentC mFeelingListFragment;
    private ListFragmentC mNeedListFragment;
    private ListFragmentC mActionListFragment;
    private ActionBar refActionBar;
    
    //------------------------Lifecycle methods, including onCreate
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//PLEASE NOTE: This method may be called not only at the start of the application but also
    	// later to recreate the activity, for example after coming back from a details screen.
        super.onCreate(savedInstanceState);
        Log.d(Utils.getClassName(), Utils.getMethodName());
        setContentView(R.layout.activity_main);
        
        setTitle(R.string.app_name);
        
        // Create the adapter that will return a fragment for each of the sections of the app.
        mSectionsPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //mViewPager.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        mViewPager.setOffscreenPageLimit(0);
        //-Using this becase getAdapter sometimes gives null, for more info, see this link:
        // http://stackoverflow.com/questions/13651262/getactivity-in-arrayadapter-sometimes-returns-null
        //mViewPager.setOffscreenPageLimit(4); //This only partly solves the problem with NPE in onPageScrollStateChanged

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int pos) {
				///updateViewPagerView(ListTypeM.getEnumListByLevel(mViewPager.getCurrentItem()).get(0));
				getActionBar().setSelectedNavigationItem(pos);
				/*
				if(mFeelingListFragment!=null){mFeelingListFragment.refreshListCursorAndAdapter(MainActivityC.this);}
				if(mNeedListFragment!=null){mNeedListFragment.refreshListCursorAndAdapter(MainActivityC.this);}
				if(mActionListFragment!=null){mActionListFragment.refreshListCursorAndAdapter(MainActivityC.this);}
				*/
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int inState) {
				switch(inState){
				case ViewPager.SCROLL_STATE_IDLE:
					//Saving the position (solves the problem in issue #41)
					sViewPagerPosition = mViewPager.getCurrentItem();
					break;
				default:
					break;
				}
			}
		});

        //Setting up the action bar spinner
        // For more details, please see these links:
        // https://developer.android.com/reference/android/app/ActionBar.html#setListNavigationCallbacks%28android.widget.SpinnerAdapter,%20android.app.ActionBar.OnNavigationListener%29
        // https://developer.android.com/reference/android/widget/ArrayAdapter.html#setDropDownViewResource%28int%29
        refActionBar = this.getActionBar();
        refActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tmpTabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition());
			}
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			}
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
			}
        };
        refActionBar.addTab(refActionBar.newTab().setText(R.string.feelings_title).setTabListener(tmpTabListener));
        refActionBar.addTab(refActionBar.newTab().setText(R.string.needs_title).setTabListener(tmpTabListener));
        refActionBar.addTab(refActionBar.newTab().setText(R.string.kindness_title).setTabListener(tmpTabListener));
        //TODO: Adding a number to show the number of checked items. We can use getResources().getString(id)


        //TODO: Direcoty is not created, please fix or find another way to choose files
        //If the directory does not already exist, create it
    	File tmpDirectory = new File(Utils.getKindMindDirectory());
    	Log.i(Utils.getClassName(), "tmpDirectory = " + tmpDirectory);
    	boolean tmpDirectoryWasCreatedSuccessfully = tmpDirectory.mkdir();
    	Log.i(Utils.getClassName(),
    			"tmpDirectoryWasCreatedSuccessfully = " + tmpDirectoryWasCreatedSuccessfully);

    	
    	if(Utils.isFirstTimeApplicationStarted(this) == true){
    		Utils.createAllStartupItems(this);
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
    	
    	//Solves the problem in issue #41
    	if(sViewPagerPosition != mViewPager.getCurrentItem()){
    		mViewPager.setCurrentItem(sViewPagerPosition);
    	}
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
    
	@Override
	public void onActivityResult(int requestcode, int resultcode, Intent intent){
		Log.d(Utils.getClassName(), Utils.getMethodName());
	}
	
	
    @Override
    public void onSaveInstanceState(Bundle outBundle){
    	super.onSaveInstanceState(outBundle);
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    	
    	//this.savePatternToDatabase();
    }

	
    
	//-------------------Pager adapter
	
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    //TODO: Try changing back to FragmentPagerAdapter since this is prefferred for a small fixed number
    // of tabs according to the documentation.
    class CustomPagerAdapter extends FragmentStatePagerAdapter {
        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Object instantiateItem (ViewGroup container, int position){
        	switch(position){
        	case 0:
        		mFeelingListFragment = ListFragmentC.newInstance(ListTypeM.FEELINGS,
        				(MainActivityCallbackListenerI)MainActivityC.this);
        		//((DataAdapter)mSufferingListFragment.getListAdapter()).notifyDataSetChanged();
        		break;
        	case 1:
        		mNeedListFragment = ListFragmentC.newInstance(ListTypeM.NEEDS,
        				(MainActivityCallbackListenerI)MainActivityC.this);
        		//((DataAdapter)mNeedListFragment.getListAdapter()).notifyDataSetChanged();
        		break;
        	case 2:
        		mActionListFragment = ListFragmentC.newInstance(ListTypeM.ACTIONS,
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
        		//case 0: return mObservationListFragment;
		    	case 0:	return mFeelingListFragment; //mFeelingListFragment; //Has already been created in the constructor
				case 1: return mNeedListFragment;
		    	case 2: return mActionListFragment;
		    	default: Log.e(Utils.getClassName(), "Error in method getItem: case not covered");return null;
        	}
        }
        @Override
        public int getCount() {
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int inPosition) {
            Locale l = Locale.getDefault();
            switch (inPosition) {
            	//TODO: Remove?
                case 0: return getString(R.string.feelings_title).toUpperCase(l);
                case 1: return getString(R.string.needs_title).toUpperCase(l);
                case 2: return getString(R.string.kindness_title).toUpperCase(l);
            }
            return null;
        }
    }

    
    //----------------------Update methods
    
	void updateViewPagerView(ListTypeM inListType){
		//NOTE: CANNOT CALL getListAdapter() ON THE FRAGMENT FROM HERE BECAUSE THE
		// FRAGMENT'S ONACTIVITYCREATED METHOD HAS NOT SET UP THE FRAGMENT WITH THE ADAPTER.

		////////KindModelM.get(getApplicationContext()).updateSortValuesForListType(inListType);
		//////////KindModelM.get(getApplicationContext()).getListOfType(inListType).sortWithKindness();
		
		//Sorting the whole list for all the different types in one go
		//String tmpSortOrder = ItemTableM.COLUMN_KINDSORTVALUE;
		//getContentResolver().query(ListContentProviderM.LIST_CONTENT_URI, null, null, null, tmpSortOrder);
		
		//Setting the name and updating
		switch(inListType){
		/*
		case EVENT:
			setTitle(R.string.events_top_title);
			updateFragmentList(mObservationListFragment);
			break;
		*/
		case FEELINGS:
			setTitle(R.string.feelings_title);
			//updateFragmentList(mFeelingListFragment);
			mFeelingListFragment.updateListWithNewData();
			//mFeelingListFragment.restartLoader();
			break;
		case NEEDS:
			setTitle(R.string.needs_title);
			//updateFragmentList(mNeedListFragment);
			mNeedListFragment.updateListWithNewData();
			//mNeedListFragment.restartLoader();
			break;
		case ACTIONS:
			setTitle(R.string.kindness_title);
			//updateFragmentList(mKindnessListFragment);
			mActionListFragment.updateListWithNewData();
			//mActionListFragment.restartLoader();
			break;
		default:
			Log.e(Utils.getClassName(), "Error in updateViewPagerView: Case not covered");
			return;
		}
		
		setTitle(R.string.app_name);
	}
    
	public void clearActivated(){
		//KindModelM.get(this).clearActivatedForAllLists();
		
		ContentValues tmpContentValueForUpdate = new ContentValues();
		tmpContentValueForUpdate.put(ItemTableM.COLUMN_ACTIVE, 0); //0 means false
		Uri tmpUri = Uri.parse(ListContentProviderM.LIST_CONTENT_URI.toString());
		this.getContentResolver().update(
				tmpUri, tmpContentValueForUpdate, null, null);
	}
	
	void updateAllFragmentLists(){
		updateFragmentList(mFeelingListFragment);
		updateFragmentList(mNeedListFragment);
		updateFragmentList(mActionListFragment);
	}
	ListFragmentC tmpListFragment = null; //Has to be put outside for it to be accessible inside the Runnable
	private void updateFragmentList(ListFragmentC inListFragment){
		/*
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
					//((ListFragmentDataAdapterC)tmpListFragment.getListAdapter()).notifyDataSetChanged();
					//tmpListFragment.getListView().invalidateViews();
					tmpListFragment.updateListWithNewData();
				}catch(Exception e){
					Log.w(Utils.getClassName(), "Error in updateFragmentList: " + e.getMessage());
				}
			}
		});
		tmpListFragment.getListView().setSelectionFromTop(0, 0);
		*/
	}

	
	//----------------------Callback methods
	
	
	@Override
	public void fireSavePatternEvent() {
		Cursor tmpItemCursor = this.getContentResolver().query(
				ListContentProviderM.LIST_CONTENT_URI, null, null, null, ListContentProviderM.sSortType);
		
		for(tmpItemCursor.moveToFirst(); tmpItemCursor.isAfterLast() == false; tmpItemCursor.moveToNext()){
		
			if(Utils.sqlToBoolean(tmpItemCursor, ItemTableM.COLUMN_ACTIVE)){

				//Saving to pattern
				ContentValues tmpInsertContentValues = new ContentValues();
				long tmpItemId = tmpItemCursor.getInt(
						tmpItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
				tmpInsertContentValues.put(PatternTableM.COLUMN_ITEM_REFERENCE, tmpItemId);
				tmpInsertContentValues.put(PatternTableM.COLUMN_TIME, Calendar.getInstance().getTimeInMillis());
				this.getContentResolver().insert(
						ListContentProviderM.PATTERN_CONTENT_URI, tmpInsertContentValues);
			}
		}

		Toast.makeText(this, "KindMind pattern saved", Toast.LENGTH_LONG).show();
		
		//Clearing all the data
		this.clearActivated();
		
		//Side scrolling to the leftmost viewpager position (feelings)
		mViewPager.setCurrentItem(0, true);
		
		//tmpItemCursor.close();
	}
}
