package com.sunyata.kindmind;

import java.util.UUID;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
	private ListTypeM mListType;
	
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
		//setHasOptionsMenu(true);
		
		mListType = ListTypeM.valueOf(this.getArguments().getString(Utils.LIST_TYPE));
		
		//mKindAct = new KindActM();
		//KindModel.get().getKindActionList().add(mKindAct);
		UUID tmpId = (UUID)getActivity().getIntent().getSerializableExtra(ListFragmentC.EXTRA_LIST_DATA_ITEM_ID);
		refListDataItem = KindModelM.get(getActivity()).getListOfType(mListType).getItem(tmpId);
		
	}
	
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    	
    	//Saving to JSON file
    	//KindModelM.get(getActivity()).getListOfType(mListType).saveToJson(true);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		//super not called in book
		Log.d(Utils.getClassName(), Utils.getMethodName());
		
		View v = inflater.inflate(R.layout.fragment_data_details, parent, false);
		
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
				KindModelM.get(getActivity()).getListOfType(mListType).delete(refListDataItem);
				getActivity().finish();
				//We don't need to notify the adapter (why?)
			}
		});
		//mDeleteButton.remove
		
		return v;
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
}