package com.sunyata.kindmind;

import java.util.Calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragmentC extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

	static OnTimeSetListenerI mOnTimeSetListener;
	
	static TimePickerFragmentC newInstance(OnTimeSetListenerI inOnTimeSetListener){
		mOnTimeSetListener = inOnTimeSetListener;
		return new TimePickerFragmentC();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle inSavedInstanceState){
		
		Calendar tmpCalendar = Calendar.getInstance();
		//TODO: Set the time which is stored in ListDataItemM
		int tmpHour = tmpCalendar.get(Calendar.HOUR_OF_DAY);
		int tmpMinute = tmpCalendar.get(Calendar.MINUTE);
		
		return new TimePickerDialog(
				getActivity(), this, tmpHour, tmpMinute, DateFormat.is24HourFormat(getActivity()));
	}
	
	@Override
	public void onTimeSet(TimePicker inView, int inHourOfDay, int inMinute) {
		mOnTimeSetListener.fireOnTimeSetEvent(inHourOfDay, inMinute);
	}
	interface OnTimeSetListenerI{
		void fireOnTimeSetEvent(int inHourOfDay, int inMinute);
	}
}
