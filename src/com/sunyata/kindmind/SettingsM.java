package com.sunyata.kindmind;

import android.os.Environment;

public class SettingsM {
	
	static String getKindMindDirectory(){
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/KindMind";
	}
	
}
