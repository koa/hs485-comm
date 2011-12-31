package ch.eleveneye.hs485.api;

import ch.eleveneye.hs485.api.data.KeyMessage;

public interface MessageHandler {
	public void handleMessage(KeyMessage keyMessage);
}
