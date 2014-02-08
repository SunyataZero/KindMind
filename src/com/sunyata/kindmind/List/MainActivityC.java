package com.sunyata.kindmind.List;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.DatabaseHelperM;
import com.sunyata.kindmind.Database.ExtendedDataTableM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.PatternTableM;

/*
 * Overview: MainActivityC holds three ListFragments in a ViewPager and handles the corresponding tabs
 * Sections:
 * ------------------------Fields
 * ------------------------onCreate and OnPageChangeListener
 * ------------------------Pager adapter
 * ------------------------Callback methods
 * ------------------------Other methods
 * Improvements: Saving the view pager position in a bundle instead of a static variable
 * Documentation: 
 *  http://developer.android.com/training/implementing-navigation/lateral.html
 *  http://developer.android.com/reference/android/support/v4/app/FragmentActivity.html
 *  http://developer.android.com/reference/android/support/v4/view/ViewPager.html
 */
public class MainActivityC extends FragmentActivity implements MainActivityCallbackListenerI{

	//------------------------Fields
	
	//Fragment changes
    private ViewPager mViewPager;
	private FragmentPagerAdapterM mPagerAdapter;
    private static int sViewPagerPosition;

    //Action bar
    private ActionBar refActionBar;
    private String mFeelingTitle;
    private String mNeedTitle;
    private String mActionTitle;
    
    
    //------------------------onCreate and OnPageChangeListener
    
    /*
	 * Overview: onCreate does fundamental setup for the app, including an OnPageChangeListener,
	 *  a TabListener and creation of the startup list items
	 * Notes: This method may be called not only at the start of the application but also later
	 *  to recreate the activity, for example after coming back from a details screen.
	 * Documentation: 
	 *  http://developer.android.com/reference/android/support/v4/view/ViewPager.OnPageChangeListener.html
	 */
    @Override
    protected void onCreate(Bundle inSavedInstanceState) {
        super.onCreate(inSavedInstanceState);
        Log.d(Utils.getClassName(), Utils.getMethodName());
        
    	//Creation of new list items
    	if(Utils.isFirstTimeApplicationStarted(this) == true){
    		Utils.createAllStartupItems(this);
    	}

        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
        
        //Create the adapter that will return a fragment for each section of the app
        mPagerAdapter = new FragmentPagerAdapterM(getSupportFragmentManager());

        //Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        //-Using this becase getAdapter sometimes gives null, for more info, see this link:
        // http://stackoverflow.com/questions/13651262/getactivity-in-arrayadapter-sometimes-returns-null

        //Create and set the OnPageChangeListener for the ViewPager
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int inPos) {
				Log.d("ViewPager.OnPageChangeListener()", "onPageSelected()");
				
				//Setting the active tab when the user has just side scrolled (swiped) to a new fragment
				getActionBar().setSelectedNavigationItem(inPos);
				
				//TODO: Refresh list here? Note: To access one fragment from here we use this line: 
				// ((CustomPagerAdapter)mViewPager.getAdapter()).getItem(pos).refreshListDataSupport();
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

        //Setup of actionbar with tabs
        refActionBar = this.getActionBar();
        refActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tmpTabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				//Scrolling to the new fragment when the user selects a tab
				mViewPager.setCurrentItem(tab.getPosition());
			}
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			}
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
			}
        };
        refActionBar.addTab(refActionBar.newTab().setText(mFeelingTitle).setTabListener(tmpTabListener));
        refActionBar.addTab(refActionBar.newTab().setText(mNeedTitle).setTabListener(tmpTabListener));
        refActionBar.addTab(refActionBar.newTab().setText(mActionTitle).setTabListener(tmpTabListener));
        this.fireUpdateTabTitles();
    }
    

	//------------------------Pager adapter
    
    /*
	 * Overview: PagerAdapterM handles the listfragments that makes up the core of the app
	 * Used in: In onCreate setAdapater is called: "mViewPager.setAdapter(mPagerAdapter);"
	 * Notes: Was previously a FragmentStatePagerAdapter
	 * Documentation: 
	 *  http://developer.android.com/reference/android/support/v4/app/FragmentPagerAdapter.html
	 */
    //TODO: 
    class FragmentPagerAdapterM extends FragmentPagerAdapter {
        private ListFragmentC mFeelingListFragment;
        private ListFragmentC mNeedListFragment;
        private ListFragmentC mActionListFragment;
        public FragmentPagerAdapterM(FragmentManager inFragmentManager) {
            super(inFragmentManager);
        }
        @Override
        public Object instantiateItem (ViewGroup inContainer, int inPosition){
        	switch(inPosition){
        	case 0:
        		mFeelingListFragment = ListFragmentC.newInstance(ListTypeM.FEELINGS,
        				(MainActivityCallbackListenerI)MainActivityC.this);
        		break;
        	case 1:
        		mNeedListFragment = ListFragmentC.newInstance(ListTypeM.NEEDS,
        				(MainActivityCallbackListenerI)MainActivityC.this);
        		break;
        	case 2:
        		mActionListFragment = ListFragmentC.newInstance(ListTypeM.KINDNESS,
        				(MainActivityCallbackListenerI)MainActivityC.this);
        		break;
        	default:
        		Log.e(Utils.getClassName(), "Error in instantiateItem: Case not covered");
        		break;
        	}
        	return super.instantiateItem(inContainer, inPosition);
        }
        @Override
        public ListFragmentC getItem(int inPosition) {
        	switch (inPosition){
		    	case 0:	return mFeelingListFragment;
				case 1: return mNeedListFragment;
		    	case 2: return mActionListFragment;
		    	default: Log.e(Utils.getClassName(), "Error in method getItem: case not covered");return null;
        	}
        }
        @Override
        public int getCount() {
            return 3;
        }
    }

    
    //------------------------Callback methods

    /*
	 * Overview: fireSavePatternEvent saves as a pattern all the currently checked list items
	 * Used in: ListFragmentC when the user presses the save button menu item
	 * Uses app internal: fireClearAllListsEvent
	 */
	@Override
	public void fireSavePatternEvent() {
		Cursor tmpItemCursor = this.getContentResolver().query(
				ContentProviderM.LIST_CONTENT_URI, null, null, null, ContentProviderM.sSortType);
		
		long tmpCurrentTime = Calendar.getInstance().getTimeInMillis();
		//-getting the time here instead of inside the for statement ensures that we are able
		// to use the time as way to group items into a pattern.
		
		//Iterate through the list items to find the ones that are checked/active..
		for(tmpItemCursor.moveToFirst(); tmpItemCursor.isAfterLast() == false; tmpItemCursor.moveToNext()){
			if(Utils.sqlToBoolean(tmpItemCursor, ItemTableM.COLUMN_ACTIVE)){
				//..saving to pattern in database
				ContentValues tmpInsertContentValues = new ContentValues();
				long tmpItemId = tmpItemCursor.getInt(tmpItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
				tmpInsertContentValues.put(PatternTableM.COLUMN_ITEM_REFERENCE, tmpItemId);
				tmpInsertContentValues.put(PatternTableM.COLUMN_CREATE_TIME, tmpCurrentTime);
				this.getContentResolver().insert(ContentProviderM.PATTERN_CONTENT_URI, tmpInsertContentValues);
			}
		}

		Toast.makeText(this, "KindMind pattern saved", Toast.LENGTH_LONG).show();
		
		//Clearing data and side scrolling to the left
		this.fireClearAllListsEvent();
		
		//tmpItemCursor.close();
	}
	
	/*
	 * Overview: fireClearAllListsEvent clears all marks for checked/activated list items and scrolls to
	 *  the leftmost position (then also calls fireUpdateTabTitles so that the numbers are cleared as well) 
	 * Used in: 1. fireSavePatternEvent 2. ListFragmentC.onOptionsItemSelected()
	 */
	@Override
	public void fireClearAllListsEvent() {
		//Clearing all the checks for all list items
		ContentValues tmpContentValueForUpdate = new ContentValues();
		tmpContentValueForUpdate.put(ItemTableM.COLUMN_ACTIVE, ItemTableM.FALSE);
		Uri tmpUri = Uri.parse(ContentProviderM.LIST_CONTENT_URI.toString());
		this.getContentResolver().update(tmpUri, tmpContentValueForUpdate, null, null);
		
		//Side scrolling to the leftmost viewpager position (feelings)
		mViewPager.setCurrentItem(0, true);
		
		this.fireUpdateTabTitles();
	}
	
	/*
	 * Overview: fireUpdateTabTitles updates tab titles with the name of the listtype and - if one or more
	 *  list items have been checked/activated - adds the number of checks for that list type/fragment
	 * Used in: 1. fireSavePatternEvent 2. ListFragmentC.onListItemClick() 3. onCreate
	 */
	@Override
	public void fireUpdateTabTitles() {
		mFeelingTitle = getResources().getString(R.string.feelings_title);
        mNeedTitle = getResources().getString(R.string.needs_title);
        mActionTitle = getResources().getString(R.string.kindness_title);
        int tmpFeelingsCount = Utils.getActiveListItemCount(this, ListTypeM.FEELINGS);
        int tmpNeedsCount = Utils.getActiveListItemCount(this, ListTypeM.NEEDS);
        int tmpActionsCount = Utils.getActiveListItemCount(this, ListTypeM.KINDNESS);
        if(tmpFeelingsCount != 0){mFeelingTitle = mFeelingTitle + " (" + tmpFeelingsCount + ")";}
        if(tmpNeedsCount != 0){mNeedTitle = mNeedTitle + " (" + tmpNeedsCount + ")";}
        if(tmpActionsCount != 0){mActionTitle = mActionTitle + " (" + tmpActionsCount + ")";}
        refActionBar.getTabAt(0).setText(mFeelingTitle);
        refActionBar.getTabAt(1).setText(mNeedTitle);
        refActionBar.getTabAt(2).setText(mActionTitle);
	}

    /*
	 * Overview: resetData clears and repopulates the list of data items. Used for testing and debug purposes
	 */
    public void fireResetData(){
    	//Clearing the data
    	this.getContentResolver().delete(ContentProviderM.LIST_CONTENT_URI, null, null);
    	this.getContentResolver().delete(ContentProviderM.PATTERN_CONTENT_URI, null, null);
    	this.getContentResolver().delete(ContentProviderM.EXTENDED_DATA_CONTENT_URI, null, null);
    	
    	//Adding new data
    	Utils.createAllStartupItems(this);
    }

	
    //------------------------Other methods
    
    @Override
    public void onResume(){
    	super.onResume();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    	
    	//Solves the problem in issue #41
    	if(sViewPagerPosition != mViewPager.getCurrentItem()){
    		mViewPager.setCurrentItem(sViewPagerPosition);
    	}
    }
    
}
