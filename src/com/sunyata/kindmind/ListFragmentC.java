package com.sunyata.kindmind;

import java.io.File;
import java.util.ArrayList;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sunyata.kindmind.contentprovider.ListContentProviderM;

public class ListFragmentC extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
//will later on extend an abstract class
	
	//-------------------Fields and constructor
	
	static final String EXTRA_ITEM_URI = "EXTRA_LIST_DATA_ITEM_ID";
	static final String EXTRA_LIST_TYPE = "EXTRA_LIST_TYPE";
	private ListTypeM refListType;
	private ToastBehaviour mToastBehaviour;
	private static MainActivityCallbackListenerI mCallbackListener;
	private KindActionBehaviour mKindActionBehaviour;
	
	public static ListFragmentC newInstance(ListTypeM inListType, MainActivityCallbackListenerI inCallbackListener){
		//Bundle tmpArguments = new Bundle();
		//tmpArguments.putString(Utils.LIST_TYPE, inListType.toString());
		
		ListFragmentC retListFragment = new ListFragmentC();
		retListFragment.refListType = inListType;
		//retListFragment.setArguments(tmpArguments);
		
		mCallbackListener = inCallbackListener;
		return retListFragment;
	}
	
	
	
	//-------------------Methods for LoaderManager.LoaderCallbacks<Cursor>
	
	private SimpleCursorAdapter mCursorAdapter;
	
	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int inId, Bundle inArguments) {

		String[] tmpProjection = {ItemTableM.COLUMN_ID, ItemTableM.COLUMN_NAME};
		String tmpSelection = ItemTableM.COLUMN_LISTTYPE + " = ?";
		String[] tmpSelectionArguments = {refListType.toString()};
		CursorLoader retCursorLoader = new CursorLoader(
				getActivity(), ListContentProviderM.CONTENT_URI,
				tmpProjection, tmpSelection, tmpSelectionArguments, null);

		return retCursorLoader;
	}


	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> inCursorLoader, Cursor inCursor) {

		//TODO: Update?

		mCursorAdapter.swapCursor(inCursor);
		
	}
	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> arg0) {

		//TODO: Update?
		
		mCursorAdapter.swapCursor(null);
	}
	
	
	private void updateListWithNewData(){
		
		String[] tmpDatabaseFrom = {ItemTableM.COLUMN_NAME};
		int[] tmpDatabaseTo = {R.id.list_item_titleTextView};
		
		getLoaderManager().initLoader(0, null, this);
		//-PLEASE NOTE: using the non-support LoaderManager import gives an error
		
		mCursorAdapter = new SimpleCursorAdapter(
				getActivity(), R.layout.ofnr_list_item, null,
				tmpDatabaseFrom, tmpDatabaseTo, 0);

		setListAdapter(mCursorAdapter);
	}
	
	
	//-------------------Lifecycle methods
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		updateListWithNewData();
		
		setHasOptionsMenu(true);
		
		//refListType = ListTypeM.valueOf(getArguments().getString(EXTRA_LIST_TYPE));
	}
    //Important: When a new activity is created, this method is called on a physical device, but not on the emulator
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
    	
    	//this.initialize();
    	
    	/*
    	if(getListAdapter() != null){
        	((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
    	}
    	*/
    	

		switch(refListType){
		case EVENT:
			setToastBehaviour(new NoToast());
			setKindActionBehaviour(new OnlyTitleKindActionBehaviour());
			break;
		case SUFFERING:
			setToastBehaviour(new FeelingsToast());
			setKindActionBehaviour(new OnlyTitleKindActionBehaviour());
			break;
		case NEEDS:
			setToastBehaviour(new NeedsToast());
			setKindActionBehaviour(new OnlyTitleKindActionBehaviour());
			break;
		case KINDNESS:
			setToastBehaviour(new NoToast());
			setKindActionBehaviour(new MediaFileKindActionBehaviour());
			break;
		default:Log.e(Utils.getClassName() ,"Error in onCreate: ListType not covered by switch statement");
		}


    }
    @Override
    public View onCreateView(LayoutInflater inInflater, ViewGroup inContainer, Bundle inSavedinstanceState){
    	View retView = super.onCreateView(inInflater, inContainer, inSavedinstanceState);
    	
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    	return retView;
    }
	//We get to onActivityCreated after onAttach and onCreateView.
    //Alternatively after onAttach, onCreate and onCreateView
    @Override
    public void onActivityCreated(Bundle inSavedInstanceState){
    	super.onActivityCreated(inSavedInstanceState);
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    	
    	this.updateListWithNewData();
    	
    	
    	super.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View v, int position, long id) {

				Uri tmpUri = Uri.parse(ListContentProviderM.CONTENT_URI + "/" + id);
				Intent intent = new Intent(getActivity(), DetailsActivityC.class);
				String tmpExtraString = tmpUri.toString();
				intent.putExtra(EXTRA_ITEM_URI, tmpExtraString); //Extracted in DataDetailsFragmentC
				startActivityForResult(intent, 0); //Calling DataDetailsActivityC
				
				return false;
			}
		});
    	
    	super.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View inView, int inPosition, long inId) {
				
				CheckBox tmpCheckBox = ((CheckBox)inView.findViewById(R.id.list_item_activeCheckBox));
				
				tmpCheckBox.toggle();

				Uri tmpUri = Uri.parse(ListContentProviderM.CONTENT_URI + "/" + inId);
				ContentValues tmpContentValues = new ContentValues();
				tmpContentValues.put(ItemTableM.COLUMN_ACTIVE, tmpCheckBox.isChecked());
				//-Boolean stored as 0 (false) or 1 (true)
				getActivity().getContentResolver().update(tmpUri, tmpContentValues, null, null);
				
				
				mToastBehaviour.toast(); //Också för när man klickar på själva checkboxen
				
				Cursor tmpCursor = getActivity().getContentResolver().query(tmpUri, null, null, null, null);
				tmpCursor.moveToFirst();
				String tmpFilePath = tmpCursor.getString(
						tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_FILEORDIRPATH));
				mKindActionBehaviour.kindAction(tmpFilePath);
				
			}
    	});
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
    //Please note that the loading is done in onCreate()
    @Override
    public void onSaveInstanceState(Bundle inBundle){
    	super.onSaveInstanceState(inBundle);
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    	//Saving the UI details to a bundle
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
			//ItemM tmpNewListDataItem = new ItemM(refListType);
			//boolean tmpCreatedSuccessfully = true; //TODO Change
					//KindModelM.get(getActivity()).getListOfType(refListType).addItem(tmpNewListDataItem, true);
			/*
			if(!tmpCreatedSuccessfully){
				Log.e(Utils.getClassName(), "Error in onOptionsItemSelected: Could not add ListDataItem to list");
			}
			*/
			ContentValues tmpContentValuesToInsert = new ContentValues();
	    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_NAME, "no_name_set");
	    	tmpContentValuesToInsert.put(ItemTableM.COLUMN_LISTTYPE, refListType.toString());
	    	Uri tmpUriOfNewlyAddedItem =
	    			getActivity().getContentResolver().insert(
	    			ListContentProviderM.CONTENT_URI, tmpContentValuesToInsert);
	    	//PLEASE NOTE: We use URIs instead of IDs for identifying items (since we don't connect directly to thd DB)
	    	
			Intent intent = new Intent(getActivity(), DetailsActivityC.class);
			
			//mCursorAdapter.getItemId()
			
			String tmpExtraString = tmpUriOfNewlyAddedItem.toString();
			intent.putExtra(EXTRA_ITEM_URI, tmpExtraString);
			//-Extracted in SingleFragmentActivityC and sent to DataDetailsFragmentC
			////intent.putExtra(EXTRA_LIST_TYPE, ListTypeM.SUFFERING.toString()); //Extracted in SingleFragmentActivityC
			startActivityForResult(intent, 0); //Calling DataDetailsActivityC
			
			//((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			
			return true;
		
		case R.id.menu_item_clear_all_list_selections:
			((MainActivityC)getActivity()).clearActivated();
			((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			//-Only done for this Fragment but this is the only place where it is necassary since
			// the others will be updated when the pager page is changed.
			
			//Move back to the left-most position
			mCallbackListener.fireGoLeftmostEvent();
			
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
			/*
			KindModelM.get(getActivity()).getListOfType(refListType).sortAlphabetically();
			((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			getListView().smoothScrollToPosition(0);//Scroll to the top of the list
			*/
			
			
			//Add some start data to the database
	    	ContentValues tmpContentValuesToInsert2 = new ContentValues();
	    	tmpContentValuesToInsert2.put(ItemTableM.COLUMN_NAME, "name_test_1");
	    	getActivity().getContentResolver().insert(ListContentProviderM.CONTENT_URI, tmpContentValuesToInsert2);
	    	//tmpContentValuesToInsert.put(ListTableM.COLUMN_LISTTYPE, "feelings");
	    	//tmpContentValuesToInsert.put(ListTableM.COLUMN_ACTIVE, 0);
	    	//tmpContentValuesToInsert.put(ListTableM.COLUMN_FILEORDIRPATH, "/");
	    	//tmpContentValuesToInsert.put(ListTableM.COLUMN_NOTIFICATIONACTIVE, 0);
	    	//tmpContentValuesToInsert.put(ListTableM.COLUMN_NOTIFICATIONTIME, 0);
	    	
			

			
			return true;
			
		case R.id.menu_item_kindsort:
			/*
			KindModelM.get(getActivity()).loadPatternListsFromJsonFiles();
			KindModelM.get(getActivity()).updateSortValuesForListType(refListType);
			KindModelM.get(getActivity()).getListOfType(refListType).sortWithKindness();
			//-Refactor: Put the two lines above into one method?
			((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			getListView().smoothScrollToPosition(0);//Scroll to the top of the list
			*/
			return true;
		
		
		case R.id.menu_item_save_pattern:
			//KindModelM.get(getActivity()).savePatternListToJson();
			return true;
			
		case R.id.menu_item_send_as_text_current:
			//sendAsEmail("KindMind list as text", refListData.toFormattedString());
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
	
	
	//-------------------Adapter that listens to button clicks
    
	/*
	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		
	}
	*/
	
	
	class ListFragmentDataAdapterC extends ArrayAdapter<ItemM> {
		
		public ListFragmentDataAdapterC(ArrayList<ItemM> inListData){
			super(getActivity(), android.R.layout.simple_list_item_1, inListData);
		}
		
		@Override
		public void notifyDataSetChanged(){ //Issue 1: Not called
			super.notifyDataSetChanged();
		}
		
		//Giving the view for a single list item
		@Override
		public View getView(int inPosition, View inConvertView, ViewGroup inParent){

			if (inConvertView == null){
				inConvertView = getActivity().getLayoutInflater().inflate(R.layout.ofnr_list_item, null);//can pass parent here
			}
			
			ItemM tmpListDataItem = getItem(inPosition);

			CheckBox tmpActiveCheckBox = (CheckBox)inConvertView.findViewById(R.id.list_item_activeCheckBox);
			tmpActiveCheckBox.setClickable(false); //We handle this ourselves
			tmpActiveCheckBox.setChecked(tmpListDataItem.isActive());
			
			//Setting the on click and on long click listeners for the whole layout
			inConvertView.setOnClickListener(new CustomOnClickListener(inPosition));
			//inConvertView.setOnLongClickListener(new CustomOnLongClickListener(inPosition));
			
			TextView tmpTitleTextView = (TextView)inConvertView.findViewById(R.id.list_item_titleTextView);
			String tmpSortValueStringOnlyForDebug = "";
			if(BuildConfig.DEBUG){
				tmpSortValueStringOnlyForDebug = " | " + Utils.formatNumber(tmpListDataItem.getTotalSortValue());
			}
			tmpTitleTextView.setText(tmpListDataItem.getName() + tmpSortValueStringOnlyForDebug);

			/*
			TextView tmpSimilarItemsTextView = (TextView)inConvertView.findViewById(R.id.feeling_list_item_similarItemsTextView);
			tmpSimilarItemsTextView.setText(tmpListDataItem.getSimilar());
			*/
			
			return inConvertView;
		}
		private class CustomOnClickListener implements OnClickListener{
			private int mPosition;
			public CustomOnClickListener(int inPosition){
				mPosition = inPosition;
			}

			@Override
			public void onClick(View inView) {
				
				/*
				boolean tmpWasChecked = refListData.getItem(mPosition).isActive();
				boolean tmpIsChecked = !tmpWasChecked;

				((CheckBox)inView.findViewById(R.id.list_item_activeCheckBox)).setChecked(tmpIsChecked);
				refListData.getItem(mPosition).setActive(tmpIsChecked);
				
				mToastBehaviour.toast();
				
				mKindActionBehaviour.kindAction(refListData.getItem(mPosition).getActionFilePath());
				
				((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
								 */

			}
		}
	}

	
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
			String tmpToastFeelingsString = KindModelM.get(getActivity()).getToastString(ListTypeM.SUFFERING);
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

			/*
			String tmpToastFeelingsString = KindModelM.get(getActivity()).getToastString(ListTypeM.SUFFERING);
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
			*/
		}
	}
	
	class NoToast implements ToastBehaviour{
		@Override
		public void toast() {
			//Nothing is done
		}
	}
	
	
	//-------------------KindAction Behaviour [uses the strategy pattern]
	
	interface KindActionBehaviour{
		public void kindAction(String inKindActionFilePath);
	}
	void setKindActionBehaviour(KindActionBehaviour inKindActionBehaviour){
		mKindActionBehaviour = inKindActionBehaviour;
	}
	
	class MediaFileKindActionBehaviour implements KindActionBehaviour{
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
	
	class OnlyTitleKindActionBehaviour implements KindActionBehaviour{
		@Override
		public void kindAction(String inKindActionFilePath) {
			//do nothing
		}
	}

}