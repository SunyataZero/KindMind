package com.sunyata.kindmind.Details;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.ListFragmentC;
import com.sunyata.kindmind.List.ListTypeM;
import com.sunyata.kindmind.WidgetAndNotifications.NotificationServiceC;

/*
 * Overview: DetailsFragmentC handles data for a single list item (row in the SQL database).
 * Details: More specifically:
 * 1a. It does the setup of buttons and other widgets, with launching of app internal or external activities/fragments,
 *     many of these activities/fragments are for choosing different types of files (audio, video, images)
 *     or information (contacts or bookmarks).
 * 1b. It handles results from these activities/fragments
 * 2. It does the setup of the button for choosing time of day for a recurring notification.
 * Extends: Fragment
 * Implements: TimePickerFragmentC.OnTimeSetListenerI
 * Sections:
 * ----------------------------Fields
 * ----------------------------onCreateView and other lifecycle methods
 * ----------------------------onActivityResult
 * ----------------------------Other methods
 */
public class DetailsFragmentC extends Fragment implements TimePickerFragmentC.OnTimeSetListenerI{

	
	//----------------------------Fields
	
	private EditText mItemEditText;
	private Button mDeleteButton;
	private ListTypeM refListType;
	private Button mImageFileChooserButton;
	private Button mAudioFileChooserButton;
	private Button mVideoFileChooserButton;
	private Button mCustomFileChooserButton;
	private Button mContactChooserButton;
	private Button mBookmarkChooserButton;
	private Button mClearChooserButton;
	private CheckBox mNotificationCheckBox;
	private Button mTimePickerButton;
	
	private Uri refItemUri; //Used to identify the item (SQL table row)
	
	static final int REQUEST_IMAGEFILECHOOSER = 11;
	static final int REQUEST_AUDIOFILECHOOSER = 12;
	static final int REQUEST_VIDEOFILECHOOSER = 13;
	static final int REQUEST_CUSTOMFILECHOOSER = 14;
	static final int REQUEST_CONTACTCHOOSER = 21;
	static final int REQUEST_BOOKMARKCHOOSER = 31;
	
	//newInstance is a simple static factory method which is used for creating instances of the class.
	//inAttachedData contains the Uri of the list item for the instance that is about to be created.
	static Fragment newInstance(Object inAttachedData){
		Bundle tmpArguments = new Bundle();
		tmpArguments.putString(ListFragmentC.EXTRA_ITEM_URI, inAttachedData.toString());
		//-inAttachedData contains the URI that identifies the item
		//-inAttachedData comes from SingleFragmentActivityC/DetailsActivityC (and not directly from ListFragmentC)
		Fragment retFragment = new DetailsFragmentC(); //"Implicit" constructor used
		retFragment.setArguments(tmpArguments);
		return retFragment;
	}
	

	
	//----------------------------onCreateView and onActivityResult
	//Bundled together since they contain the setup (onCreateView) of the various media chooser buttons
	// and the handling (onActivityResult) of the results from the (sometimes external) applications.

    //onCreateView mainly contains the setup of the buttons and other widgets. Many of the widgets are only shown
    // when we have a specific type of list.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
    	//-super not called in the Big Nerd Ranch book

    	Log.d(Utils.getClassName(), Utils.getMethodName());

    	//Inflating the layout
    	View v = inflater.inflate(R.layout.fragment_data_details, parent, false);

    	//Using the app icon and left caret for hierarchical navigation
    	if(NavUtils.getParentActivityName(getActivity()) != null){
    		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    	}

    	//Storing the URI associated with this list item in "refItemUri"
    	String tmpArgumentsString = this.getArguments().getString(ListFragmentC.EXTRA_ITEM_URI);
    	refItemUri = Uri.parse(tmpArgumentsString);

    	//Getting the SQL cursor for the list item URI
    	String[] tmpProjection = {ItemTableM.COLUMN_LISTTYPE, ItemTableM.COLUMN_NAME, ItemTableM.COLUMN_NOTIFICATION};
    	Cursor tmpCursor = getActivity().getApplicationContext().getContentResolver().query(
    			refItemUri, tmpProjection, null, null, null);
    	boolean tmpCursorIsNotEmpty = tmpCursor.moveToFirst();
    	if(!tmpCursorIsNotEmpty){
    		Log.e(Utils.getClassName(), "Error in method fillDataFromContentProvider: Cursor is empty");
    		getActivity().finish();
    		return v;
    	}

    	//Setting the list type enum value
    	refListType = ListTypeM.valueOf(
    			tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_LISTTYPE)));
    	//-Please note: We need to move the cursor to the first position before using .getString() (see above)

    	//Storing reference to EditText button containing list item name (from the SQL database)..
    	mItemEditText = (EditText)v.findViewById(R.id.listitem_name);
    	mItemEditText.setText(
    			tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME)));

    	//..adding a text changed listener for when the text changes..
    	mItemEditText.addTextChangedListener(new TextWatcher() {
    		@Override
    		public void onTextChanged(CharSequence s, int start, int before, int count) {
    			try{
    				//..update the SQL database
    				ContentValues tmpContentValues = new ContentValues();
    				tmpContentValues.put(ItemTableM.COLUMN_NAME, s.toString());
    				getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
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

    	//Setup of delete button..
    	mDeleteButton = (Button)v.findViewById(R.id.delete_button);
    	mDeleteButton.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			//..remove the row from the SQL database and finish the activity that holds this fragment
    			getActivity().getContentResolver().delete(refItemUri, null, null);
    			getActivity().finish();
    		}
    	});

    	//Getting references to the other buttons		
    	mImageFileChooserButton = (Button)v.findViewById(R.id.image_file_chooser_button);
    	mAudioFileChooserButton = (Button)v.findViewById(R.id.audio_file_chooser_button);
    	mVideoFileChooserButton = (Button)v.findViewById(R.id.video_file_chooser_button);
    	mCustomFileChooserButton = (Button)v.findViewById(R.id.custom_file_chooser_button);
    	mContactChooserButton = (Button)v.findViewById(R.id.contact_chooser_button);
    	mBookmarkChooserButton = (Button)v.findViewById(R.id.bookmark_chooser_button);
    	mClearChooserButton = (Button)v.findViewById(R.id.clear_chooser_button);
    	mNotificationCheckBox = (CheckBox)v.findViewById(R.id.notification_checkbox);
    	mTimePickerButton = (Button)v.findViewById(R.id.time_picker_button);

    	//If ListType is ACTIONS then these buttons will be shown, otherwise not
    	if(refListType != ListTypeM.ACTIONS){
    		mImageFileChooserButton.setVisibility(View.GONE);
    		mAudioFileChooserButton.setVisibility(View.GONE);
    		mVideoFileChooserButton.setVisibility(View.GONE);
    		mCustomFileChooserButton.setVisibility(View.GONE);
    		mContactChooserButton.setVisibility(View.GONE);
    		mBookmarkChooserButton.setVisibility(View.GONE);
    		mNotificationCheckBox.setVisibility(View.GONE);
    		mTimePickerButton.setVisibility(View.GONE);
    	}else{

    		//Setup of image chooser button..
    		//..using an external image app for choosing an image
    		final Intent tmpImageIntent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //-Images
    		//..we only show the button if there is at least one application that can take care of the intent
    		if(getActivity().getPackageManager().queryIntentActivities(tmpImageIntent, 0).size() == 0){
    			mImageFileChooserButton.setVisibility(View.GONE);
    		}else{
        		mImageFileChooserButton.setOnClickListener(new OnClickListener() {
        			@Override
        			public void onClick(View v) {
        				startActivityForResult(tmpImageIntent, REQUEST_IMAGEFILECHOOSER);
        				//-results handled below in the "onActivityResult" method
        			}
        		});
    		}

    		//Setup of audio file chooser button..
			//..using an external image app for choosing an image
			final Intent tmpAudioIntent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); //-Audio
    		//..we only show the button if there is at least one application that can take care of the intent
			if(getActivity().getPackageManager().queryIntentActivities(tmpAudioIntent, 0).size() == 0){
				mAudioFileChooserButton.setVisibility(View.GONE);
			}else{
	    		mAudioFileChooserButton.setOnClickListener(new OnClickListener() {
	    			@Override
	    			public void onClick(View v) {
	    				startActivityForResult(tmpAudioIntent, REQUEST_AUDIOFILECHOOSER);
	    				//-results handled below in the "onActivityResult" method
	    			}
	    		});
			}
    		
    		//Setup of video file chooser button..
    		//..using an external image app for choosing an image
			final Intent tmpVideoIntent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI); //-Video
    		//..we only show the button if there is at least one application that can take care of the intent
			if(getActivity().getPackageManager().queryIntentActivities(tmpVideoIntent, 0).size() == 0){
				mVideoFileChooserButton.setVisibility(View.GONE);
			}else{
	    		mVideoFileChooserButton.setOnClickListener(new OnClickListener() {
	    			@Override
	    			public void onClick(View v) {
	    				startActivityForResult(tmpVideoIntent, REQUEST_VIDEOFILECHOOSER);
	    				//-results handled below in the "onActivityResult" method
	    			}
	    		});
			}

    		/*-PLEASE NOTE: There is a bug in Android that creates problems for the emulator:
			01-23 14:22:48.400: E/AndroidRuntime(1829): FATAL EXCEPTION: main
			01-23 14:22:48.400: E/AndroidRuntime(1829): java.lang.SecurityException: Permission Denial: starting Intent { act=android.intent.action.PICK dat=content://media/external/video/media cmp=com.android.music/.VideoBrowserActivity } from ProcessRecord{b3a88968 1829:com.sunyata.kindmind/10039} (pid=1829, uid=10039) not exported from uid 10034
			01-23 14:22:48.400: E/AndroidRuntime(1829): 	at android.os.Parcel.readException(Parcel.java:1327)
    		 * More info here:
    		 * http://stackoverflow.com/questions/19181432/java-lang-securityexception-permission-denial-intent-in-new-version-4-3
    		 */
    		
    		//Setup of general file chooser button
			// (as for the other buttons, file type will be used by Android for inferring intent type)..
			mCustomFileChooserButton.setText(Utils.getKindMindDirectory());
    		mCustomFileChooserButton.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				//Alternative solution that searches through a volume:
    				// http://stackoverflow.com/questions/10384080/mediastore-uri-to-query-all-types-of-files-media-and-non-media
    				
    				//..starting a new (app internal) activity (and fragment) for for choosing a file
    				Intent intent = new Intent(getActivity(), FileChooserActivityC.class);
    				intent.putExtra(ListFragmentC.EXTRA_AND_BUNDLE_LIST_TYPE, refListType.toString());
    				//-Extracted in SingleFragmentActivityC
    				startActivityForResult(intent, REQUEST_CUSTOMFILECHOOSER);
    				//-Calling FileChooserActivityC
    			}
    		});

    		//Setup of contact chooser button..
    		
    		final Intent tmpContactIntent = new Intent(
					Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI);
    		mContactChooserButton.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				//..using an external contact app
    				startActivityForResult(tmpContactIntent, REQUEST_CONTACTCHOOSER);
    			}
    		});

    		//Setup of bookmark chooser button..
    		mBookmarkChooserButton.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				//..using an external app
    				Intent intent = new Intent(getActivity(), BookmarkChooserActivityC.class);
    				intent.putExtra(ListFragmentC.EXTRA_AND_BUNDLE_LIST_TYPE, refListType.toString()); //Extracted in SingleFragmentActivityC
    				startActivityForResult(intent, REQUEST_BOOKMARKCHOOSER); //Calling FileChooserActivityC
    			}
    		});
    		
    		mClearChooserButton.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				//Clearing the file/dir value in the database
    				ContentValues tmpContentValues = new ContentValues();
    				tmpContentValues.put(ItemTableM.COLUMN_FILEORDIRPATH, "");
    				getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
    				Toast.makeText(
							getActivity(),
							"Data cleared",
							Toast.LENGTH_SHORT)
							.show();
    			}
    		});

    		//Setup of notification checkbox which indicates if a recurring notification will be shown..
    		mNotificationCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
    			@Override
    			public void onCheckedChanged(CompoundButton inCompoundButton, boolean inChecked) {
    				if(inChecked == true){
        				DetailsFragmentC.this.changeNotificationService();
    				}
    			}
    		});

    		//..set status of the checkbox (checked / not checked)
    		long tmpNotification = Long.parseLong(
    				tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NOTIFICATION)));
    		//-2nd value not used yet, but may be in the future
    		//-Please note: Contains both off/on (-1 / not -1) and the time. Only the first information is used here 
    		if(tmpNotification != ItemTableM.FALSE){
    			mNotificationCheckBox.setChecked(true);
    		}else{
    			//do nothing since the checkbox starts out as checked and we don't want to trigger the reaction
    		}

    		//Setup of button for choosing time for notification..
    		mTimePickerButton.setOnClickListener(new OnClickListener(){
    			@Override
    			public void onClick(View v) { //-Alt: Using the xml property "android:onClick"
    				//..starting a new (app internal) fragment for for choosing time of day
    				DialogFragment tmpTimePickerFragment = TimePickerFragmentC.newInstance(DetailsFragmentC.this);
    				tmpTimePickerFragment.show(getFragmentManager(), "TimePicker");
    			}
    		});
    	}
    	
    	//tmpCursor.close();
    	return v;
    }

    
    //onActivityResult handles the results from the various activities started inside the anonymous inner classes
    // in the onCreateView method.
    //"tmpFilePath"	 (pending)	
    // /mnt/sdcard/DCIM/100ANDRO/DSC_0018.jpg
    // dat=content://media/external/images/media/1993
	@Override
	public void onActivityResult(int inRequestCode, int inResultCode, Intent inIntent){
		
		String tmpFilePath = "";
		
		if(inResultCode != Activity.RESULT_OK){
			Log.w(Utils.getClassName(),"Warning in onActivityResult(): inResultCode was not RESULT_OK");
			return;
		}
		
		//Handling the results that comes back after various intents have been sent
		// with various actions (see onCreateView method)
		switch(inRequestCode){
		case REQUEST_IMAGEFILECHOOSER:
			//-same as video for now
		case REQUEST_AUDIOFILECHOOSER:
			//-same as video for now
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
		
		//Updating file/dir string value in the database
		if(tmpFilePath != ""){
			ContentValues tmpContentValues = new ContentValues();
			tmpContentValues.put(ItemTableM.COLUMN_FILEORDIRPATH, tmpFilePath);
			getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
		}else{
			Log.w(Utils.getClassName(),
					"Waring in onActivityResult: tmpFilePath is empty even though the result code was RESULT_OK");
		}
	}


	
	//----------------------------Other lifecycle methods
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(Utils.getClassName(), Utils.getMethodName());
		setRetainInstance(true);
		setHasOptionsMenu(true); //for the up navigation button (left caret)
	}
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }
    @Override
    public void onDestroy(){
    	//-When a new activity is created, this method is called on a physical device, but not on
        // the emulator, maybe because of "don't keep activities"?
    	super.onDestroy();
    	Log.d(Utils.getClassName(), Utils.getMethodName());
    }

	
    
	//----------------------------Other methods
	
    //Callback method called from the TimePickerFragmentC class with the hour and minute values as arguments
	//(The alternative to send the uri of the list item into TimePickerFragmentC would not work well
	// since we still would need to be able to change the notification by simply clickling on the checkbox, 
	// without the user launching the timepicker)
	@Override
	public void fireOnTimeSetEvent(int inHourOfDay, int inMinute) {
		//Use the current date, but change the hour and minute to what was given by the user
		Calendar c = Calendar.getInstance();
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
				inHourOfDay, inMinute, 0);
		long tmpTimeInMilliSeconds = c.getTimeInMillis();
		
		//Update the SQL column "NOTIFICATION" with the new value
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
	public boolean onOptionsItemSelected(MenuItem inMenuItem){
		switch (inMenuItem.getItemId()){
		case android.R.id.home:
			//Navigating upwards in the activity heirarchy
			if(NavUtils.getParentActivityName(getActivity()) != null){
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			return true;
		default:
			return super.onOptionsItemSelected(inMenuItem);
		}
	}
}