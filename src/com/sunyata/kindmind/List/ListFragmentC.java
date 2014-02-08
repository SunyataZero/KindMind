package com.sunyata.kindmind.List;

import java.io.File;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ListView;

import com.sunyata.kindmind.BuildConfig;
import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.DatabaseHelperM;
import com.sunyata.kindmind.Database.ExtendedDataTableM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.PatternTableM;
import com.sunyata.kindmind.Details.DetailsActivityC;
import com.sunyata.kindmind.ToastsAndActions.ActionBehaviour;
import com.sunyata.kindmind.ToastsAndActions.FeelingsToast;
import com.sunyata.kindmind.ToastsAndActions.MediaFileActionBehaviour;
import com.sunyata.kindmind.ToastsAndActions.NeedsToast;
import com.sunyata.kindmind.ToastsAndActions.NoAction;
import com.sunyata.kindmind.ToastsAndActions.NoToast;
import com.sunyata.kindmind.ToastsAndActions.ToastBehaviour;

/*
 * Overview: ListFragmentC shows a list of items, each item corresponding to a row in an SQL database
 * Sections:
 *  -------------------Fields and constructor
 *  -------------------Loader and update methods
 *  -------------------onCreate, onActivityCreated and click methods
 *  -------------------Other lifecycle methods
 *  -------------------Options menu
 *  -------------------Toast and Action Behaviour
 * Implements: LoaderManager.LoaderCallbacks<Cursor>
 * Extends: ListFragment
 * Uses Android lib: *ListView*, ListFragment, LoaderManager, CursorLoader
 *  http://developer.android.com/reference/android/widget/ListView.html
 *  https://developer.android.com/reference/android/support/v4/app/ListFragment.html
 *  https://developer.android.com/reference/android/support/v4/app/LoaderManager.html
 *  https://developer.android.com/reference/android/support/v4/content/CursorLoader.html
 * Notes: *Support classes are used*
 *  import android.support.v4.app.ListFragment;
 *  import android.support.v4.app.LoaderManager;
 *  import android.support.v4.content.CursorLoader;
 *  The reason for this is that the ViewPager only is available in a support version and is not compatible
 *  with other versions
 */
public class ListFragmentC extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	//-------------------Fields and constructor
	
	private ListTypeM refListType; //-Saved in onSaveInstanceState
	private static MainActivityCallbackListenerI sCallbackListener; //-Does not have to be saved since it's static
	private ToastBehaviour mToastBehaviour; //-Not saved, but set in onResume
	private ActionBehaviour mActionBehaviour; //-Not saved, but set in onResume
	private CursorAdapterM mCursorAdapter;

	public static final String EXTRA_ITEM_URI = "EXTRA_LIST_DATA_ITEM_ID";
	public static final String EXTRA_AND_BUNDLE_LIST_TYPE = "EXTRA_LIST_TYPE";

	public static ListFragmentC newInstance(ListTypeM inListType, MainActivityCallbackListenerI inCallbackListener){
		ListFragmentC retListFragment = new ListFragmentC();
		retListFragment.refListType = inListType;
		sCallbackListener = inCallbackListener;
		return retListFragment;
	}

	
	//-------------------Loader and update methods
	//A very good example is available at the top of the following page:
	// http://developer.android.com/reference/android/app/LoaderManager.html
	//Please note that we never close the cursor that we get through the loader since this is handled by the
	// loader (closing ourselves may cause problems)
	
	/*
	 * Overview: onCreateLoader creates and returns the CursorLoader, which contains the mapping to the database
	 * Used in:
	 * Documentation: 
	 *  http://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html#onCreateLoader%28int,%20android.os.Bundle%29
	 */
	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int inIdUnused, Bundle inArgumentsUnused) {
		Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
		
		//Setup of variables used for selecting the database colums of rows
		String[] tmpProjection = {ItemTableM.COLUMN_ID, ItemTableM.COLUMN_NAME,
				ItemTableM.COLUMN_DETAILS, ItemTableM.COLUMN_ACTIVE, ItemTableM.COLUMN_KINDSORTVALUE};
		//-kindsortvalue only needed here when used for debug purposes
		String tmpSelection = ItemTableM.COLUMN_LISTTYPE + " = ?";
		String[] tmpSelectionArguments = {refListType.toString()};

		//Creating the CursorLoader
		CursorLoader retCursorLoader = new CursorLoader(
				getActivity(), ContentProviderM.LIST_CONTENT_URI,
				tmpProjection, tmpSelection, tmpSelectionArguments, ContentProviderM.sSortType);
		return retCursorLoader;
	}
	/*
	 * Overview: onLoadFinished
	 * Used in: Docs: "Called when a previously created loader has finished its load"
	 * Documentation *interesting to read*:
	 *  http://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html#onLoadFinished%28android.content.Loader%3CD%3E,%20D%29
	 */
	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> inCursorLoader, Cursor inCursor) {
		Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
		
		mCursorAdapter.swapCursor(inCursor);
	}
	/*
	 * Overview: onLoaderReset
	 * Used in: Docs: "Called when a previously created loader is being reset, and thus making its data unavailable"
	 */
	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> inCursorUnused) {
		Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
		
		mCursorAdapter.swapCursor(null);
	}
	
	/* 
	 * Overview: createListDataSupport fills the data from the database into the loader
	 *  by creating a cursor adapter (with mapping to database columns) and initiating the loader
	 * Used in: onActivityCreated
	 * Uses app internal: CursorAdapterM
	 * Uses Android lib: setListAdapter, initLoader
	 * Notes: The synching of the state of the checkboxes with the database is not done automatically,
	 *  this is handled in another place (getView in CustomCursorAdapter)
	 */
	void fillListWithDataFromAdapter(){
		Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
		
		//Creating the SimpleCursorAdapter for the specified database columns linked to the specified GUI views..
		String[] tmpDatabaseFrom = {ItemTableM.COLUMN_NAME, ItemTableM.COLUMN_DETAILS};
		int[] tmpDatabaseTo = {R.id.list_item_titleTextView, R.id.list_item_tagsTextView};
		mCursorAdapter = new CursorAdapterM(
				getActivity(), R.layout.ofnr_list_item, null,
				tmpDatabaseFrom, tmpDatabaseTo, 0, refListType);
		
		//..using this CursorAdapter as the adapter for this ListFragment
		super.setListAdapter(mCursorAdapter);
		
		//Creating (or re-creating) the loader
		getLoaderManager().initLoader(0, null, this);
		//-using the non-support LoaderManager import gives an error
		//-initLoader seems to be preferrable to restartLoader
	}
	
	/*
	 * Overview: updateCursorLoaderAndAdapter updates the data in the list.
	 *  This is done by changing the cursor and giving the cursor to the adapter, and restarting the loader 
	 * Used in: 
	 * Uses Android lib: changeCursor, setAdapter, restartLoader
	 */
	void updateCursorLoaderAndAdapter() {
		Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
		
		//Updating the cursor..
		String tmpSelection = ItemTableM.COLUMN_LISTTYPE + "=" + "'" + this.refListType.toString() + "'";
		Cursor tmpCursor = getActivity().getContentResolver().query(
				ContentProviderM.LIST_CONTENT_URI, null,
				tmpSelection, null, ContentProviderM.sSortType);
		mCursorAdapter.changeCursor(tmpCursor);
		
		//..and using the new cursor for the adapter
		getListView().setAdapter(mCursorAdapter);
		//-PLEASE NOTE: We need this line, and it was hard to find this info. It was found here:
		// http://stackoverflow.com/questions/8213200/android-listview-update-with-simplecursoradapter

		//Restarting the loader
		getLoaderManager().restartLoader(0, null, this);
		//-PLEASE NOTE: We need this line, otherwise the checkbox status will not be updated
		
		//Scrolling to the top of the list
		getListView().smoothScrollToPosition(0);
		
	}
	
	
	//-------------------onCreate, onActivityCreated and click methods
	// Please note that one click method is inside onActivityCreated and the other is outside
	
	/*
	 * Overview: onActivityCreated restores the state, does fundamental setup for the fragment
	 *  and setup for the long click
	 * Note 1: Long click is handled separately from the short click because there is no method to override for the
	 *  long click
	 * Note 2: Restore of state used to be in onCreate()
	 */
    @Override
    public void onActivityCreated(Bundle inSavedInstanceState){
    	super.onActivityCreated(inSavedInstanceState);
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));

    	//Restoring state
		if(inSavedInstanceState != null){
			refListType = ListTypeM.valueOf(inSavedInstanceState.getString(EXTRA_AND_BUNDLE_LIST_TYPE));
		}
    	
		//Setup of toast and action behaviour that correspond to the type of list we have for the fragment
		switch(refListType){
		case FEELINGS:
			setToastBehaviour(new FeelingsToast());
			setActionBehaviour(new NoAction());
			break;
		case NEEDS:
			setToastBehaviour(new NeedsToast());
			setActionBehaviour(new NoAction());
			break;
		case KINDNESS:
			setToastBehaviour(new NoToast());
			setActionBehaviour(new MediaFileActionBehaviour());
			break;
		default:
			Log.e(Utils.getClassName() ,"Error in onCreate: ListType not covered by switch statement");
		}
		
    	//Fundamental setup
		super.setRetainInstance(true);
		//-Recommended by CommonsWare:
		// http://stackoverflow.com/questions/11160412/why-use-fragmentsetretaininstanceboolean
		super.setHasOptionsMenu(true);
		this.fillListWithDataFromAdapter();

		//Setup for long click listener
    	super.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> a1, View a2, int a3, long inId) {
				//Opening the details for the list item
				Uri tmpUri = Uri.parse(ContentProviderM.LIST_CONTENT_URI + "/" + inId);
				Intent intent = new Intent(getActivity(), DetailsActivityC.class);
				String tmpExtraString = tmpUri.toString();
				intent.putExtra(EXTRA_ITEM_URI, tmpExtraString); //-Extracted in DataDetailsFragmentC
				startActivityForResult(intent, 0); //-Calling DataDetailsActivityC
				return false;
			}
		});
    }

    /*
	 * Overview: onListItemClick handles clicks on a list item, it updates the DB and refreshes the GUI
	 * Uses app internal: updateCursorLoaderAndAdapter, fireUpdateTabTitles
	 */
    @Override
    public void onListItemClick(ListView l, View inView, int pos, long inId){
    	super.onListItemClick(l, inView, pos, inId);
    	
    	//Switching the checkbox off/on
		CheckBox tmpCheckBox = ((CheckBox)inView.findViewById(R.id.list_item_activeCheckBox));
		tmpCheckBox.toggle();

		//Updating the database value
		Uri tmpUri = Uri.parse(ContentProviderM.LIST_CONTENT_URI + "/" + inId);
		ContentValues tmpContentValues = new ContentValues();
		tmpContentValues.put(ItemTableM.COLUMN_ACTIVE, tmpCheckBox.isChecked() ? 1 : ItemTableM.FALSE);
		getActivity().getContentResolver().update(tmpUri, tmpContentValues, null, null);
		
		//Showing a toast
		mToastBehaviour.toast(getActivity());

		//If the new state of the checkbox is checked..
		if(tmpCheckBox.isChecked() == true){
			
			String tmpSelection = PatternTableM.COLUMN_ITEM_REFERENCE + "=" + "'" + inId + "'";
			Cursor tmpExtendedDataCursor = getActivity().getContentResolver().query(
					ContentProviderM.EXTENDED_DATA_CONTENT_URI, null, tmpSelection, null, null);
			if(tmpExtendedDataCursor.getCount() > 0){
				//-TODO: Will this replace the strategy pattern?
				//..performing the action
				tmpExtendedDataCursor.moveToFirst();
				String tmpFilePath = tmpExtendedDataCursor.getString(
						tmpExtendedDataCursor.getColumnIndexOrThrow(ExtendedDataTableM.COLUMN_DATA));
				mActionBehaviour.kindAction(getActivity(), tmpFilePath);
			}

			//..sorting
			AlgorithmM.get(getActivity()).updateSortValuesForListType();
			ContentProviderM.sSortType = ItemTableM.COLUMN_KINDSORTVALUE + " DESC";
			this.updateCursorLoaderAndAdapter();
			
			//tmpCursor.close();
		}
		
		mCursorAdapter.notifyDataSetChanged();
		sCallbackListener.fireUpdateTabTitles();
    }
	
    
	//-------------------Other lifecycle methods
    
    /*
	 * Overview: onSaveInstanceState saves the state of the list fragment into a bundle.
	 *  Loading is done in onActivityCreated()
	 */
    @Override
    public void onSaveInstanceState(Bundle outBundle){
    	super.onSaveInstanceState(outBundle);
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    	
    	//Saving the list type
    	outBundle.putString(EXTRA_AND_BUNDLE_LIST_TYPE, refListType.toString());
    }

    
	//-------------------Options menu

    /*
	 * Overview: onCreateOptionsMenu inflates the options menu and hides a few options if we have a release build
	 */
	@Override
	public void onCreateOptionsMenu(Menu inMenu, MenuInflater inMenuInflater){
		super.onCreateOptionsMenu(inMenu, inMenuInflater);
		inMenuInflater.inflate(R.menu.actionbarmenu_datalist, inMenu);

		//If we are not running in debug mode, hide the following option items
		if(!BuildConfig.DEBUG){
			inMenu.findItem(R.id.menu_item_share_experience).setVisible(false);
			inMenu.findItem(R.id.menu_item_sort_alphabetically).setVisible(false);
			inMenu.findItem(R.id.menu_item_backup_database).setVisible(false);
			inMenu.findItem(R.id.menu_item_reset_database).setVisible(false);
		}
	}
	
	/*
	 * Overview: onOptionsItemSelected is called when an options item is clicked
	 * Details: Please see each section in the method for details about the different responses for the buttons
	 * Documentation:
	 * http://developer.android.com/reference/android/app/Activity.html#onOptionsItemSelected%28android.view.MenuItem%29
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem inMenuItem){
		
		switch (inMenuItem.getItemId()){
		case R.id.menu_item_new_listitem: //------------New item
			//Creating and inserting the new list item into the database
			ContentValues tmpContentValuesToInsert = new ContentValues();
	    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_NAME, "no_name_set");
	    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_LISTTYPE, refListType.toString());
	    	Uri tmpUriOfNewItem = getActivity().getContentResolver().insert(
	    			ContentProviderM.LIST_CONTENT_URI, tmpContentValuesToInsert);
	    	
	    	//Launching the details fragment for the newly created item
			Intent intent = new Intent(getActivity(), DetailsActivityC.class);
			String tmpExtraString = tmpUriOfNewItem.toString();
			intent.putExtra(EXTRA_ITEM_URI, tmpExtraString);
			//-Extracted in SingleFragmentActivityC and sent to DataDetailsFragmentC
			startActivityForResult(intent, 0); //-Calling DataDetailsActivityC
			
			return true;
		case R.id.menu_item_save_pattern: //------------Saving pattern
			sCallbackListener.fireSavePatternEvent();
			
			return true;
		case R.id.menu_item_share_experience: //TODO: Do this as a hard coded action instead
			/*
			sendAsEmail(
					"From the Kind Mind (Android app): My present experience",
					"I am feeling "
					+ KindModelM.get(getActivity()).getToastString(ListTypeM.SUFFERING)
					+ ", because i am needing "
					+ KindModelM.get(getActivity()).getToastString(ListTypeM.NEEDS)
					+ "\n\nSent from the Android app Kind Mind, can be found in the google play store");
					//"Please help");
			//Asking for help
			//Asking for empathy (Can you reflect back what you are hearing/reading)
			 */
			return true;
		case R.id.menu_item_sort_alphabetically: //------------Sort alphabeta
			//Changing the sort method used and refreshing list
			ContentProviderM.sSortType = ItemTableM.COLUMN_NAME + " ASC";
			this.updateCursorLoaderAndAdapter();
			
			return true;
		case R.id.menu_item_kindsort: //------------Sort kindsort
			//Updating the sort values which will be used below
			AlgorithmM.get(getActivity()).updateSortValuesForListType();
			
			//Changing the sort method used and refreshing list
			ContentProviderM.sSortType = ItemTableM.COLUMN_KINDSORTVALUE + " DESC";
			this.updateCursorLoaderAndAdapter();
			
			return true;
		case R.id.menu_item_clear_all_list_selections: //------------Clear checkmarks for all lists
			//Clearing activated and going left
			sCallbackListener.fireClearAllListsEvent();
			
			return true;
		case R.id.menu_item_send_as_text_all: //------------Send lists as text (partial backup)
			String tmpAllListsAsText =
				this.getFormattedStringForListType(ListTypeM.FEELINGS) +
				this.getFormattedStringForListType(ListTypeM.NEEDS) +
				this.getFormattedStringForListType(ListTypeM.KINDNESS);
			sendAsEmail("KindMind all lists as text", tmpAllListsAsText, null);

			return true;
		case R.id.menu_item_backup_database: //------------Backup of database by sending database file
			sendAsEmail("Backup of KindMind database", "Database file is attached",
					getActivity().getDatabasePath(DatabaseHelperM.DATABASE_NAME));

			return true;
		case R.id.menu_item_reset_database: //------------Resetting database
			sCallbackListener.fireResetData();
			
			return true;
		default:
			return super.onOptionsItemSelected(inMenuItem);
		}
		
	}
	
	/*
	 * Overview: sendAsEmail sends an email with title, text and optionally an attachment
	 * Used in: Helper method for onOptionsItemSelected above
	 * Uses app internal: Utils.copyFile
	 * Notes: File must be stored on the external storage to be accessible by email applications (not enough to
	 *  use the internal cache dir for example)
	 */
	private void sendAsEmail(String inTitle, String inTextContent, File inFileWithPath){
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, inTitle);
		i.putExtra(Intent.EXTRA_TEXT, inTextContent);
		File tmpExtCacheDir = getActivity().getExternalCacheDir();
		if(inFileWithPath != null && tmpExtCacheDir != null){
			String tmpFileName = inFileWithPath.toString().substring(inFileWithPath.toString().lastIndexOf("/") + 1);
			Utils.copyFile(inFileWithPath, new File(tmpExtCacheDir + "/" + tmpFileName));
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(tmpExtCacheDir.toString() + "/" + tmpFileName)));
		}
		startActivity(i);
	}
	
	/*
	 * Overview: getFormattedStringForListType formats a list of a given type into a string
	 * Used in: Used together with sendAsEmail
	 */
	private String getFormattedStringForListType(ListTypeM inListType){
		
		//Title
		String retString = "\n" + "===" + inListType.toString() + "===" + "\n\n";
		
		//Setup of cursor and data set
		String tmpSelection = ItemTableM.COLUMN_LISTTYPE + " = " + "'" + inListType.toString() + "'";
		Cursor tmpCursor = getActivity().getContentResolver().query(
				ContentProviderM.LIST_CONTENT_URI, null, tmpSelection, null, null);
		if(tmpCursor.getCount() == 0){
			tmpCursor.close();
			return retString;
		}

		//For each list data item..
		for(tmpCursor.moveToFirst(); tmpCursor.isAfterLast() == false; tmpCursor.moveToNext()){
			//..save the name
			retString = retString
					+ tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME)) + "\n";
		}
		tmpCursor.close();
		
		return retString;
	}
	
	
	//-------------------Toast and Action Behaviour [uses the Strategy pattern]
	
	void setToastBehaviour(ToastBehaviour inToastBehaviour){
		mToastBehaviour = inToastBehaviour;
	}

	void setActionBehaviour(ActionBehaviour inKindActionBehaviour){
		mActionBehaviour = inKindActionBehaviour;
	}
}
