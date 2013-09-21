package com.sunyata.kindmind;

import java.util.UUID;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;

import com.sunyata.kindmind.ListDataItemM.ListTypeM;

public class DataDetailsFragmentC extends Fragment {

	//----------------------------Fields and singelton get method
	
	private EditText mKindActEditText;
	private Button mDeleteButton;
	private ListDataItemM refListDataItem;
	private ListTypeM refListType;
	private Button mFileChooserButton;
	
	static final int REQUEST_FILECHOOSER = 1;

	
	static Fragment newInstance(ListTypeM inListType){
		Bundle tmpArguments = new Bundle();
		tmpArguments.putString(Utils.LIST_TYPE, inListType.toString());
		Fragment retFragment = new DataDetailsFragmentC(); //"Implicit" constructor used
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
		
		//mKindAct = new KindActM();
		//KindModel.get().getKindActionList().add(mKindAct);
		UUID tmpId = (UUID)getActivity().getIntent().getSerializableExtra(ListFragmentC.EXTRA_LIST_DATA_ITEM_ID);
		refListDataItem = KindModelM.get(getActivity()).getListOfType(refListType).getItem(tmpId);
	}
	
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    	
    	//Saving the newly created list data item
    	ListDataM tmpListData = KindModelM.get(getActivity()).getListOfType(refListType);
    	if(tmpListData != null && refListType != null){
    		tmpListData.saveToJson(true);
    	}
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
		if(refListDataItem.getName().equals(ListDataItemM.NO_NAME_SET) == false){
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
				KindModelM.get(getActivity()).getListOfType(refListType).delete(refListDataItem);
				getActivity().finish();
				//We don't need to notify the adapter (why?)
			}
		});
		//mDeleteButton.remove
		
		
		mFileChooserButton = (Button)v.findViewById(R.id.file_chooser_button);
		mFileChooserButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FileChooserActivityC.class);
				//intent.putExtra(ListFragmentC.EXTRA_LIST_DATA_ITEM_ID, refListDataItem.getId()); //Extracted in FileChooserFragmentC
				intent.putExtra(ListFragmentC.EXTRA_LIST_TYPE, refListType.toString()); //Extracted in SingleFragmentActivityC
				startActivityForResult(intent, REQUEST_FILECHOOSER); //Calling FileChooserActivityC
			}
		});
		
		return v;
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
    //Important: When a new activity is created, this method is called on a physical device, but not on the emulator
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
			/*
			Intent tmpIntent = new Intent(getActivity(), MainActivityC.class);
			tmpIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(tmpIntent);
			//finish();
			return true;
			*/

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