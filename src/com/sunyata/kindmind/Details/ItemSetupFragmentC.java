package com.sunyata.kindmind.Details;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.ListFragmentC;
import com.sunyata.kindmind.List.ListTypeM;
import com.sunyata.kindmind.List.SetupActionOnClickListenerC;
import com.sunyata.kindmind.WidgetAndNotifications.NotificationServiceC;
import com.sunyata.kindmind.WidgetAndNotifications.WidgetProviderC;

/*
 * Overview: SetupFragmentC handles data for a single list item (row in the SQL database).
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
public class ItemSetupFragmentC extends Fragment implements TimePickerFragmentC.OnTimeSetListenerI{
	
	//----------------------------Fields
	private EditText mItemEditText;
	private int refListType;
	private Button mNotificationTimePickerButton;
	private Switch mNotificationSwitch;
	private TextView mActionOnClickTextView;
	private Boolean mSupressEvents = false;
	
	private ArrayAdapter<CharSequence> mTypeChooserButtonAdapter;
	private Button mNewActionButton;
	
	private Uri refItemUri; //Used to identify the item (SQL table row)
	
	static final int REQUEST_IMAGEFILECHOOSER = 11;
	static final int REQUEST_AUDIOFILECHOOSER = 12;
	static final int REQUEST_VIDEOFILECHOOSER = 13;
	static final int REQUEST_CUSTOMFILECHOOSER = 14;
	static final int REQUEST_CONTACTCHOOSER = 21;
	static final int REQUEST_BOOKMARKCHOOSER = 31;
	
	//newInstance is a simple static factory method which is used for creating instances of the class.
	//inAttachedData contains the Uri of the list item for the instance that is about to be created.
	public static Fragment newInstance(Object inAttachedData){
		Bundle tmpArguments = new Bundle();
		tmpArguments.putString(ListFragmentC.EXTRA_ITEM_URI, inAttachedData.toString());
		//-inAttachedData contains the URI that identifies the item
		//-inAttachedData comes from SingleFragmentActivityC/DetailsActivityC (and not directly from ListFragmentC)
		
		Fragment retFragment = new ItemSetupFragmentC(); //"Implicit" constructor used
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
    	View v = inflater.inflate(R.layout.fragment_item_setup, parent, false);

    	//Using the app icon and left caret for hierarchical navigation
    	if(NavUtils.getParentActivityName(getActivity()) != null){
    		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    	}

    	//Storing the URI associated with this list item in "refItemUri"
    	String tmpArgumentsString = this.getArguments().getString(ListFragmentC.EXTRA_ITEM_URI);
    	refItemUri = Uri.parse(tmpArgumentsString);

    	//Getting the SQL cursor for the list item URI
    	String[] tmpProjection = {ItemTableM.COLUMN_LIST_TYPE, ItemTableM.COLUMN_NAME, ItemTableM.COLUMN_NOTIFICATION};
    	Cursor tmpItemCur = getActivity().getApplicationContext().getContentResolver().query(
    			refItemUri, tmpProjection, null, null, null);
    	boolean tmpCursorIsEmpty = !tmpItemCur.moveToFirst();
    	if(tmpCursorIsEmpty){
    		Log.e(Utils.getClassName(), "Error in method fillDataFromContentProvider: Cursor is empty");
    		getActivity().finish();
    		return v;
    	}

    	//Setting the list type enum value
    	refListType = tmpItemCur.getInt(tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_LIST_TYPE));
    	//-Please note: We need to move the cursor to the first position before using .getString() (see above)

    	
    	//--------------Title
    	
    	//Storing reference to EditText button containing list item name (from the SQL database)..
    	mItemEditText = (EditText)v.findViewById(R.id.listitem_name);
    	mItemEditText.setText(
    			tmpItemCur.getString(tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME)));

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

    	
    	//--------------Daily Reminder

    	mNotificationTimePickerButton = (Button) v.findViewById(R.id.timePickerButton);
    	mNotificationTimePickerButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) { //-Alt: Using the xml property "android:onClick"
				//..starting a new (app internal) fragment for for choosing time of day
				DialogFragment tmpTimePickerFragment = TimePickerFragmentC.newInstance(ItemSetupFragmentC.this);
				tmpTimePickerFragment.show(getFragmentManager(), "TimePicker");
			}
		});

    	//Setup of notification checkbox which indicates if a recurring notification will be shown..
    	mNotificationSwitch = (Switch) v.findViewById(R.id.notificationSwitch);
    	mNotificationSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
    		@Override
    		public void onCheckedChanged(CompoundButton inCompoundButton, boolean inChecked) {
    			if(mSupressEvents == false){
    				//Set the time in the database table
    				if(mNotificationSwitch.isChecked() == false){
    					updateTimeInDB(ItemTableM.FALSE);
    				}else{
        				updateTimeInDB(Calendar.getInstance().getTimeInMillis());
        				// + 1000 * 60 * 60 * 24
        				//-Minus one minute so that the alarm is not set off
    				}
    				
    				//Update GUI
    				ItemSetupFragmentC.this.updateSwitchAndNotificationButton();
    				
    				//Update notification
    				ItemSetupFragmentC.this.changeNotificationService();
    			}
    		}
    	});
    	
    	this.updateSwitchAndNotificationButton();
    	
    	
    	//--------------Actions on click
    	
    	mActionOnClickTextView = (TextView) v.findViewById(R.id.actionOnClickTextView);
    	
    	mNewActionButton = (Button) v.findViewById(R.id.newActionButton);
    	
    	if(refListType == ListTypeM.KINDNESS){
	    	
	    	ArrayList<CharSequence> tmpArrayList = new ArrayList<CharSequence>();
	    	tmpArrayList.add("Image");
	    	tmpArrayList.add("Audio");
	    	tmpArrayList.add("Video");
	    	tmpArrayList.add("Contact");
	    	tmpArrayList.add("Bookmark");
	    	///tmpArrayList.add("Custom File");
	    	///tmpArrayList.add("Custom String");
	    	
	    	mTypeChooserButtonAdapter = new ArrayAdapter<CharSequence>(
	    			getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, tmpArrayList);
	    	
	    	mNewActionButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(getActivity()).setTitle("Type of action")
					.setAdapter(mTypeChooserButtonAdapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							switch(which){
							case 0: //--------------Image
	
						    	//Setup of image chooser button..
						    	//..using an external image app for choosing an image
						    	final Intent tmpImageIntent = new Intent(
						    			Intent.ACTION_PICK,
						    			android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //-Images
						    	startActivityForResult(tmpImageIntent, REQUEST_IMAGEFILECHOOSER);
						    	//-results handled below in the "onActivityResult" method
								
								break;
							case 1: //--------------Audio
								final Intent tmpAudioIntent = new Intent(
						    			Intent.ACTION_PICK,
						    			android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
								startActivityForResult(tmpAudioIntent, REQUEST_AUDIOFILECHOOSER);
								break;
							case 2: //--------------Video
								//PLEASE NOTE: There is a bug in Android that gives an error when launching
								// this intent in the emulator (there is no problem on physical device).
								// More info:
								// http://stackoverflow.com/questions/19181432/java-lang-securityexception-permission-denial-intent-in-new-version-4-3
								final Intent tmpVideoIntent = new Intent(
						    			Intent.ACTION_PICK,
						    			android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
								startActivityForResult(tmpVideoIntent, REQUEST_VIDEOFILECHOOSER);
								break;
							case 3: //--------------Contact
						    	final Intent tmpContactIntent = new Intent(
						    			Intent.ACTION_PICK,
						    			ContactsContract.Contacts.CONTENT_URI);
						    			startActivityForResult(tmpContactIntent, REQUEST_CONTACTCHOOSER);
								break;
							case 4: //--------------Bookmark
				    			final Intent tmpBookmarkIntent = new Intent(getActivity(), BookmarkChooserActivityC.class);
				    			tmpBookmarkIntent.putExtra(ListFragmentC.EXTRA_LIST_TYPE, refListType);
				    			//-Extracted in SingleFragmentActivityC
				    			startActivityForResult(tmpBookmarkIntent, REQUEST_BOOKMARKCHOOSER); //Calling FileChooserActivityC
								break;
								/*
							case 5: //--------------Custom file
				    			//Alternative solution that searches through a volume:
				    			// http://stackoverflow.com/questions/10384080/mediastore-uri-to-query-all-types-of-files-media-and-non-media
				    			//..starting a new (app internal) activity (and fragment) for for choosing a file
								final Intent customFileIntent = new Intent(getActivity(), FileChooserActivityC.class);
								customFileIntent.putExtra(ListFragmentC.EXTRA_AND_BUNDLE_LIST_TYPE, refListType.toString());
				    			//-Extracted in SingleFragmentActivityC
				    			startActivityForResult(customFileIntent, REQUEST_CUSTOMFILECHOOSER);
				    			//-Calling FileChooserActivityC
								break;
								*/
							default:
								break;
							}
							
							dialog.dismiss();
						}
					}).create().show();
				}
			});
	
	    	this.updateActionList(v);
    		
    	}else{ //Feelings or needs
    		mActionOnClickTextView.setVisibility(View.GONE);
    		mNewActionButton.setVisibility(View.GONE);
    	}

    	tmpItemCur.close();
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
			

			Cursor tmpContactsCur = getActivity().getContentResolver().query(
					inIntent.getData(), null, null, null, null);
			if(tmpContactsCur.getCount() == 0){
				tmpContactsCur.close();
				return;
			}
			tmpContactsCur.moveToFirst();
			Uri tmpLookupUri = Uri.withAppendedPath(
					Contacts.CONTENT_LOOKUP_URI,
					tmpContactsCur.getString(tmpContactsCur.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)));
			tmpFilePath = tmpLookupUri.toString();
			
			tmpContactsCur.close();
			break;
		case REQUEST_BOOKMARKCHOOSER:
			tmpFilePath = inIntent.getStringExtra(
					BookmarkChooserFragmentC.EXTRA_RETURN_VALUE_FROM_BOOKMARK_CHOOSER_FRAGMENT);
			break;
		default:
			Log.e(Utils.getClassName(), "Error in onActivityResult(): Case not covered");
			return;
		}
		
		
		//--------------Updating file/dir string value in the database
		
		if(tmpFilePath == ""){
			Log.w(Utils.getClassName(),
					"Waring in onActivityResult: tmpFilePath is empty even though the result code was RESULT_OK");
			return;
		}
		
		//Reading the current string
		String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};
		Cursor tmpItemCur = getActivity().getContentResolver().query(refItemUri, tmpProjection, null, null, null);
		tmpItemCur.moveToFirst();
		String tmpActions = tmpItemCur.getString(tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIONS));
		tmpItemCur.close();
		
		//Verify that the string to be added does not contain the separator
		if(tmpFilePath.contains(Utils.ACTIONS_SEPARATOR)){
			Log.e(Utils.getClassName(), "Error in onActivityResult: String contains separator character");
			return;
		}
		
		if(tmpActions == null || tmpActions.equals("")){
			tmpActions = tmpFilePath;
		}else{
			//Updating the string with the appended file path
			tmpActions = tmpActions + Utils.ACTIONS_SEPARATOR + tmpFilePath;
		}
		
		//Writing the updated string to the database
		ContentValues tmpContentValues = new ContentValues();
		tmpContentValues.put(ItemTableM.COLUMN_ACTIONS, tmpActions);
		getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
		
		/*
		long tmpItemId = Utils.getIdFromUri(refItemUri);
		tmpContentValues.put(ExtendedDataTableM.COLUMN_ITEM_REFERENCE, tmpItemId);
		tmpContentValues.put(ExtendedDataTableM.COLUMN_DATA, tmpFilePath);
		getActivity().getContentResolver().insert(ContentProviderM.EXTENDED_DATA_CONTENT_URI, tmpContentValues);
		 */
		
		//Creation of a new action line in the layout
		this.updateActionList(this.getView());
	}


	//----------------------------Other methods
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(Utils.getClassName(), Utils.getMethodName());
		setRetainInstance(true);
		//-Recommended by CommonsWare:
		// http://stackoverflow.com/questions/11160412/why-use-fragmentsetretaininstanceboolean
		// but not in Reto's book: "genereally not recommended"
		setHasOptionsMenu(true); //-for the up navigation button (left caret)
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		//Setting focus and bringing up the soft keyboard if no title has been set for this item
		if(mItemEditText.getText().toString().equals(ItemTableM.NO_NAME)){
			mItemEditText.setFocusableInTouchMode(true);
			mItemEditText.requestFocus();
		}
	}
	
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
		
		this.updateTimeInDB(tmpTimeInMilliSeconds);

		
		//Update the GUI
		this.updateSwitchAndNotificationButton();
		
		this.changeNotificationService();
	}
	
	
	private void updateTimeInDB(long inTimeInMilliSeconds) {
		long tmpTimeInMilliSeconds = inTimeInMilliSeconds;
		
		//Updating the time for the alarm so that it is not triggered now
		if(tmpTimeInMilliSeconds != ItemTableM.FALSE
				&& tmpTimeInMilliSeconds < Calendar.getInstance().getTimeInMillis() + 1000 * 5){
			tmpTimeInMilliSeconds = tmpTimeInMilliSeconds + 1000 * 60 * 60 * 24;
		}
		
		//Updating the SQL column "NOTIFICATION" with the new value
		ContentValues tmpContentValues = new ContentValues();
		tmpContentValues.put(ItemTableM.COLUMN_NOTIFICATION, tmpTimeInMilliSeconds);
		getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
	}


	private void updateSwitchAndNotificationButton() {
		
		//Getting the value that will be used for both updates..
		Cursor tmpCursor = getActivity().getContentResolver().query(refItemUri, null, null, null, null);
		tmpCursor.moveToFirst();
		long tmpTimeInMilliseconds = tmpCursor.getLong(
				tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NOTIFICATION));
		tmpCursor.close();

		//..preparing extraction of minutes and hours from the result
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(tmpTimeInMilliseconds);
		
		if(tmpTimeInMilliseconds != ItemTableM.FALSE){
			//Updating the time picker button
			String tmpMinuteString = "" + c.get(Calendar.MINUTE);
			if(tmpMinuteString.length() == 1){tmpMinuteString = "0" + tmpMinuteString;}
			mNotificationTimePickerButton.setText(c.get(Calendar.HOUR_OF_DAY) + ":" + tmpMinuteString);
		}else{
			mNotificationTimePickerButton.setText("--:--");
		}
		
		//Updating the switch
		mSupressEvents = true;
		mNotificationSwitch.setChecked(tmpTimeInMilliseconds != ItemTableM.FALSE);
		mSupressEvents = false;
	}

	
    // Overview: updateActionList updates the list of actions
    // Please note: Currently only one action
    private void updateActionList(View inView) {

    	LinearLayout tmpVerticalList = (LinearLayout)inView.findViewById(R.id.actionsLinearLayout);

    	//Clearing the layout
    	tmpVerticalList.removeAllViews();
    	
    	LayoutInflater tmpLayoutInflater = LayoutInflater.from(this.getActivity().getApplicationContext());
    	
    	
		String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};
		Cursor tmpItemCur = getActivity().getContentResolver().query(
				this.refItemUri, tmpProjection, null, null, null);
		tmpItemCur.moveToFirst();
		String tmpActionsString = tmpItemCur.getString(tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIONS));
    	ArrayList<String> tmpActionsArrayList = Utils.actionsStringToArrayList(tmpActionsString);

    	LinearLayout tmpActionItem;
		for(String action : tmpActionsArrayList){
			
			tmpActionItem = (LinearLayout)tmpLayoutInflater.inflate(
					R.layout.action_list_item, tmpVerticalList, false); //-please note "attachToRoot = false"
    		
			TextView tmpTextView = (TextView)tmpActionItem.findViewById(R.id.action_list_item_itemStringTextView);
			tmpTextView.setText(action);
			
			//Launching the action if the user presses the text
			tmpTextView.setOnClickListener(new SetupActionOnClickListenerC(getActivity(), action));
			
			Button tmpDeleteButton = (Button)tmpActionItem.findViewById(R.id.action_list_item_deleteButton);

			//Using the action name as tag so we can remove this when the delete button is clicked
			tmpDeleteButton.setTag(action);
			
			tmpDeleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//Read the current string
					String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};
					Cursor tmpItemCur = getActivity().getContentResolver().query(
							refItemUri, tmpProjection, null, null, null);
					tmpItemCur.moveToFirst();
					String tmpActions = tmpItemCur.getString(tmpItemCur.getColumnIndexOrThrow(
							ItemTableM.COLUMN_ACTIONS));
					tmpItemCur.close();
					
					//Removing the matching string
					tmpActions = Utils.removeStringFromActions(tmpActions, v.getTag().toString());
					
					//Update the database string
					ContentValues tmpContentValues = new ContentValues();
					tmpContentValues.put(ItemTableM.COLUMN_ACTIONS, tmpActions);
					getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
					
					ItemSetupFragmentC.this.updateActionList(v.getRootView());
					//-not 100% sure how getRootView works
				}
			});

			//Adding the action to the list
			tmpVerticalList.addView(tmpActionItem);
    	}
	}

	void changeNotificationService(){
		NotificationServiceC.setServiceNotificationSingle(
				getActivity().getApplicationContext(),
				refItemUri,
				AlarmManager.INTERVAL_DAY);
	}

	
	//-------------------Options menu
	
	@Override
	public void onCreateOptionsMenu(Menu inMenu, MenuInflater inMenuInflater){
		super.onCreateOptionsMenu(inMenu, inMenuInflater);
		inMenuInflater.inflate(R.menu.details_menu, inMenu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem inMenuItem){
		switch (inMenuItem.getItemId()){
		case android.R.id.home:
			//Navigating upwards in the activity heirarchy
			if(NavUtils.getParentActivityName(getActivity()) != null){
				//NavUtils.navigateUpFromSameTask(getActivity()); //-this will recreate MainActivityC (different from using the back button)
				
				Intent tmpLaunchParentIntent = NavUtils.getParentActivityIntent(getActivity());
				tmpLaunchParentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				NavUtils.navigateUpTo(getActivity(), tmpLaunchParentIntent);
			}
			
			return true;
		case R.id.menu_item_delete_listitem:
			
			//Removing any alarms
			this.updateTimeInDB(ItemTableM.FALSE);
			this.changeNotificationService();

			//This may not be nessacary:
			//Updating the adapter in ListFragmentC
	    	///this.updateCursorAdapter();
			
			//Updating the app widgets
			Utils.updateWidgets(getActivity());
			
			//Remove the list item from the database
			getActivity().getContentResolver().delete(this.refItemUri, null, null);
			
			//Exit the details view of the removed item
			getActivity().finish();

			//Update of the layout (loaders, ____)?? Necassary or done automatically?
			///this.refreshListDataSupport()
			
			return true;
		default:
			return super.onOptionsItemSelected(inMenuItem);
		}
	}
}