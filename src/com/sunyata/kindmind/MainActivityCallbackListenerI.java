package com.sunyata.kindmind;

public interface MainActivityCallbackListenerI {
	public void fireSavePatternEvent();
	public void fireUpdateTabTitlesEvent();
	public void fireResetDataEvent();
	public void fireClearDatabaseAndUpdateGuiEvent();
	public void fireFeelingsToastEvent(String iName);
	public void fireNeedsToastEvent(String iName);
}
