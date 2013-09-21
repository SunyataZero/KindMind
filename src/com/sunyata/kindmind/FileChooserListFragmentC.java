package com.sunyata.kindmind;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
//ListFragment
//android.support.v4.app.Fragment
public class FileChooserListFragmentC extends ListFragment {
	
	static final String EXTRA_RETURN_VALUE_FROM_FILECHOOSERFRAGMENT = "RETURN_VALUE_FROM_FILECHOOSERFRAGMENT";
	
	public static FileChooserListFragmentC newInstance(){
		//Bundle tmpArguments = new Bundle();
		FileChooserListFragmentC retListFragment = new FileChooserListFragmentC();
		//retListFragment.setArguments(tmpArguments);
		//mCallbackListener = inCallbackListener;
		return retListFragment;
	}
	
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
    	/* From the javadoc for getExternalStorageDirectory:
    	 * "Note: don't be confused by the word "external" here.
    	 * This directory can better be thought as media/shared storage.
    	 * It is a filesystem that can hold a relatively large amount of
    	 * data and that is shared across all applications (does not
    	 * enforce permissions). Traditionally this is an SD card,
    	 * but it may also be implemented as built-in storage in a
    	 * device that is distinct from the protected internal storage
    	 * and can be mounted as a filesystem on a computer."
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
				inConvertView = getActivity().getLayoutInflater().inflate(R.layout.file_list_item, null);
			}
			
			String tmpString = getItem(inPosition);

			//Setting the on click listener for the whole layout
			inConvertView.setOnClickListener(new CustomOnClickListener(inPosition));
			
			TextView tmpTitleTextView = (TextView)inConvertView.findViewById(R.id.file_list_item_titleTextView);
			tmpTitleTextView.setText(tmpString);

			return inConvertView;
		}

		private class CustomOnClickListener implements OnClickListener{
			private int mPosition;
			public CustomOnClickListener(int inPosition){
				mPosition = inPosition;
			}

			@Override
			public void onClick(View inView) {
				
				String tmpValueToReturn = 
						Environment.getExternalStorageDirectory().getAbsolutePath()
						+ SettingsM.KIND_MIND_DIRECTORY + "/"
						+ (String)((TextView) inView.findViewById(R.id.file_list_item_titleTextView)).getText();

				Intent tmpIntent = new Intent();
				tmpIntent.putExtra(EXTRA_RETURN_VALUE_FROM_FILECHOOSERFRAGMENT, tmpValueToReturn);
						
				getActivity().setResult(Activity.RESULT_OK, tmpIntent);

				getActivity().finish();
				
				//((ListFragmentDataAdapterC)getListAdapter()).notifyDataSetChanged();
			}
		}
	}
}
