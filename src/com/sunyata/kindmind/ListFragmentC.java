package com.sunyata.kindmind;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

public class ListFragmentC extends ListFragment{
//will later on extend an abstract class
	
	//-------------------Fields and constructor
	
	static final String EXTRA_LIST_DATA_ITEM_ID = "EXTRA_LIST_DATA_ITEM_ID";
	static final String EXTRA_LIST_TYPE = "EXTRA_LIST_TYPE";
	private ListDataM refListData;
	private ListTypeM refListType;
	private ToastBehaviour mToastBehaviour;
	private static MainActivityCallbackListenerI mCallbackListener;
	private KindActionBehaviour mKindActionBehaviour;
	
	public static ListFragmentC newInstance(ListTypeM inListType, MainActivityCallbackListenerI inCallbackListener){
		Bundle tmpArguments = new Bundle();
		tmpArguments.putString(Utils.LIST_TYPE, inListType.toString());
		ListFragmentC retListFragment = new ListFragmentC();
		retListFragment.setArguments(tmpArguments);
		mCallbackListener = inCallbackListener;
		return retListFragment;
	}
	
	
	//-------------------Lifecycle methods
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
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
    	
    	if(getListAdapter() != null){
        	((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
    	}
    	
		switch(refListType){
		case SPECEV:
			setToastBehaviour(new NoToast());
			setKindActionBehaviour(new OnlyTitleKindAction());
			break;
		case SUFFERING:
			setToastBehaviour(new FeelingsToast());
			setKindActionBehaviour(new OnlyTitleKindAction());
			break;
		case NEEDS:
			setToastBehaviour(new NeedsToast());
			setKindActionBehaviour(new OnlyTitleKindAction());
			break;
		case KINDNESS:
			setToastBehaviour(new NoToast());
			setKindActionBehaviour(new ImageKindAction());
			break;
		default:Log.e(Utils.getClassName() ,"Error in onCreate: ListType not covered by switch statement");
		}

    }
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    }
    @Override
    public void onStart(){
    	super.onStart();
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    }
    @Override
    public void onStop(){
    	super.onStop();
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    }
    @Override
    public View onCreateView(LayoutInflater inInflater, ViewGroup inContainer, Bundle inSavedinstanceState){
    	View retView = super.onCreateView(inInflater, inContainer, inSavedinstanceState);
    	
    	//View retView = inInflater.inflate(R.layout.activity_fragment, inContainer);
    	
    	/*
    	View tmpFooterView = inInflater.inflate(R.layout.feelings_radiogroup, inContainer, false);
    	this.getListView().addFooterView(tmpFooterView);
    	*/
    	
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    	return retView;
    }
    @Override
    public void onDestroyView(){
    	super.onDestroyView();
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));
    }
	//We get to onActivityCreated after onAttach and onCreateView.
    //Alternatively after onAttach, onCreate and onCreateView
    @Override
    public void onActivityCreated(Bundle inSavedInstanceState){
    	super.onActivityCreated(inSavedInstanceState);
    	Log.d(Utils.getClassName(), Utils.getMethodName(refListType));

    	this.initialize();
    }
    private void initialize(){
    	refListType = ListTypeM.valueOf(this.getArguments().getString(Utils.LIST_TYPE));
		refListData = KindModelM.get(getActivity()).getListOfType(refListType);
		ListFragmentDataAdapterC adapter = new ListFragmentDataAdapterC(refListData.getListOfData());
		setListAdapter(adapter);
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
			ListDataItemM tmpNewListDataItem = new ListDataItemM(refListType);
			boolean tmpCreatedSuccessfully =
					KindModelM.get(getActivity()).getListOfType(refListType).addItem(tmpNewListDataItem, true);
			if(!tmpCreatedSuccessfully){
				Log.e(Utils.getClassName(), "Error in onOptionsItemSelected: Could not add ListDataItem to list");
			}
			
			Intent intent = new Intent(getActivity(), DataDetailsActivityC.class);
			intent.putExtra(EXTRA_LIST_DATA_ITEM_ID, tmpNewListDataItem.getId()); //Extracted in DataDetailsFragmentC
			intent.putExtra(EXTRA_LIST_TYPE, refListType.toString()); //Extracted in SingleFragmentActivityC
			startActivityForResult(intent, 0); //Calling DataDetailsActivityC
			
			((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			
			return true;
		
			/*
		case R.id.menu_item_clear_current_list_selections:
			KindModelM.get(getActivity()).getListOfType(refListType).clearActivated();
			((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			getListView().smoothScrollToPosition(0);//Scroll to the top of the list
			return true;
			 */
		case R.id.menu_item_clear_all_list_selections:
			//KindModelM.get(getActivity()).clearActivatedForAllLists();
			((MainActivityC)getActivity()).clearActivated();
			((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			//-Only done for this Fragment but this is the only place where it is necassary since
			// the others will be updated when the pager page is changed.
			
			//Move back to the left-most position
			mCallbackListener.fireGoLeftmostEvent();
			
			return true;

		case R.id.menu_item_share_experience:
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
			return true;
		
		case R.id.menu_item_sort_alphabetically:
			KindModelM.get(getActivity()).getListOfType(refListType).sortAlphabetically();
			((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			getListView().smoothScrollToPosition(0);//Scroll to the top of the list
			return true;
			
		case R.id.menu_item_kindsort:
			KindModelM.get(getActivity()).loadPatternListsFromJsonFiles();
			KindModelM.get(getActivity()).updateSortValuesForListType(refListType);
			KindModelM.get(getActivity()).getListOfType(refListType).sortWithKindness();
			//-Refactor: Put the two lines above into one method?
			((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			getListView().smoothScrollToPosition(0);//Scroll to the top of the list
			
			return true;
		
		case R.id.menu_item_save_pattern:
			KindModelM.get(getActivity()).savePatternListToJson();
			return true;
			
/*
		case R.id.menu_item_backup:
			Intent tmpBackupIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			tmpBackupIntent.setType("text/plain");
			tmpBackupIntent.putExtra(Intent.EXTRA_SUBJECT, "KindMind backup of JSON files");
			ArrayList<Uri> tmpUriList = new ArrayList<Uri>();
			File[] tmpFileListArray = getActivity().getFilesDir().listFiles();
			for(File file : tmpFileListArray){
				file.setReadable(true);
				tmpUriList.add(Uri.fromFile(file));
			}
			tmpBackupIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, tmpUriList);
			try{
				startActivity(tmpBackupIntent);
			}catch(ActivityNotFoundException e){
				Log.w(Utils.getClassName(),
						"Warning in onOptionsItemSelected, case R.id.menu_item_backup: "
						+ "No email program installed");
			}
			return true;
*/
			
		case R.id.menu_item_send_as_text_current:
			sendAsEmail("KindMind list as text", refListData.toFormattedString());
			return true;
			
		case R.id.menu_item_send_as_text_all:
			String tmpAllListAsText = KindModelM.get(getActivity()).getFormattedStringWithAllLists();
			sendAsEmail("KindMind all lists as text", tmpAllListAsText);
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
    
	class ListFragmentDataAdapterC extends ArrayAdapter<ListDataItemM>{
		
		public ListFragmentDataAdapterC(ArrayList<ListDataItemM> inListData){
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
			
			ListDataItemM tmpListDataItem = getItem(inPosition);

			CheckBox tmpActiveCheckBox = (CheckBox)inConvertView.findViewById(R.id.list_item_activeCheckBox);
			tmpActiveCheckBox.setClickable(false); //We handle this ourselves
			tmpActiveCheckBox.setChecked(tmpListDataItem.isActive());
			
			//Setting the on click and on long click listeners for the whole layout
			inConvertView.setOnClickListener(new CustomOnClickListener(inPosition));
			inConvertView.setOnLongClickListener(new CustomOnLongClickListener(inPosition));
			
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
				boolean tmpWasChecked = refListData.getItem(mPosition).isActive();
				boolean tmpIsChecked = !tmpWasChecked;

				((CheckBox)inView.findViewById(R.id.list_item_activeCheckBox)).setChecked(tmpIsChecked);
				refListData.getItem(mPosition).setActive(tmpIsChecked);
				
				mToastBehaviour.toast();
				
				mKindActionBehaviour.kindAction(refListData.getItem(mPosition).getActionFilePath());
				
				/*
				if (tmpIsChecked){
					refListData.getItem(mPosition).incrementSingleClickSortValueFromCurrentRun();
				}
				*/
				((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			}
		}
		private class CustomOnLongClickListener implements OnLongClickListener{
			private int mPosition;
			public CustomOnLongClickListener(int inPosition){
				mPosition = inPosition;
			}
			@Override
			public boolean onLongClick(View inView) {
				
				ListDataItemM tmpListDataItem = ListFragmentDataAdapterC.this.getItem(mPosition);
				
				Intent intent = new Intent(getActivity(), DataDetailsActivityC.class);
				intent.putExtra(EXTRA_LIST_DATA_ITEM_ID, tmpListDataItem.getId()); //Extracted in DataDetailsFragmentC
				intent.putExtra(EXTRA_LIST_TYPE, refListType.toString()); //Extracted in SingleFragmentActivityC
				startActivityForResult(intent, 0); //Calling DataDetailsActivityC
				
				return false;
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
	
	class ImageKindAction implements KindActionBehaviour{
		@Override
		public void kindAction(String inKindActionFilePath) {
			Log.i(Utils.getClassName(), "inKindActionFilePath = " + inKindActionFilePath);
			
			if(inKindActionFilePath == ""){
				return;
			}else{
				
				File tmpFileOrDirectoryFromString = new File(inKindActionFilePath);
				//tmpIntent.putExtra(Intent.EXTRA_TEXT, "test text using EXTRA_TEXT");
				
				Log.i(Utils.getClassName(), "tmpFileOrDirectoryFromString.isDirectory() = "
						+ tmpFileOrDirectoryFromString.isDirectory());
				if(tmpFileOrDirectoryFromString.isDirectory()){
					this.doRandomKindActionFromSetOfFiles(tmpFileOrDirectoryFromString);
				}else{
					this.doKindAction(tmpFileOrDirectoryFromString);
				}
			}
		}
		private void doKindAction(File inFileFromString){
			Log.i(Utils.getClassName(), "inFileFromString = " + inFileFromString);
			
			Intent tmpIntent = new Intent(Intent.ACTION_VIEW);
			tmpIntent.setData(Uri.fromFile(inFileFromString));
			
			//Verifying that we have at least one app that can handle this intent before starting
			PackageManager tmpPackageManager = getActivity().getPackageManager();
			List<ResolveInfo> tmpListOfAllPosibleAcitivtiesForStarting =
					tmpPackageManager.queryIntentActivities(tmpIntent, 0);
			if(tmpListOfAllPosibleAcitivtiesForStarting.size() > 0){
				startActivity(tmpIntent);
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
			this.doKindAction(tmpRandomlyGivenFile);
		}
	}
	
	class OnlyTitleKindAction implements KindActionBehaviour{
		@Override
		public void kindAction(String inKindActionFilePath) {
			//do nothing
		}
	}
}