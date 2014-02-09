package com.sunyata.kindmind.ToastsAndActions;

import android.content.Context;
import android.widget.Toast;

import com.sunyata.kindmind.Utils;
import com.sunyata.kindmind.List.ListTypeM;

public class NeedsToast implements ToastBehaviour{
	@Override
	public void toast(Context inContext) {

		String tmpToastFeelingsString = Utils.getToastString(inContext, ListTypeM.FEELINGS);
		String tmpToastNeedsString = Utils.getToastString(inContext, ListTypeM.NEEDS);
		
		if(tmpToastFeelingsString.length() > 0 & tmpToastNeedsString.length() > 0){
			Toast.makeText(
					inContext,
					"I am feeling " + tmpToastFeelingsString +
					" because I am needing " + tmpToastNeedsString, Toast.LENGTH_LONG)
					.show();
		}else if(tmpToastNeedsString.length() > 0){
				Toast.makeText(
						inContext,
						"I am needing " + tmpToastNeedsString, Toast.LENGTH_LONG)
						.show();
		}else{
				//Do nothing
		}
	}
}