package com.sunyata.kindmind;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.PatternsTableM;
import com.sunyata.kindmind.List.ListFragmentC;
import com.sunyata.kindmind.List.ListTypeM;
import com.sunyata.kindmind.List.SortingAlgorithmServiceM;

/**
 * \brief MainActivityC holds three ListFragments in a ViewPagerM using a FragmentAdapterM
 * 
 * MainActivitC also contains:
 * + Callback methods from ListFragmentC, for updating the whole gui (used for example when saving)
 * + Callback methods from the test project (for example for resetting all data)
 * 
 * Documentation: 
 * + http://developer.android.com/training/implementing-navigation/lateral.html
 * + http://developer.android.com/reference/android/support/v4/app/FragmentActivity.html
 * + http://developer.android.com/reference/android/support/v4/view/ViewPager.html
 * 
 * \nosubgrouping
 */
public class MainActivityC extends FragmentActivity implements MainActivityCallbackListenerI{

	public final static String EXTRA_URI_AS_STRING = "uri_as_string";

	private ViewPagerM mViewPager;
	private FragmentAdapterM mPagerAdapter;
	private ActionBar refActionBar;
	private String mFeelingTitle;
	private String mNeedTitle;
	private String mActionTitle;

	///@name Life cycle
	///@{
	/**
	 * \brief onCreate does fundamental setup for the app, including creation of the startup list
	 * items, creating an instance of ViewPagerM and setting the OnPageChangeListener for it, and
	 * adding a TabListener.
	 * 
	 * Documentation: 
	 * + http://developer.android.com/reference/android/support/v4/view/ViewPager.OnPageChangeListener.html
	 */
	@Override
	protected void onCreate(Bundle inSavedInstanceState) {
		super.onCreate(inSavedInstanceState);
		Log.d(Utils.getAppTag(), Utils.getMethodName());

		//Activating strict mode for debug builds
		// More info: http://developer.android.com/reference/android/os/StrictMode.html
		/*
        if(Utils.BuildConfig.DEBUG){
        	StrictMode.enableDefaults();
        }
		 */

		//Creation of new list items
		if(Utils.isFirstTimeApplicationStarted(this) == true){
			Utils.createAllStartupItems(this);
		}

		//Setting layout and title
		setContentView(R.layout.activity_main);
		setTitle(R.string.app_name);

		//Create the adapter that will return a fragment for each section of the app
		mPagerAdapter = new FragmentAdapterM(getSupportFragmentManager());

		//Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPagerM)findViewById(R.id.pager);
		/////mViewPagerPosition = ListTypeM.FEELINGS;
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(ListTypeM.NUMBER_OF_TYPES - 1);
		//-Using this becase getAdapter sometimes gives null, for more info, see this link:
		// http://stackoverflow.com/questions/13651262/getactivity-in-arrayadapter-sometimes-returns-null

		//Create and set the OnPageChangeListener for the ViewPager
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			//-To access one fragment from here we can use this line:
			// ((CustomPagerAdapter)mViewPager.getAdapter()).getItem(pos).refreshListDataSupport();
			@Override
			public void onPageSelected(int inPos) { //[list update]
				Log.d("ViewPager.OnPageChangeListener()", "onPageSelected()");

				//Resetting the sorting
				Utils.setItemTableSortType(SortTypeM.KINDSORT);

				//Setting the active tab when the user has just side scrolled (swiped) to a new fragment
				getActionBar().setSelectedNavigationItem(inPos);
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int inState) {
				//-this is called even when we press one of the tab buttons
				switch(inState){
				case ViewPager.SCROLL_STATE_IDLE:
					//Saving the position (solves the problem in issue #41)
					/////////mViewPagerPosition = mViewPager.getCurrentItem();
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
				int tmpPos = tab.getPosition();

				if(mViewPager.getCurrentItem() != tmpPos){
					mViewPager.setCurrentItem(tmpPos);
				}
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
		this.fireUpdateTabTitlesEvent();
	}

	/**
	 * \brief onResume is used for extracting data from an intent coming from \ref LauncherServiceC
	 */
	@Override
	public void onResume(){
		super.onResume();
		Log.d(Utils.getAppTag(), Utils.getMethodName());

		this.extractDataFromLauncherIntent();
		/*
    	if(mViewPagerPosition != mViewPager.getCurrentItem()){
    		mViewPager.setCurrentItem(mViewPagerPosition);
    		//-solves the problem in issue #41
    	}
		 */
	}
	///@}

	///@name Callback
	///@{
	/**
	 * \brief fireSavePatternEvent saves as a pattern all the currently checked list items
	 * 
	 * Details: The pattern table is also cut down if over the max number of rows
	 * 
	 * Used in: ListFragmentC when the user presses the save button menu item
	 * 
	 * Uses app internal: \ref fireClearDatabaseAndUpdateGuiEvent
	 */
	@Override
	public void fireSavePatternEvent() {
		long tmpCurrentTime = Calendar.getInstance().getTimeInMillis();
		//-getting the time here instead of inside the for statement ensures that we are able
		// to use the time as way to group items into a pattern.

		//Iterate through the list items to find the ones that are checked/active..
		Cursor tItemCr = this.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, null, null, ContentProviderM.sSortType);
		for(tItemCr.moveToFirst(); tItemCr.isAfterLast() == false; tItemCr.moveToNext()){
			if(Utils.sqlToBoolean(tItemCr, ItemTableM.COLUMN_ACTIVE)){
				//..saving to pattern in database
				ContentValues tInsContVals = new ContentValues();
				long tItemId = tItemCr.getInt(tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
				tInsContVals.put(PatternsTableM.COLUMN_ITEM_REFERENCE, tItemId);
				tInsContVals.put(PatternsTableM.COLUMN_CREATE_TIME, tmpCurrentTime);
				this.getContentResolver().insert(ContentProviderM.PATTERNS_CONTENT_URI, tInsContVals);
			}
		}
		tItemCr.close();
		Toast.makeText(this, "KindMind pattern saved", Toast.LENGTH_LONG).show();

		//Limiting the number of rows in the patterns table
		this.limitPatternsTable();

		//Clearing data and updating the gui
		this.fireClearDatabaseAndUpdateGuiEvent();
	}

	@Override
	public void fireClearDatabaseAndUpdateGuiEvent() {
		this.clearAllActiveInDatabase(this);
		this.scrollLeftmost();
		((FragmentAdapterM)mViewPager.getAdapter()).getCurrentFragment().sortDataWithService();
		this.fireUpdateTabTitlesEvent();
		((FragmentAdapterM)mViewPager.getAdapter()).getCurrentFragment().getListView()
		.smoothScrollToPositionFromTop(0, 0);
	}

	/**
	 * \brief fireResetDataEvent clears and repopulates the list of data items. Used for testing and debug purposes
	 * 
	 * Notes: This method is not in the test group below since we call it from a ListFragment menu item (which is only
	 * enabled when running in debug mode)
	 */
	public void fireResetDataEvent(){
		//Clearing the data
		this.getContentResolver().delete(ContentProviderM.ITEM_CONTENT_URI, null, null);
		this.getContentResolver().delete(ContentProviderM.PATTERNS_CONTENT_URI, null, null);

		//Adding new data
		Utils.createAllStartupItems(this);

		//Resetting static variables
		mViewPager.setCurrentItem(ListTypeM.FEELINGS);
	}

	/**
	 * \brief fireUpdateTabTitlesEvent updates tab titles with the name of the listtype and - if one or more
	 *  list items have been checked/activated - adds the number of checks for that list type/fragment
	 *  
	 * Used in:
	 * + fireSavePatternEvent
	 * + ListFragmentC.onListItemClick()
	 * + onCreate
	 */
	@Override
	public void fireUpdateTabTitlesEvent() {
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
	///@}

	///@name Testing methods
	///@{
	public ListView getListViewOfCurrentFragment(){
		return ((FragmentAdapterM)mViewPager.getAdapter()).getCurrentFragment().getListView();
	}

	public int getCurrentAdapterPosition(){
		return mViewPager.getCurrentItem();
	}

	public boolean isListViewPresent() {
		try{
			((FragmentAdapterM)mViewPager.getAdapter()).getCurrentFragment().getListView();
		}catch(Exception e){
			return false;
		}
		return true;
	}
	///@}


	//-------------------------------------------Private-------------------------------------------

	private void extractDataFromLauncherIntent(){

		//------------Extracting data from the intent given when calling this activity
		// (used by widgets and notifications)
		if(this.getIntent() != null && this.getIntent().hasExtra(EXTRA_URI_AS_STRING)){
			String tmpExtraFromString = this.getIntent().getStringExtra(EXTRA_URI_AS_STRING);
			Uri tmpItemUri = Uri.parse(tmpExtraFromString);
			if(tmpItemUri != null){


				Context tmpContentProviderContext = Utils.getContentProviderContext(this);


				this.clearAllActiveInDatabase(tmpContentProviderContext);

				//Updating the db value
				ContentValues tmpContentValues = new ContentValues();
				tmpContentValues.put(ItemTableM.COLUMN_ACTIVE, 1);
				tmpContentProviderContext.getContentResolver().update(tmpItemUri, tmpContentValues, null, null);

				//Sorting data for all lists without showing loading spinner
				// "((FragmentStatePagerAdapterM)mViewPager.getAdapter()).getCurrentFragment().sortDataWithService();"
				// which shows the loading spinner gives a NPE in a situation
				Intent tmpIntent = new Intent(this, SortingAlgorithmServiceM.class);
				this.startService(tmpIntent);

				this.fireUpdateTabTitlesEvent();

				//Setting up the cursor and extracting the list type..
				Cursor tmpItemCur = tmpContentProviderContext.getContentResolver().query(
						tmpItemUri, null, null, null, null);
				///if(tmpItemCur != null && tmpItemCur.moveToFirst()){}
				tmpItemCur.moveToFirst();
				int tmpListType = 0;
				try{
					tmpListType = tmpItemCur.getInt(tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_LIST_TYPE));
				}catch(CursorIndexOutOfBoundsException cioobe){
					Log.e(Utils.getAppTag(), "extractDataFromLauncherIntent: CursorIndexOutOfBoundsException. "
							+ "tmpItemUri = " + tmpItemUri, cioobe);
					finish();
					/*
					 * This problem has only been seen on an emulator and only after we have run auto tests
					 * 
    03-02 01:12:39.717: E/AndroidRuntime(2230): Caused by: android.database.CursorIndexOutOfBoundsException: Index 0 requested, with a size of 0
    03-02 01:12:39.717: E/AndroidRuntime(2230): 	at android.database.AbstractCursor.checkPosition(AbstractCursor.java:400)
    03-02 01:12:39.717: E/AndroidRuntime(2230): 	at android.database.AbstractWindowedCursor.checkPosition(AbstractWindowedCursor.java:136)
    03-02 01:12:39.717: E/AndroidRuntime(2230): 	at android.database.AbstractWindowedCursor.getInt(AbstractWindowedCursor.java:68)
    03-02 01:12:39.717: E/AndroidRuntime(2230): 	at android.database.CursorWrapper.getInt(CursorWrapper.java:102)
    03-02 01:12:39.717: E/AndroidRuntime(2230): 	at com.sunyata.kindmind.MainActivityC.onCreate(MainActivityC.java:210)
					 */

				}
				tmpItemCur.close();



				//Setting the Viewpager position
				mViewPager.setCurrentItem(tmpListType);

			}
			//Clearing the intent
			this.getIntent().removeExtra(EXTRA_URI_AS_STRING);
			//-we need this line together with the line below, why is unknown
			this.setIntent(null);
		}
	}


	/*
	 * Overview: clearAllActiveInDatabase clears all marks for checked/activated list items
	 * Used in:
	 * Improvements:
	 */
	private void clearAllActiveInDatabase(Context inContext) { //[list update]
		//Clearing all the checks for all list items
		ContentValues tmpContentValueForUpdate = new ContentValues();
		tmpContentValueForUpdate.put(ItemTableM.COLUMN_ACTIVE, ItemTableM.FALSE);
		Uri tmpUri = Uri.parse(ContentProviderM.ITEM_CONTENT_URI.toString());
		inContext.getContentResolver().update(tmpUri, tmpContentValueForUpdate, null, null);
	}

	private void scrollLeftmost(){
		//Side scrolling to the leftmost ViewPager position (feelings)
		if(mViewPager.getCurrentItem() != ListTypeM.FEELINGS){
			mViewPager.setCurrentItem(ListTypeM.FEELINGS, true);
		}
	}

	/**
	 * Overview: limitPatternsTable removes zero or more patterns, keeping the pattern table (1) relevant and
	 *  (2) at a lenght which does not take too much resources for the sorting algorithm
	 * Used in: fireSavePatternEvent
	 * Notes: 1. We limit the pattern table based on the number of rows (and not the number of patterns).
	 * 2. We expect the while loop to be run completely only one time on average since this method is called from
	 *  the same method that adds new patterns (if we have just added a very large pattern it may be run many times)
	 * 3. The only reason that a for loop is used is so that in case of some error with deletion from the database
	 *  we don't get stuck in an infinite loop.
	 * Improvements: Calling update after we have closed the cursor so that there is no risk of
	 */
	private void limitPatternsTable(){
		Log.d(Utils.getAppTag(), Utils.getMethodName());

		Cursor tmpPatternsCur = null;
		final int WARNING_LIMIT = 200;

		ArrayList<String> tmpSelectionsForDeletionList = new ArrayList<String>();

		for(int i = 0; i < WARNING_LIMIT; i++){
			//Sorting "by pattern" (by create time)
			tmpPatternsCur = this.getContentResolver().query(
					ContentProviderM.PATTERNS_CONTENT_URI, null, null, null,
					PatternsTableM.COLUMN_CREATE_TIME + " ASC");

			//Looping until we are on or under the max limit or rows
			if(tmpPatternsCur.getCount() <= Utils.getMaxNumberOfPatternRows()){
				//-please note that while debugging getCount will not be updated directly
				tmpPatternsCur.close();
				return;
			}

			//Extracting the first (and oldest) time entry
			tmpPatternsCur.moveToFirst();
			long tmpFirstTimeEntry = tmpPatternsCur.getLong(
					tmpPatternsCur.getColumnIndexOrThrow(PatternsTableM.COLUMN_CREATE_TIME));

			//Using the first time entry as a selection value for removing all rows for this whole pattern from the db
			String tmpSelection = PatternsTableM.COLUMN_CREATE_TIME + "=" + tmpFirstTimeEntry;
			tmpSelectionsForDeletionList.add(tmpSelection);

			tmpPatternsCur.close();
		}

		//After the cursor has been closed, we remove the items
		for(String selection : tmpSelectionsForDeletionList){
			this.getContentResolver().delete(ContentProviderM.PATTERNS_CONTENT_URI, selection, null);
		}

		//If we get here it means that we have looped more than the "warning limit" which is an indication that
		// something has gone wrong
		Log.w(Utils.getAppTag(),
				"Warning in limitPatternsTable: Number of iterations has reached " + WARNING_LIMIT
				+ ", exiting method");
	}

	/**
	 * Overview: PagerAdapterM handles the listfragments that makes up the core of the app
	 * Used in: In onCreate setAdapater is called: "mViewPager.setAdapter(mPagerAdapter);"
	 * Documentation:
	 *  http://developer.android.com/reference/android/support/v4/app/FragmentStatePagerAdapter.html
	 */
	private class FragmentAdapterM extends FragmentPagerAdapter {
		private ListFragmentC mFeelingListFragment;
		private ListFragmentC mNeedListFragment;
		private ListFragmentC mKindnessListFragment;
		public FragmentAdapterM(FragmentManager inFragmentManager) {
			super(inFragmentManager);
		}
		@Override
		public Object instantiateItem (ViewGroup inContainer, int inPosition){
			Log.v(Utils.getAppTag(), Utils.getMethodName() + ", position = " + inPosition);

			switch(inPosition){
			case ListTypeM.FEELINGS:
				mFeelingListFragment = ListFragmentC.newInstance(ListTypeM.FEELINGS,
						(MainActivityCallbackListenerI)MainActivityC.this);
				break;
			case ListTypeM.NEEDS:
				mNeedListFragment = ListFragmentC.newInstance(ListTypeM.NEEDS,
						(MainActivityCallbackListenerI)MainActivityC.this);
				break;
			case ListTypeM.KINDNESS:
				mKindnessListFragment = ListFragmentC.newInstance(ListTypeM.KINDNESS,
						(MainActivityCallbackListenerI)MainActivityC.this);
				break;
			case ListTypeM.NOT_SET:
			default:
				Log.e(Utils.getAppTag(), "Error in instantiateItem: Case not covered or not set");
				break;
			}
			return super.instantiateItem(inContainer, inPosition);
		}
		@Override
		public ListFragmentC getItem(int inPosition) {
			Log.v(Utils.getAppTag(), Utils.getMethodName() + ", position = " + inPosition);

			switch (inPosition){
			case ListTypeM.FEELINGS:	return mFeelingListFragment;
			case ListTypeM.NEEDS:		return mNeedListFragment;
			case ListTypeM.KINDNESS:	return mKindnessListFragment;
			case ListTypeM.NOT_SET:
			default:
				Log.e(Utils.getAppTag(), "Error in method getItem: case not covered or not set");
				return null;
			}
		}
		@Override
		public int getCount() {
			return ListTypeM.NUMBER_OF_TYPES;
		}

		public ListFragmentC getCurrentFragment(){
			Log.v(Utils.getAppTag(), Utils.getMethodName());

			ListFragmentC retListFragmentC = this.getItem(mViewPager.getCurrentItem());
			return retListFragmentC;
		}
	}
}