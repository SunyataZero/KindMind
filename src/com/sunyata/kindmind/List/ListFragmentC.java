package com.sunyata.kindmind.List;

import java.io.File;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.KindMindContentProviderM;
import com.sunyata.kindmind.Details.DetailsActivityC;

/*
 * Overview: ListFragmentC can show a list of items (each list item corresponding to a row in the SQL database)
 * 
 * Details: 
 * 
 * Implements: 
 * 
 * Extends: 
 * 
 * Used in: 
 * 
 * Uses app internal: 
 * 
 * Uses Android lib: *ListView*, ListFragment, LoaderManager, CursorLoader
 *  http://developer.android.com/reference/android/widget/ListView.html
 *  https://developer.android.com/reference/android/support/v4/app/ListFragment.html
 *  https://developer.android.com/reference/android/support/v4/app/LoaderManager.html
 *  https://developer.android.com/reference/android/support/v4/content/CursorLoader.html
 * 
 * In: 
 * 
 * Out: 
 * 
 * Does: 
 * 
 * Shows user: 
 * 
 * Notes: *Support classes are used*
 *  import android.support.v4.app.ListFragment;
 *  import android.support.v4.app.LoaderManager;
 *  import android.support.v4.content.CursorLoader;
 *  The reason for this is that the ViewPager only is available in a support version and is not compatible
 *  with other versions
 * 
 * Improvements: 
 * 
 * Documentation: 
 * 
 */
public class ListFragmentC extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
//will later on extend an abstract class
	
	//-------------------Fields and constructor
	
	private ListTypeM refListType; //-Saved in onSaveInstanceState
	private static MainActivityCallbackListenerI sCallbackListener; //-Does not have to be saved since it's static
	private ToastBehaviour mToastBehaviour; //-Not saved, but set in onResume
	private ActionBehaviour mActionBehaviour; //-Not saved, but set in onResume
	private CustomCursorAdapter mCustomCursorAdapter;

	public static final String EXTRA_ITEM_URI = "EXTRA_LIST_DATA_ITEM_ID";
	public static final String EXTRA_AND_BUNDLE_LIST_TYPE = "EXTRA_LIST_TYPE";

	public static ListFragmentC newInstance(ListTypeM inListType, MainActivityCallbackListenerI inCallbackListener){
		ListFragmentC retListFragment = new ListFragmentC();
		retListFragment.refListType = inListType;
		sCallbackListener = inCallbackListener;
		return retListFragment;
	}

	
	//-------------------Loader methods
	//A very good example is available at the top of the following page:
	// http://developer.android.com/reference/android/app/LoaderManager.html
	
	/*
	 * Overview: onCreateLoader creates and returns the CursorLoader, which contains the mapping to the database
	 * Used in: Override method called after TODO ___________________________
	 * Documentation: 
	 * http://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html#onCreateLoader%28int,%20android.os.Bundle%29
	 */
	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int inIdUnused, Bundle inArgumentsUnused) {
		Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
		
		//Setup of variables used for selecting the database colums of rows (for the creation of the CursorLoader)
		String[] tmpProjection = {ItemTableM.COLUMN_ID, ItemTableM.COLUMN_NAME,
				ItemTableM.COLUMN_TAGS, ItemTableM.COLUMN_ACTIVE};
		String tmpSelection = ItemTableM.COLUMN_LISTTYPE + " = ?";
		String[] tmpSelectionArguments = {refListType.toString()};
		//-TODO: There is an error here when restarting the app if leaving it for a while 

		//Creating the CursorLoader
		CursorLoader retCursorLoader = new CursorLoader(
				getActivity(), KindMindContentProviderM.LIST_CONTENT_URI,
				tmpProjection, tmpSelection, tmpSelectionArguments, KindMindContentProviderM.sSortType);
		return retCursorLoader;
	}
	/*
	 * Overview: onLoadFinished
	 * Used in: "Called when a previously created loader has finished its load"
	 * Documentation *interesting to read*:
	 * http://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html#onLoadFinished%28android.content.Loader%3CD%3E,%20D%29
	 */
	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> inCursorLoader, Cursor inCursor) {
		Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
		
		mCustomCursorAdapter.swapCursor(inCursor);
	}
	/*
	 * Overview: onLoaderReset
	 * Used in: "Called when a previously created loader is being reset, and thus making its data unavailable"
	 */
	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> inCursorUnused) {
		Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
		
		mCustomCursorAdapter.swapCursor(null);
	}
	
	
	//-------------------Update methods
	
	/* 
	 * Overview: createListDataSupport fills the data from the database into the loader
	 *  by creating a cursor adapter (with mapping to database columns) and initiating the loader
	 * Used in: onActivityCreated
	 * Uses app internal: CustomCursorAdapter
	 * Uses Android lib: setListAdapter, initLoader
	 * Notes: The synching on the state of the checkboxes with the database is not automatically, and
	 *  this is handled in another place (getView in CustomCursorAdapter)
	 */
	void createListDataSupport(){
		//Creating the SimpleCursorAdapter for the specified database columns linked to the specified GUI views..
		String[] tmpDatabaseFrom = {ItemTableM.COLUMN_NAME, ItemTableM.COLUMN_TAGS}; //, ItemTableM.COLUMN_ACTIVE
		int[] tmpDatabaseTo = {R.id.list_item_titleTextView, R.id.list_item_tagsTextView}; //, R.id.list_item_activeCheckBox
		mCustomCursorAdapter = new CustomCursorAdapter(
				getActivity(), R.layout.ofnr_list_item, null,
				tmpDatabaseFrom, tmpDatabaseTo, 0, refListType);
		
		
		//..using this CursorAdapter as the adapter for this ListFragment
		super.setListAdapter(mCustomCursorAdapter);
		Log.i(Utils.getClassName(), "mCursorAdapter.getCount() = " + mCustomCursorAdapter.getCount());
		
		//Creating (or re-creating) the loader 
		getLoaderManager().initLoader(0, null, this);
		//-PLEASE NOTE: using the non-support LoaderManager import gives an error
		//-initLoader seems to be preferrable to restartLoader
		
	}
	
	/*
	 * Overview: refreshList updates the list
	 *  by changing the cursor and giving the cursor to the adapter, and restarting the loader 
	 * Used in: 
	 * Uses Android lib: changeCursor, setAdapter, restartLoader
	 * In: 
	 * 
	 * Out: 
	 * 
	 * Does: 
	 * 
	 * Shows user: 
	 * 
	 * Notes: 
	 * 
	 * Improvements: 
	 * 
	 * Documentation: 
	 * 
	 */
	void refreshListDataSupport() {

		//Update the cursor..
		String tmpSelection =
				ItemTableM.COLUMN_LISTTYPE + "=" + "'" + this.refListType.toString() + "'";
		Cursor tmpCursor = getActivity().getContentResolver().query(
				KindMindContentProviderM.LIST_CONTENT_URI, null,
				tmpSelection, null,
				KindMindContentProviderM.sSortType);
		mCustomCursorAdapter.changeCursor(tmpCursor);
		
		//..and use the new cursor for the adapter
		getListView().setAdapter(mCustomCursorAdapter);
		//-PLEASE NOTE: We need this line, and it was hard to find this info. It was found here:
		// http://stackoverflow.com/questions/8213200/android-listview-update-with-simplecursoradapter

		getLoaderManager().restartLoader(0, null, this);
		//-PLEASE NOTE: We need this line, otherwise the checkbox status will not be updated
		
		//Scroll to the top of the list
		getListView().smoothScrollToPosition(0);
		
		/*
		getActivity().getContentResolver().notifyChange(KindMindContentProviderM.LIST_CONTENT_URI, null);
		((CustomCursorAdapter)super.getListAdapter()).notifyDataSetChanged();
		getLoaderManager().restartLoader(0, null, this);
		*/
		/*
		getLoaderManager().restartLoader(0, null, this);
		getLoaderManager().initLoader(0, null, this);
		*/
		/*
		getLoaderManager().getLoader(0).reset();
		getLoaderManager().getLoader(0).stopLoading();
		getLoaderManager().getLoader(0).startLoading();
		*/
		/*
		getListView().refreshDrawableState();
		getListView().invalidate();
		getListView().invalidateViews();
		*/
		
		/* PLEASE NOTE: We don't close the cursor here since if we do that we will get the following:
		01-21 21:45:28.546: E/AndroidRuntime(3173): FATAL EXCEPTION: main
		01-21 21:45:28.546: E/AndroidRuntime(3173): android.database.StaleDataException: Attempting to access a closed CursorWindow.Most probable cause: cursor is deactivated prior to calling this method.
		01-21 21:45:28.546: E/AndroidRuntime(3173): 	at android.database.AbstractWindowedCursor.checkPosition(AbstractWindowedCursor.java:139)
		01-21 21:45:28.546: E/AndroidRuntime(3173): 	at android.database.AbstractWindowedCursor.getString(AbstractWindowedCursor.java:50)
		01-21 21:45:28.546: E/AndroidRuntime(3173): 	at android.database.CursorWrapper.getString(CursorWrapper.java:114)
		01-21 21:45:28.546: E/AndroidRuntime(3173): 	at com.sunyata.kindmind.ListFragmentC$CustomCursorAdapter.getView(ListFragmentC.java:143)
		 */
	}

	
	//-------------------onCreate, onActivityCreated and click methods
	// Please note that one click method is inside onActivityCreated and the other is outside
	
	@Override
	public void onCreate(Bundle inSavedInstanceState){
		super.onCreate(inSavedInstanceState);
		if(inSavedInstanceState != null){
			refListType = ListTypeM.valueOf(inSavedInstanceState.getString(EXTRA_AND_BUNDLE_LIST_TYPE));
		}
	}
	//We get to onActivityCreated after onAttach and onCreateView.
    //Alternatively after onAttach, onCreate and onCreateView
    @Override
    public void onActivityCreated(Bundle inSavedInstanceState){
    	super.onActivityCreated(inSavedInstanceState);
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    	
		super.setRetainInstance(true);
		//super.setEmptyText("List is empty, please click the add item button");
		super.setHasOptionsMenu(true);
		this.createListDataSupport();

    	super.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View v, int position, long id) {

				Uri tmpUri = Uri.parse(KindMindContentProviderM.LIST_CONTENT_URI + "/" + id);
				Intent intent = new Intent(getActivity(), DetailsActivityC.class);
				String tmpExtraString = tmpUri.toString();
				intent.putExtra(EXTRA_ITEM_URI, tmpExtraString); //Extracted in DataDetailsFragmentC
				startActivityForResult(intent, 0); //Calling DataDetailsActivityC
				
				return false;
			}
		});

    	//Alternative to onListItemClick below where we can access the adapter:
    	/*
    	super.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View inView, int inPosition, long inId) {
			[...]
    	*/
    }
    @Override
    public void onListItemClick(ListView l, View inView, int pos, long inId){
    	super.onListItemClick(l, inView, pos, inId);
    	
    	//Switching the checkbox off/on (depending on the state)
		CheckBox tmpCheckBox = ((CheckBox)inView.findViewById(R.id.list_item_activeCheckBox));
		
		//tmpCheckBox.setOnCheckedChangeListener(null);
		tmpCheckBox.toggle();
		//tmpCheckBox.setOnCheckedChangeListener(null);

		//Updating the database value
		Uri tmpUri = Uri.parse(KindMindContentProviderM.LIST_CONTENT_URI + "/" + inId);
		ContentValues tmpContentValues = new ContentValues();
		tmpContentValues.put(ItemTableM.COLUMN_ACTIVE, tmpCheckBox.isChecked() ? 1 : 0);
		//-PLEASE NOTE: Now confirmed that only this one (right one) value is being updated, so this part is working
		// (what is left is the viewing of the data
		//-Boolean stored as 0 (false) or 1 (true)
		getActivity().getContentResolver().update(tmpUri, tmpContentValues, null, null);
		
		//Showing a toast
		mToastBehaviour.toast();

		//Doing the action associated with the list item that was clicked
		Cursor tmpCursor = getActivity().getContentResolver().query(tmpUri, null, null, null, KindMindContentProviderM.sSortType);
		tmpCursor.moveToFirst();
		String tmpFilePath = tmpCursor.getString(
				tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_FILEORDIRPATH));
		mActionBehaviour.kindAction(tmpFilePath);
		
		mCustomCursorAdapter.notifyDataSetChanged();
		
		//tmpCursor.close();
    }
	
    
	//-------------------Other lifecycle methods
    
    @Override
    public void onStop(){
    	super.onStop();
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    }
    //PLEASE NOTE: When a new activity is created, this method is called on a physical device, but not on the emulator
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    }
    @Override
    public void onResume(){
    	//-PLEASE NOTE: When switching between different fragments in the ViewPager,
    	// we cannot use this method for changes we want to see when the state changes.
    	// This method is called when the fragment is loaded to be ready, not when it is
    	// shown (strangely enough). Instead we can use onPageSelected in MainActivityC.
    	super.onResume();
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    	
    	
		switch(refListType){
		case FEELINGS:
			setToastBehaviour(new FeelingsToast());
			setActionBehaviour(new OnlyTitleActionBehaviour());
			break;
		case NEEDS:
			setToastBehaviour(new NeedsToast());
			setActionBehaviour(new OnlyTitleActionBehaviour());
			break;
		case ACTIONS:
			setToastBehaviour(new NoToast());
			setActionBehaviour(new MediaFileActionBehaviour());
			break;
		default:
			Log.e(Utils.getClassName() ,"Error in onCreate: ListType not covered by switch statement");
		}
    }
    @Override
    public View onCreateView(LayoutInflater inInflater, ViewGroup inContainer, Bundle inSavedinstanceState){
    	View retView = super.onCreateView(inInflater, inContainer, inSavedinstanceState);
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    	return retView;
    }
    @Override
    public void onAttach(Activity inActivity){
    	super.onAttach(inActivity);
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    }
    @Override
    public void onDetach(){
    	super.onDetach();
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    }
    //Please note that the loading is done in onCreate(), onCreateView() and onActivityCreated()
    @Override
    public void onSaveInstanceState(Bundle outBundle){
    	super.onSaveInstanceState(outBundle);
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    	
    	//Saving the list type
    	outBundle.putString(EXTRA_AND_BUNDLE_LIST_TYPE, refListType.toString());
    }

    
	//-------------------Options menu

	@Override
	public void onCreateOptionsMenu(Menu inMenu, MenuInflater inMenuInflater){
		super.onCreateOptionsMenu(inMenu, inMenuInflater);
		inMenuInflater.inflate(R.menu.actionbarmenu_datalist, inMenu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem inMenuItem){
		
		switch (inMenuItem.getItemId()){
		case R.id.menu_item_new_listitem:
			ContentValues tmpContentValuesToInsert = new ContentValues();
	    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_NAME, "no_name_set");
	    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_LISTTYPE, refListType.toString());
	    	Uri tmpUriOfNewlyAddedItem =
	    			getActivity().getContentResolver().insert(
	    			KindMindContentProviderM.LIST_CONTENT_URI, tmpContentValuesToInsert);
	    	//PLEASE NOTE: We use URIs instead of IDs for identifying items (since we don't connect directly to thd DB)
	    	
			Intent intent = new Intent(getActivity(), DetailsActivityC.class);
			
			String tmpExtraString = tmpUriOfNewlyAddedItem.toString();
			intent.putExtra(EXTRA_ITEM_URI, tmpExtraString);
			//-Extracted in SingleFragmentActivityC and sent to DataDetailsFragmentC
			startActivityForResult(intent, 0); //Calling DataDetailsActivityC
			
			return true;
		case R.id.menu_item_share_experience:
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
		case R.id.menu_item_sort_alphabetically:
			//Changing the sort method used and refreshing list
			KindMindContentProviderM.sSortType = ItemTableM.COLUMN_NAME + " ASC";
			this.refreshListDataSupport();
			
			return true;
		case R.id.menu_item_kindsort:
			//Updating the sort values which will be used below
			KindModelM.get(getActivity()).updateSortValuesForListType(this.getActivity(), refListType);
			
			//Changing the sort method used and refreshing list
			KindMindContentProviderM.sSortType = ItemTableM.COLUMN_KINDSORTVALUE + " DESC";
			this.refreshListDataSupport();
			
			return true;
		case R.id.menu_item_save_pattern:
			//Save all the checked list items into the PATTERN table
			sCallbackListener.fireSavePatternEvent();
			
			return true;
		case R.id.menu_item_clear_all_list_selections:
			//Clearing activated and going left, but without saving
			sCallbackListener.fireClearAllListsEvent();
			
			return true;
		case R.id.menu_item_send_as_text_all:
			//String tmpAllListAsText = KindModelM.get(getActivity()).getFormattedStringWithAllLists();
			//sendAsEmail("KindMind all lists as text", tmpAllListAsText);

			return true;
		default:
			return super.onOptionsItemSelected(inMenuItem);
		}
		
	}
	
	private void sendAsEmail(String inTitle, String inTextContent){
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, inTitle);
		i.putExtra(Intent.EXTRA_TEXT, inTextContent);
		startActivity(i);
	}
	
	/*
	String toFormattedString(){
		String retFormattedString = "List type: " + refListType + "\n";
		for(ListDataItemM ldi : mList){
			retFormattedString = retFormattedString + ldi.toFormattedString();
		}
		retFormattedString = retFormattedString + "\n\n";
		return retFormattedString;
	}
	*/
	
	
	//-------------------Toast Behaviour [uses the Strategy pattern]
	
	interface ToastBehaviour{
		public void toast();
	}
	void setToastBehaviour(ToastBehaviour inToastBehaviour){
		mToastBehaviour = inToastBehaviour;
	}
	
	class FeelingsToast implements ToastBehaviour{
		@Override
		
		public void toast() {
			String tmpToastFeelingsString = KindModelM.get(getActivity()).getToastString(ListTypeM.FEELINGS);
			if(tmpToastFeelingsString.length() > 0){
				Toast.makeText(
						getActivity(), "I am feeling " + tmpToastFeelingsString, Toast.LENGTH_LONG)
						.show();
			}
		}
	}
	
	class NeedsToast implements ToastBehaviour{
		@Override
		public void toast() {

			String tmpToastFeelingsString = KindModelM.get(getActivity()).getToastString(ListTypeM.FEELINGS);
			String tmpToastNeedsString = KindModelM.get(getActivity()).getToastString(ListTypeM.NEEDS);
			
			if(tmpToastFeelingsString.length() > 0 & tmpToastNeedsString.length() > 0){
				Toast.makeText(
						getActivity(),
						"I am feeling " + tmpToastFeelingsString +
						" because I am needing " + tmpToastNeedsString, Toast.LENGTH_LONG)
						.show();
			}else if(tmpToastNeedsString.length() > 0){
					Toast.makeText(
							getActivity(),
							"I am needing " + tmpToastNeedsString, Toast.LENGTH_LONG)
							.show();
			}else{
					//Do nothing
			}
		}
	}
	
	class NoToast implements ToastBehaviour{
		@Override
		public void toast() {
			//Nothing is done
		}
	}
	
	
	//-------------------Action Behaviour [uses the strategy pattern]
	
	interface ActionBehaviour{
		public void kindAction(String inKindActionFilePath);
	}
	void setActionBehaviour(ActionBehaviour inKindActionBehaviour){
		mActionBehaviour = inKindActionBehaviour;
	}
	
	class MediaFileActionBehaviour implements ActionBehaviour{
		@Override
		public void kindAction(String inKindActionFilePath) {
			Log.i(Utils.getClassName(), "inKindActionFilePath = " + inKindActionFilePath);
			
			if(inKindActionFilePath == ""){
				return;
			}else{
				File tmpFileOrDirectoryFromString = new File(inKindActionFilePath);
				
				Log.i(Utils.getClassName(), "tmpFileOrDirectoryFromString.isDirectory() = "
						+ tmpFileOrDirectoryFromString.isDirectory());
				if(tmpFileOrDirectoryFromString.isDirectory()){
					this.doRandomKindActionFromSetOfFiles(tmpFileOrDirectoryFromString);
				}else{
					this.doKindAction(inKindActionFilePath);
				}
			}
		}
		private void doKindAction(String inFileFromString){
			Log.i(Utils.getClassName(), "inFileFromString = " + inFileFromString);
			
			/*
			//Ok, works well!
			Intent tmpIntent = new Intent(Intent.ACTION_DIAL);
			tmpIntent.setData(Uri.parse("tel:123"));
			*/

			AudioManager tmpAudioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
			String tmpTypeString = "*/*";
			
			Intent tmpIntent;
			Uri tmpUri;
			File tmpFileOrDirectoryFromString;
			
			if(inFileFromString.toString().startsWith("content://")){ //==========Contacts==========
				
				tmpIntent = new Intent(Intent.ACTION_VIEW);
				tmpUri = Uri.parse(inFileFromString);
				//tmpIntent.setData(tmpUri); //doesn't work
				tmpIntent.setDataAndType(tmpUri, tmpTypeString);
				//-NOTE: THIS IS OK, BUT SPLITTING DATA AND TYPE DOES NOT WORK

			}else if(inFileFromString.toString().startsWith("http://")){

				tmpIntent = new Intent(Intent.ACTION_VIEW);
				tmpUri = Uri.parse(inFileFromString);
				//tmpIntent.setData(tmpUri); //doesn't work
				tmpIntent.setDataAndType(tmpUri, tmpTypeString);
				//-NOTE: THIS IS OK, BUT SPLITTING DATA AND TYPE DOES NOT WOR
				
			}else{ //==========Media files==========

				tmpFileOrDirectoryFromString = new File(inFileFromString);
				
				if(
						inFileFromString.toString().endsWith(".jpg")||
						inFileFromString.toString().endsWith(".jpeg")||
						inFileFromString.toString().endsWith(".png")||
						inFileFromString.toString().endsWith(".gif")){
					tmpTypeString = "image/*";
				}else if(
						inFileFromString.toString().endsWith(".ogg")||
						inFileFromString.toString().endsWith(".mp3")){

					if(tmpAudioManager.isWiredHeadsetOn() == false || tmpAudioManager.isSpeakerphoneOn() == true){
						/*
					isWiredHeadsetOn is used even though it is deprecated:
					"
					This method was deprecated in API level 14.
					Use only to check is a headset is connected or not.
					"
					http://stackoverflow.com/questions/2764733/android-checking-if-headphones-are-plugged-in
						 */
						Toast.makeText(
								getActivity(),
								"Not playing audio since headset is not connected or speaker phone is on",
								Toast.LENGTH_LONG)
								.show();
						return;
					}

					tmpTypeString = "audio/*";

				}else if(
						inFileFromString.toString().endsWith(".mp4")||
						inFileFromString.toString().endsWith(".avi")){
					if(tmpAudioManager.isWiredHeadsetOn() == false || tmpAudioManager.isSpeakerphoneOn() == true){
						Toast.makeText(
								getActivity(),
								"Not playing video since headset is not connected or speaker phone is on",
								Toast.LENGTH_LONG)
								.show();
						return;
					}

					tmpTypeString = "video/*";

				}else{
					//Continue with "*/*"
				}
				
				tmpIntent = new Intent(Intent.ACTION_VIEW);
				tmpUri = Uri.fromFile(tmpFileOrDirectoryFromString);
				//tmpIntent.setData(tmpUri); //doesn't work
				tmpIntent.setDataAndType(tmpUri, tmpTypeString);
				//-NOTE: THIS IS OK, BUT SPLITTING DATA AND TYPE DOES NOT WORK
				
			}
			

			/*
			TODO:
			choice of file
			choice of number/contact (nerd book)
			choice online url
			future: pinterest api, other apis
			*/
			
			//Verifying that we have at least one app that can handle this intent before starting
			PackageManager tmpPackageManager = getActivity().getApplicationContext().getPackageManager();
			List<ResolveInfo> tmpListOfAllPosibleAcitivtiesForStarting =
					tmpPackageManager.queryIntentActivities(tmpIntent, 0);
			if(tmpListOfAllPosibleAcitivtiesForStarting.size() > 0){
				//===================Starting the activity===================
				getActivity().startActivity(tmpIntent);
			}else{
				Toast.makeText(getActivity(),
						"Currently no app supports this file type on this device, " +
						"please install an app that supports this operation",
						Toast.LENGTH_LONG)
								.show();
			}
		}
		private void doRandomKindActionFromSetOfFiles(File inDirectoryFromString){
			Log.i(Utils.getClassName(), "inDirectoryFromString = " + inDirectoryFromString);
			
			String[] tmpListOfFilesInDirectory = inDirectoryFromString.list();
			Random tmpRandomNumberGenerator = new Random();
			int tmpNumberOfFilesInDirectory = tmpListOfFilesInDirectory.length;
			int tmpRandomNumber = tmpRandomNumberGenerator.nextInt(tmpNumberOfFilesInDirectory);
			
			File tmpRandomlyGivenFile = new File(
					inDirectoryFromString + "/"
					+ tmpListOfFilesInDirectory[tmpRandomNumber]);
			this.doKindAction(tmpRandomlyGivenFile.toString());
		}
	}
	
	class OnlyTitleActionBehaviour implements ActionBehaviour{
		@Override
		public void kindAction(String inKindActionFilePath) {
			//do nothing
		}
	}

}