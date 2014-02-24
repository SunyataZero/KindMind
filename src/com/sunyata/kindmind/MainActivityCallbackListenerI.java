package com.sunyata.kindmind;

public interface MainActivityCallbackListenerI {
	public void fireSavePatternEvent();
	public void fireClearAllActiveInDatabase();
	public void fireScrollLeftmostEvent();
	public void fireUpdateTabTitles();
	public void fireResetData();
}
