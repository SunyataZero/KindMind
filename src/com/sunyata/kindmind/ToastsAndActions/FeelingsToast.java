package com.sunyata.kindmind.ToastsAndActions;

import android.content.Context;
import android.widget.Toast;

import com.sunyata.kindmind.List.AlgorithmM;
import com.sunyata.kindmind.List.ListTypeM;

public class FeelingsToast implements ToastBehaviour{
	@Override
	public void toast(Context inContext) {
		String tmpToastFeelingsString = AlgorithmM.get(inContext).getToastString(ListTypeM.FEELINGS);
		if(tmpToastFeelingsString.length() > 0){
			Toast.makeText(
					inContext, "I am feeling " + tmpToastFeelingsString, Toast.LENGTH_LONG)
					.show();
		}
	}
}
