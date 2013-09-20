package com.sunyata.kindmind;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
//ListFragment
//android.support.v4.app.Fragment
public class FileChooserListFragmentC extends ListFragment {
	
	public static FileChooserListFragmentC newInstance(){
		//Bundle tmpArguments = new Bundle();
		FileChooserListFragmentC retListFragment = new FileChooserListFragmentC();
		//retListFragment.setArguments(tmpArguments);
		//mCallbackListener = inCallbackListener;
		return retListFragment;
	}
	
	/*
	@Override
	public View onCreateView(LayoutInflater inInflater, ViewGroup inParent, Bundle inSavedInstanceState){
		View v = inInflater.inflate(R.layout.fragment_filechooser_experimental, inParent, false);
		return v;
	}
	*/
	
	@Override
	public View onCreateView(LayoutInflater inInflater, ViewGroup inParent, Bundle inSavedInstanceState){
    	View retView = super.onCreateView(inInflater, inParent, inSavedInstanceState);
    	return retView;
	}

	//We get to onActivityCreated after onAttach and onCreateView.
    //Alternatively after onAttach, onCreate and onCreateView
    @Override
    public void onActivityCreated(Bundle inSavedInstanceState){
    	super.onActivityCreated(inSavedInstanceState);

    	this.initialize();
    }
    private void initialize(){
    	
    	File mDirectoryPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
    			+ SettingsM.KIND_MIND_DIRECTORY);
    	
    	//mDirectoryPath.
    	/*
    	 * Note: don't be confused by the word "external" here.
    	 * This directory can better be thought as media/shared storage.
    	 * It is a filesystem that can hold a relatively large amount of
    	 * data and that is shared across all applications (does not
    	 * enforce permissions). Traditionally this is an SD card,
    	 * but it may also be implemented as built-in storage in a
    	 * device that is distinct from the protected internal storage
    	 * and can be mounted as a filesystem on a computer.
    	 */
    	//Environment.getRootDirectory();//Environment.getExternalStorageDirectory();
    	String[] tmpDirectoryListing = mDirectoryPath.list();
    	//ArrayList<String> tmpArrayList = (ArrayList<String>)Arrays.asList(tmpDirectoryListing);
    	List<String> tmpList = Arrays.asList(tmpDirectoryListing);
    	
    	FileChooserListDataAdapterC adapter = new FileChooserListDataAdapterC(tmpList);
		setListAdapter(adapter);
    }
    
	class FileChooserListDataAdapterC extends ArrayAdapter<String>{
		
		public FileChooserListDataAdapterC(List<String> inListData){
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
				inConvertView = getActivity().getLayoutInflater().inflate(R.layout.list_item, null);//can pass parent here
			}
			
			String tmpString = getItem(inPosition);

			CheckBox tmpActiveCheckBox = (CheckBox)inConvertView.findViewById(R.id.list_item_activeCheckBox);
			tmpActiveCheckBox.setClickable(false); //We handle this ourselves
			tmpActiveCheckBox.setChecked(true);
			
			//Setting the on click and on long click listeners for the whole layout
			//inConvertView.setOnClickListener(new CustomOnClickListener(inPosition));
			//inConvertView.setOnLongClickListener(new CustomOnLongClickListener(inPosition));
			
			TextView tmpTitleTextView = (TextView)inConvertView.findViewById(R.id.list_item_titleTextView);
			tmpTitleTextView.setText(tmpString);

			return inConvertView;
		}
		/*
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
		*/
	}
}
