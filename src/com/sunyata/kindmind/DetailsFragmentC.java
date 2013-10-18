package com.sunyata.kindmind;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.sunyata.kindmind.contentprovider.ListContentProviderM;

public class DetailsFragmentC extends Fragment implements TimePickerFragmentC.OnTimeSetListenerI{

	
	//----------------------------Fields and singelton get method
	
	private EditText mKindActEditText;
	private Button mDeleteButton;
	private ItemM refListDataItem;
	private ListTypeM refListType;
	private Button mFileChooserButton;
	private CheckBox mNotificationCheckBox;
	private Button mTimePickerButton;
	
	static final int REQUEST_FILECHOOSER = 1;
	
	static Fragment newInstance(ListTypeM inListType){
		Bundle tmpArguments = new Bundle();
		tmpArguments.putString(Utils.LIST_TYPE, inListType.toString());
		Fragment retFragment = new DetailsFragmentC(); //"Implicit" constructor used
		retFragment.setArguments(tmpArguments);
		return retFragment;
	}
	

	//----------------------------Lifecycle methods

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(Utils.getClassName(), Utils.getMethodName());
		setRetainInstance(true);
		setHasOptionsMenu(true); //for the up navigation button
		
		refListType = ListTypeM.valueOf(this.getArguments().getString(Utils.LIST_TYPE));
		
		//UUID tmpId = (UUID)getActivity().getIntent().getSerializableExtra(ListFragmentC.EXTRA_LIST_DATA_ITEM_ID);
		//refListDataItem = KindModelM.get(getActivity()).getListOfType(refListType).getItem(tmpId);
	}
	
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    	
    	//Saving the newly created list data item
    	//TODO: Write to PATTERNS table
    	
    	/*
    	ListDataM tmpListData = KindModelM.get(getActivity()).getListOfType(refListType);
    	if(tmpListData != null && refListType != null){
    		tmpListData.saveToJson(true);
    	}
    	*/
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		//super not called in book
		Log.d(Utils.getClassName(), Utils.getMethodName());
		
		View v = inflater.inflate(R.layout.fragment_data_details, parent, false);
		
		if(NavUtils.getParentActivityName(getActivity()) != null){
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		
		mKindActEditText = (EditText)v.findViewById(R.id.kindact_name);
		if(refListDataItem.getName().equals(ItemM.NO_NAME_SET) == false){
			mKindActEditText.setText(refListDataItem.getName());
		}
		mKindActEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				try{
					refListDataItem.setName(s.toString());
				}catch(NullPointerException npe){
					Log.e(Utils.getClassName(), "NullPointerException in method onTextChanged");
				}
				
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		
		mDeleteButton = (Button)v.findViewById(R.id.delete_button);
		mDeleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AdapterContextMenuInfo info; 
				
				Uri tmpDeleteUri = Uri.parse(ListContentProviderM.CONTENT_URI + "/" + refListDataItem.getId());
				getActivity().getContentResolver().delete(tmpDeleteUri, null, null);
				getActivity().finish();
				//We don't need to notify the adapter (why?)
			}
		});
		
		
		mFileChooserButton = (Button)v.findViewById(R.id.file_chooser_button);
		if(refListType != ListTypeM.KINDNESS){
			//Only show this button for the strategies
			mFileChooserButton.setVisibility(View.GONE);
		}
		
		
		mFileChooserButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FileChooserActivityC.class);
				//intent.putExtra(ListFragmentC.EXTRA_LIST_DATA_ITEM_ID, refListDataItem.getId()); //Extracted in FileChooserFragmentC
				intent.putExtra(ListFragmentC.EXTRA_LIST_TYPE, refListType.toString()); //Extracted in SingleFragmentActivityC
				startActivityForResult(intent, REQUEST_FILECHOOSER); //Calling FileChooserActivityC
			}
		});

		
		mNotificationCheckBox = (CheckBox)v.findViewById(R.id.notification_checkbox);
		if(refListType != ListTypeM.KINDNESS){
			//Only show this button for the strategies
			mNotificationCheckBox.setVisibility(View.GONE);
		}
		Log.i(Utils.getClassName(), "refListDataItem.isNotificationActive() = "
				+ refListDataItem.isNotificationActive());
		mNotificationCheckBox.setChecked(refListDataItem.isNotificationActive());
		//-TODO: Is this the most helpful way?
		mNotificationCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton inCompoundButton, boolean inChecked) {
				DetailsFragmentC.this.changeNotificationService();
			}
		});

		
		mTimePickerButton = (Button)v.findViewById(R.id.time_picker_button);
		if(refListType != ListTypeM.KINDNESS){
			//Only show this button for the strategies
			mTimePickerButton.setVisibility(View.GONE);
		}
		mTimePickerButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) { //Alt: Using the xml property "android:onClick"
				DialogFragment tmpTimePickerFragment = TimePickerFragmentC.newInstance(DetailsFragmentC.this);
				tmpTimePickerFragment.show(getFragmentManager(), "TimePicker");
			}
		});

		
		return v;
	}
	@Override
	public void fireOnTimeSetEvent(int inHourOfDay, int inMinute) {
		refListDataItem.setUserTime(inHourOfDay, inMinute);
		this.changeNotificationService();
	}
	void changeNotificationService(){
		refListDataItem.setNotificationActive(mNotificationCheckBox.isChecked());
		
		NotificationServiceC.setServiceNotificationSingle(
				getActivity().getApplicationContext(),
				refListDataItem.isNotificationActive(),
				refListDataItem.getUserTimeInMilliSeconds(),
				AlarmManager.INTERVAL_DAY,
				refListDataItem.getId(),
				refListDataItem.getName());
	}
	
	@Override
	public void onActivityResult(int inRequestCode, int inResultCode, Intent inIntent){
		
		if(inRequestCode == REQUEST_FILECHOOSER){

			if(inResultCode == Activity.RESULT_OK){
				String tmpReturnValueFromFileChooserFragment =
						inIntent.getStringExtra(FileChooserListFragmentC.EXTRA_RETURN_VALUE_FROM_FILE_CHOOSER_FRAGMENT);
				
				Log.i(Utils.getClassName(),
						"tmpReturnValueFromFileChooserFragment = " + tmpReturnValueFromFileChooserFragment);
				
				refListDataItem.setActionFilePath(tmpReturnValueFromFileChooserFragment);
				
			}else{
				Log.e(Utils.getClassName(),"Error in onActivityResult(): inResultCode was not RESULT_OK");
			}

		}
		
	}
	
    @Override
    //When a new activity is created, this method is called on a physical device, but not on
    // the emulator, maybe because of "don't keep activities"?
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
    public void onDestroyView(){
    	super.onDestroyView();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
    @Override
    public void onActivityCreated(Bundle inSavedInstanceState){
    	super.onActivityCreated(inSavedInstanceState);
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
    @Override
    public void onAttach(Activity inActivity){
    	super.onAttach(inActivity);
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
    @Override
    public void onDetach(){
    	super.onDetach();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem inMenuItem){
		switch (inMenuItem.getItemId()){
		case android.R.id.home:

			if(NavUtils.getParentActivityName(getActivity()) != null){
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			return true;
		default:
			return super.onOptionsItemSelected(inMenuItem);
		}
	}

	//----------------------------Other methods
	
}