package com.sunyata.kindmind.main;

public interface MainActivityCallbackListenerI {
	public void fireSavePatternEvent();
	public void fireUpdateTabTitlesEvent();
	public void fireResetDataEvent();
	public void fireClearDatabaseAndUpdateGuiEvent();
}
