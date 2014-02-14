package com.sunyata.kindmind.WidgetAndNotifications;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.List.ListTypeM;

public class WidgetConfigActivityC extends Activity {

	private int mWidgetId;
	
	private RadioButton mFeelingsRadioButton;
	private RadioButton mNeedsRadioButton;
	private RadioButton mKindnessRadioButton;
	private Button mOkButton;
	private Button mCancelButton;

	public static String WIDGET_CONFIG_LIST_TYPE = "widgetConfigPreferences";
	//public static String PREFERENCE_LIST_TYPE = "listType";
	public static String PREFERENCE_LIST_TYPE_DEFAULT = "error";

	@Override
	public void onCreate(Bundle inSavedInstanceState){
		super.onCreate(inSavedInstanceState);
		
		//Loading the layout
		super.setContentView(R.layout.activity_widgetconfig);
		
		//Extracting the id of the widget
		Bundle inExtras = super.getIntent().getExtras();
		if(inExtras != null){
			mWidgetId = inExtras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		//Setting the result to cancelled so that if the activity exits, we will get this result
		super.setResult(RESULT_CANCELED, null);
		
		
		mFeelingsRadioButton = ((RadioButton)WidgetConfigActivityC.this.findViewById(
				R.id.widgetConfigFeelings_radioButton));
		mNeedsRadioButton = ((RadioButton)WidgetConfigActivityC.this.findViewById(
				R.id.widgetConfigNeeds_radioButton));
		mKindnessRadioButton = ((RadioButton)WidgetConfigActivityC.this.findViewById(
				R.id.widgetConfigKindness_radioButton));
		
		//Setting up the Ok button
		mOkButton = (Button) super.findViewById(R.id.widgetConfigOk_button);
		mOkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				//Creating the intent holding the result..
				Intent retResultIntent = new Intent();
				
				//..adding the id of the widget
				retResultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);

				//..adding the list type..
				SharedPreferences.Editor tmpPreferencesEditor =
						WidgetConfigActivityC.this.getSharedPreferences(WIDGET_CONFIG_LIST_TYPE,
								Context.MODE_PRIVATE).edit();
				if(mFeelingsRadioButton.isChecked()){
					tmpPreferencesEditor.putString(String.valueOf(mWidgetId), ListTypeM.FEELINGS.toString()).commit();
					//-"String.valueOf(mWidgetId)" is used as the key
					setResult(RESULT_OK, retResultIntent);
				}else if(mNeedsRadioButton.isChecked()){
					tmpPreferencesEditor.putString(String.valueOf(mWidgetId), ListTypeM.NEEDS.toString()).commit();
					setResult(RESULT_OK, retResultIntent);
				}else if(mKindnessRadioButton.isChecked()){
					tmpPreferencesEditor.putString(String.valueOf(mWidgetId), ListTypeM.KINDNESS.toString()).commit();
					setResult(RESULT_OK, retResultIntent);
				}else{
					//..in the case that no radiobutton has been chosen, exiting without result
					setResult(RESULT_CANCELED, null);
				}
				
				//Closing the activity
				finish();
			}
		});

		//Setting up the Cancel button
		mCancelButton = (Button) super.findViewById(R.id.widgetConfigCancel_button);
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				//Setting the result as cancelled
				setResult(RESULT_CANCELED, null);
			
				//Closing the activity
				finish();
			}
		});
	}

	/*
	public void onRadioButtonClicked(View inView){
		boolean
		
		switch(inView.getId()){
		case R.id.widgetConfigFeelings_radioButton:
			
			break;
		case R.id.widgetConfigNeeds_radioButton:
			break;
		case R.id.widgetConfigKindness_radioButton:
			break;
		default:
		}
	}
	*/
	
}
