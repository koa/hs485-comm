package ch.eleveneye.hs485.api;

import ch.eleveneye.hs485.protocol.IMessage;

public interface MessageHandler {
	public void handleMessage(IMessage message);
}
