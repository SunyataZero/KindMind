package com.sunyata.kindmind;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
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

public class DetailsFragmentC extends Fragment implements TimePickerFragmentC.OnTimeSetListenerI{

	
	//----------------------------Fields and singelton get method
	
	private EditText mItemEditText;
	private Button mDeleteButton;
	//private ItemM refListDataItem;
	private ListTypeM refListType;
	private Button mImageFileChooserButton;
	private Button mAudioFileChooserButton;
	private Button mVideoFileChooserButton;
	private Button mCustomFileChooserButton;
	private Button mContactChooserButton;
	private Button mBookmarkChooserButton;
	private CheckBox mNotificationCheckBox;
	private Button mTimePickerButton;
	
	private Uri refItemUri; //Used to identify the item (table row)
	
	
	static final int REQUEST_IMAGEFILECHOOSER = 11;
	static final int REQUEST_AUDIOFILECHOOSER = 12;
	static final int REQUEST_VIDEOFILECHOOSER = 13;
	static final int REQUEST_CUSTOMFILECHOOSER = 14;
	static final int REQUEST_CONTACTCHOOSER = 21;
	static final int REQUEST_BOOKMARKCHOOSER = 31;
	private static final String EXTRA_RETURN_VALUE_FROM_EXTERNAL_CONTACT_CHOOSER = "asdf";
	
	static Fragment newInstance(Object inAttachedData){
		Bundle tmpArguments = new Bundle();
		tmpArguments.putString(ListFragmentC.EXTRA_ITEM_URI, inAttachedData.toString());
		//-inAttachedData contains the URI that identifies the item
		//-inAttachedData comes from SingleFragmentActivity (and not directly from ListFragmentC)
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
		
		//refListType = ListTypeM.valueOf(this.getArguments().getString(Utils.LIST_TYPE));
		
		
		//UUID tmpId = (UUID)getActivity().getIntent().getSerializableExtra(ListFragmentC.EXTRA_LIST_DATA_ITEM_ID);
		//refListDataItem = KindModelM.get(getActivity()).getListOfType(refListType).getItem(tmpId);
	}
	
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		//super not called in book
		Log.d(Utils.getClassName(), Utils.getMethodName());
		
		View v = inflater.inflate(R.layout.fragment_data_details, parent, false);
		
		if(NavUtils.getParentActivityName(getActivity()) != null){
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		
		String tmpArgumentsString = this.getArguments().getString(ListFragmentC.EXTRA_ITEM_URI);
		refItemUri = Uri.parse(tmpArgumentsString);
		

		
		String[] tmpProjection = {ItemTableM.COLUMN_LISTTYPE, ItemTableM.COLUMN_NAME, ItemTableM.COLUMN_NOTIFICATION};
		Cursor tmpCursor = getActivity().getApplicationContext().getContentResolver().query(refItemUri, tmpProjection, null, null, null);
		
		boolean tmpCursorIsNotEmpty = tmpCursor.moveToFirst();
		
		refListType = ListTypeM.valueOf(
				tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_LISTTYPE)));
		//-Please note: We need to move the cursor to the first position before using .getString()
		
		if(!tmpCursorIsNotEmpty){
			Log.e(Utils.getClassName(), "Error in method fillDataFromContentProvider: Cursor is empty");
			getActivity().finish();
			return v;
		}

		refListType = ListTypeM.valueOf(
				tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_LISTTYPE)));


		
		
		
		
		
		mItemEditText = (EditText)v.findViewById(R.id.kindact_name);
		/*
		if(refListDataItem.getName().equals(ItemM.NO_NAME_SET) == false){
			mKindActEditText.setText(refListDataItem.getName());
		}
		*/
		mItemEditText.setText(
				tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME)));


		
		mItemEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				try{
					ContentValues tmpContentValues = new ContentValues();
					tmpContentValues.put(ItemTableM.COLUMN_NAME, s.toString());
					getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
					//refListDataItem.setName(s.toString());
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
				
				getActivity().getContentResolver().delete(refItemUri, null, null);
				getActivity().finish();
				//We don't need to notify the adapter (why?)
			}
		});
		
		
		
		
		mImageFileChooserButton = (Button)v.findViewById(R.id.image_file_chooser_button);
		mAudioFileChooserButton = (Button)v.findViewById(R.id.audio_file_chooser_button);
		mVideoFileChooserButton = (Button)v.findViewById(R.id.video_file_chooser_button);
		mCustomFileChooserButton = (Button)v.findViewById(R.id.custom_file_chooser_button);
		mContactChooserButton = (Button)v.findViewById(R.id.contact_chooser_button);
		mBookmarkChooserButton = (Button)v.findViewById(R.id.bookmark_chooser_button);
		
		if(refListType != ListTypeM.KINDNESS){
			mImageFileChooserButton.setVisibility(View.GONE);
			mAudioFileChooserButton.setVisibility(View.GONE);
			mVideoFileChooserButton.setVisibility(View.GONE);
			mCustomFileChooserButton.setVisibility(View.GONE);
			mContactChooserButton.setVisibility(View.GONE);
			mBookmarkChooserButton.setVisibility(View.GONE);
		}
		
		mCustomFileChooserButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FileChooserActivityC.class);
				intent.putExtra(ListFragmentC.EXTRA_AND_BUNDLE_LIST_TYPE, refListType.toString()); //Extracted in SingleFragmentActivityC
				startActivityForResult(intent, REQUEST_CUSTOMFILECHOOSER); //Calling FileChooserActivityC
			}
		});

		mImageFileChooserButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent tmpIntent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(tmpIntent, REQUEST_IMAGEFILECHOOSER);
			}
		});
		
		//TODO: Audio
		
		//TODO: Video
		
		mContactChooserButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent tmpIntent = new Intent(
						Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(tmpIntent, REQUEST_CONTACTCHOOSER);
			}
		});
		
		mBookmarkChooserButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), BookmarkChooserActivityC.class);
				intent.putExtra(ListFragmentC.EXTRA_AND_BUNDLE_LIST_TYPE, refListType.toString()); //Extracted in SingleFragmentActivityC
				startActivityForResult(intent, REQUEST_BOOKMARKCHOOSER); //Calling FileChooserActivityC
			}
		});
		

		mNotificationCheckBox = (CheckBox)v.findViewById(R.id.notification_checkbox);
		if(refListType != ListTypeM.KINDNESS){
			//Only show this button for the strategies
			mNotificationCheckBox.setVisibility(View.GONE);
		}
		mNotificationCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton inCompoundButton, boolean inChecked) {
				DetailsFragmentC.this.changeNotificationService();
			}
		});
		
		long tmpNotificationTwoInOne = Long.parseLong(
				tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NOTIFICATION)));
		//-2nd value not used yet, but may be in the future
		mNotificationCheckBox.setChecked(tmpNotificationTwoInOne != -1);


		
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

		tmpCursor.close();
		return v;
	}

	@Override
	public void fireOnTimeSetEvent(int inHourOfDay, int inMinute) {
		////refListDataItem.setUserTime(inHourOfDay, inMinute);
		
		Calendar c = Calendar.getInstance();
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
				inHourOfDay, inMinute, 0);
		long tmpTimeInMilliSeconds = c.getTimeInMillis();
		
		ContentValues tmpContentValues = new ContentValues();
		tmpContentValues.put(ItemTableM.COLUMN_NOTIFICATION, tmpTimeInMilliSeconds);
		getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
		
		this.changeNotificationService();
	}
	void changeNotificationService(){

		NotificationServiceC.setServiceNotificationSingle(
				getActivity().getApplicationContext(),
				refItemUri,
				AlarmManager.INTERVAL_DAY);
	}
	
	@Override
	public void onActivityResult(int inRequestCode, int inResultCode, Intent inIntent){
		
		String tmpFilePath = "";
		
		if(inResultCode != Activity.RESULT_OK){
			Log.w(Utils.getClassName(),"Warning in onActivityResult(): inResultCode was not RESULT_OK");
			return;
		}
		
		switch(inRequestCode){
		case REQUEST_IMAGEFILECHOOSER:
			tmpFilePath = Utils.getFilePathFromIntent(getActivity(), inIntent);
			break;
		case REQUEST_AUDIOFILECHOOSER:
			tmpFilePath = Utils.getFilePathFromIntent(getActivity(), inIntent);
			break;
		case REQUEST_VIDEOFILECHOOSER:
			tmpFilePath = Utils.getFilePathFromIntent(getActivity(), inIntent);
			break;
		case REQUEST_CUSTOMFILECHOOSER:
			tmpFilePath = inIntent.getStringExtra(
					FileChooserFragmentC.EXTRA_RETURN_VALUE_FROM_FILE_CHOOSER_FRAGMENT);
			//Log.i(Utils.getClassName(),"tmpReturnValueFromFileChooserFragment = " + tmpReturnValueFromFileChooserFragment);
			break;
		case REQUEST_CONTACTCHOOSER:
			
			//Uri tmpReturnedUri = inIntent.getData().getPath();
			//String tmpReturnedUriAsStringPath = inIntent.getData().getPath();
			//String[] tmpProjection = new String[] {ContactsContract.Contacts.};
			/*
			 * tmpFilePath = inIntent.getStringExtra(
			 * DetailsFragmentC.EXTRA_RETURN_VALUE_FROM_EXTERNAL_CONTACT_CHOOSER);
			 */
			
			/*
			 * Alternative solution:
			 * http://stackoverflow.com/questions/4275167/how-to-open-a-contact-card-in-android-by-id
			 */
			
			Cursor tmpCursor = getActivity().getContentResolver().query(
					inIntent.getData(), null, null, null, null);
			if(tmpCursor.getCount() == 0){
				tmpCursor.close();
				return;
			}
			tmpCursor.moveToFirst();
			Uri tmpLookupUri = Uri.withAppendedPath(
					Contacts.CONTENT_LOOKUP_URI,
					tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)));
			tmpFilePath = tmpLookupUri.toString();
			tmpCursor.close();
			break;
		case REQUEST_BOOKMARKCHOOSER:
			tmpFilePath = inIntent.getStringExtra(
					BookmarkChooserFragmentC.EXTRA_RETURN_VALUE_FROM_BOOKMARK_CHOOSER_FRAGMENT);
			break;
		}
		
		if(tmpFilePath != ""){
			ContentValues tmpContentValues = new ContentValues();
			tmpContentValues.put(ItemTableM.COLUMN_FILEORDIRPATH, tmpFilePath);
			getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
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